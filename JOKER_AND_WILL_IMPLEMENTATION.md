# Joker and Will — Coding Agent Implementation Brief

**Repository:** `Saeidoscar/Detective-Genius`  
**Task:** Implement the Joker role and eliminated-player Will feature.  
**Status:** New feature specification  
**Priority:** Small, testable additions to the existing game

## 1. Instructions for the coding agent

- Read `GAME_DESIGN.md`, `TECHNICAL_ARCHITECTURE.md`, `IMPLEMENTATION_STATUS.md`, `TESTING.md`, and `DEVELOPMENT_ROADMAP.md` before editing code.
- Preserve the existing question creation, answering, package assignment, mystery solving, three-mistake elimination, chat, local networking, snapshot, and Android UI behavior.
- Implement this feature inside the existing module boundaries:
  - pure game rules in `game-engine`;
  - shared serialized data and protocol contracts in `core`;
  - authoritative networking/state delivery and sanitization in `networking`;
  - Compose UI and resources in `app`.
- Do not redesign the application or add online multiplayer, extra roles, extra guesses, Joker actions, generated clues, reputation tracking, or spectator powers.
- Keep the implementation offline/local multiplayer compatible.
- All rules, elimination accounting, role state, and victory decisions must remain authoritative on the host.
- Do not expose hidden Joker state or private player data in public DTOs, logs, chat, or ordinary client state.
- Run and extend existing tests. Do not mark the work complete until relevant engine, networking, persistence, and UI tests pass.

## 2. Product summary

Add two features:

1. **The Joker** — exactly one secret player receives an asymmetric objective: eligible eliminations must reach a fixed target before a regular player wins.
2. **The Will** — a player eliminated for reaching three wrong guesses receives one final public, constrained action before becoming a spectator.

The Joker does not receive special evidence, special elimination powers, or a normal detective victory condition. Information sharing remains voluntary for every player.

## 3. Existing rules that must not change

- Supported player counts remain even: 4, 6, 8, 10, 12, 14, 16, 18, or 20.
- Question and answer creation remains unchanged.
- Package assignment and evidence distribution remain unchanged.
- The four mystery challenges remain unchanged.
- A player is eliminated after three incorrect submissions.
- Solving all four mysteries remains the regular player's existing victory condition.
- A correct challenge permanently solves it.
- A Will must not alter evidence, guesses, mistakes, elimination, or victory rules.

## 4. Joker rules

### 4.1 Assignment

At game setup:

- Randomly select exactly one Joker from the original players.
- Store the Joker identity only in authoritative state.
- Calculate the target once using:

  ```text
  jokerTarget = ceil(2 * originalPlayerCount / 3)
  ```

- Never recalculate the target during the match.
- The selected player receives normal evidence only. The Joker must not receive extra or modified evidence.

Target examples:

| Original players | Target |
|---:|---:|
| 4 | 3 |
| 6 | 4 |
| 8 | 6 |
| 10 | 7 |
| 12 | 8 |
| 14 | 10 |
| 16 | 11 |
| 18 | 12 |
| 20 | 14 |

### 4.2 Eligible eliminations

Increment `eligibleEliminationCount` only when:

- a player reaches the existing three-mistake elimination rule;
- the eliminated player is still a regular player, not the Joker;
- that player is counted for the first time.

Do not count:

- Joker elimination;
- disconnection or inactivity;
- spectating;
- duplicate elimination events;
- eliminations added by anything other than the existing mistake rule.

Keep the original player count fixed for the target even if players disconnect or leave.

### 4.3 Victory conditions

- The Joker wins when eligible eliminations reach or exceed `jokerTarget`.
- The Joker loses immediately if the Joker is eliminated before reaching the target.
- A regular winner wins according to the existing four-mystery rule.
- If a regular winner is announced first, the Joker loses.
- If the Joker reaches the target first, the Joker wins.
- If the engine processes both events in a single state update, use the host's existing serialized event order. If no order can be established, check the regular winner before the Joker threshold, consistent with the existing first-winner convention.
- Document and test the chosen same-update rule.

### 4.4 Joker privacy

Only the Joker client may receive:

- the private role screen;
- the Joker objective;
- the fixed target;
- the Joker's private current eligible-elimination count, if the UI displays it.

Ordinary public state must not contain:

- `jokerPlayerId`;
- public Joker progress;
- Joker UI/role badges;
- role identity in public chat or logs;
- any extra Joker-specific network message visible to non-Jokers.

The host stores authoritative Joker state. A client must never report or modify it.

### 4.5 At game end

Reveal only:

- who the Joker was;
- whether the Joker reached the target;
- target and actual eligible-elimination count;
- whether a regular player or Joker won;
- the existing winner/result reason.

Do not reveal private evidence or private chat as part of the reveal.

## 5. Joker UI

Add a private role screen/prompt available only to the Joker:

```text
YOU ARE THE JOKER

Your only objective is to eliminate at least X of the original N players
before a regular winner is announced.

You do not need to solve your mysteries.
Information sharing remains voluntary.
Nobody knows your identity.
```

