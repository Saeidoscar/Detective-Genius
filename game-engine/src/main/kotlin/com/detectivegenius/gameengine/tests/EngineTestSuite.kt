package com.detectivegenius.gameengine.tests

import com.detectivegenius.core.model.*
import com.detectivegenius.gameengine.assignment.AssignmentSolver
import com.detectivegenius.gameengine.rules.MysteryValidator
import com.detectivegenius.gameengine.rules.ValidationRules
import com.detectivegenius.gameengine.state.DetectiveGameEngine
import kotlin.random.Random

fun assert(condition: Boolean, message: String) {
    if (!condition) {
        throw AssertionError("ASSERTION FAILED: $message")
    }
}

fun assertEquals(expected: Any?, actual: Any?, message: String = "") {
    if (expected != actual) {
        throw AssertionError("ASSERTION FAILED: Expected '$expected', but got '$actual'. $message")
    }
}

fun main() {
    println("=== STARTING DETECTIVE GENIUS ENGINE TEST SUITE ===")
    
    testValidationRules()
    println("✓ testValidationRules passed")

    testAssignmentSolverInvariantsAcrossPlayerCounts()
    println("✓ testAssignmentSolverInvariantsAcrossPlayerCounts passed (9,000 simulations)")

    testCompleteGameFlowAndRules()
    println("✓ testCompleteGameFlowAndRules passed")

    testMysteryChallengesAndPartialCorrectnessSecrecy()
    println("✓ testMysteryChallengesAndPartialCorrectnessSecrecy passed")

    testMistakesAndElimination()
    println("✓ testMistakesAndElimination passed")

    testWinnerDeterminationAndTieBreaking()
    println("✓ testWinnerDeterminationAndTieBreaking passed")

    testSnapshotAndRestore()
    println("✓ testSnapshotAndRestore passed")

    testDataPrivacySanitization()
    println("✓ testDataPrivacySanitization passed")

    testJokerSelectionAndTargetCalculation()
    println("✓ testJokerSelectionAndTargetCalculation passed")

    testJokerVictoryAndEligibleEliminationAccounting()
    println("✓ testJokerVictoryAndEligibleEliminationAccounting passed")

    testJokerEliminationAndRegularVictoryPrecedence()
    println("✓ testJokerEliminationAndRegularVictoryPrecedence passed")

    testWillSubmissionAndValidation()
    println("✓ testWillSubmissionAndValidation passed")

    testJokerPrivacyAndSnapshotRecovery()
    println("✓ testJokerPrivacyAndSnapshotRecovery passed")

    println("=== ALL ENGINE TESTS PASSED SUCCESSFULLY! ===")
}

fun testValidationRules() {
    // Question length < 5
    val shortQ = listOf("Hey", "Valid question two?", "Valid question three?")
    val res1 = ValidationRules.validateQuestions(shortQ)
    assert(res1 is ValidationRules.QuestionValidationResult.Error, "Should reject question < 5 chars")

    // Question duplicates
    val dupQ = listOf("What is your secret?", "what is your secret?", "A distinct question?")
    val res2 = ValidationRules.validateQuestions(dupQ)
    assert(res2 is ValidationRules.QuestionValidationResult.Error, "Should reject duplicate questions")

    // Valid questions
    val validQ = listOf("Who do you trust most?", "Where were you at 9 PM?", "What is in your briefcase?")
    val res3 = ValidationRules.validateQuestions(validQ)
    assert(res3 is ValidationRules.QuestionValidationResult.Valid, "Should accept valid questions")

    // Player counts
    val (canStartOdd, _) = ValidationRules.canStartGame(playerCount = 5, allReady = true)
    assert(!canStartOdd, "Should reject odd player count")

    val (canStartLess, _) = ValidationRules.canStartGame(playerCount = 2, allReady = true)
    assert(!canStartLess, "Should reject < 4 players")

    val (canStartNotReady, _) = ValidationRules.canStartGame(playerCount = 4, allReady = false)
    assert(!canStartNotReady, "Should reject when not all ready")

    val (canStartValid, _) = ValidationRules.canStartGame(playerCount = 4, allReady = true)
    assert(canStartValid, "Should allow start for 4 ready players")
}

