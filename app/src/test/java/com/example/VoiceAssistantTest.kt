package com.example

import com.example.domain.model.AppLanguage
import com.example.domain.model.SpeechRate
import com.example.domain.model.VoiceAssistantState
import com.example.domain.model.VoiceLanguage
import com.example.domain.model.VoiceSettings
import com.example.services.AndroidSpeechManager
import com.example.ui.localization.LocalizedStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceAssistantTest {

  @Test
  fun testVoiceLanguageLocales() {
    assertEquals("ckb-IQ", VoiceLanguage.KURDISH_SORANI.code)
    assertEquals("ar-IQ", VoiceLanguage.ARABIC.code)
    assertEquals("en-US", VoiceLanguage.ENGLISH.code)
  }

  @Test
  fun testSpeechRateFactors() {
    assertEquals(0.8f, SpeechRate.SLOW.rate)
    assertEquals(1.0f, SpeechRate.NORMAL.rate)
    assertEquals(1.3f, SpeechRate.FAST.rate)
  }

  @Test
  fun testVoiceSettingsDefaults() {
    val defaultSettings = VoiceSettings()
    assertTrue(defaultSettings.voiceInputEnabled)
    assertTrue(defaultSettings.autoSend)
    assertTrue(defaultSettings.speakAiResponses)
    assertEquals(SpeechRate.NORMAL, defaultSettings.speechRate)
    assertEquals(VoiceLanguage.KURDISH_SORANI, defaultSettings.voiceLanguage)
  }

  @Test
  fun testKurdishStringsAvailable() {
    val kurdishStrings = LocalizedStrings.get(AppLanguage.KURDISH_SORANI)
    assertEquals("Baso AI", kurdishStrings.appName)
    assertTrue(kurdishStrings.voiceListening.contains("گوێم لێیە"))
    assertTrue(kurdishStrings.stopSpeakingAction.contains("وەستان"))
    assertTrue(kurdishStrings.micPermissionRequiredMessage.contains("Microphone"))
  }

  @Test
  fun testCleanMarkdownForTts() {
    val markdown = "Hello **world**! Check [this link](https://example.com) and `code` here."
    val cleaned = AndroidSpeechManager.cleanTextForSpeech(markdown)
    assertFalse(cleaned.contains("**"))
    assertFalse(cleaned.contains("`"))
    assertFalse(cleaned.contains("https://example.com"))
    assertTrue(cleaned.contains("Hello world!"))
    assertTrue(cleaned.contains("Check this link and code here."))
  }

  @Test
  fun testVoiceAssistantStates() {
    val states = VoiceAssistantState.entries
    assertTrue(states.contains(VoiceAssistantState.IDLE))
    assertTrue(states.contains(VoiceAssistantState.LISTENING))
    assertTrue(states.contains(VoiceAssistantState.PROCESSING))
    assertTrue(states.contains(VoiceAssistantState.THINKING))
    assertTrue(states.contains(VoiceAssistantState.SPEAKING))
    assertTrue(states.contains(VoiceAssistantState.ERROR))
  }
}
