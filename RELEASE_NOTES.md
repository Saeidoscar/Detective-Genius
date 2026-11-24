# Detective Genius v1.0.1 — Multiplayer LAN, 8-Hour Clock & UI Refinement Release

Welcome to the updated production release of **Detective Genius (کارآگاه نابغه - v1.0.1)**!

This release fulfills all gameplay refinements and design fixes requested by the user:
- **🎨 Refined Avatar Picker (Fix for Image-1 Overflow):**
  - The avatar selector is now elegantly contained inside a dedicated bordered tray (`.avatar-selector-tray`) strictly within card bounds.
  - Sized at 44×44 px with smooth touch-scrolling, active glow, and balanced margins on all sides — no overflowing, bleeding, or clipping out of bands.
- **📶 Host IP Display & Smooth LAN/Hotspot Multiplayer:**
  - When the host creates a room, the device's actual LAN / Wi-Fi / Hotspot IP address (e.g. `192.168.43.1` or `192.168.1.X`) is clearly detected and displayed in a dedicated card with a 1-tap **📋 کپی IP** button.
  - Unique Room ID (e.g. `DG-4829`) with a 1-tap **📋 کپی شناسه** button.
  - Quick-preset IP buttons in Join Room (`📱 هات‌اسپات پیش‌فرض` and `📶 وای‌فای مشترک`).
- **📖 Comprehensive Network & Hotspot FAQ Guide:**
  - Built-in interactive guide in Persian detailing:
    1. **Connecting via Mobile Hotspot (هات‌اسپات همراه):** 100% offline, zero internet or mobile data required. Default Android gateway IP is `192.168.43.1`.
    2. **Connecting via Shared Wi-Fi (مودم مشترک).**
    3. **Fixing Hotspot Limit on Xiaomi & Samsung:** Step-by-step path to change `Limit of connected devices` to `Unlimited`.
    4. **Fixing AP / Client Isolation:** Explains router isolation and how Host Hotspot completely solves it.
    5. **VPN Advisory:** Turning off VPNs for local network play.
- **⏱️ 8-Hour Maximum Running Clock & Overtime / Tie-Breaking Rules:**
  - Running countdown clock (max 8 hours: `08:00:00`) visible in the header and Mystery Board.
  - Persistent wall-clock calculation across app restarts and phone locks.
  - If 8 hours elapse without a normal 4-mystery winner:
    - **Priority #1:** Player with the **most right guesses** (`rightGuesses`) wins!
    - **Priority #2:** If tied on #1, player with the **fewest wrong guesses** (`less wrong guesses`) wins!
    - **Priority #3 (Overtime):** If still tied, award **3 more guesses** and **1 additional hour** to tied players until a winner is set!
- **🔄 Unique Room ID & Seamless Player Reconnection:**
  - Every player receives a unique Room ID (`DG-XXXX`) and session token.
  - Continuous persistence in `localStorage`: if a player turns off Wi-Fi, locks their phone, or leaves the app, reopening presents a 1-tap **«ادامه پرونده فعال» (Resume Active Case)** button that restores their exact screen, questions, answers, package, and timer!

---

## 📦 Direct Downloads & Verified Release Packages

| Artifact File | Size | Format / Build Type | Version | SHA-256 Checksum | Direct Download Link |
|---|---|---|---|---|---|
| **`DetectiveGenius-release.apk`** | ~2.8 MB | Android Production Release (Signed v1+v2+v3) | **1.0.1** (code: 2) | `7d0feef9ef561e9dece528a59da43f715150435502d1fc7a22f267b122cbca70` | [Download Release APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-release.apk) |
| **`DetectiveGenius-Testing-1.0.1.apk`** | ~2.8 MB | Android Release Test Artifact | **1.0.1** (code: 2) | `7d0feef9ef561e9dece528a59da43f715150435502d1fc7a22f267b122cbca70` | [Download Test APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-Testing-1.0.1.apk) |
| **`DetectiveGenius-debug.apk`** | ~2.8 MB | Android Debug Build (Standard Debug Key) | **1.0.1** (code: 2) | `cce4b9528143dc89d8e980994b419e78e74b480aa9899b2222458df64ba1ad91` | [Download Debug APK](https://github.com/Saeidoscar/Detective-Genius/raw/main/release/DetectiveGenius-debug.apk) |

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
3. Open **کارآگاه نابغه (Detective Genius)** — enjoy seamless multiplayer with contained avatars, host IP display, 8-hour clock, and instant session restore!