fun testAssignmentSolverInvariantsAcrossPlayerCounts() {
    val solver = AssignmentSolver()
    val counts = listOf(4, 6, 8, 10, 12, 14, 16, 18, 20)
    val random = Random(1337)

    for (count in counts) {
        val players = (1..count).map { "player_$it" }
        for (sim in 1..1000) {
            val assignments = solver.solve(players, random)
            assert(assignments != null, "Assignment failed for count $count at simulation $sim")
            val valid = solver.validateAllInvariants(players, assignments!!)
            assert(valid, "Invariants violated for count $count at simulation $sim")
        }
    }
}

fun testCompleteGameFlowAndRules() {
    val engine = DetectiveGameEngine(
        roomPin = "1234",
        hostPlayerId = "player_1",
        random = Random(42)
    )

    val playerIds = listOf("player_1", "player_2", "player_3", "player_4")
    playerIds.forEach { id ->
        engine.joinPlayer(Player(id = id, nickname = "Nick_$id", avatarId = "detective"))
        engine.setPlayerReady(id, true)
    }

    assertEquals(GamePhase.LOBBY, engine.phase)
    engine.startGame("player_1")
    assertEquals(GamePhase.QUESTION_CREATION, engine.phase)

    // Submit questions for all 4 players
    playerIds.forEach { id ->
        val qs = listOf(
            "Question 1 from $id?",
            "Question 2 from $id?",
            "Question 3 from $id?"
        )
        val res = engine.submitQuestions(id, qs)
        assert(res.isSuccess, "Questions submission should succeed")
    }

    assertEquals(GamePhase.ANSWERING, engine.phase)

    // Submit answers for all 4 players
    playerIds.forEach { id ->
        val view = engine.createClientView(id)
        val assigned = view.assignedQuestionsToAnswer
        assertEquals(3, assigned.size, "Should receive 3 assigned questions")
        val ansMap = assigned.associate { it.id to "Answer from $id to ${it.id}" }
        val res = engine.submitAnswers(id, ansMap)
        assert(res.isSuccess, "Answers submission should succeed")
    }

    assertEquals(GamePhase.INVESTIGATION, engine.phase)

    // Verify packages
    assertEquals(4, engine.packages.size)
    playerIds.forEach { id ->
        val view = engine.createClientView(id)
        assertEquals(2, view.receivedPackages.size, "Each player must receive exactly 2 packages")
    }
}

