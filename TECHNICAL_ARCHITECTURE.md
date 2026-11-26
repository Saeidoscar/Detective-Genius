# Detective Genius — Technical Architecture

**Document Version:** 1.0  
**Status:** In Progress / Milestone 1  
**Architecture Pattern:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF)  
**Target Platform:** Android (Portrait only, API 26+ / Target SDK 34)

---

## 1. Architectural Principles

1. **Pure Kotlin Game Engine:**
   - The game rules, state machine, package assignment algorithm, and challenge validation logic reside in a pure Kotlin module (`game-engine`).
   - Zero Android framework dependencies in the game engine.
   - 100% testable via standard JVM / Kotlin test suites and simulations.

2. **Authoritative Host Model:**
   - One device acts as both Host and Player.
   - The Host runs an embedded Ktor HTTP + WebSocket server.
   - All state transitions, validation, mistake counting, assignments, and eliminations are strictly computed and authorized by the host engine.
   - Clients only maintain UI state and receive sanitized Data Transfer Objects (DTOs).

3. **Privacy by Design:**
   - Hidden author and answerer identities are NEVER sent to clients during distribution or investigation.
   - Specific DTOs (`AuthorizedGameStateDto`, `PackageSummaryDto`, `MysteryChallengeDto`) sanitize internal engine models before wire serialization.
   - Set-based mystery challenges (Q3 and Q4) do not disclose partial matches to prevent information leakage.

4. **Offline First & Resilient Networking:**
   - Operates entirely without external internet access over local Wi-Fi or Android Hotspot.
   - Room discovery via NSD (Network Service Discovery / mDNS) with fallbacks for QR-code joining and manual IP + Room PIN entry.
   - Reconnection tokens allow seamless recovery from transient Wi-Fi drops without state loss.

5. **Portrait-Only Interface & Design System:**
   - Strictly enforced portrait orientation (`android:screenOrientation="portrait"`).
   - Centralized semantic design tokens for Dark mode and Light mode.
   - Bilingual first-class support for English (LTR) and Persian (RTL) with externalized string resources.

---

## 2. System Architecture & Module Boundaries

```
+-------------------------------------------------------------+
|                           App                               |
|   (Application, MainActivity, Navigation, DI Injection)     |
+-------------------------------------------------------------+
                              |
       +----------------------+----------------------+
       |                                             |
+-----------------------+                 +---------------------+
|       Features        |                 |    Design System    |
| - Onboarding / Home   |                 | - Color Tokens      |
| - Lobby / Host / Join |                 | - Typography        |
| - Question Creation   |                 | - 24 Stylized       |
| - Answering           |                 |   Avatars           |
| - Distribution        |                 | - Animations        |
| - Mystery Board       |                 | - Audio / Haptics   |
| - Chat (Group/Private)|                 +---------------------+
| - Victory/Elimination |
+-----------------------+
       |
       +----------------------+----------------------+
       |                                             |
+-----------------------+                 +---------------------+
|      Networking       |                 |     Game Engine     |
| - Ktor WS Server      |                 |   (Pure Kotlin)     |
| - Ktor WS Client      | <-------------> | - Domain Models     |
| - NSD / Discovery     |                 | - State Machine     |
| - Session Auth        |                 | - Assignment Algo   |
| - DTO Sanitization    |                 | - Mystery Validator |
+-----------------------+                 +---------------------+
       |                                             |
       +----------------------+----------------------+
                              |
+-------------------------------------------------------------+
|                            Core                             |
|       (Common Models, Protocols, Events, Serialization)     |
+-------------------------------------------------------------+
```

---

## 3. Package Assignment Algorithm

The constrained randomized assignment algorithm solves the following bipartite distribution constraints:
1. $N$ players ($N \in \{4, 6, 8, \dots, 20\}$).
2. Exactly $N$ packages: Package $P_i$ has Author $A_i$ and Answerer $B_i$ ($A_i \ne B_i$).
3. Exactly 1 recipient per package: $R_i \notin \{A_i, B_i\}$.
4. Every player receives exactly 1 package: $\forall p, |\{P_i : p \in \text{Recipients}(P_i)\}| = 1$.
5. Distinct recipient assignment: Recipient mapping $R$ is a derangement/permutation such that no player receives their own authored or answered package.
6. Derangement mapping for Question Answering: $B = \text{Derangement}(A)$ such that $\forall i, B_i \ne A_i$.

Validation checks enforce invariants deterministically with bounded restart heuristics (max 200 attempts, average convergence < 2 attempts).

---

## 4. Mystery Challenges Validation

- **Q1 (Who answered my questions?):** Correct if $guess == \text{Answerer}(P_{\text{self}})$.
- **Q2 (Whose questions did I answer?):** Correct if $guess == \text{Author}(P_{\text{answered}})$.
- **Q3 (Whose questions am I holding?):** Correct if $guess == \text{Author}(P_{\text{held}})$.
- **Q4 (Whose answers am I holding?):** Correct if $guess == \text{Answerer}(P_{\text{held}})$.

Failure behavior: Each failed attempt increments mistake count. Upon 3 mistakes, player is transitioned to `Eliminated` state.

---

## 5. Security & Privacy Model

- **Session Tokens:** Cryptographically secure random UUID tokens issued upon room join.
- **WebSocket Route Security:** All client packets validate `(sessionId, playerId)`.
- **Information Leak Prevention:** The server generates player-specific views (`PlayerViewDto`). Secret package identities are strictly stored in host memory/persistence and never transmitted over the wire.
- **RTL & Typography:** Persian string formatting handles numerals, punctuation, and bidirectionality correctly without text corruption.

---

## 6. Release Packaging & Google Play Store Compliance

- **Build Pipeline (`build-apk.sh`):**
  - **AAPT2 (Android Asset Packaging Tool 2):** Compiles XML resources, vector drawables, launcher icons, and localizations (`values/strings.xml`, `values-fa/strings.xml`). Links compiled assets against official Android API 34 `android.jar`.
  - **Kotlin Compiler (kotlinc 2.0):** Compiles domain engine, data models, networking protocols, design tokens, and state flows to JVM class bytecode.
  - **Google D8 Dexer:** Translates class bytecode to optimized Dalvik Executable format (`classes.dex`) targeting `minSdkVersion 26` and `targetSdkVersion 34`.
  - **APK Assembly & Zip Alignment:** Stores `resources.arsc` uncompressed (`ZIP_STORED`) for immediate memory-mapped access and 4-byte runtime alignment required by Android.
  - **Google ApkSigner:** Cryptographically signs release packages with 2048-bit RSA key using dual APK Signature Scheme v2 and v3.
- **Play Store Requirements Checklist:**
  - `minSdkVersion`: 26 (Android 8.0 Oreo)
  - `targetSdkVersion`: 34 (Android 14+, fully compliant with Google Play 2024–2026 mandates)
  - Package ID: `com.detectivegenius`
  - Explicit component exports: `android:exported="true"` declared on launcher activity
  - Portrait orientation strictly locked
  - Permitted network permissions only (`INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE`, `VIBRATE`) — zero invasive permissions required.
