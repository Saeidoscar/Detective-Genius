package com.detectivegenius.gameengine.state

import com.detectivegenius.core.model.*
import com.detectivegenius.core.protocol.AssignedQuestionDto
import com.detectivegenius.core.protocol.ClientPackageDto
import com.detectivegenius.core.protocol.ClientPlayerViewDto
import com.detectivegenius.core.protocol.PlayerSummaryDto
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
    val winnerPlayerId: String?
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
            // Plan assignments and transition to ANSWERING
            setupAnsweringPhase()
        }

        Result.success(Unit)
    }

    private fun setupAnsweringPhase() {
        val playerIds = _players.keys.toList()
        // Generate derangement for author -> answerer
        val rawAssignments = assignmentSolver.solve(playerIds, random)
            ?: throw IllegalStateException("Failed to find valid assignment for ${playerIds.size} players")

        // Pre-store raw assignments for package creation after answers are submitted
        this.cachedRawAssignments = rawAssignments

        for (ra in rawAssignments) {
            // ra.authorId's questions will be answered by ra.answererId
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

        // Prevent duplicate answer submission
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

        // Check if all players have answered
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
        // In local state, players transition directly or after animation to INVESTIGATION
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

                // Check Victory
                if (updatedProgress.isComplete && winnerPlayerId == null) {
                    winnerPlayerId = playerId
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
                    // Check if all players are eliminated
                    if (_playerProgress.values.all { it.isEliminated }) {
                        phase = GamePhase.FINISHED
                    }
                }
            }
            else -> Unit
        }

        result
    }

    /**
     * Sanitizes game state for a specific player view.
     * ZERO AUTHOR / ANSWERER IDS LEAKED.
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

        // Assigned questions to answer (for current player during ANSWERING phase)
        val assignedQuestions = if (phase == GamePhase.ANSWERING) {
            val authorId = _assignedAnswererToAuthor[playerId]
            if (authorId != null) {
                _questions.filter { it.authorId == authorId }.map {
                    AssignedQuestionDto(id = it.id, text = it.text)
                }
            } else emptyList()
        } else emptyList()

        // Packages received by current player (during DISTRIBUTION, INVESTIGATION, FINISHED)
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
            hostPlayerId = hostPlayerId
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
            winnerPlayerId = winnerPlayerId
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
    }
}
