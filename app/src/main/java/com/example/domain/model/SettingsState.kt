package com.example.domain.model

data class SettingsState(
  val language: AppLanguage = AppLanguage.DEFAULT,
  val isDarkTheme: Boolean = true,
  val voiceSettings: VoiceSettings = VoiceSettings(),
  val aiModelName: String = "Gemini 3.5 Flash",
  val memoryEnabled: Boolean = true,
  val microphonePermissionGranted: Boolean = false,
  val storagePermissionGranted: Boolean = false,
  val isGeminiKeyConfigured: Boolean = false,
  val maskedApiKey: String = "",
  val isTestingApiKey: Boolean = false,
  val apiKeyFeedbackMessage: String? = null,
  val isApiKeyTestSuccess: Boolean? = null
)
