#!/usr/bin/env bash
set -euo pipefail

echo "=============================================="
echo " Building Detective Genius Release APK        "
echo " Target SDK: 34 (Android 14)                  "
echo " Min SDK: 26 (Android 8.0)                    "
echo " Native Android Activity & Offline Assets     "
echo " Play Store Ready & Signed                    "
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

# 1. Compile Resources with AAPT2
echo "==> [1/5] Compiling resources with AAPT2..."
rm -rf "$BUILD_DIR/compiled_res"/*
"$AAPT2" compile --dir app/src/main/res -o "$BUILD_DIR/compiled_res"

# 2. Link Resources & Manifest with Assets
echo "==> [2/5] Linking binary resources, assets, and AndroidManifest.xml..."
"$AAPT2" link \
  -I "$ANDROID_JAR" \
  --manifest app/src/main/AndroidManifest.xml \
  -A app/src/main/assets \
  --min-sdk-version 26 \
  --target-sdk-version 34 \
  --version-code 1 \
  --version-name "1.0.0" \
  -o "$BUILD_DIR/base_linked.apk" \
  "$BUILD_DIR/compiled_res"/*.flat

# 3. Compile Kotlin Code to Class Bytecode (including MainActivity & DetectiveGeniusApp!)
echo "==> [3/5] Compiling Kotlin application and domain engine..."
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

# 4. Convert Class Files to Dalvik Executable (classes.dex) with D8 (including Kotlin stdlib runtime!)
echo "==> [4/5] Running Google D8 dexer to produce classes.dex with Kotlin runtime..."
rm -rf "$BUILD_DIR/dex"/*
KOTLIN_STDLIB="/usr/local/lib/node_modules/kotlin-compiler/lib/kotlin-stdlib.jar"
java -cp "$BUNDLETOOL" com.android.tools.r8.D8 \
  --lib "$ANDROID_JAR" \
  --min-api 26 \
  --release \
  --output "$BUILD_DIR/dex" \
  "$KOTLIN_STDLIB" \
  $(find "$BUILD_DIR/classes" -name "*.class")

# Verify that MainActivity, DetectiveGeniusApp, and Kotlin Intrinsics are present in classes.dex
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

# 5. Assemble and Sign Release APK (v1, v2, v3 Signature Schemes)
echo "==> [5/5] Assembling and signing release APK with Google ApkSigner..."
python3 -c "
import zipfile, os

base_apk = '$BUILD_DIR/base_linked.apk'
dex_file = '$BUILD_DIR/dex/classes.dex'
unsigned_apk = '$BUILD_DIR/app-unsigned.apk'

with zipfile.ZipFile(base_apk, 'r') as zin, zipfile.ZipFile(unsigned_apk, 'w') as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        # resources.arsc must be stored uncompressed for 4-byte runtime alignment
        compress_type = zipfile.ZIP_STORED if item.filename == 'resources.arsc' else zipfile.ZIP_DEFLATED
        zout.writestr(item, data, compress_type=compress_type)
    
    with open(dex_file, 'rb') as f:
        dex_data = f.read()
    dex_info = zipfile.ZipInfo('classes.dex')
    dex_info.compress_type = zipfile.ZIP_DEFLATED
    zout.writestr(dex_info, dex_data)
"

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
        throw IllegalStateException("APK signature verification failed!")
    }
    println("APK signature verification PASSED: V2=${result.isVerifiedUsingV2Scheme}, V3=${result.isVerifiedUsingV3Scheme}")
}
EOF

kotlinc -cp "$BUNDLETOOL" "$BUILD_DIR/SignApk.kt" -include-runtime -d "$BUILD_DIR/SignApk.jar"
java -cp "$BUNDLETOOL:$BUILD_DIR/SignApk.jar" SignApkKt \
  "$BUILD_DIR/app-unsigned.apk" \
  "$RELEASE_DIR/DetectiveGenius-release.apk" \
  "$KEYSTORE" \
  "$KEY_PASS" \
  "$KEY_ALIAS"

# Create a copy in app/build/outputs/apk/release/
mkdir -p "$ROOT_DIR/app/build/outputs/apk/release"
cp "$RELEASE_DIR/DetectiveGenius-release.apk" "$ROOT_DIR/app/build/outputs/apk/release/DetectiveGenius-release.apk"

echo "=============================================="
echo " BUILD SUCCESSFUL!                            "
echo " Output: $RELEASE_DIR/DetectiveGenius-release.apk"
echo " Size: $(du -h "$RELEASE_DIR/DetectiveGenius-release.apk" | cut -f1)"
echo " SHA-256: $(sha256sum "$RELEASE_DIR/DetectiveGenius-release.apk" | cut -d' ' -f1)"
echo "=============================================="
apkutils info "$RELEASE_DIR/DetectiveGenius-release.apk"