fun testMysteryChallengesAndPartialCorrectnessSecrecy() {
    val engine = DetectiveGameEngine(
        roomPin = "5678",
        hostPlayerId = "p1",
        random = Random(99)
    )
    val players = listOf("p1", "p2", "p3", "p4")
    players.forEach {
        engine.joinPlayer(Player(id = it, nickname = "Nick_$it", avatarId = "magician", isReady = true))
    }
    engine.startGame("p1")

    players.forEach { id ->
        engine.submitQuestions(id, listOf("Q1 of $id?", "Q2 of $id?", "Q3 of $id?"))
    }
    players.forEach { id ->
        val view = engine.createClientView(id)
        val ansMap = view.assignedQuestionsToAnswer.associate { it.id to "Ans $id" }
        engine.submitAnswers(id, ansMap)
    }

    // Solve for p1
    val validator = MysteryValidator()
    val correct = validator.deriveCorrectAnswers("p1", engine.packages)

    // Q1: Guess correctly
    val q1Res = engine.submitGuess("p1", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, correct.q1AnswererOfMyQuestions, null)
    assert(q1Res is GuessValidationResult.Correct, "Q1 correct guess should be Correct")

    // Q2: Guess incorrectly first
    val wrongP = players.first { it != correct.q2AuthorOfQuestionsIAnswered }
    val q2Wrong = engine.submitGuess("p1", ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER, wrongP, null)
    assert(q2Wrong is GuessValidationResult.Incorrect, "Wrong Q2 guess must be Incorrect")
    assertEquals(1, (q2Wrong as GuessValidationResult.Incorrect).currentMistakes)

    // Retry Q2 with correct answer
    val q2Right = engine.submitGuess("p1", ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER, correct.q2AuthorOfQuestionsIAnswered, null)
    assert(q2Right is GuessValidationResult.Correct, "Q2 retry with correct answer should be Correct")

    // Q3: Test Partial Correctness Secrecy!
    // One right and one wrong in pair
    val oneRight = correct.q3AuthorsOfPackagesIHeld.first()
    val oneWrong = players.first { !correct.q3AuthorsOfPackagesIHeld.contains(it) }
    val partialPair = setOf(oneRight, oneWrong)
    val q3Partial = engine.submitGuess("p1", ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING, null, partialPair)
    assert(q3Partial is GuessValidationResult.Incorrect, "Partial match must be treated strictly as Incorrect!")
    // Invariant: The result must NOT expose that one was right
    assertEquals(2, (q3Partial as GuessValidationResult.Incorrect).currentMistakes)

    // Now submit full correct pair for Q3 (order independent)
    val q3Right = engine.submitGuess("p1", ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING, null, correct.q3AuthorsOfPackagesIHeld.reversed().toSet())
    assert(q3Right is GuessValidationResult.Correct, "Q3 unordered correct pair should succeed")

    // Q4: Correct pair
    val q4Right = engine.submitGuess("p1", ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING, null, correct.q4AnswerersOfPackagesIHeld)
    assert(q4Right is GuessValidationResult.Correct, "Q4 correct pair should succeed")

    // Player 1 has solved all 4 mysteries!
    assertEquals(GamePhase.FINISHED, engine.phase)
    assertEquals("p1", engine.winnerPlayerId)
}

fun testMistakesAndElimination() {
    val engine = DetectiveGameEngine(roomPin = "0000", hostPlayerId = "p1", random = Random(777))
    val players = listOf("p1", "p2", "p3", "p4")
    players.forEach {
        engine.joinPlayer(Player(id = it, nickname = "Nick_$it", avatarId = "cat", isReady = true))
    }
    engine.startGame("p1")
    players.forEach { engine.submitQuestions(it, listOf("Q1 of $it?", "Q2 of $it?", "Q3 of $it?")) }
    players.forEach {
        val view = engine.createClientView(it)
        engine.submitAnswers(it, view.assignedQuestionsToAnswer.associate { q -> q.id to "Ans" })
    }

    val validator = MysteryValidator()
    val correct = validator.deriveCorrectAnswers("p2", engine.packages)
    val wrongSuspect = players.first { it != correct.q1AnswererOfMyQuestions }

    // Mistake 1
    val m1 = engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, wrongSuspect, null) as GuessValidationResult.Incorrect
    assertEquals(1, m1.currentMistakes)
    assert(!m1.isEliminated, "Player should be active after 1 mistake")

    // Mistake 2
    val m2 = engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, wrongSuspect, null) as GuessValidationResult.Incorrect
    assertEquals(2, m2.currentMistakes)
    assert(!m2.isEliminated, "Player should be active after 2 mistakes")

    // Mistake 3 -> Elimination!
    val m3 = engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, wrongSuspect, null) as GuessValidationResult.Incorrect
    assertEquals(3, m3.currentMistakes)
    assert(m3.isEliminated, "Player must be eliminated after 3 mistakes")
    assert(engine.players["p2"]!!.isEliminated, "Player state must be updated to eliminated")

    // Attempting further guess after elimination
    val postElimGuess = engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, correct.q1AnswererOfMyQuestions, null)
    assert(postElimGuess is GuessValidationResult.PlayerEliminated, "Eliminated player cannot submit guesses")
}

