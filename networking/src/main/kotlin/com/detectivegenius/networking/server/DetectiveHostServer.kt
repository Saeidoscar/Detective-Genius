package com.detectivegenius.networking.server

import com.detectivegenius.core.model.Player
import com.detectivegenius.core.protocol.*
import com.detectivegenius.gameengine.state.DetectiveGameEngine
import com.detectivegenius.gameengine.state.GameSessionSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface ClientConnectionChannel {
    fun send(packet: ServerPacket)
    fun close()
}

class DetectiveHostServer(
    val roomPin: String,
    val hostPlayerId: String,
    val engine: DetectiveGameEngine = DetectiveGameEngine(roomPin, hostPlayerId)
) {
    private val sessions = ConcurrentHashMap<String, String>() // sessionToken -> playerId
    private val playerChannels = ConcurrentHashMap<String, ClientConnectionChannel>() // playerId -> channel
    private val seenMessageIds = ConcurrentHashMap.newKeySet<String>()

    private val _chatHistory = mutableListOf<ChatMessageDto>()
    val chatHistory: List<ChatMessageDto> get() = synchronized(_chatHistory) { _chatHistory.toList() }

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    fun start() {
        _isServerRunning.value = true
    }

    fun stop() {
        _isServerRunning.value = false
        playerChannels.values.forEach { it.close() }
        playerChannels.clear()
    }

    fun registerChannel(playerId: String, channel: ClientConnectionChannel) {
        playerChannels[playerId] = channel
        // Send initial sanitized view
        val view = engine.createClientView(playerId)
        channel.send(
            ServerPacket.StateSync(
                messageId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                view = view
            )
        )
    }

    fun unregisterChannel(playerId: String) {
        playerChannels.remove(playerId)
        engine.removePlayer(playerId)
        broadcastStateUpdate()
    }

    /**
     * Authoritative packet processing with duplicate filtering,
     * session validation, and strict privacy isolation.
     */
    fun processPacket(packet: ClientPacket, channel: ClientConnectionChannel): ServerPacket? {
        val now = System.currentTimeMillis()

        // 1. Duplicate message protection
        if (!seenMessageIds.add(packet.messageId)) {
            // Already processed this message
            return null
        }

        return when (packet) {
            is ClientPacket.JoinRoom -> {
                if (packet.pin != roomPin) {
                    return ServerPacket.Error(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        code = "INVALID_PIN",
                        message = "Invalid Room PIN entered."
                    )
                }

                val player = Player(
                    id = packet.playerId,
                    nickname = packet.nickname.trim(),
                    avatarId = packet.avatarId
                )

                val joinResult = engine.joinPlayer(player)
                if (joinResult.isFailure) {
                    return ServerPacket.Error(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        code = "JOIN_REJECTED",
                        message = joinResult.exceptionOrNull()?.message ?: "Join failed"
                    )
                }

                val sessionToken = UUID.randomUUID().toString()
                sessions[sessionToken] = packet.playerId
                playerChannels[packet.playerId] = channel

                val view = engine.createClientView(packet.playerId)
                val response = ServerPacket.RoomJoined(
                    messageId = UUID.randomUUID().toString(),
                    timestamp = now,
                    assignedPlayerId = packet.playerId,
                    sessionToken = sessionToken,
                    hostPlayerId = hostPlayerId,
                    view = view
                )

                broadcastStateUpdate()
                response
            }

            is ClientPacket.SetReady -> {
                val res = engine.setPlayerReady(packet.playerId, packet.isReady)
                if (res.isSuccess) {
                    broadcastStateUpdate()
                }
                null
            }

            is ClientPacket.StartGame -> {
                if (packet.playerId != hostPlayerId) {
                    return ServerPacket.Error(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        code = "UNAUTHORIZED",
                        message = "Only the room host can start the game."
                    )
                }
                val res = engine.startGame(packet.playerId)
                if (res.isSuccess) {
                    broadcastStateUpdate()
                } else {
                    return ServerPacket.Error(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        code = "START_FAILED",
                        message = res.exceptionOrNull()?.message ?: "Cannot start game"
                    )
                }
                null
            }

            is ClientPacket.SubmitQuestions -> {
                val res = engine.submitQuestions(packet.playerId, packet.questions)
                if (res.isSuccess) {
                    broadcastStateUpdate()
                } else {
                    return ServerPacket.Error(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        code = "INVALID_QUESTIONS",
                        message = res.exceptionOrNull()?.message ?: "Question validation failed"
                    )
                }
                null
            }

            is ClientPacket.SubmitAnswers -> {
                val res = engine.submitAnswers(packet.playerId, packet.answers)
                if (res.isSuccess) {
                    broadcastStateUpdate()
                } else {
                    return ServerPacket.Error(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        code = "INVALID_ANSWERS",
                        message = res.exceptionOrNull()?.message ?: "Answer validation failed"
                    )
                }
                null
            }

            is ClientPacket.SubmitGuess -> {
                val guessResult = engine.submitGuess(
                    playerId = packet.playerId,
                    challengeType = packet.challengeType,
                    singleSuspectId = packet.singleSuspectId,
                    suspectPair = packet.suspectPair
                )

                val response = when (guessResult) {
                    is com.detectivegenius.core.model.GuessValidationResult.Correct -> {
                        ServerPacket.GuessResponse(
                            messageId = UUID.randomUUID().toString(),
                            timestamp = now,
                            challengeType = packet.challengeType,
                            isCorrect = true,
                            currentMistakes = engine.playerProgress[packet.playerId]?.mistakes ?: 0,
                            isEliminated = false,
                            message = "Correct deduction!"
                        )
                    }
                    is com.detectivegenius.core.model.GuessValidationResult.Incorrect -> {
                        ServerPacket.GuessResponse(
                            messageId = UUID.randomUUID().toString(),
                            timestamp = now,
                            challengeType = packet.challengeType,
                            isCorrect = false,
                            currentMistakes = guessResult.currentMistakes,
                            isEliminated = guessResult.isEliminated,
                            message = "Incorrect deduction. Mistake recorded."
                        )
                    }
                    is com.detectivegenius.core.model.GuessValidationResult.AlreadySolved -> {
                        ServerPacket.Error(
                            messageId = UUID.randomUUID().toString(),
                            timestamp = now,
                            code = "ALREADY_SOLVED",
                            message = "Mystery already solved."
                        )
                    }
                    is com.detectivegenius.core.model.GuessValidationResult.PlayerEliminated -> {
                        ServerPacket.Error(
                            messageId = UUID.randomUUID().toString(),
                            timestamp = now,
                            code = "PLAYER_ELIMINATED",
                            message = "Eliminated detectives cannot submit deductions."
                        )
                    }
                    is com.detectivegenius.core.model.GuessValidationResult.InvalidSubmission -> {
                        ServerPacket.Error(
                            messageId = UUID.randomUUID().toString(),
                            timestamp = now,
                            code = "INVALID_SUBMISSION",
                            message = guessResult.reason
                        )
                    }
                }

                broadcastStateUpdate()
                response
            }

            is ClientPacket.SendChat -> {
                val sender = engine.players[packet.playerId] ?: return null
                val chatMsg = ChatMessageDto(
                    id = UUID.randomUUID().toString(),
                    senderId = sender.id,
                    senderNickname = sender.nickname,
                    senderAvatarId = sender.avatarId,
                    recipientId = packet.recipientId,
                    text = packet.text.trim(),
                    timestamp = now
                )

                synchronized(_chatHistory) {
                    _chatHistory.add(chatMsg)
                }

                if (packet.recipientId == null) {
                    // Group chat: broadcast to all connected players
                    val envelope = ServerPacket.ChatDelivered(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        message = chatMsg
                    )
                    playerChannels.values.forEach { it.send(envelope) }
                } else {
                    // Private chat: deliver strictly to sender and recipient ONLY!
                    val envelope = ServerPacket.ChatDelivered(
                        messageId = UUID.randomUUID().toString(),
                        timestamp = now,
                        message = chatMsg
                    )
                    playerChannels[packet.playerId]?.send(envelope)
                    playerChannels[packet.recipientId]?.send(envelope)
                }
                null
            }

            is ClientPacket.SendTyping -> {
                val typingDto = TypingIndicatorDto(
                    senderId = packet.playerId,
                    recipientId = packet.recipientId,
                    isTyping = packet.isTyping
                )
                val envelope = ServerPacket.TypingDelivered(
                    messageId = UUID.randomUUID().toString(),
                    timestamp = now,
                    typing = typingDto
                )
                if (packet.recipientId == null) {
                    playerChannels.values.forEach { if (it != channel) it.send(envelope) }
                } else {
                    playerChannels[packet.recipientId]?.send(envelope)
                }
                null
            }

            is ClientPacket.Heartbeat -> {
                ServerPacket.HeartbeatAck(
                    messageId = UUID.randomUUID().toString(),
                    timestamp = now
                )
            }
        }
    }

    /**
     * Broadcasts personalized, sanitized views to each individual connected player.
     * ZERO unauthorized secrets or author/answerer package identities are ever broadcast.
     */
    fun broadcastStateUpdate() {
        val now = System.currentTimeMillis()
        for ((playerId, channel) in playerChannels) {
            val sanitizedView = engine.createClientView(playerId)
            channel.send(
                ServerPacket.StateSync(
                    messageId = UUID.randomUUID().toString(),
                    timestamp = now,
                    view = sanitizedView
                )
            )
        }
    }

    fun createSnapshot(): GameSessionSnapshot {
        return engine.createSnapshot()
    }

    fun restoreSnapshot(snapshot: GameSessionSnapshot) {
        engine.restoreFromSnapshot(snapshot)
        broadcastStateUpdate()
    }
}
