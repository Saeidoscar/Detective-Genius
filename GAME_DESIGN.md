# Detective Genius
## Game Design, Product Requirements & Technical Development Specification

**Document Version:** 1.0  
**Product:** Detective Genius  
**Platform:** Android  
**Orientation:** Portrait only  
**Game Type:** Offline Local Multiplayer / Social Deduction / Mystery Party Game  
**Minimum Players:** 4  
**Maximum Players (V1):** 20  
**Supported Languages:** English and Persian (Farsi)  
**Connectivity:** Local Wi-Fi only; no internet required

---

# 1. Product Vision

Detective Genius is a premium, visually polished, offline multiplayer social deduction game designed for families and groups of friends.

Players exchange anonymous questions and answers, receive mysterious collections of other players' content, and try to reconstruct the hidden relationships behind them.

The experience should combine:
- The social excitement of party games.
- The curiosity of detective investigations.
- The psychological fun of bluffing and deduction.
- The satisfaction of solving interconnected mysteries.
- A friendly, playful atmosphere suitable for families.

This must be a complete, functional Android game, not a prototype or a collection of disconnected screens.

## Core product principles

1. Offline-first: the game must work without internet access.
2. Local multiplayer: devices communicate over the same local Wi-Fi network.
3. Host-as-player: the host device participates in the game like everybody else.
4. Fairness: all game rules and scoring are enforced by the authoritative game engine.
5. Privacy: players only receive information they are authorized to see.
6. Accessibility: readable typography, accessible controls, and reduced-motion support.
7. Premium design: coherent visual identity, polished animation, and original avatars.
8. Portrait-only: every screen and interaction is designed exclusively for vertical phone orientation.
9. Bilingual: English and Persian are first-class interfaces, not afterthoughts.

---

# 2. Game Overview

Players answer questions written by another participant. Their questions and answers are assembled into anonymous packages.

Every player receives two packages and must investigate four mysteries:

1. Who answered my questions?
2. Whose questions did I answer?
3. Whose questions am I holding?
4. Whose answers am I holding?

The first player to correctly solve all four mysteries wins.

Players can challenge their theories, make mistakes, and eventually be eliminated. Even eliminated players remain part of the social experience through chat and observation.

## Game configuration

| Setting | Requirement |
|---|---|
| Minimum players | 4 |
| Maximum players | 20 |
| Player count | Even numbers only |
| Packages created per player | 1 |
| Questions per package | 3 |
| Answers per package | 3 |
| Recipients per package | Exactly 2 |
| Packages received per player | Exactly 2 |
| Incorrect submissions tolerated | 2 |
| Elimination | Third incorrect submission |
| Winner | First player to solve all four mysteries |
| Network | Local Wi-Fi |
| Internet | Not required |
| Orientation | Portrait only |

---

# 3. Player Roles and Game Structure

Every participant has two roles:

- **Question Author:** writes three questions.
- **Answerer:** answers three questions written by another participant.

Each player creates one package containing three questions and their corresponding answers.

A package preserves the original author-answerer relationship.

For example:

- Player A writes three questions.
- Player B answers those questions.
- The resulting package contains A's questions and B's corresponding answers.

The package is then distributed to exactly two eligible recipients.

A recipient cannot be:
- The author of that package.
- The answerer of that package.

Every player must receive exactly two packages from distinct authors.

The assignment algorithm should preferably also ensure distinct answerers for the two packages a player receives, whenever the constraints permit.

---

# 4. Game Flow

The game follows this state machine:

```text
LOBBY
  |
  v
QUESTION_CREATION
  |
  v
ANSWERING
  |
  v
DISTRIBUTION
  |
  v
INVESTIGATION
  |
  v
FINISHED
```

## 4.1 Lobby

Players create or join a room.

Requirements:
- Host creates the room.
- Other players discover or join it.
- Players choose a nickname and avatar.
- The lobby displays connected players.
- Players can mark themselves ready.
- Host can start only when:
  - At least four players have joined.
  - The player count is even.
  - All players are ready.
- New players cannot join after the game starts.

## 4.2 Question Creation

Every player creates exactly three questions.

Requirements:
- Minimum question length: 5 characters.
- Maximum question length: 300 characters.
- All three questions are required.
- Duplicate questions from the same author are forbidden.
- Questions remain editable until final submission.
- Submitted questions become immutable.