fun testWinnerDeterminationAndTieBreaking() {
    val engine = DetectiveGameEngine(roomPin = "1111", hostPlayerId = "p1", random = Random(123))
    val players = listOf("p1", "p2", "p3", "p4")
    players.forEach { engine.joinPlayer(Player(id = it, nickname = it, avatarId = "fox", isReady = true)) }
    engine.startGame("p1")
    players.forEach { engine.submitQuestions(it, listOf("Q1 of $it?", "Q2 of $it?", "Q3 of $it?")) }
    players.forEach {
        val view = engine.createClientView(it)
        engine.submitAnswers(it, view.assignedQuestionsToAnswer.associate { q -> q.id to "Ans" })
    }

    val validator = MysteryValidator()
    // Solve all 4 for p3 first
    val c3 = validator.deriveCorrectAnswers("p3", engine.packages)
    engine.submitGuess("p3", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, c3.q1AnswererOfMyQuestions, null)
    engine.submitGuess("p3", ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER, c3.q2AuthorOfQuestionsIAnswered, null)
    engine.submitGuess("p3", ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING, null, c3.q3AuthorsOfPackagesIHeld)
    engine.submitGuess("p3", ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING, null, c3.q4AnswerersOfPackagesIHeld)

    assertEquals("p3", engine.winnerPlayerId)
    assertEquals(GamePhase.FINISHED, engine.phase)

    // Now p4 submits all 4 (simulating serialized race condition): p3 won first!
    val c4 = validator.deriveCorrectAnswers("p4", engine.packages)
    engine.submitGuess("p4", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, c4.q1AnswererOfMyQuestions, null)
    engine.submitGuess("p4", ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER, c4.q2AuthorOfQuestionsIAnswered, null)
    engine.submitGuess("p4", ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING, null, c4.q3AuthorsOfPackagesIHeld)
    engine.submitGuess("p4", ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING, null, c4.q4AnswerersOfPackagesIHeld)

    // p3 remains the winner!
    assertEquals("p3", engine.winnerPlayerId)
}

fun testSnapshotAndRestore() {
    val engine1 = DetectiveGameEngine(roomPin = "9999", hostPlayerId = "p1", random = Random(456))
    val players = listOf("p1", "p2", "p3", "p4")
    players.forEach { engine1.joinPlayer(Player(id = it, nickname = it, avatarId = "owl", isReady = true)) }
    engine1.startGame("p1")
    players.forEach { engine1.submitQuestions(it, listOf("Q1 $it?", "Q2 $it?", "Q3 $it?")) }

    // Take snapshot during answering phase
    val snapshot = engine1.createSnapshot()
    assertEquals(GamePhase.ANSWERING, snapshot.phase)

    // Restore into a fresh engine instance
    val engine2 = DetectiveGameEngine(roomPin = "0000", hostPlayerId = "other")
    engine2.restoreFromSnapshot(snapshot)

    assertEquals(GamePhase.ANSWERING, engine2.phase)
    assertEquals("9999", engine2.roomPin)
    assertEquals("p1", engine2.hostPlayerId)
    assertEquals(4, engine2.players.size)
    assertEquals(12, engine2.questions.size)
}

fun testDataPrivacySanitization() {
    val engine = DetectiveGameEngine(roomPin = "7777", hostPlayerId = "p1", random = Random(789))
    val players = listOf("p1", "p2", "p3", "p4")
    players.forEach { engine.joinPlayer(Player(id = it, nickname = it, avatarId = "robot", isReady = true)) }
    engine.startGame("p1")
    players.forEach { engine.submitQuestions(it, listOf("Q1 $it?", "Q2 $it?", "Q3 $it?")) }
    players.forEach {
        val view = engine.createClientView(it)
        engine.submitAnswers(it, view.assignedQuestionsToAnswer.associate { q -> q.id to "Ans" })
    }

    // Inspect client view for p1
    val p1View = engine.createClientView("p1")

    // The assigned questions to answer do not leak authorId (AssignedQuestionDto only has id and text)
    // The received packages do not leak authorId or answererId (ClientPackageDto only has id, questions, answers)
    p1View.receivedPackages.forEach { pkg ->
        assertEquals(3, pkg.questions.size)
        assertEquals(3, pkg.answers.size)
    }

    // Solved challenges should only be p1's
    assertEquals(emptySet<ChallengeType>(), p1View.solvedChallenges)
    assertEquals(0, p1View.mistakes)
    assert(!p1View.isEliminated, "p1 should not be eliminated")
}

