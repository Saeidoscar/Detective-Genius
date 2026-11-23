package com.detectivegenius.viewmodel

import androidx.lifecycle.ViewModel
import com.detectivegenius.core.model.ChallengeType
import com.detectivegenius.core.model.GamePhase
import com.detectivegenius.core.protocol.ChatMessageDto
import com.detectivegenius.core.protocol.ClientPlayerViewDto
import com.detectivegenius.designsystem.audio.HapticPattern
import com.detectivegenius.designsystem.audio.SoundAndHapticsManager
import com.detectivegenius.designsystem.audio.SoundCue
import com.detectivegenius.networking.client.ConnectionStatus
import com.detectivegenius.networking.client.DetectiveClient
import com.detectivegenius.networking.discovery.LocalDiscoveryManager
import com.detectivegenius.networking.server.DetectiveHostServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class GameViewModel(
    val soundAndHapticsManager: SoundAndHapticsManager = SoundAndHapticsManager(),
    val discoveryManager: LocalDiscoveryManager = LocalDiscoveryManager()
) : ViewModel() {

    private var hostServer: DetectiveHostServer? = null
    val client: DetectiveClient = DetectiveClient()

    // Preferences / Settings
    val isDarkTheme = MutableStateFlow(true)
    val currentLanguage = MutableStateFlow("en") // "en" or "fa"
    val reducedMotion = MutableStateFlow(false)

    // Current Room / Game State
    val clientView: StateFlow<ClientPlayerViewDto?> = client.currentView
    val connectionStatus: StateFlow<ConnectionStatus> = client.connectionStatus
    val groupChat: StateFlow<List<ChatMessageDto>> = client.groupChat
    val privateChats: StateFlow<Map<String, List<ChatMessageDto>>> = client.privateChats

    // UI Local State
    val selectedAvatarId = MutableStateFlow("detective")
    val nickname = MutableStateFlow("Sherlock")
    val enteredPin = MutableStateFlow("")
    val enteredHostIp = MutableStateFlow("192.168.1.100")

    // Question Creation Input
    val questionInputs = MutableStateFlow(listOf("", "", ""))
    val questionError = MutableStateFlow<String?>(null)

    // Answering Input
    val answerInputs = MutableStateFlow<Map<String, String>>(emptyMap())
    val answerError = MutableStateFlow<String?>(null)

    // Mystery Board Deduction Selection
    val activeChallengeForDeduction = MutableStateFlow<ChallengeType?>(null)
    val selectedSuspectSingle = MutableStateFlow<String?>(null)
    val selectedSuspectPair = MutableStateFlow<Set<String>>(emptySet())
    val deductionFeedback = MutableStateFlow<String?>(null)

    // Chat Active Tab & Partner
    val activeChatTab = MutableStateFlow(0) // 0: Group, 1: Private
    val activePrivatePartnerId = MutableStateFlow<String?>(null)
    val chatInputText = MutableStateFlow("")

    fun hostRoom(pin: String) {
        soundAndHapticsManager.playCue(SoundCue.BUTTON_CLICK)
        val hostId = client.playerId
        client.nickname = nickname.value
        client.avatarId = selectedAvatarId.value

        val server = DetectiveHostServer(roomPin = pin, hostPlayerId = hostId)
        server.start()
        this.hostServer = server

        client.connectDirectToHost(server, pin)
    }

    fun joinRoom(hostIp: String, pin: String) {
        soundAndHapticsManager.playCue(SoundCue.BUTTON_CLICK)
        client.nickname = nickname.value
        client.avatarId = selectedAvatarId.value

        val (valid, error) = discoveryManager.validateConnectionInput(hostIp, pin)
        if (!valid) {
            deductionFeedback.value = error
            return
        }

        // For local loopback test / same process
        hostServer?.let { server ->
            client.connectDirectToHost(server, pin)
        }
    }

    fun setReady(isReady: Boolean) {
        soundAndHapticsManager.triggerHaptic(HapticPattern.SELECTION_TICK)
        client.setReady(isReady)
    }

    fun startGame() {
        soundAndHapticsManager.playCue(SoundCue.BUTTON_CLICK)
        client.startGame()
    }

    fun updateQuestionInput(index: Int, text: String) {
        val updated = questionInputs.value.toMutableList()
        updated[index] = text
        questionInputs.value = updated
        questionError.value = null
    }

    fun submitQuestions() {
        soundAndHapticsManager.playCue(SoundCue.PACKAGE_DELIVERY)
        client.submitQuestions(questionInputs.value)
    }

    fun updateAnswerInput(questionId: String, text: String) {
        val updated = answerInputs.value.toMutableMap()
        updated[questionId] = text
        answerInputs.value = updated
        answerError.value = null
    }

    fun submitAnswers() {
        soundAndHapticsManager.playCue(SoundCue.PACKAGE_DELIVERY)
        client.submitAnswers(answerInputs.value)
    }

    fun openDeductionDialog(challengeType: ChallengeType) {
        soundAndHapticsManager.playCue(SoundCue.CARD_SELECT)
        activeChallengeForDeduction.value = challengeType
        selectedSuspectSingle.value = null
        selectedSuspectPair.value = emptySet()
        deductionFeedback.value = null
    }

    fun closeDeductionDialog() {
        activeChallengeForDeduction.value = null
    }

    fun toggleSuspectSelection(suspectId: String) {
        soundAndHapticsManager.triggerHaptic(HapticPattern.SELECTION_TICK)
        val currentType = activeChallengeForDeduction.value ?: return

        if (!currentType.isSetBased) {
            selectedSuspectSingle.value = suspectId
        } else {
            val currentPair = selectedSuspectPair.value
            if (currentPair.contains(suspectId)) {
                selectedSuspectPair.value = currentPair - suspectId
            } else {
                if (currentPair.size < 2) {
                    selectedSuspectPair.value = currentPair + suspectId
                }
            }
        }
    }

    fun submitDeduction() {
        val challenge = activeChallengeForDeduction.value ?: return
        if (!challenge.isSetBased) {
            val suspect = selectedSuspectSingle.value
            if (suspect == null) {
                deductionFeedback.value = "Please select one suspect."
                return
            }
            client.submitGuess(challengeType = challenge, singleSuspectId = suspect)
        } else {
            val pair = selectedSuspectPair.value
            if (pair.size != 2) {
                deductionFeedback.value = "Please select exactly two distinct suspects."
                return
            }
            client.submitGuess(challengeType = challenge, suspectPair = pair)
        }

        closeDeductionDialog()
    }

    fun sendChatMessage() {
        val text = chatInputText.value.trim()
        if (text.isBlank()) return

        soundAndHapticsManager.triggerHaptic(HapticPattern.LIGHT_CLICK)
        val recipient = if (activeChatTab.value == 1) activePrivatePartnerId.value else null
        client.sendChat(text, recipient)
        chatInputText.value = ""
    }
}
