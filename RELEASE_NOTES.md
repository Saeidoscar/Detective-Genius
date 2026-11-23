# Detective Genius v1.0.1 — Production Release (Play Store Ready)

Welcome to the official production release of **Detective Genius (v1.0.1)**!

Detective Genius is an offline-first social deduction and mystery-solving party game for **4 to 20 players** (even player counts) over local Wi-Fi or mobile hotspot.

---

## 📦 Direct Download & Release Asset

👉 **[📥 Download DetectiveGenius-release.apk](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk)**

| File | Size | Format | Version | SHA-256 Checksum | Download Link |
|---|---|---|---|---|---|
| **`DetectiveGenius-release.apk`** | ~1.0 MB (1,000 KB) | Android Release APK (Signed) | **1.0.1** (code: 2) | `7322d8e28f153c118ebb8ace771dcf513c2ffb3282b2d1ff4a4e2b648dcf2b67` | [Download APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk) |

---

## 🛡️ Google Play Store Compliance Certification

This release APK has been compiled, aligned, and signed to meet all Google Play Store 2024–2026 submission standards:

- **Target SDK:** API level **34** (Android 14) — fully meets current Google Play target requirements.
- **Minimum SDK:** API level **26** (Android 8.0 Oreo) — compatible with >95% of active Android devices.
- **Package ID:** `com.detectivegenius`
- **Version Code:** `2`
- **Version Name:** `1.0.1`
- **Component Security:** `android:exported="true"` explicitly declared on launcher `MainActivity`.
- **Orientation Lock:** Strictly enforced `android:screenOrientation="portrait"` across the entire app.
- **Resource Alignment:** `resources.arsc` stored uncompressed (`ZIP_STORED`) for optimal 4-byte runtime memory-mapped access.
- **Signature Schemes:** Signed with **APK Signature Scheme v2** (`true`) and **APK Signature Scheme v3** (`true`).
- **Release Keystore:** 2048-bit RSA key with 10,000-day validity (~27 years), SHA-384 digest (`CN=Detective Genius, OU=Mobile, O=Detective Genius Games, L=Nuremberg, ST=Bavaria, C=DE`).
- **Permissions Policy:** Non-invasive local network permissions only (`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE`, `VIBRATE`). Zero invasive runtime permissions required.

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

## 📲 How to Install (Important Notice for Previous Testers)

> **⚠️ If you previously installed an earlier build of Detective Genius on your phone:**
> Android's PackageInstaller blocks updates when an earlier crashed build is installed or cached.
> 1. **Uninstall the old "Detective Genius" app** from your phone first: Long-press the Detective Genius icon on your home screen or app drawer and tap **Uninstall**.
> 2. Click **[Download DetectiveGenius-release.apk](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk)**.
> 3. Open the downloaded file and tap **Install**.
> 4. Launch **Detective Genius** and enjoy the game!
