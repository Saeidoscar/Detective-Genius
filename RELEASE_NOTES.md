# Detective Genius v1.0.1 — Verified Physical Device & Play Store Release

Welcome to the verified production release of **Detective Genius (v1.0.1)**!

Detective Genius is an offline-first social deduction and mystery-solving party game for **4 to 20 players** (even player counts) over local Wi-Fi or mobile hotspot.

---

## 📦 Direct Downloads & Release Artifacts

Both release-signed and debug-signed APK packages are provided. Every package is verified with 4-byte ZIP alignment and full multi-scheme cryptographic signatures (v1 JAR, v2 Full APK, and v3 Rotation).

| Artifact File | Size | Format / Build Type | Version | SHA-256 Checksum | Download Link |
|---|---|---|---|---|---|
| **`DetectiveGenius-release.apk`** | ~1.0 MB (1,000 KB) | Android Production Release (Signed v1+v2+v3) | **1.0.1** (code: 2) | `5e1d4d16f8155be398229e86e80bd52117af8c17ba345eb99aa3afae41b57b1a` | [Download Release APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk) |
| **`DetectiveGenius-Testing-1.0.1.apk`** | ~1.0 MB (1,000 KB) | Android Release Test Artifact | **1.0.1** (code: 2) | `5e1d4d16f8155be398229e86e80bd52117af8c17ba345eb99aa3afae41b57b1a` | [Download Test APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-Testing-1.0.1.apk) |
| **`DetectiveGenius-debug.apk`** | ~1.0 MB (1,000 KB) | Android Debug Build (Standard Debug Key) | **1.0.1** (code: 2) | `eb7b27ce12ce79d946401039e2553dafa318812920826f7cf21aeb5eaab4a69c` | [Download Debug APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-debug.apk) |

---

## 🔧 Physical Device Installation & Root Cause Resolution

### Root Cause of "App not installed" on Xiaomi Poco M5s (MIUI / HyperOS)
- **Mechanical Defect:** In earlier builds, `resources.arsc` was stored uncompressed (`ZIP_STORED`) at byte offset `2733` (`2733 % 4 = 1`).
- **Android OS OS Constraint:** Android's native `AssetManager2.cpp` and `PackageParser` strictly require that all uncompressed resource tables (`resources.arsc`) reside at a **4-byte memory boundary** (`offset % 4 == 0`) so they can be memory-mapped (`mmap`) directly into the process address space.
- When `resources.arsc` is not 4-byte aligned, the Android package installer aborts the installation with `INSTALL_FAILED_INVALID_APK` / `INSTALL_PARSE_FAILED_BAD_MANIFEST` ("App not installed"), even after security scanners approve the file.

### Repair Applied:
1. **Pre-signing 4-Byte ZipAlign:** Added a byte-exact 4-byte zipalignment step using ZIP local header extra-field padding before invoking `apksigner`. Uncompressed `resources.arsc` is now aligned to byte offset **`2736`** (`2736 % 4 == 0`).
2. **Post-Alignment Signature:** Signed with Google `com.android.apksig.ApkSigner` using APK Signature Scheme **v1 (JAR)**, **v2 (Full APK)**, and **v3 (APK Key Rotation)**.
3. **Automated Verification:** Added strict post-signing assertions to `build-apk.sh` ensuring all uncompressed files maintain 4-byte boundary alignment and pass `ApkVerifier`.
4. **Version Alignment:** Synchronized `versionCode = 2` and `versionName = "1.0.1"` across `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, and the build pipeline.

---

## 🛡️ Android & Play Store Standards Verification

| Inspection Parameter | Verified Value | Compliance Status |
|---|---|---|
| **Package ID** | `com.detectivegenius` | ✅ Match |
| **Version Code** | `2` | ✅ Authoritative Match |
| **Version Name** | `1.0.1` | ✅ Authoritative Match |
| **Minimum SDK** | `26` (Android 8.0 Oreo) | ✅ Compatible |
| **Target SDK** | `34` (Android 14) | ✅ Google Play 2024–2026 Compliant |
| **Screen Orientation** | `portrait` | ✅ Portrait Only |
| **Launcher Activity** | `.MainActivity` (`android:exported="true"`) | ✅ Compliant |
| **Resource Alignment** | `resources.arsc` at offset `2736` (`% 4 == 0`) | ✅ 4-Byte ZipAligned |
| **Signing Scheme v1 (JAR)** | Verified `true` | ✅ Legacy Android Support |
| **Signing Scheme v2 (Full APK)** | Verified `true` | ✅ Modern Android Security |
| **Signing Scheme v3 (Key Rotation)** | Verified `true` | ✅ Android 9+ Integrity |
| **Kotlin Runtime** | `kotlin-stdlib` classes bundled in DEX (1,161 classes total) | ✅ NoClassDefFoundError Resolved |
| **Offline Assets** | WebView HTML, 31 SVG badges/stamps, 6 WAV audio clips | ✅ 100% Offline |

---

## 🎮 Game Features & Assets Included

- **Offline-First Multiplayer:** Play anywhere on a local Wi-Fi network or an Android phone's portable hotspot — no internet connection required.
- **Authoritative Host Engine:** One player hosts and plays simultaneously without unfair gameplay advantages.
- **4 to 20 Players (Even Counts):** Constrained randomized solver rigorously balances package delivery across all player counts (4, 6, 8, 10, 12, 14, 16, 18, 20).
- **4 Mystery Challenges:**
  - **Q1:** *Who answered my questions?*
  - **Q2:** *Whose questions did I answer?*
  - **Q3:** *Whose questions am I holding?* (Set of 2 authors)
  - **Q4:** *Whose answers am I holding?* (Set of 2 answerers)
- **Elimination & Spectator Mode:** 3rd mistake eliminates the player, but spectators remain fully engaged in chat and viewing live mystery resolution.
- **24 Original Stylized Avatars & Vector Stickers:** Detailed SVG character stickers for all 24 characters with 6 emotional states (`IDLE`, `SELECTED`, `THINKING`, `TALKING`, `ELIMINATED`, `VICTORY`).
- **In-Game Chat with Clue Stickers:** Public room chat and strictly isolated 1-to-1 private chat with typing indicators, unread counters, and investigation clue stamps (`EVIDENCE`, `SECRET CLUE`, `SUSPECT`, `FALSE ALIBI`, `VERIFIED`, `EUREKA!`).
- **Bundled Sound Bank:** Dedicated audio sound effects (card deals, envelope tears, chime on correct deduction, buzzer on mistake, victory fanfare).
- **Bilingual English & Persian (Farsi):** 100% string parity with authentic RTL layout, typography, and numbers.

---

## 📲 Installation Instructions for Xiaomi Poco M5s & Other Devices

1. Download **[DetectiveGenius-release.apk](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk)** (or `DetectiveGenius-Testing-1.0.1.apk`).
2. If installing via Android Package Installer / Xiaomi File Manager:
   - Tap the APK file.
   - When prompted by the system installer, confirm **Install**.
3. Launch **Detective Genius** — the app will open directly into the title screen with instant offline loading, responsive portrait layout, sound effects, and character selection.
