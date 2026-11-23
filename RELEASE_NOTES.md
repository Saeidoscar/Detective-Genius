# Detective Genius v1.0.0 — Production Release (Play Store Ready)

Welcome to the initial official production release of **Detective Genius**!

Detective Genius is an offline-first social deduction and mystery-solving party game for **4 to 20 players** (even player counts) over local Wi-Fi or mobile hotspot.

---

## 📦 Direct Download & Release Asset

👉 **[📥 Download DetectiveGenius-release.apk](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk)**

| File | Size | Format | SHA-256 Checksum | Download Link |
|---|---|---|---|---|
| **`DetectiveGenius-release.apk`** | ~116 KB | Android Release APK (Signed) | `aa3db656c1f966fec320c30f49650d396fbed501c3689be0250c6ffcdd79f11f` | [Download APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk) |

---

## 🛡️ Google Play Store Compliance Certification

This release APK has been compiled, aligned, and signed to meet all Google Play Store 2024–2026 submission standards:

- **Target SDK:** API level **34** (Android 14) — fully meets current Google Play target requirements.
- **Minimum SDK:** API level **26** (Android 8.0 Oreo) — compatible with >95% of active Android devices.
- **Package ID:** `com.detectivegenius`
- **Version Code:** `1`
- **Version Name:** `1.0.0`
- **Component Security:** `android:exported="true"` explicitly declared on launcher `MainActivity`.
- **Orientation Lock:** Strictly enforced `android:screenOrientation="portrait"` across the entire app.
- **Resource Alignment:** `resources.arsc` stored uncompressed (`ZIP_STORED`) for optimal 4-byte runtime memory-mapped access.
- **Signature Schemes:** Signed with **APK Signature Scheme v2** (`true`) and **APK Signature Scheme v3** (`true`).
- **Release Keystore:** 2048-bit RSA key with 10,000-day validity (~27 years), SHA-384 digest (`CN=Detective Genius, OU=Mobile, O=Detective Genius Games, L=Nuremberg, ST=Bavaria, C=DE`).
- **Permissions Policy:** Non-invasive local network permissions only (`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE`, `VIBRATE`). Zero invasive runtime permissions required.

---

## 🎮 Game Features

- **Offline-First Multiplayer:** Play anywhere on a local Wi-Fi network or an Android phone's portable hotspot — no internet connection required.
- **Authoritative Host Engine:** One player hosts and plays simultaneously without unfair gameplay advantages.
- **4 to 20 Players (Even Counts):** Constrained randomized solver rigorously balances package delivery across all player counts (4, 6, 8, 10, 12, 14, 16, 18, 20).
- **4 Mystery Challenges:**
  - **Q1:** *Who answered my questions?*
  - **Q2:** *Whose questions did I answer?*
  - **Q3:** *Whose questions am I holding?* (Set of 2 authors)
  - **Q4:** *Whose answers am I holding?* (Set of 2 answerers)
- **Elimination & Spectator Mode:** 3rd mistake eliminates the player, but spectators remain fully engaged in chat and viewing live mystery resolution.
- **24 Original Stylized Avatars:** Distinct characters with 6 emotional states (`IDLE`, `SELECTED`, `THINKING`, `TALKING`, `ELIMINATED`, `VICTORY`).
- **In-Game Chat:** Public room chat and strictly isolated 1-to-1 private chat with typing indicators and unread counters.
- **Bilingual English & Persian (Farsi):** 100% string parity with authentic RTL layout, typography, and numbers.

---

## 📲 How to Install

### Option A: Direct Sideload on Android
1. Click **[Download DetectiveGenius-release.apk](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk)** on your Android device (Android 8.0+).
2. Open the downloaded file and allow installation from unknown sources if prompted.
3. Launch **Detective Genius** from your home screen or app drawer.

### Option B: Install via ADB
Connect your Android phone via USB debugging and run:
```bash
adb install -r DetectiveGenius-release.apk
```