Display a clear progress indicator:

`2 / 3 Questions Completed`

The interface should make writing questions enjoyable rather than feel like filling out a form.

## 4.3 Answering

Each player receives three questions from one other participant.

They must provide an answer to each question.

Requirements:
- Maximum answer length: 500 characters.
- All three answers are required.
- Answers cannot be edited after final submission.
- Every answer must retain its association with its original question ID.

The interface must never reveal the identity of the question author.

## 4.4 Distribution

The server generates packages and distributes them.

Each package contains:
- Three questions.
- Three corresponding answers.
- Hidden author identity.
- Hidden answerer identity.
- Exactly two recipients.

Each player receives exactly two packages.

The distribution animation should feel like a mysterious delivery of sealed evidence.

Never reveal hidden identities during the animation.

## 4.5 Investigation

Players inspect their packages and submit guesses.

The four mysteries are described in Section 6.

Players can investigate at their own pace. Mandatory timers are not required.

## 4.6 Game End

The first player to solve all four mysteries wins.

If all players are eliminated before anyone solves all four mysteries, the game ends without a winner.

If two players become eligible to win simultaneously, the authoritative server's serialized event acceptance order determines the winner.

---

# 5. Package Assignment Algorithm

This is one of the most important technical parts of the game.

Do not hardcode package assignments for four players or rely on one fixed distribution pattern.

Implement a constrained randomized assignment algorithm.

## Required invariants

For every valid game:

1. Every player creates exactly one package.
2. Every package contains exactly three questions.
3. Every question has exactly one corresponding answer.
4. Every package has one author.
5. Every package has one answerer.
6. Author and answerer must be different.
7. Every package has exactly two recipients.
8. A package cannot be delivered to its author.
9. A package cannot be delivered to its answerer.
10. Every player receives exactly two packages.
11. The two packages received by a player must have distinct authors.
12. No player receives the same package twice.
13. Every player participates as an author and answerer.

Where feasible, ensure that a player does not receive two packages with the same answerer.

## Example: Four players

One possible author-answerer relationship:

| Package | Author | Answerer |
|---|---|---|
| P1 | A | B |
| P2 | B | C |
| P3 | C | D |
| P4 | D | A |

Possible distribution:

| Recipient | Package 1 | Package 2 |
|---|---|---|
| A | P2 | P3 |
| B | P3 | P4 |
| C | P4 | P1 |
| D | P1 | P2 |

This is only an example. The implementation must generate valid assignments dynamically.

## Algorithm requirements

- Generate candidate assignments.
- Validate all invariants.
- Use a proper randomized strategy.
- Reject invalid assignments.
- Retry with bounded attempts.
- If no valid assignment is found, fail safely without starting the investigation phase.
- Never distribute partially validated packages.

Use deterministic random seeds in tests to reproduce failures.

Run thousands of randomized simulations for player counts:

`4, 6, 8, 10, 12, 14, 16, 18, 20`

Do not assume feasibility for every constraint combination without testing it.

---

# 6. The Four Mystery Challenges

Each player must solve four challenges.

## Q1 — Who answered my questions?

The player must identify the person who answered their three original questions.

**Correct answer:** One player identity.

## Q2 — Whose questions did I answer?

The player must identify the author of the questions they answered.

**Correct answer:** One player identity.

## Q3 — Whose questions am I holding?

The player must identify the two distinct authors of their received packages.

**Correct answer:** A set of two player identities.

## Q4 — Whose answers am I holding?

The player must identify the two distinct answerers of their received packages.

**Correct answer:** A set of two player identities.

## Validation rules

For Q1 and Q2:
- Validate the selected identity against the correct identity.
- One incorrect submission consumes one mistake.

For Q3 and Q4:
- The player must submit two distinct identities.
- Validate the complete pair as an unordered set.
- The selection order does not matter.
- If either identity is incorrect, the entire submission is incorrect.
- Count it as one mistake.
- Never reveal partial correctness.

Example:

Correct set: `{Player A, Player B}`

Submitted set: `{Player A, Player C}`

Result: Incorrect. One mistake consumed.

Do not reveal that Player A was correct.

## Challenge progression

- Correctly solved challenges become permanently locked.
- Players cannot change a correct answer.
- Incorrect submissions can be retried until elimination.
- The interface must show challenge completion without exposing answers to other players.

