package com.detectivegenius.gameengine.state

import com.detectivegenius.core.model.*
import com.detectivegenius.core.protocol.*
import com.detectivegenius.gameengine.assignment.AssignmentSolver
import com.detectivegenius.gameengine.assignment.RawAssignment
import com.detectivegenius.gameengine.rules.MysteryValidator
import com.detectivegenius.gameengine.rules.ValidationRules
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class GameSessionSnapshot(
    val phase: GamePhase,
    val roomPin: String,
    val hostPlayerId: String,
    val players: List<Player>,
    val questions: List<Question>,
    val answers: List<Answer>,
    val packages: List<QuestionPackage>,
    val playerProgress: Map<String, PlayerProgress>,
    val assignedAnswererToAuthor: Map<String, String>, // answererId -> authorId
    val winnerPlayerId: String?,
    val jokerPlayerId: String? = null,
    val jokerTarget: Int = 0,
    val eligibleEliminationCount: Int = 0,
    val jokerStatus: JokerStatus = JokerStatus.ACTIVE,
    val gameWinnerType: GameWinnerType = GameWinnerType.NONE,
    val gameEndReason: String? = null,
    val playerWillStatus: Map<String, WillStatus> = emptyMap(),
    val publishedWills: List<WillRecord> = emptyList(),
    val countedEliminations: Set<String> = emptySet()
)

