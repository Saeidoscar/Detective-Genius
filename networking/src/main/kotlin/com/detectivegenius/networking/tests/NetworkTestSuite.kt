package com.detectivegenius.networking.tests

import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.model.GamePhase
import com.detectivegenius.core.protocol.ServerPacket
import com.detectivegenius.networking.client.DetectiveClient
import com.detectivegenius.networking.discovery.LocalDiscoveryManager
import com.detectivegenius.networking.server.DetectiveHostServer
import java.util.UUID

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
    println("=== STARTING DETECTIVE GENIUS NETWORKING TEST SUITE ===")

    testDiscoveryAndQrCode()
    println("✓ testDiscoveryAndQrCode passed")

    testMultiClientMultiplayerAndStateSync()
    println("✓ testMultiClientMultiplayerAndStateSync passed")

    testPrivateChatStrictIsolation()
    println("✓ testPrivateChatStrictIsolation passed")

    testDuplicateMessageProtection()
    println("✓ testDuplicateMessageProtection passed")

    println("=== ALL NETWORKING TESTS PASSED SUCCESSFULLY! ===")
}

fun testDiscoveryAndQrCode() {
    val discovery = LocalDiscoveryManager()
    val qr = discovery.createQrPayload("192.168.1.105", 8080, "4821")
    assert(qr.contains("192.168.1.105"), "QR payload must contain host IP")
    assert(qr.contains("4821"), "QR payload must contain room PIN")

    val parsed = discovery.parseQrPayload(qr)
    assert(parsed != null, "QR parser should parse valid payload")
    assertEquals("192.168.1.105", parsed!!.first)
    assertEquals(8080, parsed.second)
    assertEquals("4821", parsed.third)

    // Validation
    val (validIp, _) = discovery.validateConnectionInput("192.168.4.1", "1234")
    assert(validIp, "Valid IP and PIN should pass validation")

    val (invalidIp, _) = discovery.validateConnectionInput("999.1.1.1", "1234")
    assert(!invalidIp, "Invalid IP should fail validation")

    val (invalidPin, _) = discovery.validateConnectionInput("192.168.1.1", "12")
    assert(!invalidPin, "PIN with < 4 digits should fail validation")
}

fun testMultiClientMultiplayerAndStateSync() {
    val pin = "7733"
    val hostPlayerId = "player_host"
    val server = DetectiveHostServer(roomPin = pin, hostPlayerId = hostPlayerId)
    server.start()

    val hostClient = DetectiveClient(playerId = hostPlayerId, nickname = "Sherlock", avatarId = "detective")
    val p2Client = DetectiveClient(playerId = "player_2", nickname = "Watson", avatarId = "owl")
    val p3Client = DetectiveClient(playerId = "player_3", nickname = "Irene", avatarId = "cat")
    val p4Client = DetectiveClient(playerId = "player_4", nickname = "Moriarty", avatarId = "phantom")

    // Connect all 4
    hostClient.connectDirectToHost(server, pin)
    p2Client.connectDirectToHost(server, pin)
    p3Client.connectDirectToHost(server, pin)
    p4Client.connectDirectToHost(server, pin)

    assertEquals(4, hostClient.currentView.value?.players?.size)
    assertEquals(4, p2Client.currentView.value?.players?.size)
    assertEquals(GamePhase.LOBBY, hostClient.currentView.value?.phase)

    // Ready all
    hostClient.setReady(true)
    p2Client.setReady(true)
    p3Client.setReady(true)
    p4Client.setReady(true)

    // Non-host attempts to start game -> Should fail
    p2Client.startGame()
    assertEquals(GamePhase.LOBBY, hostClient.currentView.value?.phase)

    // Host starts game
    hostClient.startGame()
    assertEquals(GamePhase.QUESTION_CREATION, hostClient.currentView.value?.phase)
    assertEquals(GamePhase.QUESTION_CREATION, p4Client.currentView.value?.phase)

    // Submit questions
    val clients = listOf(hostClient, p2Client, p3Client, p4Client)
    clients.forEach { c ->
        c.submitQuestions(listOf("Q1 by ${c.nickname}?", "Q2 by ${c.nickname}?", "Q3 by ${c.nickname}?"))
    }

    assertEquals(GamePhase.ANSWERING, hostClient.currentView.value?.phase)

    // Submit answers
    clients.forEach { c ->
        val assigned = c.currentView.value!!.assignedQuestionsToAnswer
        assertEquals(3, assigned.size)
        val ansMap = assigned.associate { it.id to "Ans from ${c.nickname}" }
        c.submitAnswers(ansMap)
    }

    assertEquals(GamePhase.INVESTIGATION, hostClient.currentView.value?.phase)
    clients.forEach { c ->
        assertEquals(2, c.currentView.value!!.receivedPackages.size)
    }
}

