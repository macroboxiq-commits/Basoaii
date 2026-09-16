package com.example.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepositoryImpl
import com.example.domain.model.AppLanguage
import com.example.domain.model.SettingsState
import com.example.domain.model.SpeechRate
import com.example.domain.model.VoiceLanguage
import com.example.domain.repository.SettingsRepository
import com.example.services.CreativeApiClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val repository: SettingsRepository
) : ViewModel() {

  constructor() : this(SettingsRepositoryImpl())

  init {
    refreshApiKeyStatus()
  }

  val settingsState: StateFlow<SettingsState> = repository.settings
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = SettingsState()
    )

  fun refreshApiKeyStatus(context: Context? = null) {
    if (context != null) {
      CreativeApiClient.initialize(context)
    }
    viewModelScope.launch {
      repository.updateApiKeyStatus(
        isConfigured = CreativeApiClient.isKeyConfigured(),
        maskedKey = CreativeApiClient.getMaskedApiKey()
      )
    }
  }

  fun saveAndActivateApiKey(
    context: Context,
    key: String,
    onComplete: ((Boolean, String) -> Unit)? = null
  ) {
    val clean = key.trim()
    if (clean.isBlank()) {
      onComplete?.invoke(false, "API key cannot be empty")
      return
    }

    viewModelScope.launch {
      repository.setApiKeyTesting(isTesting = true, feedback = null, success = null)
      val (isValid, message) = CreativeApiClient.testApiKey(clean)
      if (isValid) {
        CreativeApiClient.setCustomApiKey(context, clean)
        repository.updateApiKeyStatus(
          isConfigured = true,
          maskedKey = CreativeApiClient.getMaskedApiKey()
        )
        repository.setApiKeyTesting(
          isTesting = false,
          feedback = message,
          success = true
        )
        onComplete?.invoke(true, message)
      } else {
        repository.setApiKeyTesting(
          isTesting = false,
          feedback = message,
          success = false
        )
        onComplete?.invoke(false, message)
      }
    }
  }

  fun testApiKey(key: String, onComplete: ((Boolean, String) -> Unit)? = null) {
    val clean = key.trim().ifEmpty { CreativeApiClient.getApiKey() }
    viewModelScope.launch {
      repository.setApiKeyTesting(isTesting = true, feedback = null, success = null)
      val (isValid, message) = CreativeApiClient.testApiKey(clean)
      repository.setApiKeyTesting(
        isTesting = false,
        feedback = message,
        success = isValid
      )
      onComplete?.invoke(isValid, message)
    }
  }

  fun removeApiKey(context: Context) {
    CreativeApiClient.setCustomApiKey(context, "")
    viewModelScope.launch {
      repository.updateApiKeyStatus(
        isConfigured = CreativeApiClient.isKeyConfigured(),
        maskedKey = CreativeApiClient.getMaskedApiKey()
      )
      repository.setApiKeyTesting(
        isTesting = false,
        feedback = null,
        success = null
      )
    }
  }

  fun clearApiKeyFeedback() {
    viewModelScope.launch {
      repository.setApiKeyTesting(isTesting = false, feedback = null, success = null)
    }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      repository.setLanguage(language)
    }
  }

  fun setDarkTheme(enabled: Boolean) {
    viewModelScope.launch {
      repository.setDarkTheme(enabled)
    }
  }

  fun setVoiceInputEnabled(enabled: Boolean) {
    viewModelScope.launch {
      repository.setVoiceInputEnabled(enabled)
    }
  }

  fun setAutoSend(enabled: Boolean) {
    viewModelScope.launch {
      repository.setAutoSend(enabled)
    }
  }

  fun setSpeakAiResponses(enabled: Boolean) {
    viewModelScope.launch {
      repository.setSpeakAiResponses(enabled)
    }
  }

  fun setSpeechRate(rate: SpeechRate) {
    viewModelScope.launch {
      repository.setSpeechRate(rate)
    }
  }

  fun setVoiceLanguage(language: VoiceLanguage) {
    viewModelScope.launch {
      repository.setVoiceLanguage(language)
    }
  }

  fun setMemoryEnabled(enabled: Boolean) {
    viewModelScope.launch {
      repository.setMemoryEnabled(enabled)
    }
  }

  fun updatePermissions(mic: Boolean, storage: Boolean) {
    viewModelScope.launch {
      repository.updatePermissions(mic, storage)
    }
  }
}
