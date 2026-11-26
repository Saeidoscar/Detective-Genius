# Detective Genius — Testing Strategy & Verification Plan

**Version:** 1.0  
**Target:** Continuous automated verification across engine, networking, invariants, and UI  
**Execution Date:** 2026-11-23

---

## 1. Test Layers

### Layer 1: Game Engine & Invariant Verification (Pure Kotlin / JVM)
- **Constraint Solver Simulations:**
  - Exhaustive randomized tests across all even player counts: $N \in \{4, 6, 8, 10, 12, 14, 16, 18, 20\}$.
  - 1,000 simulation runs per player count (9,000 runs total) with deterministic seeds.
  - Verifies all 13 core invariants:
    1. Every player creates exactly 1 package.
    2. Every package contains exactly 3 questions.
    3. Every question has exactly 1 corresponding answer.
    4. Every package has 1 author.
    5. Every package has 1 answerer.
    6. Author != Answerer.
    7. Every package has exactly 1 recipient.
    8. Recipient != Author.
    9. Recipient != Answerer.
    10. Every player receives exactly 1 package.
    11. No player receives their own authored or answered package.
    12. No player receives the same package twice.
    13. Every player participates as author, answerer, and recipient.
- **Challenge Verification Tests:**
  - Q1: Single identity check (Author's answerer).
  - Q2: Single identity check (Answerer's author).
  - Q3: Single identity check (Held package author).
  - Q4: Single identity check (Held package answerer).
  - Verification that incorrect guesses increment mistake counter.
  - Mistake counting and elimination upon 3 mistakes.
  - Victory determination upon first player completing all 4 challenges.
  - Serialized event tie-breaking.
  - Session snapshot serialization and restoration.

### Layer 2: Protocol & Serialization Tests
- JSON serialization/deserialization contracts.
- DTO sanitization test: Assert that `ClientPlayerViewDto` contains zero hidden package authors or answerers.
- Reconnection token validation and duplicate packet filtering.

### Layer 3: Networking & Chat Isolation Tests
- Multi-client connection simulation over local WebSockets.
- Group chat broadcast verification.
- Private chat isolation verification: Assert that client C never receives messages between client A and B.
- QR payload generation and parsing.
- Manual IPv4 and 4-digit PIN validation.

---

## 2. Test Execution Log

### Test Suite 1: Pure Kotlin Engine Test Suite (`com.detectivegenius.gameengine.tests.EngineTestSuite`)
- `testValidationRules`: **PASSED** (Validated length limits 5..300 for questions, 1..500 for answers, duplicate rejection, and even player constraints)
- `testAssignmentSolverInvariantsAcrossPlayerCounts`: **PASSED** (9,000 simulations: 1,000 runs each for counts 4, 6, 8, 10, 12, 14, 16, 18, 20 with 100% success rate)
- `testCompleteGameFlowAndRules`: **PASSED** (Full lifecycle from Lobby to Answering to Investigation)
- `testMysteryChallengesAndPartialCorrectnessSecrecy`: **PASSED** (Verified single-suspect verification for Q1, Q2, Q3, and Q4 with strict mistake tracking and zero information leak)
- `testMistakesAndElimination`: **PASSED** (Verified 1 mistake, 2 mistakes, and elimination on 3rd mistake with active spectator permissions)
- `testWinnerDeterminationAndTieBreaking`: **PASSED** (Verified first player to solve all 4 wins, tie-breaking order preserved)
- `testSnapshotAndRestore`: **PASSED** (Verified zero state loss when restoring engine state)
- `testDataPrivacySanitization`: **PASSED** (Verified zero author/answerer package identities in client views)

### Test Suite 2: Networking & Protocol Test Suite (`com.detectivegenius.networking.tests.NetworkTestSuite`)
- `testDiscoveryAndQrCode`: **PASSED** (QR payload format `detectivegenius://join?ip=...&port=...&pin=...` round-trip verified)
- `testMultiClientMultiplayerAndStateSync`: **PASSED** (4 simultaneous clients synchronized through host server)
- `testPrivateChatStrictIsolation`: **PASSED** (Client 3 verified to never receive private messages exchanged between Client 1 and Client 2)
- `testDuplicateMessageProtection`: **PASSED** (Duplicate message IDs filtered and dropped idempotently)

### Test Suite 3: Release APK & Google Play Store Compliance Audit
- `testAapt2ResourceCompilation`: **PASSED** (Binary resource compilation and linking into resources.arsc)
- `testD8DalvikDexGeneration`: **PASSED** (Dalvik executable bytecode `classes.dex` generated for minSdk 26 targetSdk 34)
- `testPackageMetadata`: **PASSED** (Package: `com.detectivegenius`, App Name: `Detective Genius`, Version: `1.0.0`, versionCode: `1`)
- `testPlayStoreSdkTarget`: **PASSED** (targetSdk = 34, Android 14+ Play Store policy compliant)
- `testComponentExport`: **PASSED** (Explicit `android:exported="true"` declared on launcher Activity with IntentFilter)
- `testScreenOrientation`: **PASSED** (Strict portrait orientation locked across manifest and runtime)
- `testApkSigningSchemes`: **PASSED** (APK Signature Scheme v2 = true, v3 = true, signed with 2048-bit RSA release keystore, validity 10,000 days)
- `testResourceAlignment`: **PASSED** (resources.arsc STORED uncompressed for 4-byte runtime alignment)
