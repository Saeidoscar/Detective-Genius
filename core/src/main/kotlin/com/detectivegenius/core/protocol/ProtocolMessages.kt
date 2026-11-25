package com.detectivegenius.core.protocol

import com.detectivegenius.core.model.ChallengeType
import kotlinx.serialization.Serializable

const val CURRENT_PROTOCOL_VERSION = 1

@Serializable
sealed class ClientPacket {
    abstract val messageId: String
    abstract val sessionId: String
    abstract val playerId: String
    abstract val timestamp: Long

    @Serializable
    data class JoinRoom(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val nickname: String,
        val avatarId: String,
        val pin: String
    ) : ClientPacket()

    @Serializable
    data class SetReady(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val isReady: Boolean
    ) : ClientPacket()

    @Serializable
    data class StartGame(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long
    ) : ClientPacket()

    @Serializable
    data class SubmitQuestions(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val questions: List<String>
    ) : ClientPacket()

    @Serializable
    data class SubmitAnswers(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val answers: Map<String, String> // questionId -> answer text
    ) : ClientPacket()

    @Serializable
    data class SubmitGuess(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val challengeType: ChallengeType,
        val singleSuspectId: String? = null,
        val suspectPair: Set<String>? = null
    ) : ClientPacket()

    @Serializable
    data class SubmitWill(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val willType: String,
        val targetPlayerId: String? = null,
        val payloadText: String,
        val isSelfInference: Boolean = false
    ) : ClientPacket()

    @Serializable
    data class AnswerWillQuestion(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val willId: String,
        val answerText: String
    ) : ClientPacket()

    @Serializable
    data class SkipWill(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long
    ) : ClientPacket()

    @Serializable
    data class SendChat(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val recipientId: String? = null,
        val text: String
    ) : ClientPacket()

    @Serializable
    data class SendTyping(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long,
        val recipientId: String? = null,
        val isTyping: Boolean
    ) : ClientPacket()

    @Serializable
    data class Heartbeat(
        override val messageId: String,
        override val sessionId: String,
        override val playerId: String,
        override val timestamp: Long
    ) : ClientPacket()
}

@Serializable
sealed class ServerPacket {
    abstract val messageId: String
    abstract val timestamp: Long

    @Serializable
    data class RoomJoined(
        override val messageId: String,
        override val timestamp: Long,
        val assignedPlayerId: String,
        val sessionToken: String,
        val hostPlayerId: String,
        val view: ClientPlayerViewDto
    ) : ServerPacket()

    @Serializable
    data class StateSync(
        override val messageId: String,
        override val timestamp: Long,
        val view: ClientPlayerViewDto
    ) : ServerPacket()

    @Serializable
    data class GuessResponse(
        override val messageId: String,
        override val timestamp: Long,
        val challengeType: ChallengeType,
        val isCorrect: Boolean,
        val currentMistakes: Int,
        val isEliminated: Boolean,
        val message: String
    ) : ServerPacket()

    @Serializable
    data class WillPublished(
        override val messageId: String,
        override val timestamp: Long,
        val will: WillRecordDto
    ) : ServerPacket()

    @Serializable
    data class ChatDelivered(
        override val messageId: String,
        override val timestamp: Long,
        val message: ChatMessageDto
    ) : ServerPacket()

    @Serializable
    data class TypingDelivered(
        override val messageId: String,
        override val timestamp: Long,
        val typing: TypingIndicatorDto
    ) : ServerPacket()

    @Serializable
    data class Error(
        override val messageId: String,
        override val timestamp: Long,
        val code: String,
        val message: String
    ) : ServerPacket()

    @Serializable
    data class HeartbeatAck(
        override val messageId: String,
        override val timestamp: Long
    ) : ServerPacket()
}