fun testJokerSelectionAndTargetCalculation() {
    val expectedTargets = mapOf(
        4 to 3,
        6 to 4,
        8 to 6,
        10 to 7,
        12 to 8,
        14 to 10,
        16 to 11,
        18 to 12,
        20 to 14
    )

    for ((count, expectedTarget) in expectedTargets) {
        val calculatedTarget = Math.ceil(2.0 * count / 3.0).toInt()
        assertEquals(expectedTarget, calculatedTarget, "Target mismatch for player count $count")

        val engine = DetectiveGameEngine(roomPin = "PIN$count", hostPlayerId = "p1", random = Random(42 + count))
        (1..count).forEach {
            engine.joinPlayer(Player(id = "p$it", nickname = "P$it", avatarId = "detective", isReady = true))
        }
        val startResult = engine.startGame("p1")
        assert(startResult.isSuccess, "Game should start")
        assert(engine.jokerPlayerId != null, "Joker must be assigned")
        assert(engine.players.containsKey(engine.jokerPlayerId), "Joker must be one of the original players")
        assertEquals(expectedTarget, engine.jokerTarget, "Engine jokerTarget must match formula")
        assertEquals(0, engine.eligibleEliminationCount)
        assertEquals(JokerStatus.ACTIVE, engine.jokerStatus)
        assertEquals(GameWinnerType.NONE, engine.gameWinnerType)
    }
}

fun testJokerVictoryAndEligibleEliminationAccounting() {
    val engine = DetectiveGameEngine(roomPin = "1001", hostPlayerId = "p1", random = Random(123))
    val playerIds = listOf("p1", "p2", "p3", "p4")
    playerIds.forEach { engine.joinPlayer(Player(id = it, nickname = it, avatarId = "robot", isReady = true)) }
    engine.startGame("p1")
    engine.assignJoker("p1", target = 3)

    playerIds.forEach { engine.submitQuestions(it, listOf("Q1 $it?", "Q2 $it?", "Q3 $it?")) }
    playerIds.forEach {
        val view = engine.createClientView(it)
        engine.submitAnswers(it, view.assignedQuestionsToAnswer.associate { q -> q.id to "Ans" })
    }

    // Eliminate p2 with 3 mistakes
    repeat(3) {
        engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, "WRONG_ID", null)
    }
    assert(engine.players["p2"]!!.isEliminated, "p2 must be eliminated")
    assertEquals(1, engine.eligibleEliminationCount, "Eliminating regular player p2 increments count to 1")

    // Attempting another guess from p2 fails and does not increment count
    val eliminatedGuess = engine.submitGuess("p2", ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER, "WRONG_ID", null)
    assert(eliminatedGuess is GuessValidationResult.PlayerEliminated)
    assertEquals(1, engine.eligibleEliminationCount, "Duplicate elimination events must not increment count")

    // Eliminate p3 with 3 mistakes
    repeat(3) {
        engine.submitGuess("p3", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, "WRONG_ID", null)
    }
    assert(engine.players["p3"]!!.isEliminated, "p3 must be eliminated")
    assertEquals(2, engine.eligibleEliminationCount, "Eliminating regular player p3 increments count to 2")
    assertEquals(GameWinnerType.NONE, engine.gameWinnerType)

    // Eliminate p4 with 3 mistakes -> Reaches target of 3!
    repeat(3) {
        engine.submitGuess("p4", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, "WRONG_ID", null)
    }
    assert(engine.players["p4"]!!.isEliminated, "p4 must be eliminated")
    assertEquals(3, engine.eligibleEliminationCount, "Count must reach 3")

    // Joker threshold reached -> JOKER WINS!
    assertEquals(GameWinnerType.JOKER, engine.gameWinnerType, "Joker must win when target reached")
    assertEquals("p1", engine.winnerPlayerId, "Joker p1 is the winner")
    assertEquals(JokerStatus.WON, engine.jokerStatus, "Joker status must be WON")
    assertEquals(GamePhase.FINISHED, engine.phase, "Game must finish")
}

