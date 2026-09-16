package com.example.services

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.domain.model.AppLanguage
import com.example.domain.model.SpeechRate
import com.example.domain.model.VoiceLanguage
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SpeechManager {
  val isSpeaking: StateFlow<Boolean>
  val isInitialized: StateFlow<Boolean>
  val lastSpokenText: StateFlow<String>

  fun speak(
    text: String,
    voiceLanguage: VoiceLanguage = VoiceLanguage.KURDISH_SORANI,
    appLanguage: AppLanguage = AppLanguage.KURDISH_SORANI,
    speechRate: Float = 1.0f,
    pitch: Float = 1.0f,
    onStart: () -> Unit = {},
    onDone: () -> Unit = {},
    onError: (String) -> Unit = {}
  )

  fun stop()
  fun pause()
  fun resume()
  fun setLanguage(locale: Locale): Int
  fun setSpeechRate(rate: Float)
  fun setPitch(pitch: Float)
  fun isLanguageAvailable(locale: Locale): Boolean
  fun shutdown()
}

class AndroidSpeechManager(
  private val context: Context
) : SpeechManager, TextToSpeech.OnInitListener {

  private val tag = "SpeechManager"
  private var textToSpeech: TextToSpeech? = null

  private val _isSpeaking = MutableStateFlow(false)
  override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _isInitialized = MutableStateFlow(false)
  override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

  private val _lastSpokenText = MutableStateFlow("")
  override val lastSpokenText: StateFlow<String> = _lastSpokenText.asStateFlow()

  private var onStartCallback: (() -> Unit)? = null
  private var onDoneCallback: (() -> Unit)? = null
  private var onErrorCallback: ((String) -> Unit)? = null

  private var currentUtteranceText: String = ""
  private var isPaused: Boolean = false

  init {
    try {
      textToSpeech = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e(tag, "Error creating TextToSpeech instance", e)
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      _isInitialized.value = true
      textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
          onStartCallback?.invoke()
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
          isPaused = false
          onDoneCallback?.invoke()
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
          isPaused = false
          onErrorCallback?.invoke("TTS playback error")
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
          _isSpeaking.value = false
          isPaused = false
          onErrorCallback?.invoke("TTS playback error: code $errorCode")
        }
      })
    } else {
      Log.e(tag, "TextToSpeech init failed with status: $status")
      _isInitialized.value = false
    }
  }

  override fun speak(
    text: String,
    voiceLanguage: VoiceLanguage,
    appLanguage: AppLanguage,
    speechRate: Float,
    pitch: Float,
    onStart: () -> Unit,
    onDone: () -> Unit,
    onError: (String) -> Unit
  ) {
    val tts = textToSpeech
    if (tts == null || !_isInitialized.value) {
      onError("خزمەتگوزاری خوێندنەوەی دەنگ لەم ئامێرەدا هێشتا ئامادە نییە.")
      return
    }

    onStartCallback = onStart
    onDoneCallback = onDone
    onErrorCallback = onError

    // Clean text of markdown stars, hashtags, and code blocks for smooth speech
    val speechCleanedText = cleanTextForSpeech(text)
    currentUtteranceText = speechCleanedText
    _lastSpokenText.value = speechCleanedText

    try {
      tts.setSpeechRate(speechRate)
      tts.setPitch(pitch)

      // Resolve locale with fallback
      val targetLocale = resolveLocale(voiceLanguage, appLanguage)
      val availability = tts.isLanguageAvailable(targetLocale)

      if (availability >= TextToSpeech.LANG_AVAILABLE) {
        tts.language = targetLocale
      } else {
        // Fallback gracefully without crash
        if (targetLocale.language == "ckb" || targetLocale.language == "ku") {
          // Kurdish voice is often not installed by default in Google TTS.
          // Gracefully fallback to Arabic or device default
          val arabicLocale = Locale.forLanguageTag("ar")
          if (tts.isLanguageAvailable(arabicLocale) >= TextToSpeech.LANG_AVAILABLE) {
            tts.language = arabicLocale
          } else {
            tts.language = Locale.getDefault()
          }
        } else {
          tts.language = Locale.getDefault()
        }
      }

      val utteranceId = UUID.randomUUID().toString()
      val params = Bundle()
      tts.speak(speechCleanedText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    } catch (e: Exception) {
      Log.e(tag, "Failed to speak text", e)
      _isSpeaking.value = false
      onError("هەڵە لە خوێندنەوەی دەنگ: ${e.localizedMessage ?: ""}")
    }
  }

  override fun stop() {
    try {
      textToSpeech?.stop()
      _isSpeaking.value = false
      isPaused = false
    } catch (e: Exception) {
      Log.e(tag, "Error stopping TTS", e)
    }
  }

  override fun pause() {
    if (_isSpeaking.value) {
      stop()
      isPaused = true
    }
  }

  override fun resume() {
    if (isPaused && currentUtteranceText.isNotBlank()) {
      speak(
        text = currentUtteranceText,
        onStart = onStartCallback ?: {},
        onDone = onDoneCallback ?: {},
        onError = onErrorCallback ?: {}
      )
      isPaused = false
    }
  }

  override fun setLanguage(locale: Locale): Int {
    return textToSpeech?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
  }

  override fun setSpeechRate(rate: Float) {
    textToSpeech?.setSpeechRate(rate)
  }

  override fun setPitch(pitch: Float) {
    textToSpeech?.setPitch(pitch)
  }

  override fun isLanguageAvailable(locale: Locale): Boolean {
    val result = textToSpeech?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
    return result >= TextToSpeech.LANG_AVAILABLE
  }

  override fun shutdown() {
    try {
      textToSpeech?.stop()
      textToSpeech?.shutdown()
      textToSpeech = null
      _isSpeaking.value = false
      _isInitialized.value = false
    } catch (e: Exception) {
      Log.e(tag, "Error shutting down TTS", e)
    }
  }

  private fun resolveLocale(voiceLanguage: VoiceLanguage, appLanguage: AppLanguage): Locale {
    return when (voiceLanguage) {
      VoiceLanguage.KURDISH_SORANI -> Locale.forLanguageTag("ckb")
      VoiceLanguage.ARABIC -> Locale.forLanguageTag("ar")
      VoiceLanguage.ENGLISH -> Locale.ENGLISH
      VoiceLanguage.AUTO -> when (appLanguage) {
        AppLanguage.KURDISH_SORANI -> Locale.forLanguageTag("ckb")
        AppLanguage.ARABIC -> Locale.forLanguageTag("ar")
        AppLanguage.ENGLISH -> Locale.ENGLISH
      }
    }
  }

  companion object {
    fun cleanTextForSpeech(input: String): String {
      return input
        .replace(Regex("[*#`_~>]"), "") // Remove markdown asterisks, hashes, backticks, tildes
        .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1") // Simplify markdown links
        .replace(Regex("```[\\s\\S]*?```"), "کۆدی بەرنامەسازی") // Replace large codeblocks with label
        .replace(Regex("\\s+"), " ")
        .trim()
    }
  }
}