---

# 7. Mistakes and Elimination

Every player starts with zero mistakes.

| Incorrect submissions | Result |
|---|---|
| 0 | Active |
| 1 | Active |
| 2 | Active |
| 3 | Eliminated |

When a player is eliminated:

- They cannot submit further guesses.
- Their previously solved challenges remain recorded.
- They can continue watching the game.
- They can use group chat.
- They can use private chat.
- They can see public game progress.
- They cannot receive information they were not previously authorized to access.

Elimination should feel dramatic but friendly.

Avoid humiliating or overly negative visual language.

---

# 8. Host Architecture and Authority

One Android device acts as the host and server.

The host is also a normal player.

The host must:
- Join the lobby.
- Select an avatar.
- Write questions.
- Answer questions.
- Receive two packages.
- Solve all four challenges.
- Compete for victory.

The host must not receive special gameplay advantages.

## Authoritative server

The host device owns:
- Game state.
- Player authentication.
- Assignment generation.
- Package distribution.
- Challenge validation.
- Mistake counters.
- Elimination.
- Winner determination.
- Chat routing.
- Session persistence.

Clients must never determine their own scores or challenge correctness.

The host serializes game-changing events to prevent conflicting submissions.

## Host trust model

The host's application UI must obey the same visibility rules as other clients.

However, because the host device owns the authoritative database and process, its owner may technically inspect local storage or runtime data.

Do not promise protection against a malicious or technically knowledgeable host.

---

# 9. Offline Multiplayer Networking

The game must function without internet access.

Supported environments:
- A normal Wi-Fi router without internet.
- An Android phone hotspot.
- Local networks where connected devices can communicate directly.

## Recommended networking stack

- Ktor Server.
- Ktor Client.
- WebSockets.
- Android NSD / mDNS service discovery.
- Manual IP connection as fallback.
- QR room joining.

## Joining methods

### Automatic discovery

Players open the app and discover available local rooms.

Use Android Network Service Discovery where supported.

### QR code

Host displays a QR code containing safe connection information.

It should not expose long-lived authentication secrets.

### Manual connection

Provide:
- Host IP address.
- Room PIN.

The joining player enters both.

## Networking reliability

Implement:
- Player authentication.
- Session IDs.
- Reconnection tokens.
- Duplicate message detection.
- Versioned protocol messages.
- Payload validation.
- Connection status.
- Heartbeats.
- Reconnection support.
- Authorized state restoration.

If a player disconnects temporarily, their game state must remain intact.

On reconnect, return only information that player is allowed to see.

## Android hotspot considerations

Test different manufacturers and Android versions.

Some hotspots may isolate clients or restrict discovery.

Manual IP connection must remain available.

Do not assume NSD works identically on every device.

## Host interruption

Persist critical state and create recovery snapshots.

The V1 application should support safe host process recovery where practical.

Do not promise seamless host migration between devices in V1.

Keep the host application active during gameplay and evaluate foreground-service requirements against the target Android SDK and current Android restrictions.

---

# 10. Chat System

Chat is a major part of the social experience.

Implement two separate chat systems.

## 10.1 Group Chat

Features:
- Shared room chat.
- Player nicknames.
- Avatars.
- Message timestamps.
- Online/offline indicators.
- Message history.
- Unread message count.
- Emoji support.
- Persian and English text.
- Correct RTL and LTR behavior.

## 10.2 Private Chat

Features:
- One-to-one messaging.
- Private message routing.
- Message history.
- Typing indicators.
- Unread count.
- Online/offline status.
- Optional read receipts.

Private messages must never be broadcast to other players.

The host routes private messages and may technically inspect locally stored messages. Do not advertise end-to-end encryption unless it is genuinely implemented.

## Chat restrictions

Chat must never reveal:
- Hidden package identities.
- Correct answers.
- Other players' guesses.
- Private challenge progress.
- Unauthorized game metadata.

Provide message length limits and basic rate limiting.

---

# 11. Visual Identity and Art Direction

Detective Genius should look like a polished commercial party game.

Avoid:
- Generic quiz-game aesthetics.
- Excessive horror imagery.
- Cheap cartoon templates.
- Inconsistent iconography.
- Overly complex backgrounds.
- Unnecessary gradients everywhere.

## Art direction

Suggested visual identity:

