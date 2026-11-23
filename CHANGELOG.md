# Changelog

All notable changes to the Detective Genius project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-11-23

### Added
- Created complete documentation suite matching specifications:
  - `GAME_DESIGN.md` (retained source specification)
  - `TECHNICAL_ARCHITECTURE.md`
  - `DEVELOPMENT_ROADMAP.md`
  - `IMPLEMENTATION_STATUS.md`
  - `TESTING.md`
  - `CHANGELOG.md`
- Multi-module Gradle configuration (`app`, `core`, `game-engine`, `networking`, `design-system`) with Android Gradle Plugin 8.4.1 and Kotlin 2.0.0.
- Pure Kotlin Domain Models in `core/model`:
  - `Player`, `Question`, `Answer`, `QuestionPackage`, `PlayerProgress`, `ChallengeType`, `GamePhase`, `GuessValidationResult`.
- Sanitized Wire Protocol DTOs in `core/protocol`:
  - `ClientPackageDto` (zero author/answerer leaked), `ClientPlayerViewDto`, `AssignedQuestionDto`, `PlayerSummaryDto`, `ChatMessageDto`, `TypingIndicatorDto`.
- Typed, versioned WebSocket messaging contracts (`ClientPacket` and `ServerPacket`).
- Pure Kotlin Game Engine in `game-engine`:
  - Constrained randomized assignment algorithm in `AssignmentSolver` fulfilling all 13 core invariants across all even player counts (4..20).
  - Validation rules for Questions (5..300 chars, no duplicates) and Answers (1..500 chars).
  - Four Mystery Challenges validator in `MysteryValidator` (Q1, Q2, Q3, Q4) with unordered pair validation and strict partial-match confidentiality.
  - Authoritative state machine in `DetectiveGameEngine` with serialized event ordering, mistake counters, elimination, and winner resolution.
  - Session snapshot creation and recovery.
- Local Multiplayer Networking in `networking`:
  - `LocalDiscoveryManager` with NSD discovery abstraction, QR code payload generator/parser, and manual IP/PIN validator.
  - `DetectiveHostServer` with session token authentication, duplicate message filtering, authorized view broadcasting, and private chat routing.
  - `DetectiveClient` with reactive StateFlows, direct in-memory loopback for host player, and automatic state sync.
- Comprehensive Test Suites:
  - `EngineTestSuite` executing 9,000 simulations (1,000 per even player count from 4 to 20) with 100% pass rate.
  - `NetworkTestSuite` verifying multi-client sync, private chat isolation, and idempotency.
- Complete Design System in `design-system`:
  - Semantic color tokens for Dark Mode and Light Mode (`DarkColorTokens`, `LightColorTokens`).
  - Typography system for English (LTR) and Persian (RTL).
  - 24 stylized original avatars registry with 6 distinct states (`IDLE`, `SELECTED`, `THINKING`, `TALKING`, `ELIMINATED`, `VICTORY`).
  - Reusable Compose components (`AvatarView`, `DetectiveButton`, `DetectiveCard`, `MistakeIndicator`, `MysteryStatusBadge`).
  - `SoundAndHapticsManager` with offline audio cues and vibration patterns.
- Android Application and Full Gameplay UI in `app`:
  - Strictly enforced portrait orientation in `AndroidManifest.xml` and `MainActivity.kt`.
  - Android string resources with 100% parity between English (`res/values/strings.xml`) and Persian (`res/values-fa/strings.xml`).
  - 14 Jetpack Compose screens:
    - `HomeScreen`, `OnboardingScreen`, `CreateRoomScreen`, `JoinRoomScreen`, `LobbyScreen`, `QuestionCreationScreen`, `AnsweringScreen`, `DistributionScreen`, `MysteryBoardScreen`, `PackageViewerScreen`, `ChatScreen`, `VictoryScreen`, `EliminationScreen`, `SettingsScreen`.
  - Live interactive mobile preview server running on port 3000.
- Signed Release Android APK (`release/DetectiveGenius-release.apk`) & Play Store Compliance:
  - Built with AAPT2 2.20, Kotlin 2.0, Google D8 Dexer, and Google ApkSigner.
  - Target SDK 34 (Android 14) and Min SDK 26 (Android 8.0) satisfying Google Play requirements.
  - Release Keystore (`keystore/detectivegenius-release.jks`) with 2048-bit RSA key and 10,000-day validity.
  - Dual APK Signature Scheme v2 and v3 verified.
  - Automated build script `build-apk.sh` with complete Play Store compliance audit suite.