Also add/update:

- private role reveal content;
- private Joker result content;
- final-results Joker identity reveal;
- winner reason and elimination counts.

Do not add public Joker badges, public progress, public role labels, or Joker-only lobby controls.

## 6. Will rules

After the existing three-mistake elimination event:

1. Mark the player eliminated immediately.
2. Increment the eligible-elimination count if the player is regular.
3. Check the Joker threshold immediately.
4. If the game has not ended, offer one Will opportunity.
5. The eliminated player chooses exactly one action and submits it.
6. Publish the submitted Will publicly according to its rules.
7. Transition the player to spectator status.

A Joker who is eliminated:

- receives the normal Will opportunity;
- loses the special Joker objective;
- does not add to the Joker target;
- must not be identified as the Joker before the end-of-game reveal.

### 6.1 Will status

The authoritative state should support:

- `unavailable`
- `pending`
- `submitted`
- `skipped`
- `expired`

A player may submit once only. Submitted Wills are immutable.

Only an eliminated player may submit. A Will cannot:

- add a guess;
- add a mistake;
- eliminate another player;
- change evidence;
- change mystery progress;
- grant a life;
- trigger a victory;
- keep the eliminated player active.

Use the same spectator permissions as existing elimination behavior.

### 6.2 Timeout

- Add a configurable submission window, initially 30–45 seconds.
- If the deadline expires or the player disconnects, mark it `expired` or `skipped` and continue.
- The timeout must never delay or block Joker/regular victory resolution.
- The remaining game must continue while the Will is pending; it must not pause the whole match.

### 6.3 Option A — Final Accusation

Allow the eliminated player to select one active player and provide a short suspicion text.

Rules:

- Public to all active players.
- Recorded in the final results.
- Not proof and not an answer or guess.
- No wrong guess is added to the accused player.
- The accused player is not forced to respond.
- The target must be an active player at submission time.

### 6.4 Option B — Final Revelation

Allow the eliminated player to reveal exactly one selected piece of information they legitimately possess or learned.

Rules:

- Public to all active players.
- The exact text is selected by the eliminated player.
- The player may not reveal another player's private evidence without authorization.
- Inferences/suspicions must be clearly labeled as such.
- Do not offer “reveal entire evidence package” in the first implementation.
- Do not automatically confirm an inference.

### 6.5 Option C — One Last Question

Allow the eliminated player to ask one active player a constrained, game-related question.

For the first implementation:

- Use predefined question templates or host-reviewed free text, not unrestricted natural-language validation.
- The question is sent publicly to all active players.
- The target must be active at submission time.
- The target answers truthfully to the best of their knowledge.
- The target may say they do not know.
- The target must not be forced to disclose private information they do not possess or have not chosen to share.
- Do not permit asking for another player's private evidence.
- Publish the question and answer publicly.

The UI and engine must validate that the target is an active player and that the payload is within supported limits. Do not attempt to machine-verify truthfulness.

## 7. Will UI

Display after elimination:

```text
YOU HAVE BEEN ELIMINATED

Your investigation ends here, but you have one final opportunity
to influence the game.

Choose exactly one Will:

1. Final Accusation
2. Final Revelation
3. One Last Question

After submitting, you become a spectator.
```

Requirements:

- Clearly state that the choice is final and cannot be edited.
- Show an enabled Skip/Leave action if product UX requires one.
- Preserve the existing elimination and spectator animation/screen.
- Add public Will history/notice UI for active players.
- Externalize English and Persian strings.
- Support portrait layout, RTL/LTR, keyboard-safe dialogs, and existing theme tokens.

## 8. State/model requirements

Adapt names to existing conventions instead of duplicating systems.

Authoritative state should contain fields equivalent to:

```text
jokerPlayerId
jokerTarget
eligibleEliminationCount
jokerStatus              // active, won, lost
gameWinnerType           // regular, joker, none
gameEndReason            // regular victory or Joker threshold
```

Will state should contain fields equivalent to:

```text
willStatus
willType                 // accusation, revelation, or question
willPayload
willSubmittedAt
willTargetPlayerId
```

The Will payload must be a structured, typed value appropriate to the selected option. It must not be a free-form unvalidated object.

Persistence should include:

- selected Joker and Joker status;
- original player count and fixed target;
- eligible-elimination count;
- winner type and end reason;
- every eliminated player's Will status;
- submitted, immutable Will content and target.

On recovery:

- Do not select a new Joker.
- Do not recalculate the target.
- Do not count an existing elimination twice.
- Do not offer a second Will.
- Do not allow editing a submitted Will.
- Restore public and player-specific sanitized views.

## 9. Networking requirements

The host must validate:

- Joker assignment and target;
- elimination eligibility and duplicate events;
- whether a regular winner exists;
- whether the Joker threshold is reached;
- whether a player is eliminated;
- whether a Will is available;
- whether a Will was already submitted or expired;
- whether the selected Will action and target are valid;
- whether the game has ended.

Clients must not determine any of these values.