**Theme:** Playful mystery, clever investigation, friendly competition.

Suggested palette:

### Dark Mode

- Deep navy backgrounds.
- Rich violet accents.
- Cyan investigation highlights.
- Soft white typography.
- Subtle luminous details.

### Light Mode

- Warm off-white backgrounds.
- Clean white cards.
- Navy typography.
- Violet primary accents.
- Soft neutral separators.

Use a centralized semantic design-token system.

Never hardcode individual screen colors.

## Typography

Use a modern, highly readable typeface.

English and Persian typography must be designed and tested separately.

Persian must support:
- Proper shaping.
- Connected letters.
- RTL layout.
- Mixed English/Persian content.
- Correct punctuation.
- Number handling.
- Appropriate line heights.

---

# 12. Portrait-Only Interface Requirement

**The entire game must operate exclusively in portrait orientation.**

This is a strict product requirement.

Landscape mode is not supported.

Device rotation must not cause the game interface to switch into a landscape layout.

Apply this requirement to every screen:

- Onboarding.
- Home.
- Create Room.
- Join Room.
- Lobby.
- Question Creation.
- Answering.
- Distribution.
- Investigation Board.
- Package Viewer.
- Group Chat.
- Private Chat.
- Player Profile.
- Victory.
- Elimination.
- Settings.

## Implementation requirements

- Configure the Android activity for portrait orientation.
- Verify orientation behavior against the selected target SDK and supported Android versions.
- Do not create landscape-specific layouts.
- Do not create tablet horizontal gameplay layouts.
- Design primarily for 360–430 dp wide portrait viewports.
- Support smaller and larger Android phones.
- Handle display cutouts and status/navigation bars.
- Handle keyboard appearance without clipping critical controls.
- Use vertical scrolling when content exceeds available height.
- Maintain comfortable touch targets.
- Ensure all dialogs and bottom sheets remain usable in portrait.

## Portrait layout guidance

- Prefer vertically stacked content.
- Use compact two-column grids only when they remain readable.
- Keep mystery cards readable and touch-friendly.
- Chat should use a conventional vertical conversation layout.
- Use sticky bottom actions only when they do not obstruct content.
- Never assume a specific screen height.

Test multiple portrait resolutions and font scales.

---

# 13. Original Avatar System

Create 24 original, stylized 2D avatars.

Suggested characters:

1. Detective.
2. Magician.
3. Astronaut.
4. Fox.
5. Owl.
6. Cat.
7. Robot.
8. Ghost.
9. Artist.
10. Scientist.
11. Pirate.
12. Knight.
13. Alien.
14. Penguin.
15. Raven.
16. Explorer.
17. Jester.
18. Ninja.
19. Wizard.
20. Inventor.
21. Wolf.
22. Panda.
23. Time Traveler.
24. Phantom.

These must be original or properly licensed designs.

Do not use copyrighted characters.

## Avatar states

Every avatar should support:

- Idle.
- Selected.
- Thinking.
- Talking.
- Eliminated.
- Victory.

## Avatar implementation

Use reusable avatar components.

Avatars must remain visually consistent across:
- Lobby.
- Player list.
- Chat.
- Challenge cards.
- Victory screen.
- Elimination screen.

Provide accessible names and descriptions.

Do not create 24 unrelated art styles.

---

# 14. Animation System

Animations are a key part of the product identity.

They should make the game feel alive without making it slow or distracting.

Use Jetpack Compose Animation.

Lottie may be used for selected complex effects.

## Required animations

### App introduction
- Animated logo appearance.
- Subtle detective-themed particles or light effects.
- Smooth transition to home.

### Home screen
- Staggered card entrance.
- Soft scaling.
- Subtle hover-like interaction feedback where appropriate.

### Avatar selection
- Character entrance.
- Selected state animation.
- Idle breathing or movement.
- Selection confirmation.

### Lobby
- Players enter with subtle motion.
- Ready indicators animate.
- Host start action feels responsive.

### Question creation
- Progress indicators animate.
- Completed question cards transition smoothly.
- Submission confirmation.

### Distribution
- Packages appear sealed.
- Cards shuffle.
- Package delivery animation.
- No hidden information is revealed.

### Investigation
- Challenge cards reveal progressively.
- Correct feedback uses a satisfying success animation.
- Incorrect feedback is noticeable but not punishing.
- Locked challenges remain visually distinct.