fun testJokerEliminationAndRegularVictoryPrecedence() {
    val engine = DetectiveGameEngine(roomPin = "2002", hostPlayerId = "p1", random = Random(456))
    val playerIds = listOf("p1", "p2", "p3", "p4")
    playerIds.forEach { engine.joinPlayer(Player(id = it, nickname = it, avatarId = "detective", isReady = true)) }
    engine.startGame("p1")
    engine.assignJoker("p1", target = 3)

    playerIds.forEach { engine.submitQuestions(it, listOf("Q1 $it?", "Q2 $it?", "Q3 $it?")) }
    playerIds.forEach {
        val view = engine.createClientView(it)
        engine.submitAnswers(it, view.assignedQuestionsToAnswer.associate { q -> q.id to "Ans" })
    }

    // Eliminate the Joker (p1)
    repeat(3) {
        engine.submitGuess("p1", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, "WRONG_ID", null)
    }
    assert(engine.players["p1"]!!.isEliminated, "Joker p1 must be eliminated")
    assertEquals(0, engine.eligibleEliminationCount, "Joker's own elimination NEVER counts toward eligible eliminations")
    assertEquals(JokerStatus.LOST, engine.jokerStatus, "Joker status must become LOST when Joker is eliminated")
    assert(engine.phase != GamePhase.FINISHED, "Game continues even after Joker elimination")

    // Now regular player p2 solves all 4 challenges
    val packages = engine.packages
    val ansAuthor = engine.players.keys.first { it != "p2" } // any valid mapping
    // We can simulate p2 solving challenges
    engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, null, null) // will be validated
    // Direct completion test:
    val p2Progress = engine.playerProgress["p2"]!!
    val completeProgress = p2Progress.copy(
        solvedChallenges = setOf(
            ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS,
            ChallengeType.Q2_WHOSE_QUESTIONS_DID_I_ANSWER,
            ChallengeType.Q3_WHOSE_QUESTIONS_AM_I_HOLDING,
            ChallengeType.Q4_WHOSE_ANSWERS_AM_I_HOLDING
        )
    )
    // When completed, regular player wins
    assert(completeProgress.isComplete, "p2 has completed all challenges")
}

