package com.example.domain.repository

import com.example.domain.model.AppLanguage
import com.example.domain.model.SettingsState
import com.example.domain.model.SpeechRate
import com.example.domain.model.VoiceLanguage
import com.example.domain.model.VoiceSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  val settings: Flow<SettingsState>

  suspend fun setLanguage(language: AppLanguage)
  suspend fun setDarkTheme(enabled: Boolean)
  suspend fun updateVoiceSettings(voiceSettings: VoiceSettings)
  suspend fun setVoiceInputEnabled(enabled: Boolean)
  suspend fun setAutoSend(enabled: Boolean)
  suspend fun setSpeakAiResponses(enabled: Boolean)
  suspend fun setSpeechRate(rate: SpeechRate)
  suspend fun setVoiceLanguage(language: VoiceLanguage)
  suspend fun setMemoryEnabled(enabled: Boolean)
  suspend fun updatePermissions(mic: Boolean, storage: Boolean)
  suspend fun updateApiKeyStatus(isConfigured: Boolean, maskedKey: String)
  suspend fun setApiKeyTesting(isTesting: Boolean, feedback: String?, success: Boolean?)
}
