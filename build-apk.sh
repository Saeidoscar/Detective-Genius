#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " Detective Genius APK Repair & Build Pipeline "
echo " Target SDK: 34 (Android 14)                  "
echo " Min SDK: 26 (Android 8.0)                    "
echo " Version: 1.0.1 (code: 2)                     "
echo " 4-Byte ZipAlign + Dual v2/v3 Signing         "
echo "=============================================="

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

BUILD_DIR="$ROOT_DIR/build/apk-build"
RELEASE_DIR="$ROOT_DIR/release"
mkdir -p "$BUILD_DIR/compiled_res" "$BUILD_DIR/classes" "$BUILD_DIR/dex" "$RELEASE_DIR"

AAPT2="$ROOT_DIR/tools/package/prebuilds/linux-x64/aapt2"
BUNDLETOOL="$ROOT_DIR/tools/package/prebuilds/bundletool.jar"
ANDROID_JAR="$ROOT_DIR/tools/android-34/android.jar"
KEYSTORE="$ROOT_DIR/keystore/detectivegenius-release.jks"
KEY_ALIAS="detectivegenius"
KEY_PASS="DetectiveGeniusRelease2026!"

# Ensure required build tools exist, or bootstrap automatically
if ! command -v java &>/dev/null || ! command -v keytool &>/dev/null; then
    echo "==> Setting up Java & Keytool runtime..."
    python3 -c "import jdk4py, os; keytool = jdk4py.JAVA.parent / 'keytool'; os.system(f'sudo ln -sf {jdk4py.JAVA} /usr/local/bin/java'); os.system(f'sudo ln -sf {keytool} /usr/local/bin/keytool')" || true
fi

if ! command -v kotlinc &>/dev/null; then
    if [ -f "/usr/local/lib/node_modules/kotlin-compiler/bin/kotlinc" ]; then
        sudo ln -sf /usr/local/lib/node_modules/kotlin-compiler/bin/kotlinc /usr/local/bin/kotlinc
    fi
fi

if [ ! -f "$AAPT2" ] || [ ! -f "$BUNDLETOOL" ]; then
    echo "==> Bootstrapping AAPT2 and Bundletool..."
    mkdir -p "$ROOT_DIR/tools"
    npm pack bare-apk@0.2.0 --pack-destination "$ROOT_DIR/tools/"
    tar -xzf "$ROOT_DIR/tools"/bare-apk-0.2.0.tgz -C "$ROOT_DIR/tools/"
    chmod +x "$AAPT2"
fi

if [ ! -f "$ANDROID_JAR" ]; then
    echo "==> Bootstrapping Android API 34 android.jar..."
    mkdir -p "$ROOT_DIR/tools/android-34"
    rm -rf /tmp/android-platforms
    git clone --depth 1 --filter=blob:none --sparse https://github.com/Sable/android-platforms.git /tmp/android-platforms
    cd /tmp/android-platforms
    git sparse-checkout set android-34
    cp android-34/android.jar "$ANDROID_JAR"
    rm -rf /tmp/android-platforms
    cd "$ROOT_DIR"
fi

# Ensure debug keystore exists for debug builds
DEBUG_KEYSTORE="$ROOT_DIR/keystore/debug.keystore"
if [ ! -f "$DEBUG_KEYSTORE" ]; then
    echo "==> Generating standard Android debug keystore..."
    keytool -genkeypair -v \
      -keystore "$DEBUG_KEYSTORE" \
      -alias androiddebugkey \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -storepass android \
      -keypass android \
      -dname "CN=Android Debug,O=Android,C=US"
fi