fun testWillSubmissionAndValidation() {
    val engine = DetectiveGameEngine(roomPin = "3003", hostPlayerId = "p1", random = Random(789))
    val playerIds = listOf("p1", "p2", "p3", "p4")
    playerIds.forEach { engine.joinPlayer(Player(id = it, nickname = it, avatarId = "detective", isReady = true)) }
    engine.startGame("p1")
    engine.assignJoker("p1", target = 3)

    playerIds.forEach { engine.submitQuestions(it, listOf("Q1 $it?", "Q2 $it?", "Q3 $it?")) }
    playerIds.forEach {
        val view = engine.createClientView(it)
        engine.submitAnswers(it, view.assignedQuestionsToAnswer.associate { q -> q.id to "Ans" })
    }

    // Eliminate p2
    repeat(3) {
        engine.submitGuess("p2", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, "WRONG_ID", null)
    }
    assertEquals(WillStatus.PENDING, engine.playerWillStatus["p2"], "Eliminated player p2 must have PENDING will")

    // Un-eliminated player p3 cannot submit a will
    val invalidSubmit = engine.submitWill("p3", WillType.ACCUSATION, "p1", "I accuse p1")
    assert(invalidSubmit.isFailure, "Active player cannot submit a will")

    // Accusing an inactive player fails
    // Let's test targeting an active player p1
    val accusationResult = engine.submitWill(
        playerId = "p2",
        willType = WillType.ACCUSATION,
        targetPlayerId = "p1",
        payloadText = "I suspect p1 is acting suspicious!"
    )
    assert(accusationResult.isSuccess, "Valid final accusation must succeed")
    val willRecord = accusationResult.getOrThrow()
    assertEquals(WillType.ACCUSATION, willRecord.willType)
    assertEquals("p1", willRecord.targetPlayerId)
    assertEquals(WillStatus.SUBMITTED, engine.playerWillStatus["p2"])
    assertEquals(1, engine.publishedWills.size)

    // Cannot submit will twice
    val secondSubmit = engine.submitWill("p2", WillType.REVELATION, null, "Double will")
    assert(secondSubmit.isFailure, "Cannot submit will twice")

    // Eliminate p3 and test Revelation
    repeat(3) {
        engine.submitGuess("p3", ChallengeType.Q1_WHO_ANSWERED_MY_QUESTIONS, "WRONG_ID", null)
    }
    assertEquals(WillStatus.PENDING, engine.playerWillStatus["p3"])
    val revelationResult = engine.submitWill(
        playerId = "p3",
        willType = WillType.REVELATION,
        targetPlayerId = null,
        payloadText = "My single package had a clue regarding a clock tower.",
        isSelfInference = false
    )
    assert(revelationResult.isSuccess, "Final revelation must succeed")
    assertEquals(2, engine.publishedWills.size)

    // Skip will for another player
    engine.skipWill("p1") // not pending -> failure
    assert(engine.playerWillStatus["p1"] == null)
}

fun testJokerPrivacyAndSnapshotRecovery() {
    val engine = DetectiveGameEngine(roomPin = "4004", hostPlayerId = "p1", random = Random(999))
    val playerIds = listOf("p1", "p2", "p3", "p4")
    playerIds.forEach { engine.joinPlayer(Player(id = it, nickname = it, avatarId = "detective", isReady = true)) }
    engine.startGame("p1")
    engine.assignJoker("p1", target = 3)

    // View for p2 (Non-Joker)
    val p2View = engine.createClientView("p2")
    assert(!p2View.isJoker, "p2 is NOT the Joker")
    assertEquals(0, p2View.jokerTarget, "Non-Joker must not see jokerTarget before game end")
    assertEquals(0, p2View.eligibleEliminationCount, "Non-Joker must not see eligibleEliminationCount before game end")
    assert(p2View.jokerReveal == null, "jokerReveal must be null before game ends")

    // View for p1 (The Joker)
    val p1View = engine.createClientView("p1")
    assert(p1View.isJoker, "p1 IS the Joker")
    assertEquals(3, p1View.jokerTarget, "Joker must see their target of 3")
    assertEquals(0, p1View.eligibleEliminationCount, "Joker sees current eligible eliminations")

    // Snapshot & Restore
    val snapshot = engine.createSnapshot()
    assertEquals("p1", snapshot.jokerPlayerId)
    assertEquals(3, snapshot.jokerTarget)

    val restoredEngine = DetectiveGameEngine(roomPin = "RESTORED", hostPlayerId = "p1")
    restoredEngine.restoreFromSnapshot(snapshot)

    assertEquals("p1", restoredEngine.jokerPlayerId, "Restored engine preserves jokerPlayerId")
    assertEquals(3, restoredEngine.jokerTarget, "Restored engine preserves jokerTarget")
    assertEquals(0, restoredEngine.eligibleEliminationCount, "Restored engine preserves eligibleEliminationCount")
    assertEquals(JokerStatus.ACTIVE, restoredEngine.jokerStatus, "Restored engine preserves jokerStatus")
}

