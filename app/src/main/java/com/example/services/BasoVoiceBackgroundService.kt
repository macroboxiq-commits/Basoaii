package com.example.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground Service that continuously listens in the background for the wake word
 * "سڵاو باسۆ" (Hey Baso) and direct device commands (Wi-Fi, Flashlight, Alarms, Apps).
 *
 * Runs even when the user closes or minimizes the app.
 */
class BasoVoiceBackgroundService : Service() {

  companion object {
    private const val TAG = "BasoVoiceBgService"
    const val CHANNEL_ID = "baso_voice_channel"
    const val NOTIFICATION_ID = 2026

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isActivelyListening = MutableStateFlow(false)
    val isActivelyListening: StateFlow<Boolean> = _isActivelyListening.asStateFlow()

    private val _lastRecognizedCommand = MutableStateFlow("")
    val lastRecognizedCommand: StateFlow<String> = _lastRecognizedCommand.asStateFlow()

    private val _lastFeedback = MutableStateFlow("")
    val lastFeedback: StateFlow<String> = _lastFeedback.asStateFlow()

    fun startService(context: Context) {
      val intent = Intent(context, BasoVoiceBackgroundService::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ContextCompat.startForegroundService(context, intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, BasoVoiceBackgroundService::class.java)
      context.stopService(intent)
      _isServiceRunning.value = false
      _isActivelyListening.value = false
    }

    fun triggerDirectListen(context: Context) {
      val intent = Intent(context, BasoVoiceBackgroundService::class.java).apply {
        action = "ACTION_FORCE_LISTEN"
      }
      context.startService(intent)
    }
  }

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
  private val mainHandler = Handler(Looper.getMainLooper())

  private var speechRecognizer: SpeechRecognizer? = null
  private var speechManager: SpeechManager? = null
  private var actionHandler: AndroidActionHandler? = null
  private var toneGenerator: ToneGenerator? = null

  private var isListeningLoopActive = false
  private var isServiceStopping = false

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "BasoVoiceBackgroundService created")

