package com.example.data.repository

import com.example.domain.model.AppLanguage
import com.example.domain.model.SettingsState
import com.example.domain.model.SpeechRate
import com.example.domain.model.VoiceLanguage
import com.example.domain.model.VoiceSettings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl : SettingsRepository {

  private val _settings = MutableStateFlow(SettingsState())
  override val settings: Flow<SettingsState> = _settings.asStateFlow()

  override suspend fun setLanguage(language: AppLanguage) {
    _settings.update { it.copy(language = language) }
  }

  override suspend fun setDarkTheme(enabled: Boolean) {
    _settings.update { it.copy(isDarkTheme = enabled) }
  }

  override suspend fun updateVoiceSettings(voiceSettings: VoiceSettings) {
    _settings.update { it.copy(voiceSettings = voiceSettings) }
  }

  override suspend fun setVoiceInputEnabled(enabled: Boolean) {
    _settings.update { it.copy(voiceSettings = it.voiceSettings.copy(voiceInputEnabled = enabled)) }
  }

  override suspend fun setAutoSend(enabled: Boolean) {
    _settings.update { it.copy(voiceSettings = it.voiceSettings.copy(autoSend = enabled)) }
  }

  override suspend fun setSpeakAiResponses(enabled: Boolean) {
    _settings.update { it.copy(voiceSettings = it.voiceSettings.copy(speakAiResponses = enabled)) }
  }

  override suspend fun setSpeechRate(rate: SpeechRate) {
    _settings.update { it.copy(voiceSettings = it.voiceSettings.copy(speechRate = rate)) }
  }

  override suspend fun setVoiceLanguage(language: VoiceLanguage) {
    _settings.update { it.copy(voiceSettings = it.voiceSettings.copy(voiceLanguage = language)) }
  }

  override suspend fun setMemoryEnabled(enabled: Boolean) {
    _settings.update { it.copy(memoryEnabled = enabled) }
  }

  override suspend fun updatePermissions(mic: Boolean, storage: Boolean) {
    _settings.update {
      it.copy(
        microphonePermissionGranted = mic,
        storagePermissionGranted = storage
      )
    }
  }

  override suspend fun updateApiKeyStatus(isConfigured: Boolean, maskedKey: String) {
    _settings.update {
      it.copy(
        isGeminiKeyConfigured = isConfigured,
        maskedApiKey = maskedKey
      )
    }
  }

  override suspend fun setApiKeyTesting(isTesting: Boolean, feedback: String?, success: Boolean?) {
    _settings.update {
      it.copy(
        isTestingApiKey = isTesting,
        apiKeyFeedbackMessage = feedback,
        isApiKeyTestSuccess = success
      )
    }
  }
}