### Elimination
- Avatar transitions into an eliminated visual state.
- Subtle cinematic transition.
- Preserve the friendly tone.

### Victory
- Celebration.
- Confetti or particles.
- Winning player spotlight.
- Final score or solved-mystery presentation.

## Performance requirements

- Avoid blocking the main thread.
- Avoid excessive recomposition.
- Use efficient image assets.
- Stop nonessential animations when screens are not visible.
- Respect Android reduced-motion preferences.
- Support lower-end Android phones.
- Target smooth 60 FPS where supported.

---

# 15. Sound and Haptics

Sound is optional but recommended.

Include bundled offline assets:
- Button interaction.
- Card selection.
- Package delivery.
- Correct answer.
- Incorrect answer.
- Elimination.
- Victory.

Optional background music.

Settings must independently control:
- Sound effects.
- Music.
- Haptics.

No internet audio streaming.

Avoid excessive or repetitive sound.

---

# 16. Screens and Navigation

Implement these screens:

| Screen | Purpose |
|---|---|
| Onboarding | Introduce the game |
| Home | Main navigation |
| Create Room | Host a new game |
| Join Room | Discover or connect |
| Lobby | Manage players and readiness |
| Question Creation | Write three questions |
| Answering | Answer three questions |
| Distribution | Receive packages |
| Mystery Board | Four challenges |
| Package Viewer | Inspect questions and answers |
| Group Chat | Room conversation |
| Private Chat | Direct messaging |
| Player Profile | Nickname and avatar |
| Victory | Winner presentation |
| Elimination | Elimination presentation |
| Settings | Language, theme, sound, accessibility |

## Navigation principles

- Never lose game state when navigating.
- Preserve chat history.
- Prevent accidental duplicate submissions.
- Confirm destructive actions.
- Handle back navigation carefully during gameplay.
- Use clear loading and connection states.
- Never show nonfunctional controls.

---

# 17. Localization: English and Persian

Both languages must be fully supported.

## Required languages

- English.
- Persian (Farsi).

Use Android string resources:

```text
res/
  values/
    strings.xml
  values-fa/
    strings.xml
```

All application UI strings must be externalized.

Do not hardcode UI text inside individual Compose screens.

User-generated questions, answers, and chat messages must never be automatically translated.

## Persian requirements

- Correct RTL layout.
- Correct Persian letter shaping.
- Mixed Persian/English content.
- Correct punctuation.
- Appropriate number formatting.
- Correct text alignment.
- Proper handling of URLs and IP addresses.
- Appropriate chat bubble direction.
- Correct keyboard behavior.

Do not blindly mirror every icon.

Physical direction and brand artwork may need to remain unchanged.

## Language switching

Allow users to change language in settings.

Changing language must not reset an active game.

---

# 18. Theme System

Support:

1. System.
2. Light.
3. Dark.

Dark mode should be the default visual direction, but light mode must be equally polished.

Use centralized semantic tokens for:

- Background.
- Surface.
- Elevated surface.
- Primary.
- Secondary.
- Accent.
- Text primary.
- Text secondary.
- Success.
- Warning.
- Error.
- Borders.
- Dividers.
- Disabled elements.

Components must use tokens instead of individual hardcoded colors.

Test every screen in all theme modes.

---

# 19. Settings

Provide:

- Language selection.
- Theme selection.
- Animation toggle.
- Reduced motion.
- Sound effects toggle.
- Background music toggle.
- Haptics toggle.
- Optional game timers.
- Tutorial replay.
- About screen.
- Version information.

Settings should persist locally.

---

# 20. Technical Architecture

## Recommended stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | Clean Architecture + MVVM |
| Async | Coroutines + StateFlow |
| Networking | Ktor |
| Local server | Ktor Server |
| Client networking | Ktor Client |
| Real-time | WebSockets |
| Discovery | Android NSD / mDNS |
| Database | Room / SQLite |
| Serialization | Kotlin Serialization |
| Dependency injection | Hilt or Koin |
| Animations | Compose Animation |
| Complex animations | Lottie |
| QR generation/scanning | Maintained offline library |
| Unit tests | JUnit |
| UI tests | Compose UI Testing |

Use maintained stable dependency versions compatible with the project's Android Gradle Plugin and Kotlin versions.

