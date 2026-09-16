package com.example.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VoiceState {
  IDLE,
  LISTENING,
  PROCESSING,
  SPEAKING,
  ERROR
}

/**
 * Service contract for Voice Assistant capability (speech recognition & TTS).
 * Ready for future speech-to-text / speech synthesis integration.
 */
interface VoiceAssistantService {
  val voiceState: StateFlow<VoiceState>
  suspend fun startListening(): Boolean
  suspend fun stopListening()
  suspend fun speak(text: String)
  suspend fun stopSpeaking()
}

class DefaultVoiceAssistantService : VoiceAssistantService {
  private val _voiceState = MutableStateFlow(VoiceState.IDLE)
  override val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

  override suspend fun startListening(): Boolean {
    // Scaffolded for speech recognition integration in upcoming phases
    _voiceState.value = VoiceState.LISTENING
    return true
  }

  override suspend fun stopListening() {
    _voiceState.value = VoiceState.IDLE
  }

  override suspend fun speak(text: String) {
    _voiceState.value = VoiceState.SPEAKING
  }

  override suspend fun stopSpeaking() {
    _voiceState.value = VoiceState.IDLE
  }
}
