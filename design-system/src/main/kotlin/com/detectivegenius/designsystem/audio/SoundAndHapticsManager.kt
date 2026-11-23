package com.detectivegenius.designsystem.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SoundCue {
    BUTTON_CLICK,
    CARD_SELECT,
    PACKAGE_DELIVERY,
    CORRECT_ANSWER,
    INCORRECT_ANSWER,
    ELIMINATION,
    VICTORY
}

enum class HapticPattern {
    LIGHT_CLICK,
    SELECTION_TICK,
    SUCCESS_DOUBLE,
    ERROR_BUZZ,
    HEAVY_ELIMINATION
}

class SoundAndHapticsManager {
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        _musicEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }

    fun playCue(cue: SoundCue) {
        if (!_soundEnabled.value) return
        // Offline bundled audio trigger
    }

    fun triggerHaptic(pattern: HapticPattern) {
        if (!_hapticsEnabled.value) return
        // Android vibrator pattern trigger
    }
}