Do not introduce unnecessary dependencies.

## Architectural principle

The game engine must be independent of Android UI and networking.

It should be a pure Kotlin module that can be tested using simulated players without running an Android device.

---

# 21. Suggested Project Structure

```text
DetectiveGenius/
|
├── app/
|
├── core/
│   ├── common/
│   ├── model/
│   ├── serialization/
│   └── protocol/
|
├── design-system/
│   ├── colors/
│   ├── typography/
│   ├── components/
│   ├── animations/
│   └── avatars/
|
├── game-engine/
│   ├── model/
│   ├── state/
│   ├── rules/
│   ├── assignment/
│   ├── distribution/
│   ├── validation/
│   └── scoring/
|
├── networking/
│   ├── server/
│   ├── client/
│   ├── discovery/
│   ├── protocol/
│   ├── security/
│   └── recovery/
|
├── database/
│   ├── entities/
│   ├── dao/
│   ├── migrations/
│   └── repositories/
|
├── features/
│   ├── onboarding/
│   ├── home/
│   ├── lobby/
│   ├── question-creation/
│   ├── answering/
│   ├── distribution/
│   ├── mystery-board/
│   ├── package-viewer/
│   ├── group-chat/
│   ├── private-chat/
│   ├── victory/
│   ├── elimination/
│   └── settings/
|
└── tests/
```

The actual module structure may be adapted to the existing repository, but separation of responsibilities must be preserved.

---

# 22. Game Engine and Data Models

Use strongly typed models.

Suggested domain models:

```kotlin
data class Player(
    val id: String,
    val nickname: String,
    val avatarId: String,
    val isReady: Boolean,
    val isConnected: Boolean,
    val isEliminated: Boolean
)

data class Question(
    val id: String,
    val authorId: String,
    val text: String
)

data class Answer(
    val id: String,
    val questionId: String,
    val answererId: String,
    val text: String
)

data class QuestionPackage(
    val id: String,
    val authorId: String,
    val answererId: String,
    val questionIds: List<String>,
    val recipientIds: Set<String>
)

data class PlayerProgress(
    val playerId: String,
    val solvedChallenges: Set<ChallengeType>,
    val mistakes: Int,
    val isEliminated: Boolean
)
```

These are conceptual examples. The final domain model should include validation, immutability, and appropriate encapsulation.

## Important data separation

Never send internal package models directly to clients.

Create separate DTOs for:
- Public player data.
- Private player data.
- Visible package data.
- Internal package metadata.
- Challenge submissions.
- Public game events.

The client must never receive hidden author or answerer identities unless explicitly authorized.

---

# 23. Networking Protocol

Use typed, versioned messages.

Every message should include appropriate metadata such as:

- Protocol version.
- Message ID.
- Session ID.
- Sender identity.
- Message type.
- Payload.
- Timestamp where relevant.

Use server-side authorization for every message.

Suggested domain events:

```text
PlayerJoined
PlayerReady
GameStarted
QuestionsSubmitted
AnswersSubmitted
AssignmentsGenerated
PackageDelivered
ChallengeSolved
IncorrectGuess
PlayerEliminated
MessageSent
GameWon
GameFinished
```

The host must validate all incoming messages.

Implement:
- Duplicate message detection.
- Idempotent handling where appropriate.
- Payload length limits.
- Rate limiting.
- Session authorization.
- Reconnection support.
- Protocol compatibility checks.

Never trust a client's local game state.

---

# 24. Database and Persistence

Use Room with SQLite.

Suggested entities:

```text
game_sessions
players
questions
answers
question_packages
package_recipients
player_progress
guesses
chat_messages
game_events
session_snapshots
```

Requirements:
- Proper foreign keys.
- Appropriate indexes.
- Transactional critical operations.
- Database migrations.
- Consistent session recovery.
- Safe handling of deleted or expired sessions.

Store sensitive assignment metadata separately from client-visible DTOs.

Persist important game events and snapshots so the host can recover from unexpected process termination where possible.

Do not claim guaranteed recovery without testing it.

---

# 25. Security and Privacy

This is a local party game, not a banking application, but it still needs meaningful security.

Implement:

- Random session identifiers.
- Random player session tokens.
- Room PIN.
- Per-message authorization.
- Hidden metadata protection.
- Private message routing.
- Payload validation.
- Length limits.
- Basic rate limiting.
- Duplicate message protection.
- Safe reconnection.

