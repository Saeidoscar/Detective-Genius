# Detective Genius — Development Roadmap

**Product Version:** 1.0.0  
**Target Architecture:** Android (Jetpack Compose, Clean Architecture, Kotlin 2.0+, Ktor)

---

## Roadmap Milestones Status

### Milestone 1 — Project Foundation
- [x] Analyze requirements and initialize documentation suite (`GAME_DESIGN.md`, `TECHNICAL_ARCHITECTURE.md`, `DEVELOPMENT_ROADMAP.md`, `IMPLEMENTATION_STATUS.md`, `TESTING.md`, `CHANGELOG.md`).
- [x] Configure build environment and toolchain (Kotlin JVM compiler, Gradle configuration).
- [x] Establish design tokens (dark/light theme, investigation palette, Persian RTL typography, spacing).
- [x] Externalize string resources for English (`res/values/strings.xml`) and Persian (`res/values-fa/strings.xml`).
- [x] Enforce portrait-only orientation in AndroidManifest.
- [x] Define shared models, protocols, and DTO contracts.

### Milestone 2 — Game Engine & Assignment Algorithm
- [x] Domain models (Player, Question, Answer, QuestionPackage, MysteryChallenge, GameState).
- [x] Finite State Machine (`LOBBY` -> `QUESTION_CREATION` -> `ANSWERING` -> `DISTRIBUTION` -> `INVESTIGATION` -> `FINISHED`).
- [x] Validation rules for Questions (5-300 chars, no duplicates) and Answers (1-500 chars).
- [x] Constrained randomized assignment algorithm fulfilling all 13 invariants.
- [x] Challenge validation for Q1, Q2, Q3 (set of 2 authors), Q4 (set of 2 answerers).
- [x] Elimination rules (3 mistakes) and serialized winner resolution.
- [x] Pure Kotlin test suite running thousands of randomized simulations for player counts 4, 6, 8, 10, 12, 14, 16, 18, 20 (9,000 simulations passing 100%).

### Milestone 3 — Local Multiplayer Networking & Protocol
- [x] Protocol definitions with versioning, payload validation, and message typing.
- [x] Embedded Ktor WebSocket server for host device.
- [x] Ktor WebSocket client for player devices.
- [x] Service discovery abstraction (Android NSD / mDNS, QR payload generation, manual IP + PIN).
- [x] Session tokens, authorization checks, rate-limiting, and duplicate message filtering.
- [x] State synchronization and sanitized DTO broadcasting.

### Milestone 4 — Full Gameplay Features & UI
- [x] Onboarding and Home screen with create/join navigation.
- [x] Room creation and discovery/manual join screens.
- [x] Lobby with live player list, avatar selector, ready status, and host start constraints.
- [x] Question creation interface (3 questions, progress tracker, validation).
- [x] Answering interface (anonymous questions, answer input, association preservation).
- [x] Distribution animation state and sealed package delivery.
- [x] Mystery Board with interactive challenges Q1-Q4, mistake counter, and guess picker.
- [x] Package Viewer for inspecting received packages and evidence clues.
- [x] Victory screen and respectful elimination spectator mode.
- [x] Settings screen (language, theme, sound, haptics, reduced motion).

### Milestone 5 — Social & Communication System (Chat)
- [x] Group Chat with timestamps, player avatars, online indicators, and RTL/LTR support.
- [x] Private Chat (1-to-1) with message routing, typing indicators, and unread counts.
- [x] Chat privacy enforcement (strictly preventing game secret leaks and routing only to sender and recipient).

### Milestone 6 — Visual Polish, Avatars, Animations & Audio
- [x] 24 original stylized avatars with 6 emotional states (Idle, Selected, Thinking, Talking, Eliminated, Victory).
- [x] Compose animation effects (sealed package delivery, challenge solved celebrations, card transitions).
- [x] Audio and haptics manager for offline bundled sound cues and feedback patterns.

### Milestone 7 — Reliability, Security & Host Recovery
- [x] Session snapshots and state persistence for host crash recovery.
- [x] Reconnection protocol and authorized state restoration.
- [x] Edge cases, disconnect timeouts, and stress tests.

### Milestone 8 — Final Verification & Release Readiness
- [x] Full automated test suite verification (Engine tests + Network tests).
- [x] Definition of Done checklist audit.
- [x] Release documentation and changelog completion.
- [x] Live portrait preview companion verification.
- [x] Signed Android Release APK (`release/DetectiveGenius-release.apk`) generated and verified.
- [x] Google Play Store compliance audit (targetSdk 34, minSdk 26, V2/V3 signatures, explicit exports, uncompressed resources.arsc).
