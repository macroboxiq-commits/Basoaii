package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AppLanguage
import com.example.domain.model.VoiceAssistantState
import com.example.domain.model.VoiceLanguage
import com.example.domain.model.VoiceSettings
import com.example.services.AndroidSpeechManager
import com.example.services.AndroidSpeechRecognitionService
import com.example.services.RecognitionStatus
import com.example.services.SpeechManager
import com.example.services.SpeechRecognitionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VoiceViewModel(
  application: Application,
  private val speechRecognitionService: SpeechRecognitionService,
  private val speechManager: SpeechManager
) : AndroidViewModel(application) {

  constructor(application: Application) : this(
    application = application,
    speechRecognitionService = AndroidSpeechRecognitionService(application),
    speechManager = AndroidSpeechManager(application)
  )

  private val _assistantState = MutableStateFlow(VoiceAssistantState.IDLE)
  val assistantState: StateFlow<VoiceAssistantState> = _assistantState.asStateFlow()

  val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking

  val soundLevel: StateFlow<Float> = speechRecognitionService.soundLevel

  private val _recognizedText = MutableStateFlow("")
  val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _isVoiceModeActive = MutableStateFlow(false)
  val isVoiceModeActive: StateFlow<Boolean> = _isVoiceModeActive.asStateFlow()

  init {
    // Monitor recognition status
    viewModelScope.launch {
      speechRecognitionService.recognitionStatus.collect { status ->
        when (status) {
          RecognitionStatus.LISTENING -> _assistantState.value = VoiceAssistantState.LISTENING
          RecognitionStatus.PROCESSING -> _assistantState.value = VoiceAssistantState.PROCESSING
          RecognitionStatus.ERROR -> _assistantState.value = VoiceAssistantState.ERROR
          RecognitionStatus.IDLE -> {
            if (_assistantState.value == VoiceAssistantState.LISTENING || _assistantState.value == VoiceAssistantState.PROCESSING) {
              _assistantState.value = VoiceAssistantState.IDLE
            }
          }
        }
      }
    }

    // Monitor speaking status
    viewModelScope.launch {
      speechManager.isSpeaking.collect { speaking ->
        if (speaking) {
          _assistantState.value = VoiceAssistantState.SPEAKING
        } else if (_assistantState.value == VoiceAssistantState.SPEAKING) {
          _assistantState.value = VoiceAssistantState.IDLE
        }
      }
    }
  }

  fun startListening(
    voiceSettings: VoiceSettings,
    appLanguage: AppLanguage,
    onResult: (String) -> Unit
  ) {
    if (!voiceSettings.voiceInputEnabled) {
      _errorMessage.value = "تکایە سەرەتا هاتنەناوەی دەنگ لە ڕێکخستنەکان چالاک بکە."
      return
    }

    // If already speaking, stop TTS first
    speechManager.stop()
    _errorMessage.value = null
    _recognizedText.value = ""

    speechRecognitionService.startListening(
      voiceLanguage = voiceSettings.voiceLanguage,
      appLanguage = appLanguage,
      onResult = { text ->
        _recognizedText.value = text
        _assistantState.value = VoiceAssistantState.IDLE
        onResult(text)
      },
      onError = { error ->
        _errorMessage.value = error
        _assistantState.value = VoiceAssistantState.ERROR
      }
    )
  }

  fun stopListening() {
    speechRecognitionService.stopListening()
  }

  fun cancelListening() {
    speechRecognitionService.cancel()
    _assistantState.value = VoiceAssistantState.IDLE
  }

  fun speakResponse(
    text: String,
    voiceSettings: VoiceSettings,
    appLanguage: AppLanguage
  ) {
    if (!voiceSettings.speakAiResponses) return

    _assistantState.value = VoiceAssistantState.SPEAKING
    speechManager.speak(
      text = text,
      voiceLanguage = voiceSettings.voiceLanguage,
      appLanguage = appLanguage,
      speechRate = voiceSettings.speechRate.rate,
      pitch = voiceSettings.speechPitch,
      onStart = {
        _assistantState.value = VoiceAssistantState.SPEAKING
      },
      onDone = {
        _assistantState.value = VoiceAssistantState.IDLE
      },
      onError = { error ->
        _errorMessage.value = error
        _assistantState.value = VoiceAssistantState.IDLE
      }
    )
  }

  fun stopSpeaking() {
    speechManager.stop()
    _assistantState.value = VoiceAssistantState.IDLE
  }

  fun openVoiceMode() {
    _isVoiceModeActive.value = true
  }

  fun closeVoiceMode() {
    speechRecognitionService.cancel()
    speechManager.stop()
    _isVoiceModeActive.value = false
    _assistantState.value = VoiceAssistantState.IDLE
  }

  fun clearError() {
    _errorMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    speechRecognitionService.destroy()
    speechManager.shutdown()
  }
}