Never expose:
- Package authors.
- Package answerers.
- Other players' guesses.
- Private chat messages.
- Internal assignment metadata.

The host trust limitation must be documented honestly.

---

# 26. User Experience and Accessibility

Requirements:

- Readable typography.
- Large touch targets.
- Clear progress indicators.
- Accessible contrast.
- Screen-reader descriptions.
- Reduced-motion support.
- Responsive portrait layouts.
- Clear connection feedback.
- Helpful error messages.
- Loading states.
- Empty states.
- Confirmation for destructive actions.

Avoid unnecessary modal dialogs.

Use clear, friendly language.

Never punish users with unclear or unexplained errors.

---

# 27. Testing Strategy

Testing must be part of development, not postponed until the end.

## 27.1 Game Engine Tests

Test:
- Four players.
- Six players.
- Eight players.
- Ten players.
- Twenty players.
- Invalid player counts.
- Question validation.
- Answer validation.
- Assignment invariants.
- Package delivery.
- Correct guesses.
- Incorrect guesses.
- Set-based challenge validation.
- Elimination.
- Winner determination.
- Simultaneous guesses.
- Repeated messages.

Run thousands of randomized assignment simulations using reproducible seeds.

## 27.2 Network Tests

Test:
- Host creation.
- Room discovery.
- QR joining.
- Manual IP joining.
- PIN validation.
- Four devices.
- Eight devices.
- Hotspot connectivity.
- Router connectivity without internet.
- Reconnection.
- Duplicate messages.
- Simultaneous guesses.
- Private chat isolation.
- Host interruption.

## 27.3 UI Tests

Test:
- Dark mode.
- Light mode.
- System theme.
- English.
- Persian.
- RTL.
- LTR.
- Small phones.
- Large phones.
- Keyboard appearance.
- Portrait-only behavior.
- Avatar selection.
- Animations.
- Accessibility.

## 27.4 Privacy Tests

Verify:
- Players cannot inspect unauthorized packages.
- No hidden metadata is leaked.
- Private messages reach only their intended recipient.
- Incorrect set submissions reveal no partial correctness.
- Eliminated players cannot submit guesses.
- Clients cannot manipulate their scores.

---

# 28. Performance Requirements

Target:
- Smooth 60 FPS where supported.
- No blocking network operations on the main thread.
- Efficient database queries.
- Efficient image loading.
- Controlled recomposition.
- Low unnecessary battery consumption.
- No memory leaks.
- No unnecessary background work.

The game must remain usable on ordinary Android phones, not only flagship devices.

---

# 29. Development Roadmap

Build in controlled milestones.

## Milestone 1 — Project Foundation

- Inspect existing project.
- Configure Android project.
- Configure Kotlin and Compose.
- Establish navigation.
- Build design system.
- Implement themes.
- Implement English and Persian.
- Enforce portrait-only orientation.
- Add foundational tests.

**Deliverable:** A polished navigable application foundation.

## Milestone 2 — Game Engine

- Implement domain models.
- Implement state machine.
- Implement question and answer validation.
- Implement constrained random assignment.
- Implement package generation.
- Implement challenge validation.
- Implement mistakes and elimination.
- Implement winner determination.

**Deliverable:** A fully testable pure Kotlin game engine.

## Milestone 3 — Local Multiplayer

- Implement local Ktor server.
- Implement client connection.
- Implement WebSockets.
- Implement room discovery.
- Implement QR joining.
- Implement manual IP joining.
- Implement room PIN.
- Implement authentication.
- Implement state synchronization.

**Deliverable:** Multiple physical Android devices can join and synchronize a room offline.

## Milestone 4 — Full Gameplay

- Lobby.
- Question creation.
- Answering.
- Distribution.
- Investigation board.
- Package viewer.
- Challenge submissions.
- Victory.
- Elimination.

**Deliverable:** A complete playable game.

## Milestone 5 — Chat

- Group chat.
- Private chat.
- Message history.
- Typing indicators.
- Unread messages.
- Online/offline status.

**Deliverable:** Complete local social communication.

## Milestone 6 — Visual Polish

- Original avatars.
- Animation system.
- Sound effects.
- Music.
- Haptics.
- Screen transitions.
- Feedback effects.