    actionHandler = AndroidActionHandler(applicationContext)
    speechManager = AndroidSpeechManager(applicationContext)
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
    } catch (e: Exception) {
      Log.w(TAG, "Could not initialize ToneGenerator", e)
    }

    createNotificationChannel()
    startForegroundWithNotification("باسۆ بەردەوام گوێ دەگرێت 🎙️", "بڵێ «سڵاو باسۆ» یان فرمانی وەک (وایفای، فلاش، زەنگ) بدە")
    _isServiceRunning.value = true

    startContinuousListening()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == "ACTION_FORCE_LISTEN") {
      restartListeningImmediate()
    }
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "خزمەتگوزاری گوێگرتنی دەنگی باسۆ (Baso Voice)",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "بەردەوام گوێگرتن لە وشەی «سڵاو باسۆ» و جێبەجێکردنی فرمانەکان کاتێک ئەپەکە داخراوە"
        setShowBadge(false)
      }
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(title: String, contentText: String): Notification {
    val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      this,
      0,
      mainActivityIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action 1: Listen
    val listenIntent = Intent(this, BasoVoiceActionReceiver::class.java).apply {
      action = BasoVoiceActionReceiver.ACTION_TRIGGER_LISTEN
    }
    val listenPendingIntent = PendingIntent.getBroadcast(
      this, 1, listenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action 2: Flashlight
    val flashIntent = Intent(this, BasoVoiceActionReceiver::class.java).apply {
      action = BasoVoiceActionReceiver.ACTION_TOGGLE_FLASHLIGHT
    }
    val flashPendingIntent = PendingIntent.getBroadcast(
      this, 2, flashIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action 3: Wi-Fi
    val wifiIntent = Intent(this, BasoVoiceActionReceiver::class.java).apply {
      action = BasoVoiceActionReceiver.ACTION_TOGGLE_WIFI
    }
    val wifiPendingIntent = PendingIntent.getBroadcast(
      this, 3, wifiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action 4: Stop Service
    val stopIntent = Intent(this, BasoVoiceActionReceiver::class.java).apply {
      action = BasoVoiceActionReceiver.ACTION_STOP_SERVICE
    }
    val stopPendingIntent = PendingIntent.getBroadcast(
      this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(title)
      .setContentText(contentText)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentIntent(contentPendingIntent)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .addAction(0, "🎙️ گوێگرتن", listenPendingIntent)
      .addAction(0, "🔦 فلاش", flashPendingIntent)
      .addAction(0, "📶 وایفای", wifiPendingIntent)
      .addAction(0, "⏹️ ڕاگرتن", stopPendingIntent)
      .build()
  }

  private fun startForegroundWithNotification(title: String, contentText: String) {
    val notification = buildNotification(title, contentText)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
  }

  private fun updateNotification(title: String, contentText: String) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    notificationManager?.notify(NOTIFICATION_ID, buildNotification(title, contentText))
  }

  private fun startContinuousListening() {
    if (isServiceStopping) return

    val micGranted = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    if (!micGranted) {
      Log.w(TAG, "Record audio permission not granted for background service")
      updateNotification("پێویستی بە مۆڵەتی مایکرۆفۆنە", "تکایە مۆڵەتی دەنگ بدە بۆ چالاککردنی گوێگرتن")
      return
    }

    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
      Log.w(TAG, "SpeechRecognizer is not available on this device")
      updateNotification("خزمەتگوزاری گوێگرتن بەردەست نییە", "SpeechRecognizer لە ئامێرەکەتدا نییە")
      return
    }

    mainHandler.post {
      try {
        cleanupRecognizer()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ckb-IQ")
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ckb-IQ")
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isActivelyListening.value = true
            isListeningLoopActive = true
          }

          override fun onBeginningOfSpeech() {
            _isActivelyListening.value = true
          }

          override fun onRmsChanged(rmsdB: Float) {}
          override fun onBufferReceived(buffer: ByteArray?) {}
          override fun onEndOfSpeech() {
            _isActivelyListening.value = false
          }

          override fun onError(error: Int) {
            _isActivelyListening.value = false
            Log.d(TAG, "Speech recognition error code: $error")
            scheduleNextListen(delayMs = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) 800L else 1800L)
          }

          override fun onResults(results: Bundle?) {
            _isActivelyListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
              handleRecognizedSpeech(matches)
            } else {
              scheduleNextListen(delayMs = 600L)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            partials?.firstOrNull()?.let { partialText ->
              checkEarlyTrigger(partialText)
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
      } catch (e: Exception) {
        Log.e(TAG, "Failed starting speech recognizer in background", e)
        scheduleNextListen(delayMs = 2000L)
      }
    }
  }

  private fun checkEarlyTrigger(text: String) {
    val lower = text.lowercase().trim()
    if (lower.contains("سڵاو باسۆ") || lower.contains("سلاو باسو") || lower.contains("hey baso")) {
      playChime()
    }
  }

  private fun handleRecognizedSpeech(matches: List<String>) {
    val spokenText = matches.firstOrNull()?.trim() ?: return
    Log.d(TAG, "Background Recognized: $spokenText")
    _lastRecognizedCommand.value = spokenText

    val lower = spokenText.lowercase()

    // Wake words check
    val isWakeWordOnly = lower in listOf("سڵاو باسۆ", "سلاو باسو", "سڵاو باسو", "سڵاو", "باسۆ", "hey baso", "hello baso", "ok baso", "مرحبا باسو")

    if (isWakeWordOnly) {
      playChime()
      val greetingReply = "سڵاو! گوێم لێتە، چۆن یارمەتیت بدەم؟"
      _lastFeedback.value = greetingReply
      updateNotification("باسۆ: گوێم لێتە 🎙️", greetingReply)

      speechManager?.speak(
        text = greetingReply,
        voiceLanguage = com.example.domain.model.VoiceLanguage.KURDISH_SORANI,
        onDone = {
          scheduleNextListen(delayMs = 800L)
        }
      )
      return
    }

    // Check device command parsing
    val parsed = SystemActionParser.parsePrompt(spokenText, AppLanguage.KURDISH_SORANI)
    if (parsed != null) {
      playChime()
      _lastFeedback.value = parsed.feedbackMessage
      updateNotification("باسۆ فەرمانی جێبەجێ کرد", parsed.feedbackMessage)

      // Execute Android Action
      actionHandler?.executeAction(parsed.action)

      // Speak response aloud
      speechManager?.speak(
        text = parsed.feedbackMessage,
        voiceLanguage = com.example.domain.model.VoiceLanguage.KURDISH_SORANI,
        onDone = {
          scheduleNextListen(delayMs = 1200L)
        },
        onError = {
          scheduleNextListen(delayMs = 800L)
        }
      )
    } else {
      // General question or query
      if (lower.contains("باسۆ") || lower.contains("سڵاو")) {
        val ack = "داواکارییەکەت وەرگیرا: \"$spokenText\""
        _lastFeedback.value = ack
        updateNotification("باسۆ: $spokenText", "بۆ زانیاری زیاتر دەتوانیت ئەپەکە بکەیتەوە")
        speechManager?.speak(
          text = ack,
          voiceLanguage = com.example.domain.model.VoiceLanguage.KURDISH_SORANI,
          onDone = {
            scheduleNextListen(delayMs = 1200L)
          }
        )
      } else {
        scheduleNextListen(delayMs = 800L)
      }
    }
  }

  private fun playChime() {
    try {
      toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    } catch (e: Exception) {
      Log.w(TAG, "Tone playback error", e)
    }
  }

  private fun scheduleNextListen(delayMs: Long) {
    if (isServiceStopping) return
    mainHandler.removeCallbacksAndMessages(null)
    mainHandler.postDelayed({
      startContinuousListening()
    }, delayMs)
  }

  private fun restartListeningImmediate() {
    mainHandler.removeCallbacksAndMessages(null)
    cleanupRecognizer()
    startContinuousListening()
  }

  private fun cleanupRecognizer() {
    try {
      speechRecognizer?.stopListening()
      speechRecognizer?.cancel()
      speechRecognizer?.destroy()
    } catch (e: Exception) {
      Log.w(TAG, "Error cleaning up SpeechRecognizer", e)
    }
    speechRecognizer = null
  }

  override fun onDestroy() {
    isServiceStopping = true
    _isServiceRunning.value = false
    _isActivelyListening.value = false

    mainHandler.removeCallbacksAndMessages(null)
    cleanupRecognizer()
    try {
      speechManager?.stop()
      speechManager?.shutdown()
    } catch (_: Exception) {}

    try {
      toneGenerator?.release()
    } catch (_: Exception) {}

    serviceJob.cancel()
    Log.d(TAG, "BasoVoiceBackgroundService destroyed")
    super.onDestroy()
  }
}
