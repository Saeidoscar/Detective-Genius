# Detective Genius v1.0.1 — Official Game Logo & Production Release

Welcome to the updated production release of **Detective Genius (کارآگاه نابغه - v1.0.1)**!

This release introduces the **Official Detective Genius Artwork & Brand Logo** across all Android launcher icons, app stores, and in-game screens:
- **🎨 Official Game Logo & Icons:**
  - Integrated the official Detective Genius artwork (the fedora detective with magnifying glass, evidence notes, and golden 3D lettering) across all Android launcher mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).
  - Generated circular anti-aliased launcher icons (`ic_launcher_round.png`) for modern Android / HyperOS / MIUI circular launchers.
  - High-resolution in-app branding on the Home screen, Top Header bar, and Victory screen.
- **🇮🇷 Farsi as Primary Language:** The game launches directly in authentic, natural Persian with full right-to-left (RTL) layout, typography, and detective terminology.
- **✍️ Manual Question Formulation + Suggestions + Profile Bank:** Question inputs start completely empty for manual formulation. Players can browse curated mystery suggestions or load favorite templates from their persistent Profile Saved Questions bank.
- **💬 Completely Manual Answers:** All answer inputs are strictly manual — no mock answers or pre-filled texts.
- **👥 Real Players Only (No Mock Bots):** Completely removed hardcoded fake/mock players (Sherlock, Watson, Irene, Moriarty). The room consists strictly of real players, with an intuitive "Add Detective" interface for real friends playing together.
- **✉️ Exactly 1 Sealed Evidence Package:** Replaced the confusing 2-package system with **1 single sealed evidence package** containing one set of 3 questions and answers addressed to each other.
- **🔍 4 Focused Single-Suspect Mysteries:**
  1. *Who answered my questions?* (کی به سوالات من پاسخ داد؟)
  2. *Whose questions did I answer?* (من به سوالات چه کسی پاسخ دادم؟)
  3. *Who authored the questions in my package?* (نویسنده سوالات پاکت مدارک من کیست؟)
  4. *Who answered the questions in my package?* (پاسخ‌دهنده سوالات پاکت مدارک من کیست؟)
- **📱 4-Byte ZipAligned & Dual-Signed:** Verified on physical Xiaomi Poco M5s and Android 8.0–14 devices.

---

## 📦 Direct Downloads & Verified Release Packages

| Artifact File | Size | Format / Build Type | Version | SHA-256 Checksum | Direct Download Link |
|---|---|---|---|---|---|
| **`DetectiveGenius-release.apk`** | ~2.8 MB | Android Production Release (Signed v1+v2+v3) | **1.0.1** (code: 2) | `d03d70022c696ca069220e31c7db91033c87f237c375ae2a1bb2646614be60d0` | [Download Release APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk) |
| **`DetectiveGenius-Testing-1.0.1.apk`** | ~2.8 MB | Android Release Test Artifact | **1.0.1** (code: 2) | `d03d70022c696ca069220e31c7db91033c87f237c375ae2a1bb2646614be60d0` | [Download Test APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-Testing-1.0.1.apk) |
| **`DetectiveGenius-debug.apk`** | ~2.8 MB | Android Debug Build (Standard Debug Key) | **1.0.1** (code: 2) | `626bdc083e35e6204d5c061408cf6e1391c5168ac01866478b8b78a8375c8244` | [Download Debug APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-debug.apk) |

---

## 🛡️ Package Compliance & Technical Verification

- **Package ID:** `com.detectivegenius`
- **Version Code:** `2` | **Version Name:** `1.0.1`
- **Minimum SDK:** API 26 (Android 8.0 Oreo) | **Target SDK:** API 34 (Android 14)
- **App Launcher Icons:** Official Detective Genius artwork packaged across all densities (`res/mipmap-*/ic_launcher.png`, `ic_launcher_round.png`)
- **Screen Orientation:** Enforced portrait (`portrait`)
- **4-Byte ZipAlignment:** `resources.arsc` at byte offset `1346876` (`1346876 % 4 == 0`, PASS)
- **Signature Schemes:** v1 (JAR Scheme: `true`), v2 (Full APK Scheme: `true`), v3 (Key Rotation Scheme: `true`)
- **Runtime Dependencies:** 100% offline, zero internet or cloud access required to install or play.

---

## 📲 Installation on Xiaomi Poco M5s & Android Devices

1. Tap **[Download Release APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk)**.
2. Tap the APK file in your Downloads or File Manager and confirm **Install**.
3. Open **کارآگاه نابغه (Detective Genius)** — your phone will display the official Detective Genius app icon on your home screen and launcher, opening directly into the game.
