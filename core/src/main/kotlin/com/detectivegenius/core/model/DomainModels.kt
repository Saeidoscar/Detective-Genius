package com.detectivegenius.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    LOBBY,
    QUESTION_CREATION,
    ANSWERING,
    DISTRIBUTION,
    INVESTIGATION,
    FINISHED
}

@Serializable
enum class ChallengeType {
    Q1_WHO_ANSWERED_MY_QUESTIONS,
    Q2_WHOSE_QUESTIONS_DID_I_ANSWER,
    Q3_WHOSE_QUESTIONS_AM_I_HOLDING,
    Q4_WHOSE_ANSWERS_AM_I_HOLDING;

    val isSetBased: Boolean
        get() = false
}

@Serializable
enum class JokerStatus {
    ACTIVE,
    WON,
    LOST
}

@Serializable
enum class GameWinnerType {
    NONE,
    REGULAR,
    JOKER
}

@Serializable
enum class WillStatus {
    UNAVAILABLE,
    PENDING,
    SUBMITTED,
    SKIPPED,
    EXPIRED
}

@Serializable
enum class WillType {
    ACCUSATION,
    REVELATION,
    QUESTION
}

@Serializable
data class WillRecord(
    val id: String,
    val playerId: String,
    val playerNickname: String,
    val playerAvatarId: String,
    val willType: WillType,
    val targetPlayerId: String? = null,
    val targetPlayerNickname: String? = null,
    val payloadText: String,
    val answerText: String? = null,
    val isSelfInference: Boolean = false,
    val submittedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Player(
    val id: String,
    val nickname: String,
    val avatarId: String,
    val isReady: Boolean = false,
    val isConnected: Boolean = true,
    val isEliminated: Boolean = false
)

@Serializable
data class Question(
    val id: String,
    val authorId: String,
    val text: String
)

@Serializable
data class Answer(
    val id: String,
    val questionId: String,
    val answererId: String,
    val text: String
)

/**
 * Internal authoritative model containing hidden identities.
 * STRICTLY HOST-ONLY: This model is never serialized directly to clients.
 */
@Serializable
data class QuestionPackage(
    val id: String,
    val authorId: String,
    val answererId: String,
    val questionIds: List<String>,
    val recipientIds: Set<String>
)

@Serializable
data class PlayerProgress(
    val playerId: String,
    val solvedChallenges: Set<ChallengeType> = emptySet(),
    val mistakes: Int = 0,
    val isEliminated: Boolean = false
) {
    val isComplete: Boolean
        get() = solvedChallenges.size == ChallengeType.entries.size
}

@Serializable
data class MysteryGuess(
    val challengeType: ChallengeType,
    val singleSuspectId: String? = null,
    val suspectPair: Set<String>? = null
)

@Serializable
sealed class GuessValidationResult {
    data class Correct(val challengeType: ChallengeType) : GuessValidationResult()
    data class Incorrect(val challengeType: ChallengeType, val currentMistakes: Int, val isEliminated: Boolean) : GuessValidationResult()
    data class AlreadySolved(val challengeType: ChallengeType) : GuessValidationResult()
    data object PlayerEliminated : GuessValidationResult()
    data class InvalidSubmission(val reason: String) : GuessValidationResult()
}