# 1. Compile Resources with AAPT2
echo "==> [1/6] Compiling resources with AAPT2..."
rm -rf "$BUILD_DIR/compiled_res"/*
"$AAPT2" compile --dir app/src/main/res -o "$BUILD_DIR/compiled_res"

# 2. Link Resources & Manifest with Assets
echo "==> [2/6] Linking binary resources, assets, and AndroidManifest.xml..."
"$AAPT2" link \
  -I "$ANDROID_JAR" \
  --manifest app/src/main/AndroidManifest.xml \
  -A app/src/main/assets \
  --min-sdk-version 26 \
  --target-sdk-version 34 \
  --version-code 2 \
  --version-name "1.0.1" \
  -o "$BUILD_DIR/base_linked.apk" \
  "$BUILD_DIR/compiled_res"/*.flat

# 3. Compile Kotlin Code to Class Bytecode
echo "==> [3/6] Compiling Kotlin application and domain engine..."
rm -rf "$BUILD_DIR/classes"/*
kotlinc \
  -classpath "$ANDROID_JAR" \
  app/src/main/kotlin/com/detectivegenius/DetectiveGeniusApp.kt \
  app/src/main/kotlin/com/detectivegenius/MainActivity.kt \
  core/src/main/kotlin/kotlinx/serialization/*.kt \
  core/src/main/kotlin/kotlinx/coroutines/flow/*.kt \
  core/src/main/kotlin/com/detectivegenius/core/model/*.kt \
  core/src/main/kotlin/com/detectivegenius/core/protocol/*.kt \
  game-engine/src/main/kotlin/com/detectivegenius/gameengine/assignment/*.kt \
  game-engine/src/main/kotlin/com/detectivegenius/gameengine/rules/*.kt \
  game-engine/src/main/kotlin/com/detectivegenius/gameengine/state/*.kt \
  networking/src/main/kotlin/com/detectivegenius/networking/discovery/*.kt \
  networking/src/main/kotlin/com/detectivegenius/networking/server/*.kt \
  networking/src/main/kotlin/com/detectivegenius/networking/client/*.kt \
  design-system/src/main/kotlin/com/detectivegenius/designsystem/avatar/*.kt \
  -d "$BUILD_DIR/classes"

# 4. Convert Class Files to Dalvik Executable (classes.dex) with D8
echo "==> [4/6] Running Google D8 dexer with Kotlin runtime stdlib..."
rm -rf "$BUILD_DIR/dex"/*
KOTLIN_STDLIB="/usr/local/lib/node_modules/kotlin-compiler/lib/kotlin-stdlib.jar"
java -cp "$BUNDLETOOL" com.android.tools.r8.D8 \
  --lib "$ANDROID_JAR" \
  --min-api 26 \
  --release \
  --output "$BUILD_DIR/dex" \
  "$KOTLIN_STDLIB" \
  $(find "$BUILD_DIR/classes" -name "*.class")

# Verify classes.dex contents
python3 -c "
with open('$BUILD_DIR/dex/classes.dex', 'rb') as f:
    dex = f.read()
required = [
    b'Lcom/detectivegenius/MainActivity;',
    b'Lcom/detectivegenius/DetectiveGeniusApp;',
    b'Lkotlin/jvm/internal/Intrinsics;'
]
for r in required:
    if r not in dex:
        raise ValueError(f'CRITICAL: {r.decode()} not found in classes.dex!')
print('Verified classes.dex contains launcher MainActivity, Application, and Kotlin runtime intrinsics.')
"

# 5. Assemble and 4-Byte ZipAlign Uncompressed Entries
echo "==> [5/6] Assembling and applying 4-byte ZipAlignment to uncompressed resources..."
python3 -c "
import zipfile, struct

base_apk = '$BUILD_DIR/base_linked.apk'
dex_file = '$BUILD_DIR/dex/classes.dex'
aligned_unsigned_apk = '$BUILD_DIR/app-aligned-unsigned.apk'
valid_date = (2026, 11, 23, 12, 0, 0)
alignment = 4

with zipfile.ZipFile(base_apk, 'r') as zin, zipfile.ZipFile(aligned_unsigned_apk, 'w') as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        compress_type = zipfile.ZIP_STORED if item.filename == 'resources.arsc' else zipfile.ZIP_DEFLATED
        new_item = zipfile.ZipInfo(item.filename, valid_date)
        new_item.compress_type = compress_type
        
        extra = b''
        if compress_type == zipfile.ZIP_STORED:
            current_pos = zout.fp.tell()
            fn_len = len(new_item.filename.encode('utf-8'))
            header_len = 30 + fn_len
            data_offset = current_pos + header_len
            padding = (alignment - (data_offset % alignment)) % alignment
            if padding > 0:
                extra = b'\x00' * padding
        
        new_item.extra = extra
        zout.writestr(new_item, data, compress_type=compress_type)
    
    # Append classes.dex (deflated)
    with open(dex_file, 'rb') as f:
        dex_data = f.read()
    dex_info = zipfile.ZipInfo('classes.dex', valid_date)
    dex_info.compress_type = zipfile.ZIP_DEFLATED
    zout.writestr(dex_info, dex_data)

# Strictly verify 4-byte alignment of every STORED entry
with open(aligned_unsigned_apk, 'rb') as f:
    raw = f.read()

p = 0
while True:
    idx = raw.find(b'PK\x03\x04', p)
    if idx == -1:
        break
    compress = struct.unpack('<H', raw[idx+8:idx+10])[0]
    fn_len = struct.unpack('<H', raw[idx+26:idx+28])[0]
    extra_len = struct.unpack('<H', raw[idx+28:idx+30])[0]
    filename = raw[idx+30:idx+30+fn_len].decode('utf-8', errors='ignore')
    data_offset = idx + 30 + fn_len + extra_len
    if compress == 0:
        if data_offset % 4 != 0:
            raise ValueError(f'CRITICAL: {filename} is NOT 4-byte aligned! offset={data_offset}')
        print(f'Verified 4-byte alignment: {filename} at byte offset {data_offset} (offset % 4 == 0)')
    p = idx + 4
"

# 6. Cryptographically Sign Release & Debug APKs with Google ApkSigner
echo "==> [6/6] Cryptographically signing release and debug packages with Google ApkSigner..."
cat << 'EOF' > "$BUILD_DIR/SignApk.kt"
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

fun main(args: Array<String>) {
    val inApk = File(args[0])
    val outApk = File(args[1])
    val ksFile = File(args[2])
    val ksPass = args[3].toCharArray()
    val alias = args[4]

    val ks = KeyStore.getInstance("JKS")
    FileInputStream(ksFile).use { ks.load(it, ksPass) }

    val key = ks.getKey(alias, ksPass) as PrivateKey
    val cert = ks.getCertificate(alias) as X509Certificate

    val signerConfig = com.android.apksig.ApkSigner.SignerConfig.Builder(
        alias,
        key,
        listOf(cert)
    ).build()

    val signer = com.android.apksig.ApkSigner.Builder(listOf(signerConfig))
        .setInputApk(inApk)
        .setOutputApk(outApk)
        .setV1SigningEnabled(true)
        .setV2SigningEnabled(true)
        .setV3SigningEnabled(true)
        .setMinSdkVersion(26)
        .build()

    signer.sign()

    val verifier = com.android.apksig.ApkVerifier.Builder(outApk).build()
    val result = verifier.verify()
    if (!result.isVerified) {
        throw IllegalStateException("APK signature verification failed for ${outApk.name}!")
    }
    println("Signature verification PASSED for ${outApk.name}: V2=${result.isVerifiedUsingV2Scheme}, V3=${result.isVerifiedUsingV3Scheme}")
}
EOF

kotlinc -cp "$BUNDLETOOL" "$BUILD_DIR/SignApk.kt" -include-runtime -d "$BUILD_DIR/SignApk.jar"

# Sign Production Release APK
java -cp "$BUNDLETOOL:$BUILD_DIR/SignApk.jar" SignApkKt \
  "$BUILD_DIR/app-aligned-unsigned.apk" \
  "$RELEASE_DIR/DetectiveGenius-release.apk" \
  "$KEYSTORE" \
  "$KEY_PASS" \
  "$KEY_ALIAS"

# Create Testing Named Copy
cp "$RELEASE_DIR/DetectiveGenius-release.apk" "$RELEASE_DIR/DetectiveGenius-Testing-1.0.1.apk"

# Sign Debug APK with standard Android debug key
java -cp "$BUNDLETOOL:$BUILD_DIR/SignApk.jar" SignApkKt \
  "$BUILD_DIR/app-aligned-unsigned.apk" \
  "$RELEASE_DIR/DetectiveGenius-debug.apk" \
  "$DEBUG_KEYSTORE" \
  "android" \
  "androiddebugkey"

# Mirror to standard Gradle output paths
mkdir -p "$ROOT_DIR/app/build/outputs/apk/release" "$ROOT_DIR/app/build/outputs/apk/debug"
cp "$RELEASE_DIR/DetectiveGenius-release.apk" "$ROOT_DIR/app/build/outputs/apk/release/app-release.apk"
cp "$RELEASE_DIR/DetectiveGenius-release.apk" "$ROOT_DIR/app/build/outputs/apk/release/DetectiveGenius-release.apk"
cp "$RELEASE_DIR/DetectiveGenius-debug.apk" "$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"

# Final Alignment Verification on Signed Production APK
python3 -c "
import struct

with open('$RELEASE_DIR/DetectiveGenius-release.apk', 'rb') as f:
    raw = f.read()

p = 0
while True:
    idx = raw.find(b'PK\x03\x04', p)
    if idx == -1:
        break
    compress = struct.unpack('<H', raw[idx+8:idx+10])[0]
    fn_len = struct.unpack('<H', raw[idx+26:idx+28])[0]
    extra_len = struct.unpack('<H', raw[idx+28:idx+30])[0]
    filename = raw[idx+30:idx+30+fn_len].decode('utf-8', errors='ignore')
    data_offset = idx + 30 + fn_len + extra_len
    if compress == 0:
        if data_offset % 4 != 0:
            raise ValueError(f'CRITICAL: {filename} in signed APK is NOT 4-byte aligned! offset={data_offset}')
        print(f'Final Signed APK Alignment Verified: {filename} offset={data_offset} (divisible by 4)')
    p = idx + 4
"

echo "=============================================="
echo " BUILD & VERIFICATION SUCCESSFUL!             "
echo " Release: $RELEASE_DIR/DetectiveGenius-release.apk"
echo " Testing: $RELEASE_DIR/DetectiveGenius-Testing-1.0.1.apk"
echo " Debug:   $RELEASE_DIR/DetectiveGenius-debug.apk"
echo " Size:    $(du -h "$RELEASE_DIR/DetectiveGenius-release.apk" | cut -f1)"
echo " SHA-256: $(sha256sum "$RELEASE_DIR/DetectiveGenius-release.apk" | cut -d' ' -f1)"
echo "=============================================="
apkutils info "$RELEASE_DIR/DetectiveGenius-release.apk"
