package com.detectivegenius.core.protocol

import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.model.GamePhase
import kotlinx.serialization.Serializable

/**
 * Sanitized client package: Contains questions and answers,
 * but NEVER authorId or answererId.
 */
@Serializable
data class ClientPackageDto(
    val id: String,
    val questions: List<String>,
    val answers: List<String>
)

/**
 * Sanitized question presented to the designated answerer:
 * Has question text and id, but NEVER authorId.
 */
@Serializable
data class AssignedQuestionDto(
    val id: String,
    val text: String
)

@Serializable
data class PlayerSummaryDto(
    val id: String,
    val nickname: String,
    val avatarId: String,
    val isReady: Boolean,
    val isConnected: Boolean,
    val isEliminated: Boolean,
    val solvedChallengesCount: Int,
    val mistakes: Int
)

@Serializable
data class JokerRevealDto(
    val jokerPlayerId: String,
    val jokerTarget: Int,
    val eligibleEliminationCount: Int,
    val jokerWon: Boolean
)

@Serializable
data class PendingWillDto(
    val playerId: String,
    val deadlineSeconds: Int = 45
)

@Serializable
data class WillRecordDto(
    val id: String,
    val playerId: String,
    val playerNickname: String,
    val playerAvatarId: String,
    val willType: String, // ACCUSATION, REVELATION, QUESTION
    val targetPlayerId: String? = null,
    val targetPlayerNickname: String? = null,
    val payloadText: String,
    val answerText: String? = null,
    val isSelfInference: Boolean = false,
    val timestamp: Long = 0L
)

/**
 * Complete personalized view sent to an individual player.
 */
@Serializable
data class ClientPlayerViewDto(
    val playerId: String,
    val phase: GamePhase,
    val players: List<PlayerSummaryDto>,
    val assignedQuestionsToAnswer: List<AssignedQuestionDto> = emptyList(),
    val receivedPackages: List<ClientPackageDto> = emptyList(),
    val solvedChallenges: Set<ChallengeType> = emptySet(),
    val mistakes: Int = 0,
    val isEliminated: Boolean = false,
    val winnerPlayerId: String? = null,
    val roomPin: String = "",
    val hostPlayerId: String = "",
    // Joker & Will properties
    val isJoker: Boolean = false,
    val jokerTarget: Int = 0,
    val eligibleEliminationCount: Int = 0,
    val jokerStatus: String = "ACTIVE",
    val gameWinnerType: String = "NONE",
    val gameEndReason: String? = null,
    val jokerReveal: JokerRevealDto? = null,
    val pendingWill: PendingWillDto? = null,
    val publishedWills: List<WillRecordDto> = emptyList()
)

@Serializable
data class ChatMessageDto(
    val id: String,
    val senderId: String,
    val senderNickname: String,
    val senderAvatarId: String,
    val recipientId: String? = null, // null for public room chat, specific playerId for private
    val text: String,
    val timestamp: Long
)

@Serializable
data class TypingIndicatorDto(
    val senderId: String,
    val recipientId: String?,
    val isTyping: Boolean
)
