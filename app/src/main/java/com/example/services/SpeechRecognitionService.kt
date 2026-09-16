package com.example.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.domain.model.AppLanguage
import com.example.domain.model.VoiceLanguage
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RecognitionStatus {
  IDLE,
  LISTENING,
  PROCESSING,
  ERROR
}

interface SpeechRecognitionService {
  val recognitionStatus: StateFlow<RecognitionStatus>
  val soundLevel: StateFlow<Float>

  fun startListening(
    voiceLanguage: VoiceLanguage,
    appLanguage: AppLanguage,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
  )

  fun stopListening()
  fun cancel()
  fun destroy()
}

class AndroidSpeechRecognitionService(
  private val context: Context
) : SpeechRecognitionService {

  private val tag = "SpeechRecognition"
  private val mainHandler = Handler(Looper.getMainLooper())

  private val _recognitionStatus = MutableStateFlow(RecognitionStatus.IDLE)
  override val recognitionStatus: StateFlow<RecognitionStatus> = _recognitionStatus.asStateFlow()

  private val _soundLevel = MutableStateFlow(0f)
  override val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

  private var speechRecognizer: SpeechRecognizer? = null

  override fun startListening(
    voiceLanguage: VoiceLanguage,
    appLanguage: AppLanguage,
    onResult: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    mainHandler.post {
      try {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
          _recognitionStatus.value = RecognitionStatus.ERROR
          onError(getErrorMessage(SpeechRecognizer.ERROR_CLIENT, appLanguage))
          return@post
        }

        // Clean up previous instance if any
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val languageTag = when (voiceLanguage) {
          VoiceLanguage.KURDISH_SORANI -> "ckb-IQ"
          VoiceLanguage.ARABIC -> "ar-IQ"
          VoiceLanguage.ENGLISH -> "en-US"
          VoiceLanguage.AUTO -> when (appLanguage) {
            AppLanguage.KURDISH_SORANI -> "ckb-IQ"
            AppLanguage.ARABIC -> "ar-IQ"
            AppLanguage.ENGLISH -> "en-US"
          }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
          putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageTag)
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _recognitionStatus.value = RecognitionStatus.LISTENING
          }

          override fun onBeginningOfSpeech() {
            _recognitionStatus.value = RecognitionStatus.LISTENING
          }

          override fun onRmsChanged(rmsdB: Float) {
            _soundLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _recognitionStatus.value = RecognitionStatus.PROCESSING
          }

          override fun onError(error: Int) {
            _recognitionStatus.value = RecognitionStatus.ERROR
            val message = getErrorMessage(error, appLanguage)
            onError(message)
          }

          override fun onResults(results: Bundle?) {
            _recognitionStatus.value = RecognitionStatus.IDLE
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: ""
            if (recognizedText.isNotBlank()) {
              onResult(recognizedText)
            } else {
              onError(getErrorMessage(SpeechRecognizer.ERROR_NO_MATCH, appLanguage))
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull()
            if (!partial.isNullOrBlank()) {
              // Can optionally update partial text
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        _recognitionStatus.value = RecognitionStatus.LISTENING
        speechRecognizer?.startListening(intent)
      } catch (e: Exception) {
        Log.e(tag, "Failed to start speech recognition", e)
        _recognitionStatus.value = RecognitionStatus.ERROR
        onError("هەڵەیەک ڕوویدا لە کاتی کارپێکردنی دەنگ: ${e.localizedMessage ?: ""}")
      }
    }
  }

  override fun stopListening() {
    mainHandler.post {
      try {
        speechRecognizer?.stopListening()
        _recognitionStatus.value = RecognitionStatus.PROCESSING
      } catch (e: Exception) {
        Log.e(tag, "Error stopping speech recognizer", e)
      }
    }
  }

  override fun cancel() {
    mainHandler.post {
      try {
        speechRecognizer?.cancel()
        _recognitionStatus.value = RecognitionStatus.IDLE
        _soundLevel.value = 0f
      } catch (e: Exception) {
        Log.e(tag, "Error cancelling speech recognizer", e)
      }
    }
  }

  override fun destroy() {
    mainHandler.post {
      try {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _recognitionStatus.value = RecognitionStatus.IDLE
      } catch (e: Exception) {
        Log.e(tag, "Error destroying speech recognizer", e)
      }
    }
  }

  private fun getErrorMessage(errorCode: Int, appLanguage: AppLanguage): String {
    return when (errorCode) {
      SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> when (appLanguage) {
        AppLanguage.KURDISH_SORANI -> "دەنگەکەت نەدۆزرایەوە. تکایە دووبارە هەوڵ بدەرەوە."
        AppLanguage.ARABIC -> "لم يتم التعرف على الصوت. يرجى المحاولة مرة أخرى."
        AppLanguage.ENGLISH -> "No speech detected. Please try speaking again."
      }
      SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> when (appLanguage) {
        AppLanguage.KURDISH_SORANI -> "کێشەی پەیوەندی هێڵ هەیە. تکایە هێڵی ئینتەرنێتەکەت بپشکنە."
        AppLanguage.ARABIC -> "خطأ في الاتصال بالشبكة. يرجى التحقق من اتصال الإنترنت."
        AppLanguage.ENGLISH -> "Network error. Please check your internet connection."
      }
      SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> when (appLanguage) {
        AppLanguage.KURDISH_SORANI -> "بۆ بەکارهێنانی دەنگ، پێویستە مۆڵەتی Microphone بدەیت."
        AppLanguage.ARABIC -> "لاستخدام الصوت، يجب منح إذن الميكروفون."
        AppLanguage.ENGLISH -> "Microphone permission is required to use voice input."
      }
      SpeechRecognizer.ERROR_AUDIO -> when (appLanguage) {
        AppLanguage.KURDISH_SORANI -> "هەڵەی تۆمارکردنی دەنگ ڕوویدا."
        AppLanguage.ARABIC -> "حدث خطأ في تسجيل الصوت."
        AppLanguage.ENGLISH -> "Audio recording error occurred."
      }
      else -> when (appLanguage) {
        AppLanguage.KURDISH_SORANI -> "دەنگ ناسینەوە لەم ئامێرەدا لە کار کەوتووە یان ڕێگەنەدراوە."
        AppLanguage.ARABIC -> "خدمة التعرف على الصوت غير متاحة حالياً على هذا الجهاز."
        AppLanguage.ENGLISH -> "Speech recognition service is currently unavailable on this device."
      }
    }
  }
}
