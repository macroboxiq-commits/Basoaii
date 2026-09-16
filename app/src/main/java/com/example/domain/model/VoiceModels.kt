package com.example.domain.model

enum class SpeechRate(val rate: Float, val labelKurdish: String, val labelArabic: String, val labelEnglish: String) {
  SLOW(0.8f, "هێواش", "بطيء", "Slow"),
  NORMAL(1.0f, "ئاسایی", "عادي", "Normal"),
  FAST(1.3f, "خێرا", "سريع", "Fast")
}

enum class VoiceLanguage(val code: String, val nativeName: String, val englishName: String) {
  KURDISH_SORANI("ckb-IQ", "کوردی (سۆرانی)", "Kurdish (Sorani)"),
  ARABIC("ar-IQ", "العربية", "Arabic"),
  ENGLISH("en-US", "English", "English"),
  AUTO("auto", "خۆکار (Auto)", "Auto Detect")
}

enum class VoiceAssistantState {
  IDLE,
  LISTENING,
  PROCESSING,
  THINKING,
  SPEAKING,
  ERROR
}

data class VoiceSettings(
  val voiceInputEnabled: Boolean = true,
  val autoSend: Boolean = true,
  val speakAiResponses: Boolean = true,
  val speechRate: SpeechRate = SpeechRate.NORMAL,
  val voiceLanguage: VoiceLanguage = VoiceLanguage.KURDISH_SORANI,
  val speechPitch: Float = 1.0f
)
