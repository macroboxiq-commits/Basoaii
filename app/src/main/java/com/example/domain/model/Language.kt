package com.example.domain.model

enum class AppLanguage(
  val code: String,
  val nativeName: String,
  val englishName: String,
  val isRtl: Boolean
) {
  KURDISH_SORANI(
    code = "ckb",
    nativeName = "کوردی (سۆرانی)",
    englishName = "Kurdish (Sorani)",
    isRtl = true
  ),
  ARABIC(
    code = "ar",
    nativeName = "العربية",
    englishName = "Arabic",
    isRtl = true
  ),
  ENGLISH(
    code = "en",
    nativeName = "English",
    englishName = "English",
    isRtl = false
  );

  companion object {
    val DEFAULT = KURDISH_SORANI

    fun fromCode(code: String): AppLanguage {
      return entries.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
    }
  }
}