fun testPrivateChatStrictIsolation() {
    val pin = "9999"
    val server = DetectiveHostServer(roomPin = pin, hostPlayerId = "p1")
    server.start()

    val p1 = DetectiveClient(playerId = "p1", nickname = "Player 1", avatarId = "detective")
    val p2 = DetectiveClient(playerId = "p2", nickname = "Player 2", avatarId = "owl")
    val p3 = DetectiveClient(playerId = "p3", nickname = "Player 3", avatarId = "fox")

    p1.connectDirectToHost(server, pin)
    p2.connectDirectToHost(server, pin)
    p3.connectDirectToHost(server, pin)

    // 1. Group Chat
    p1.sendChat("Hello everyone in room!", recipientId = null)
    assertEquals(1, p1.groupChat.value.size)
    assertEquals(1, p2.groupChat.value.size)
    assertEquals(1, p3.groupChat.value.size)

    // 2. Private Chat between p1 and p2
    val secretMessage = "Hey p2, I think you answered my question!"
    p1.sendChat(secretMessage, recipientId = "p2")

    // p1 and p2 should have received the private message
    assertEquals(1, p1.privateChats.value["p2"]?.size)
    assertEquals(1, p2.privateChats.value["p1"]?.size)
    assertEquals(secretMessage, p2.privateChats.value["p1"]?.first()?.text)

    // CRITICAL SECURITY INVARIANT: p3 MUST NEVER RECEIVE THIS MESSAGE!
    val p3PrivateMap = p3.privateChats.value
    assert(p3PrivateMap.isEmpty(), "Player 3 must NOT have any private chats delivered!")
    assert(p3.groupChat.value.none { it.text == secretMessage }, "Secret message must not be in group chat!")
}

fun testDuplicateMessageProtection() {
    val pin = "5555"
    val server = DetectiveHostServer(roomPin = pin, hostPlayerId = "host")
    val client = DetectiveClient(playerId = "host", nickname = "Host", avatarId = "detective")
    client.connectDirectToHost(server, pin)

    val msgId = UUID.randomUUID().toString()
    val chatPacket = com.detectivegenius.core.protocol.ClientPacket.SendChat(
        messageId = msgId,
        sessionId = "",
        playerId = "host",
        timestamp = System.currentTimeMillis(),
        recipientId = null,
        text = "Idempotent packet test"
    )

    // Send first time
    server.processPacket(chatPacket, object : com.detectivegenius.networking.server.ClientConnectionChannel {
        override fun send(packet: ServerPacket) {}
        override fun close() {}
    })
    assertEquals(1, server.chatHistory.size)

    // Send second time with same messageId
    val duplicateResult = server.processPacket(chatPacket, object : com.detectivegenius.networking.server.ClientConnectionChannel {
        override fun send(packet: ServerPacket) {}
        override fun close() {}
    })
    assert(duplicateResult == null, "Duplicate messageId should be ignored and dropped")
    assertEquals(1, server.chatHistory.size, "Chat history must not contain duplicate messages")
}