**Deliverable:** Premium visual and interaction experience.

## Milestone 7 — Reliability and Security

- Reconnection.
- Session snapshots.
- Host recovery.
- Authorization.
- Privacy validation.
- Performance testing.
- Device compatibility.

**Deliverable:** Stable offline multiplayer experience.

## Milestone 8 — Release

- Build signed APK.
- Verify installation.
- Verify updates.
- Test on multiple physical devices.
- Create screenshots.
- Prepare documentation.
- Produce release notes.

**Deliverable:** A distributable Android application.

---

# 30. Agent Development Instructions

You are the senior Android engineering agent responsible for delivering Detective Genius.

Treat this document as the product source of truth.

## Before making changes

1. Inspect the existing repository.
2. Identify the current project structure.
3. Check build configuration.
4. Identify existing working code.
5. Identify missing features.
6. Create an implementation plan.
7. Preserve useful existing work.

Do not blindly overwrite the repository.

## Development rules

- Implement working features, not empty screens.
- Avoid placeholder buttons.
- Avoid fake networking.
- Avoid simulated multiplayer presented as real multiplayer.
- Do not claim a feature works without testing it.
- Keep business logic outside Compose screens.
- Keep the game engine independently testable.
- Use transactions for critical state changes.
- Handle errors explicitly.
- Avoid unnecessary abstractions.
- Prefer maintainable code over shortcuts.
- Build and test after each meaningful milestone.
- Do not move forward while foundational tests are failing.

## Documentation to maintain

Keep these files updated:

```text
GAME_DESIGN.md
TECHNICAL_ARCHITECTURE.md
DEVELOPMENT_ROADMAP.md
IMPLEMENTATION_STATUS.md
TESTING.md
CHANGELOG.md
```

This document should be retained as `GAME_DESIGN.md`.

## Progress reporting

After each milestone, report:

1. What was implemented.
2. Files created.
3. Files modified.
4. Tests executed.
5. Test results.
6. Known limitations.
7. Remaining work.
8. Next milestone.

Clearly distinguish between:
- Implemented.
- Partially implemented.
- Planned.
- Tested successfully.
- Not yet tested.

Never mark a feature complete just because its UI exists.

---

# 31. Future Features — Not V1

Keep the architecture extensible, but do not delay the first release for these features.

Potential future modes:

- Classic.
- Chaos.
- Speed.
- Team.
- Expert.
- Family.
- Custom.

Other future ideas:

- Player statistics.
- Game history.
- Custom question packs.
- Avatar cosmetics.
- Additional languages.
- Optional AI question suggestions.
- Host migration.
- Cross-platform clients.
- Online multiplayer.
- End-to-end encrypted messaging.

These are future possibilities, not V1 commitments.

---

# 32. Definition of Done

Detective Genius V1 is complete only when:

- [ ] The Android application installs and launches.
- [ ] All screens work in portrait orientation only.
- [ ] Dark, light, and system themes work.
- [ ] English and Persian interfaces work.
- [ ] RTL and LTR layouts are correct.
- [ ] At least four players can play.
- [ ] All supported even player counts are validated.
- [ ] Host participates as a normal player.
- [ ] Players can connect without internet.
- [ ] Room discovery works where supported.
- [ ] QR and manual connection work.
- [ ] Questions and answers are submitted correctly.
- [ ] Assignment invariants are verified.
- [ ] Every player receives exactly two packages.
- [ ] All four mystery challenges work.
- [ ] Incorrect guesses are counted correctly.
- [ ] Elimination works.
- [ ] Victory is determined correctly.
- [ ] Group chat works.
- [ ] Private chat works.
- [ ] Reconnection works.
- [ ] Unauthorized information is not sent to clients.
- [ ] Avatars and animations are integrated.
- [ ] Sound and haptics settings work.
- [ ] Tests pass.
- [ ] The application has been tested on multiple physical Android devices.
- [ ] A signed release APK is produced.

---

# Final Product Direction

Build Detective Genius as a complete, polished, family-friendly mystery party game.

Prioritize the integrity of the game engine, the fairness of package distribution, reliable offline networking, and a delightful player experience.

The interface should make players curious. The animation should make every interaction satisfying. The avatars should give the game personality. The mystery system should make every round unpredictable.

Most importantly:

**Build the actual game, not just something that looks like a game.**