Private Joker role delivery must use an authorized player-specific packet/state view. Public broadcasts must be sanitized and must not accidentally serialize private role fields.

Re-use existing session authentication, payload validation, duplicate packet filtering, reconnection, snapshot, and DTO conventions.

## 10. Implementation order

Use small commits/steps:

1. Add Joker state and fixed target calculation to the pure Kotlin engine.
2. Add eligibility tracking and idempotent elimination accounting.
3. Integrate Joker and regular winner resolution with existing event sequencing.
4. Add snapshot/persistence fields and restoration tests.
5. Add private Joker role DTO/state delivery.
6. Add Will domain state and submission validation.
7. Add public Will submission/result protocol messages.
8. Add the private eliminated-player Will UI.
9. Add public accusation/revelation/question UI and question answer handling.
10. Add timeout, skip, spectator transition, and recovery behavior.
11. Add end-of-game reveal and Joker count/target presentation.
12. Run full regression, networking, persistence, Compose/UI, and play-test verification.

## 11. Required tests

### Joker engine tests

- Exactly one Joker is selected for supported player counts.
- Joker selection is random but can be seeded/reproduced in tests.
- Target matches `ceil(2 * N / 3)` for 4–20 players.
- Target remains fixed after eliminations and reconnects.
- Regular eliminations count once only.
- Duplicate elimination events do not increment the count.
- Joker elimination does not increase eligible elimination count.
- Joker elimination sets the Joker's special objective to lost.
- Regular victory wins first and makes Joker lose.
- Joker threshold ends the match and makes Joker win.
- Defined same-update tie behavior passes tests.
- Joker evidence distribution matches normal player evidence distribution.

### Will engine tests

- Every regular-player elimination receives pending Will state when the match continues.
- Joker elimination also receives pending Will state.
- Only eliminated players may submit.
- Only active targets are accepted for accusation/question.
- A submitted Will cannot be edited or submitted twice.
- Timeout/skip produces no active-player effect.
- Accusation produces no guess/mistake/elimination.
- Revelation remains a public but selected/authorized payload.
- Question/answer payload validates and cannot target an inactive player.
- A Will cannot trigger victory or add evidence.
- Snapshot recovery does not duplicate or recreate Joker/Will state.

### Privacy/networking tests

- Non-Joker client DTOs contain no Joker identity or target before game end.
- Joker private state is received only by the Joker client.
- Public state does not leak Joker progress before reveal.
- Public Will broadcast contains no private evidence beyond the chosen payload.
- Private chat remains isolated.
- Reconnected clients receive only authorized state.

### Regression tests

- Existing assignment simulation still passes for all even counts.
- Existing Q1–Q4 behavior remains unchanged.
- Existing 3-mistake elimination remains unchanged.
- Existing regular winner/tie behavior remains unchanged.
- Existing host recovery and duplicate packet handling remain functional.
- Restarting the game clears stale Joker and Will state.

## 12. Acceptance checklist

### Joker

- [ ] Exactly one Joker is randomly selected.
- [ ] Only the Joker receives the private role/objective.
- [ ] Joker receives normal evidence distribution.
- [ ] Joker has no normal detective victory condition.
- [ ] Default target is `ceil(2 * originalPlayerCount / 3)`.
- [ ] Target never changes.
- [ ] Only eliminated regular players count.
- [ ] Joker own elimination never counts.
- [ ] Duplicate eliminations cannot count twice.
- [ ] Regular victory before threshold makes Joker lose.
- [ ] Joker threshold makes Joker win and ends game immediately.
- [ ] Joker identity is revealed in final results only.
- [ ] Public/network state does not leak role or progress.

### Will

- [ ] Every eliminated player is offered one Will if match continues.
- [ ] Exactly one action can be submitted.
- [ ] Final Accusation is public and is not a guess.
- [ ] Final Revelation contains only selected authorized information.
- [ ] One Last Question targets one active player.
- [ ] Question answers are public and not forced beyond player knowledge/authority.
- [ ] Timeout/disconnect cannot stall the match.
- [ ] Submitted Will cannot be edited or repeated.
- [ ] Player becomes spectator afterward.
- [ ] Evidence is not deleted or automatically revealed.

### Regression protection

- [ ] Existing gameplay remains unchanged.
- [ ] Existing offline/local multiplayer works.
- [ ] English and Persian UI text is externalized.
- [ ] Portrait-only behavior remains unchanged.
- [ ] Snapshot recovery is stable.
- [ ] Restart removes all stale role and Will state.

## 13. Definition of done

The feature is complete only when:

- the pure engine and networking tests pass;
- public DTO/privacy tests pass;
- persistence/recovery tests pass;
- Compose UI and localized resource checks pass;
- existing regression tests pass;
- all acceptance checklist items are implemented or explicitly marked with a reason;
- the feature can be tested with real groups using the existing four-to-twenty-player setup.

Do not make any balance adjustment after a single test game. Record Joker wins, regular wins, eliminations, Will choices, game duration, and player feedback across multiple matches.