class DetectiveGameEngine(
    var roomPin: String,
    var hostPlayerId: String,
    private val random: Random = Random.Default,
    private val assignmentSolver: AssignmentSolver = AssignmentSolver(),
    private val mysteryValidator: MysteryValidator = MysteryValidator()
) {
    private val lock = Any()

    var phase: GamePhase = GamePhase.LOBBY
        private set

    private val _players = mutableMapOf<String, Player>()
    val players: Map<String, Player> get() = synchronized(lock) { _players.toMap() }

    private val _questions = mutableListOf<Question>()
    val questions: List<Question> get() = synchronized(lock) { _questions.toList() }

    private val _answers = mutableListOf<Answer>()
    val answers: List<Answer> get() = synchronized(lock) { _answers.toList() }

    private val _packages = mutableListOf<QuestionPackage>()
    val packages: List<QuestionPackage> get() = synchronized(lock) { _packages.toList() }

    private val _playerProgress = mutableMapOf<String, PlayerProgress>()
    val playerProgress: Map<String, PlayerProgress> get() = synchronized(lock) { _playerProgress.toMap() }

    // Map of answererId -> authorId whose questions this answerer must answer
    private val _assignedAnswererToAuthor = mutableMapOf<String, String>()

    var winnerPlayerId: String? = null
        private set

    // --- JOKER STATE ---
    var jokerPlayerId: String? = null
        private set
    var jokerTarget: Int = 0
        private set
    var eligibleEliminationCount: Int = 0
        private set
    var jokerStatus: JokerStatus = JokerStatus.ACTIVE
        private set
    var gameWinnerType: GameWinnerType = GameWinnerType.NONE
        private set
    var gameEndReason: String? = null
        private set

    // --- WILL STATE ---
    private val _playerWillStatus = mutableMapOf<String, WillStatus>()
    val playerWillStatus: Map<String, WillStatus> get() = synchronized(lock) { _playerWillStatus.toMap() }

    private val _publishedWills = mutableListOf<WillRecord>()
    val publishedWills: List<WillRecord> get() = synchronized(lock) { _publishedWills.toList() }

    private val _countedEliminations = mutableSetOf<String>()
    val countedEliminations: Set<String> get() = synchronized(lock) { _countedEliminations.toSet() }

    fun assignJoker(playerId: String, target: Int? = null) = synchronized(lock) {
        jokerPlayerId = playerId
        jokerTarget = target ?: Math.ceil(2.0 * _players.size / 3.0).toInt()
    }

    fun joinPlayer(player: Player): Result<Unit> = synchronized(lock) {
        if (phase != GamePhase.LOBBY) {
            return Result.failure(IllegalStateException("Cannot join game in phase $phase"))
        }
        if (_players.size >= ValidationRules.MAX_PLAYERS) {
            return Result.failure(IllegalStateException("Room is full (max ${ValidationRules.MAX_PLAYERS})"))
        }
        _players[player.id] = player
        _playerProgress[player.id] = PlayerProgress(playerId = player.id)
        Result.success(Unit)
    }

    fun removePlayer(playerId: String): Unit = synchronized(lock) {
        if (phase == GamePhase.LOBBY) {
            _players.remove(playerId)
            _playerProgress.remove(playerId)
        } else {
            _players[playerId]?.let {
                _players[playerId] = it.copy(isConnected = false)
            }
        }
    }

    fun setPlayerReady(playerId: String, isReady: Boolean): Result<Unit> = synchronized(lock) {
        if (phase != GamePhase.LOBBY) {
            return Result.failure(IllegalStateException("Cannot change ready status outside lobby"))
        }
        val p = _players[playerId] ?: return Result.failure(IllegalArgumentException("Player not found"))
        _players[playerId] = p.copy(isReady = isReady)
        Result.success(Unit)
    }

    fun startGame(requestingPlayerId: String): Result<Unit> = synchronized(lock) {
        if (requestingPlayerId != hostPlayerId) {
            return Result.failure(IllegalStateException("Only the host can start the investigation"))
        }
        if (phase != GamePhase.LOBBY) {
            return Result.failure(IllegalStateException("Game is already in progress"))
        }
        val (canStart, reason) = ValidationRules.canStartGame(
            playerCount = _players.size,
            allReady = _players.values.all { it.isReady }
        )
        if (!canStart) {
            return Result.failure(IllegalStateException(reason ?: "Cannot start game"))
        }

        // Randomly select exactly one Joker from the original players
        if (jokerPlayerId == null && _players.isNotEmpty()) {
            val playerIds = _players.keys.toList()
            jokerPlayerId = playerIds.random(random)
            // Fixed target: ceil(2 * originalPlayerCount / 3.0)
            jokerTarget = Math.ceil(2.0 * playerIds.size / 3.0).toInt()
        }

        phase = GamePhase.QUESTION_CREATION
        Result.success(Unit)
    }

    fun submitQuestions(playerId: String, questionTexts: List<String>): Result<Unit> = synchronized(lock) {
        if (phase != GamePhase.QUESTION_CREATION) {
            return Result.failure(IllegalStateException("Not in question creation phase"))
        }
        val p = _players[playerId] ?: return Result.failure(IllegalArgumentException("Player not found"))
        val validation = ValidationRules.validateQuestions(questionTexts)
        if (validation is ValidationRules.QuestionValidationResult.Error) {
            return Result.failure(IllegalArgumentException(validation.message))
        }

        // Avoid duplicate submission from same author
        if (_questions.any { it.authorId == playerId }) {
            return Result.failure(IllegalStateException("Questions already submitted for this author"))
        }

        questionTexts.forEachIndexed { idx, text ->
            _questions.add(
                Question(
                    id = "q_${playerId}_$idx",
                    authorId = playerId,
                    text = text.trim()
                )
            )
        }

        // Check if all players have submitted questions
        val authorsCount = _questions.map { it.authorId }.distinct().size
        if (authorsCount == _players.size) {
            setupAnsweringPhase()
        }

        Result.success(Unit)
    }

    private fun setupAnsweringPhase() {
        val playerIds = _players.keys.toList()
        val rawAssignments = assignmentSolver.solve(playerIds, random)
            ?: throw IllegalStateException("Failed to find valid assignment for ${playerIds.size} players")

        this.cachedRawAssignments = rawAssignments

        for (ra in rawAssignments) {
            _assignedAnswererToAuthor[ra.answererId] = ra.authorId
        }

        phase = GamePhase.ANSWERING
    }

    private var cachedRawAssignments: List<RawAssignment>? = null

    fun submitAnswers(playerId: String, answersMap: Map<String, String>): Result<Unit> = synchronized(lock) {
        if (phase != GamePhase.ANSWERING) {
            return Result.failure(IllegalStateException("Not in answering phase"))
        }
        val authorIdWhoseQuestionsIAnswer = _assignedAnswererToAuthor[playerId]
            ?: return Result.failure(IllegalStateException("No questions assigned to player $playerId"))

        val expectedQuestions = _questions.filter { it.authorId == authorIdWhoseQuestionsIAnswer }
        val expectedQIds = expectedQuestions.map { it.id }

        val validation = ValidationRules.validateAnswers(answersMap, expectedQIds)
        if (validation is ValidationRules.AnswerValidationResult.Error) {
            return Result.failure(IllegalArgumentException(validation.message))
        }

        if (_answers.any { it.answererId == playerId }) {
            return Result.failure(IllegalStateException("Answers already submitted for this player"))
        }

        expectedQIds.forEachIndexed { idx, qId ->
            _answers.add(
                Answer(
                    id = "ans_${playerId}_$idx",
                    questionId = qId,
                    answererId = playerId,
                    text = answersMap[qId]!!.trim()
                )
            )
        }

        val answerersCount = _answers.map { it.answererId }.distinct().size
        if (answerersCount == _players.size) {
            assembleAndDistributePackages()
        }

        Result.success(Unit)
    }

    private fun assembleAndDistributePackages() {
        val rawAssignments = cachedRawAssignments
            ?: throw IllegalStateException("Cached assignments missing during package distribution")

        for (ra in rawAssignments) {
            val qIds = _questions.filter { it.authorId == ra.authorId }.map { it.id }
            val pkg = QuestionPackage(
                id = ra.packageId,
                authorId = ra.authorId,
                answererId = ra.answererId,
                questionIds = qIds,
                recipientIds = ra.recipientIds
            )
            _packages.add(pkg)
        }

        phase = GamePhase.DISTRIBUTION
        phase = GamePhase.INVESTIGATION
    }

    fun submitGuess(
        playerId: String,
        challengeType: ChallengeType,
        singleSuspectId: String?,
        suspectPair: Set<String>?
    ): GuessValidationResult = synchronized(lock) {
        if (phase != GamePhase.INVESTIGATION) {
            return GuessValidationResult.InvalidSubmission("Investigation is not currently active")
        }

        val currentProgress = _playerProgress[playerId]
            ?: return GuessValidationResult.InvalidSubmission("Player not found")

        if (currentProgress.isEliminated) {
            return GuessValidationResult.PlayerEliminated
        }

        if (currentProgress.solvedChallenges.contains(challengeType)) {
            return GuessValidationResult.AlreadySolved(challengeType)
        }

        val correctAnswers = mysteryValidator.deriveCorrectAnswers(playerId, _packages)

        val result = mysteryValidator.evaluateGuess(
            currentProgress = currentProgress,
            challengeType = challengeType,
            singleSuspectId = singleSuspectId,
            suspectPair = suspectPair,
            correctAnswers = correctAnswers
        )

        when (result) {
            is GuessValidationResult.Correct -> {
                val updatedSolved = currentProgress.solvedChallenges + challengeType
                val updatedProgress = currentProgress.copy(solvedChallenges = updatedSolved)
                _playerProgress[playerId] = updatedProgress

                // Check Regular Player Victory
                if (updatedProgress.isComplete && winnerPlayerId == null) {
                    winnerPlayerId = playerId
                    gameWinnerType = GameWinnerType.REGULAR
                    gameEndReason = "Player $playerId solved all 4 mystery challenges"
                    jokerStatus = JokerStatus.LOST
                    phase = GamePhase.FINISHED
                }
            }
            is GuessValidationResult.Incorrect -> {
                val updatedProgress = currentProgress.copy(
                    mistakes = result.currentMistakes,
                    isEliminated = result.isEliminated
                )
                _playerProgress[playerId] = updatedProgress

                if (result.isEliminated) {
                    _players[playerId]?.let {
                        _players[playerId] = it.copy(isEliminated = true)
                    }

                    // 1. Eligible elimination accounting for Joker:
                    val isJokerEliminated = (playerId == jokerPlayerId)
                    if (isJokerEliminated) {
                        jokerStatus = JokerStatus.LOST
                    } else if (_countedEliminations.add(playerId)) {
                        eligibleEliminationCount++
                    }

                    // 2. Check Joker Victory Threshold:
                    if (eligibleEliminationCount >= jokerTarget && gameWinnerType == GameWinnerType.NONE && jokerStatus == JokerStatus.ACTIVE) {
                        winnerPlayerId = jokerPlayerId
                        gameWinnerType = GameWinnerType.JOKER
                        jokerStatus = JokerStatus.WON
                        phase = GamePhase.FINISHED
                        gameEndReason = "Joker reached elimination target ($eligibleEliminationCount / $jokerTarget)"
                    }

                    // 3. If game has not ended, offer 1 Will opportunity:
                    if (phase != GamePhase.FINISHED) {
                        _playerWillStatus[playerId] = WillStatus.PENDING
                    } else {
                        _playerWillStatus[playerId] = WillStatus.UNAVAILABLE
                    }

                    // Check if all players are eliminated
                    if (_playerProgress.values.all { it.isEliminated } && phase != GamePhase.FINISHED) {
                        phase = GamePhase.FINISHED
                        gameEndReason = "All detectives eliminated"
                    }
                }
            }
            else -> Unit
        }

        result
    }

    // --- WILL SUBMISSION & MANAGEMENT ---
    fun submitWill(
        playerId: String,
        willType: WillType,
        targetPlayerId: String? = null,
        payloadText: String,
        isSelfInference: Boolean = false
    ): Result<WillRecord> = synchronized(lock) {
        val status = _playerWillStatus[playerId]
        if (status != WillStatus.PENDING) {
            return Result.failure(IllegalStateException("Will is not pending for player $playerId (current status: $status)"))
        }

        val player = _players[playerId]
            ?: return Result.failure(IllegalArgumentException("Player not found: $playerId"))

        // Target validation for ACCUSATION and QUESTION: Target must be an ACTIVE player at submission time!
        if (willType == WillType.ACCUSATION || willType == WillType.QUESTION) {
            if (targetPlayerId == null) {
                return Result.failure(IllegalArgumentException("Target player is required for $willType"))
            }
            val target = _players[targetPlayerId]
                ?: return Result.failure(IllegalArgumentException("Target player not found: $targetPlayerId"))
            val targetProgress = _playerProgress[targetPlayerId]
            if (target.isEliminated || targetProgress?.isEliminated == true) {
                return Result.failure(IllegalStateException("Target player $targetPlayerId is not active"))
            }
        }

        if (payloadText.isBlank()) {
            return Result.failure(IllegalArgumentException("Will payload cannot be blank"))
        }

        val targetNickname = targetPlayerId?.let { _players[it]?.nickname }
        val record = WillRecord(
            id = "will_${playerId}_${System.currentTimeMillis()}",
            playerId = playerId,
            playerNickname = player.nickname,
            playerAvatarId = player.avatarId,
            willType = willType,
            targetPlayerId = targetPlayerId,
            targetPlayerNickname = targetNickname,
            payloadText = payloadText.trim(),
            isSelfInference = isSelfInference,
            submittedAt = System.currentTimeMillis()
        )

        _publishedWills.add(record)
        _playerWillStatus[playerId] = WillStatus.SUBMITTED

        Result.success(record)
    }

    fun answerWillQuestion(
        playerId: String,
        willId: String,
        answerText: String
    ): Result<WillRecord> = synchronized(lock) {
        val idx = _publishedWills.indexOfFirst { it.id == willId }
        if (idx == -1) {
            return Result.failure(IllegalArgumentException("Will not found: $willId"))
        }
        val will = _publishedWills[idx]
        if (will.willType != WillType.QUESTION) {
            return Result.failure(IllegalStateException("Will is not a question"))
        }
        if (will.targetPlayerId != playerId) {
            return Result.failure(IllegalStateException("Only target player can answer this question"))
        }
        if (answerText.isBlank()) {
            return Result.failure(IllegalArgumentException("Answer cannot be blank"))
        }

        val updated = will.copy(answerText = answerText.trim())
        _publishedWills[idx] = updated
        Result.success(updated)
    }

    fun skipWill(playerId: String): Result<Unit> = synchronized(lock) {
        val status = _playerWillStatus[playerId]
        if (status != WillStatus.PENDING) {
            return Result.failure(IllegalStateException("Will is not pending for player $playerId"))
        }
        _playerWillStatus[playerId] = WillStatus.SKIPPED
        Result.success(Unit)
    }

    fun expireWill(playerId: String): Result<Unit> = synchronized(lock) {
        val status = _playerWillStatus[playerId]
        if (status == WillStatus.PENDING) {
            _playerWillStatus[playerId] = WillStatus.EXPIRED
        }
        Result.success(Unit)
    }

    /**
     * Sanitizes game state for a specific player view.
     * ZERO AUTHOR / ANSWERER IDS LEAKED.
     * ZERO JOKER IDENTITY LEAKED BEFORE GAME END.
     */
    fun createClientView(playerId: String): ClientPlayerViewDto = synchronized(lock) {
        val playerSummaries = _players.values.map { p ->
            val prog = _playerProgress[p.id]
            PlayerSummaryDto(
                id = p.id,
                nickname = p.nickname,
                avatarId = p.avatarId,
                isReady = p.isReady,
                isConnected = p.isConnected,
                isEliminated = p.isEliminated,
                solvedChallengesCount = prog?.solvedChallenges?.size ?: 0,
                mistakes = prog?.mistakes ?: 0
            )
        }

        val assignedQuestions = if (phase == GamePhase.ANSWERING) {
            val authorId = _assignedAnswererToAuthor[playerId]
            if (authorId != null) {
                _questions.filter { it.authorId == authorId }.map {
                    AssignedQuestionDto(id = it.id, text = it.text)
                }
            } else emptyList()
        } else emptyList()

        val receivedPackages = if (phase == GamePhase.DISTRIBUTION || phase == GamePhase.INVESTIGATION || phase == GamePhase.FINISHED) {
            _packages.filter { it.recipientIds.contains(playerId) }.map { pkg ->
                val qTexts = pkg.questionIds.mapNotNull { qId -> _questions.find { it.id == qId }?.text }
                val aTexts = pkg.questionIds.mapNotNull { qId -> _answers.find { it.questionId == qId }?.text }
                ClientPackageDto(
                    id = pkg.id,
                    questions = qTexts,
                    answers = aTexts
                )
            }
        } else emptyList()

        val progress = _playerProgress[playerId]

        // Joker privacy enforcement:
        val isCurrentJoker = (playerId == jokerPlayerId)
        val jokerRevealDto = if (phase == GamePhase.FINISHED) {
            JokerRevealDto(
                jokerPlayerId = jokerPlayerId ?: "",
                jokerTarget = jokerTarget,
                eligibleEliminationCount = eligibleEliminationCount,
                jokerWon = (gameWinnerType == GameWinnerType.JOKER)
            )
        } else null

        val pendingWillDto = if (_playerWillStatus[playerId] == WillStatus.PENDING && phase != GamePhase.FINISHED) {
            PendingWillDto(playerId = playerId, deadlineSeconds = 45)
        } else null

        val publishedWillsDto = _publishedWills.map { w ->
            WillRecordDto(
                id = w.id,
                playerId = w.playerId,
                playerNickname = w.playerNickname,
                playerAvatarId = w.playerAvatarId,
                willType = w.willType.name,
                targetPlayerId = w.targetPlayerId,
                targetPlayerNickname = w.targetPlayerNickname,
                payloadText = w.payloadText,
                answerText = w.answerText,
                isSelfInference = w.isSelfInference,
                timestamp = w.submittedAt
            )
        }

        ClientPlayerViewDto(
            playerId = playerId,
            phase = phase,
            players = playerSummaries,
            assignedQuestionsToAnswer = assignedQuestions,
            receivedPackages = receivedPackages,
            solvedChallenges = progress?.solvedChallenges ?: emptySet(),
            mistakes = progress?.mistakes ?: 0,
            isEliminated = progress?.isEliminated ?: false,
            winnerPlayerId = winnerPlayerId,
            roomPin = roomPin,
            hostPlayerId = hostPlayerId,
            isJoker = isCurrentJoker,
            jokerTarget = if (isCurrentJoker || phase == GamePhase.FINISHED) jokerTarget else 0,
            eligibleEliminationCount = if (isCurrentJoker || phase == GamePhase.FINISHED) eligibleEliminationCount else 0,
            jokerStatus = jokerStatus.name,
            gameWinnerType = gameWinnerType.name,
            gameEndReason = gameEndReason,
            jokerReveal = jokerRevealDto,
            pendingWill = pendingWillDto,
            publishedWills = publishedWillsDto
        )
    }

    fun createSnapshot(): GameSessionSnapshot = synchronized(lock) {
        GameSessionSnapshot(
            phase = phase,
            roomPin = roomPin,
            hostPlayerId = hostPlayerId,
            players = _players.values.toList(),
            questions = _questions.toList(),
            answers = _answers.toList(),
            packages = _packages.toList(),
            playerProgress = _playerProgress.toMap(),
            assignedAnswererToAuthor = _assignedAnswererToAuthor.toMap(),
            winnerPlayerId = winnerPlayerId,
            jokerPlayerId = jokerPlayerId,
            jokerTarget = jokerTarget,
            eligibleEliminationCount = eligibleEliminationCount,
            jokerStatus = jokerStatus,
            gameWinnerType = gameWinnerType,
            gameEndReason = gameEndReason,
            playerWillStatus = _playerWillStatus.toMap(),
            publishedWills = _publishedWills.toList(),
            countedEliminations = _countedEliminations.toSet()
        )
    }

    fun restoreFromSnapshot(snapshot: GameSessionSnapshot) = synchronized(lock) {
        phase = snapshot.phase
        roomPin = snapshot.roomPin
        hostPlayerId = snapshot.hostPlayerId
        _players.clear()
        snapshot.players.forEach { _players[it.id] = it }
        _questions.clear()
        _questions.addAll(snapshot.questions)
        _answers.clear()
        _answers.addAll(snapshot.answers)
        _packages.clear()
        _packages.addAll(snapshot.packages)
        _playerProgress.clear()
        _playerProgress.putAll(snapshot.playerProgress)
        _assignedAnswererToAuthor.clear()
        _assignedAnswererToAuthor.putAll(snapshot.assignedAnswererToAuthor)
        winnerPlayerId = snapshot.winnerPlayerId
        jokerPlayerId = snapshot.jokerPlayerId
        jokerTarget = snapshot.jokerTarget
        eligibleEliminationCount = snapshot.eligibleEliminationCount
        jokerStatus = snapshot.jokerStatus
        gameWinnerType = snapshot.gameWinnerType
        gameEndReason = snapshot.gameEndReason
        _playerWillStatus.clear()
        _playerWillStatus.putAll(snapshot.playerWillStatus)
        _publishedWills.clear()
        _publishedWills.addAll(snapshot.publishedWills)
        _countedEliminations.clear()
        _countedEliminations.addAll(snapshot.countedEliminations)
    }
}
