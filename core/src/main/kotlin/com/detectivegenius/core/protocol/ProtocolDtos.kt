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
    val hostPlayerId: String = ""
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
