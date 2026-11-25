package com.detectivegenius.networking.client

import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.protocol.*
import com.detectivegenius.networking.server.ClientConnectionChannel
import com.detectivegenius.networking.server.DetectiveHostServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed class ConnectionStatus {
    data object Disconnected : ConnectionStatus()
    data object Connecting : ConnectionStatus()
    data object Connected : ConnectionStatus()
    data object Reconnecting : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

class DetectiveClient(
    val playerId: String = UUID.randomUUID().toString(),
    var nickname: String = "",
    var avatarId: String = "detective"
) {
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentView = MutableStateFlow<ClientPlayerViewDto?>(null)
    val currentView: StateFlow<ClientPlayerViewDto?> = _currentView.asStateFlow()

    private val _groupChat = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val groupChat: StateFlow<List<ChatMessageDto>> = _groupChat.asStateFlow()

    private val _privateChats = MutableStateFlow<Map<String, List<ChatMessageDto>>>(emptyMap())
    val privateChats: StateFlow<Map<String, List<ChatMessageDto>>> = _privateChats.asStateFlow()

    private val _typingUsers = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val typingUsers: StateFlow<Map<String, Boolean>> = _typingUsers.asStateFlow()

    private val _lastGuessResult = MutableStateFlow<ServerPacket.GuessResponse?>(null)
    val lastGuessResult: StateFlow<ServerPacket.GuessResponse?> = _lastGuessResult.asStateFlow()

    private var sessionToken: String? = null
    private var hostServer: DetectiveHostServer? = null

    // Direct loopback channel (for local host device or in-memory simulation)
    private val loopbackChannel = object : ClientConnectionChannel {
        override fun send(packet: ServerPacket) {
            handleServerPacket(packet)
        }
        override fun close() {
            _connectionStatus.value = ConnectionStatus.Disconnected
        }
    }

    /**
     * Connects this client directly to a local host server running on the same device.
     */
    fun connectDirectToHost(server: DetectiveHostServer, pin: String) {
        this.hostServer = server
        _connectionStatus.value = ConnectionStatus.Connecting

        val joinPacket = ClientPacket.JoinRoom(
            messageId = UUID.randomUUID().toString(),
            sessionId = sessionToken ?: "",
            playerId = playerId,
            timestamp = System.currentTimeMillis(),
            nickname = nickname,
            avatarId = avatarId,
            pin = pin
        )

        val response = server.processPacket(joinPacket, loopbackChannel)
        if (response != null) {
            handleServerPacket(response)
        }
    }

    fun handleServerPacket(packet: ServerPacket) {
        when (packet) {
            is ServerPacket.RoomJoined -> {
                this.sessionToken = packet.sessionToken
                _currentView.value = packet.view
                _connectionStatus.value = ConnectionStatus.Connected
            }
            is ServerPacket.StateSync -> {
                _currentView.value = packet.view
            }
            is ServerPacket.GuessResponse -> {
                _lastGuessResult.value = packet
            }
            is ServerPacket.ChatDelivered -> {
                val msg = packet.message
                if (msg.recipientId == null) {
                    _groupChat.value = _groupChat.value + msg
                } else {
                    val partnerId = if (msg.senderId == playerId) msg.recipientId else msg.senderId
                    val currentPartnerMsgs: List<ChatMessageDto> = _privateChats.value[partnerId] ?: emptyList()
                    val updated: MutableMap<String, List<ChatMessageDto>> = _privateChats.value.toMutableMap()
                    updated[partnerId] = currentPartnerMsgs + msg
                    _privateChats.value = updated
                }
            }
            is ServerPacket.TypingDelivered -> {
                val updated: MutableMap<String, Boolean> = _typingUsers.value.toMutableMap()
                updated[packet.typing.senderId] = packet.typing.isTyping
                _typingUsers.value = updated
            }
            is ServerPacket.Error -> {
                _connectionStatus.value = ConnectionStatus.Error(packet.message)
            }
            is ServerPacket.WillPublished -> {
                // Will published to all players
            }
            is ServerPacket.HeartbeatAck -> {
                // Keep-alive received
            }
        }
    }

    fun setReady(isReady: Boolean) {
        sendPacket(
            ClientPacket.SetReady(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                isReady = isReady
            )
        )
    }

    fun startGame() {
        sendPacket(
            ClientPacket.StartGame(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun submitQuestions(questions: List<String>) {
        sendPacket(
            ClientPacket.SubmitQuestions(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                questions = questions
            )
        )
    }

    fun submitAnswers(answers: Map<String, String>) {
        sendPacket(
            ClientPacket.SubmitAnswers(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                answers = answers
            )
        )
    }

    fun submitGuess(
        challengeType: ChallengeType,
        singleSuspectId: String? = null,
        suspectPair: Set<String>? = null
    ) {
        sendPacket(
            ClientPacket.SubmitGuess(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                challengeType = challengeType,
                singleSuspectId = singleSuspectId,
                suspectPair = suspectPair
            )
        )
    }

    fun sendChat(text: String, recipientId: String? = null) {
        sendPacket(
            ClientPacket.SendChat(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                recipientId = recipientId,
                text = text
            )
        )
    }

    fun sendTyping(isTyping: Boolean, recipientId: String? = null) {
        sendPacket(
            ClientPacket.SendTyping(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                recipientId = recipientId,
                isTyping = isTyping
            )
        )
    }

    fun submitWill(willType: String, targetPlayerId: String? = null, payloadText: String, isSelfInference: Boolean = false) {
        sendPacket(
            ClientPacket.SubmitWill(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                willType = willType,
                targetPlayerId = targetPlayerId,
                payloadText = payloadText,
                isSelfInference = isSelfInference
            )
        )
    }

    fun answerWillQuestion(willId: String, answerText: String) {
        sendPacket(
            ClientPacket.AnswerWillQuestion(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis(),
                willId = willId,
                answerText = answerText
            )
        )
    }

    fun skipWill() {
        sendPacket(
            ClientPacket.SkipWill(
                messageId = UUID.randomUUID().toString(),
                sessionId = sessionToken ?: "",
                playerId = playerId,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun sendPacket(packet: ClientPacket) {
        val server = hostServer
        if (server != null) {
            val response = server.processPacket(packet, loopbackChannel)
            if (response != null) {
                handleServerPacket(response)
            }
        }
    }
}
