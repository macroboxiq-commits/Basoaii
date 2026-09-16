package com.example.services

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.domain.model.ActionExecutionResult
import com.example.domain.model.AndroidAction
import com.example.domain.model.BrightnessMode
import com.example.domain.model.RequiredPermissionType
import com.example.domain.model.SettingsTarget
import com.example.domain.model.SilentModeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Dedicated service layer to execute native Android system intents and device actions
 * outputted by Gemini tool calls or voice/text natural language commands.
 */
class AndroidActionHandler(private val context: Context) {

  private val TAG = "AndroidActionHandler"

  private val _isFlashlightOn = MutableStateFlow(false)
  val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

  /**
   * Executes a requested AndroidAction safely.
   */
  fun executeAction(action: AndroidAction): ActionExecutionResult {
    return try {
      when (action) {
        is AndroidAction.CreateCalendarEvent -> handleCalendarEvent(action)
        is AndroidAction.SetAlarm -> handleSetAlarm(action)
        is AndroidAction.SetTimer -> handleSetTimer(action)
        is AndroidAction.ToggleFlashlight -> handleToggleFlashlight(action)
        is AndroidAction.SetSilentMode -> handleSilentMode(action)
        is AndroidAction.AdjustBrightness -> handleBrightness(action)
        is AndroidAction.OpenSystemSettings -> handleOpenSettings(action)
        is AndroidAction.ToggleWifi -> handleToggleWifi(action)
        is AndroidAction.ToggleBluetooth -> handleToggleBluetooth(action)
        is AndroidAction.OpenApplication -> handleOpenApplication(action)
        is AndroidAction.AdjustVolume -> handleAdjustVolume(action)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed executing action: $action", e)
      ActionExecutionResult.Failure(
        error = e.localizedMessage ?: "Unknown error executing system action",
        action = action
      )
    }
  }

  // ---------------------------------------------------------
  // 1. Reminders & Calendar
  // ---------------------------------------------------------

  private fun handleCalendarEvent(action: AndroidAction.CreateCalendarEvent): ActionExecutionResult {
    val intent = Intent(Intent.ACTION_INSERT).apply {
      data = CalendarContract.Events.CONTENT_URI
      putExtra(CalendarContract.Events.TITLE, action.title)
      action.description?.let { putExtra(CalendarContract.Events.DESCRIPTION, it) }
      action.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }

      action.startEpochMillis?.let {
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it)
      }
      action.endEpochMillis?.let {
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it)
      }
      if (action.allDay) {
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
      }
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    return try {
      context.startActivity(intent)
      ActionExecutionResult.IntentLaunched(
        message = "ڕۆژمێری ئەندرۆید کرایەوە بۆ پاشەکەوتکردنی: ${action.title}",
        intentName = "CalendarContract.ACTION_INSERT",
        action = action
      )
    } catch (e: ActivityNotFoundException) {
      Log.w(TAG, "No calendar activity found", e)
      ActionExecutionResult.Failure(
        error = "هیچ ئەپێکی ڕۆژژمێر نەدۆزرایەوە لەم ئامێرەدا.",
        action = action
      )
    }
  }

  private fun handleSetAlarm(action: AndroidAction.SetAlarm): ActionExecutionResult {
    val formattedTime = String.format("%02d:%02d", action.hour, action.minutes)
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
      putExtra(AlarmClock.EXTRA_HOUR, action.hour)
      putExtra(AlarmClock.EXTRA_MINUTES, action.minutes)
      putExtra(AlarmClock.EXTRA_SKIP_UI, action.skipUi)
      action.label?.let { putExtra(AlarmClock.EXTRA_MESSAGE, it) }
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    return try {
      context.startActivity(intent)
      ActionExecutionResult.IntentLaunched(
        message = "زەنگی کاتژمێر دانرا بۆ: $formattedTime" + (action.label?.let { " ($it)" } ?: ""),
        intentName = "AlarmClock.ACTION_SET_ALARM",
        action = action
      )
    } catch (e: ActivityNotFoundException) {
      Log.w(TAG, "No alarm activity found", e)
      ActionExecutionResult.Failure(
        error = "هیچ ئەپێکی کاتژمێری زەنگ نەدۆزرایەوە.",
        action = action
      )
    }
  }

  private fun handleSetTimer(action: AndroidAction.SetTimer): ActionExecutionResult {
    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
      putExtra(AlarmClock.EXTRA_LENGTH, action.lengthSeconds)
      putExtra(AlarmClock.EXTRA_SKIP_UI, action.skipUi)
      action.label?.let { putExtra(AlarmClock.EXTRA_MESSAGE, it) }
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    return try {
      context.startActivity(intent)
      val minutes = action.lengthSeconds / 60
      val seconds = action.lengthSeconds % 60
      val timeStr = if (minutes > 0) "$minutes خولەک" + if (seconds > 0) " و $seconds چرکە" else "" else "$seconds چرکە"
      ActionExecutionResult.IntentLaunched(
        message = "تایمەر دەستی پێکرد بۆ: $timeStr",
        intentName = "AlarmClock.ACTION_SET_TIMER",
        action = action
      )
    } catch (e: ActivityNotFoundException) {
      Log.w(TAG, "No timer activity found", e)
      ActionExecutionResult.Failure(
        error = "ئەپی تایمەر لە ئامێرەکەدا بەردەست نییە.",
        action = action
      )
    }
  }

  // ---------------------------------------------------------
  // 2. Device Controls: Flashlight, Silent Mode, Brightness
  // ---------------------------------------------------------

  private fun handleToggleFlashlight(action: AndroidAction.ToggleFlashlight): ActionExecutionResult {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      ?: return ActionExecutionResult.Failure("خزمەتگوزاری کامێرا بەردەست نییە لەسەر ئەم ئامێرە", action)

    return try {
      val cameraIdWithFlash = cameraManager.cameraIdList.firstOrNull { id ->
        try {
          val characteristics = cameraManager.getCameraCharacteristics(id)
          characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } catch (e: Exception) {
          false
        }
      } ?: cameraManager.cameraIdList.firstOrNull()

      if (cameraIdWithFlash == null) {
        return ActionExecutionResult.Failure("فلاری کامێرا (Flashlight) لەم ئامێرەدا نییە", action)
      }

      val targetState = action.enabled
      cameraManager.setTorchMode(cameraIdWithFlash, targetState)
      _isFlashlightOn.value = targetState

      ActionExecutionResult.Success(
        message = if (targetState) "فلاش بە سەرکەوتوویی داگیرسا 🔦" else "فلاش کوژایەوە 🔦",
        action = action,
        details = if (targetState) "Flashlight ON" else "Flashlight OFF"
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error toggling flashlight", e)
      // Check if permission is needed on specific devices
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        ActionExecutionResult.PermissionRequired(
          message = "بۆ بەکارهێنانی فلاش، مۆڵەتی کامێرا پێویستە.",
          permissionType = RequiredPermissionType.CAMERA,
          action = action,
          guidance = "تکایە مۆڵەتی کامێرا بدە بۆ کۆنترۆڵکردنی فلاش."
        )
      } else {
        ActionExecutionResult.Failure(
          error = "هەڵەیەک ڕوویدا لە کۆنترۆڵی فلاش: ${e.localizedMessage}",
          action = action
        )
      }
    }
  }

  private fun handleSilentMode(action: AndroidAction.SetSilentMode): ActionExecutionResult {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
      ?: return ActionExecutionResult.Failure("خزمەتگوزاری دەنگ بەردەست نییە", action)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    // In Android 7.0 (API 24)+, modifying ringer mode / DND requires Notification Policy Access
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && notificationManager != null) {
      if (!notificationManager.isNotificationPolicyAccessGranted) {
        return ActionExecutionResult.PermissionRequired(
          message = "بۆ گۆڕینی دۆخی دەنگ و بێدەنگکردن، پێویستە دەسەڵاتی دۆخی سەرقاڵ مەکە (Do Not Disturb) بدەیت.",
          permissionType = RequiredPermissionType.NOTIFICATION_POLICY,
          action = action,
          guidance = "کرتە لە دوگمەی خوارەوە بکە بۆ بەخشینی مۆڵەتی Do Not Disturb Access."
        )
      }
    }

    val currentMode = audioManager.ringerMode
    val targetMode = when (action.mode) {
      SilentModeType.SILENT -> AudioManager.RINGER_MODE_SILENT
      SilentModeType.NORMAL -> AudioManager.RINGER_MODE_NORMAL
      SilentModeType.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
      SilentModeType.TOGGLE -> {
        if (currentMode == AudioManager.RINGER_MODE_SILENT || currentMode == AudioManager.RINGER_MODE_VIBRATE) {
          AudioManager.RINGER_MODE_NORMAL
        } else {
          AudioManager.RINGER_MODE_SILENT
        }
      }
    }

    audioManager.ringerMode = targetMode

    val modeName = when (targetMode) {
      AudioManager.RINGER_MODE_SILENT -> "دۆخی بێدەنگ (Silent) 🔕"
      AudioManager.RINGER_MODE_VIBRATE -> "دۆخی لەرزین (Vibrate) 📳"
      AudioManager.RINGER_MODE_NORMAL -> "دۆخی ئاسایی دەنگ (Normal) 🔔"
      else -> "دۆخی دەنگ"
    }

    return ActionExecutionResult.Success(
      message = "دۆخی دەنگی مۆبایل گۆڕدرا بۆ: $modeName",
      action = action,
      details = "Ringer mode changed"
    )
  }

  private fun handleBrightness(action: AndroidAction.AdjustBrightness): ActionExecutionResult {
    val canWriteSettings = Settings.System.canWrite(context)

    if (action.mode == BrightnessMode.OPEN_SETTINGS || !canWriteSettings) {
      if (!canWriteSettings && action.mode != BrightnessMode.OPEN_SETTINGS) {
        return ActionExecutionResult.PermissionRequired(
          message = "بۆ دەستکاریکردنی ڕووناکی شاشە بە شێوەی ڕاستەوخۆ، پێویستە دەسەڵاتی Modify System Settings بدەیت.",
          permissionType = RequiredPermissionType.WRITE_SETTINGS,
          action = action,
          guidance = "کرتە بکە بۆ کردنەوەی ڕێکخستنەکانی ڕووناکی شاشە."
        )
      }

      // Launch Display Settings
      val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      return try {
        context.startActivity(intent)
        ActionExecutionResult.IntentLaunched(
          message = "ڕێکخستنەکانی ڕووناکی و پیشاندانی شاشە کرایەوە",
          intentName = "Settings.ACTION_DISPLAY_SETTINGS",
          action = action
        )
      } catch (e: Exception) {
        ActionExecutionResult.Failure("نەتوانرا ڕێکخستنەکانی شاشە بکرێتەوە", action)
      }
    }

    val currentBrightness = try {
      Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
    } catch (e: Exception) {
      128
    }

    val targetValue = when (action.mode) {
      BrightnessMode.SET_LEVEL -> {
        val percent = (action.levelPercent ?: 50).coerceIn(5, 100)
        (percent * 255) / 100
      }
      BrightnessMode.INCREASE -> {
        (currentBrightness + 50).coerceAtMost(255)
      }
      BrightnessMode.DECREASE -> {
        (currentBrightness - 50).coerceAtLeast(15)
      }
      BrightnessMode.OPEN_SETTINGS -> currentBrightness
    }

    return try {
      Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS_MODE,
        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
      )
      Settings.System.putInt(
        context.contentResolver,
        Settings.System.SCREEN_BRIGHTNESS,
        targetValue
      )
      val percent = (targetValue * 100) / 255
      ActionExecutionResult.Success(
        message = "ڕووناکی شاشە ڕێکخرا لەسەر: $percent% ☀️",
        action = action,
        details = "Brightness set to $percent%"
      )
    } catch (e: Exception) {
      ActionExecutionResult.Failure(
        error = "نەتوانرا ڕووناکی شاشە بگۆڕدرێت: ${e.localizedMessage}",
        action = action
      )
    }
  }

  private fun handleOpenSettings(action: AndroidAction.OpenSystemSettings): ActionExecutionResult {
    val intentAction = when (action.target) {
      SettingsTarget.GENERAL -> Settings.ACTION_SETTINGS
      SettingsTarget.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
      SettingsTarget.SOUND -> Settings.ACTION_SOUND_SETTINGS
      SettingsTarget.NOTIFICATION_POLICY -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
      SettingsTarget.WRITE_SETTINGS -> Settings.ACTION_MANAGE_WRITE_SETTINGS
      SettingsTarget.ALARM -> AlarmClock.ACTION_SHOW_ALARMS
      SettingsTarget.APPLICATION_DETAILS -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    }

    val intent = Intent(intentAction).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
      if (action.target == SettingsTarget.APPLICATION_DETAILS || action.target == SettingsTarget.WRITE_SETTINGS) {
        data = Uri.parse("package:${context.packageName}")
      }
    }

    return try {
      context.startActivity(intent)
      ActionExecutionResult.IntentLaunched(
        message = "ڕێکخستنەکان کرانەوە",
        intentName = intentAction,
        action = action
      )
    } catch (e: Exception) {
      ActionExecutionResult.Failure("نەتوانرا ڕێکخستنەکان بکرێتەوە", action)
    }
  }

  private fun handleToggleWifi(action: AndroidAction.ToggleWifi): ActionExecutionResult {
    // Modern Android (API 29+ / Android 10+) security requires using Settings.Panel.ACTION_WIFI
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      Intent(Settings.Panel.ACTION_WIFI)
    } else {
      Intent(Settings.ACTION_WIFI_SETTINGS)
    }.apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    return try {
      context.startActivity(intent)
      ActionExecutionResult.IntentLaunched(
        message = "پەنجەرەی خێرای وایفای (Wi-Fi) کرایەوە بۆ کوژاندنەوە یان پێکردن 📶",
        intentName = "Settings.Panel.ACTION_WIFI",
        action = action
      )
    } catch (e: Exception) {
      try {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        ActionExecutionResult.IntentLaunched(
          message = "ڕێکخستنەکانی وایفای کرایەوە 📶",
          intentName = "Settings.ACTION_WIFI_SETTINGS",
          action = action
        )
      } catch (e2: Exception) {
        ActionExecutionResult.Failure("نەتوانرا پەنجەرەی وایفای بکرێتەوە", action)
      }
    }
  }

  private fun handleToggleBluetooth(action: AndroidAction.ToggleBluetooth): ActionExecutionResult {
    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    return try {
      context.startActivity(intent)
      ActionExecutionResult.IntentLaunched(
        message = "ڕێکخستنەکانی بلوتوز (Bluetooth) کرایەوە 📡",
        intentName = "Settings.ACTION_BLUETOOTH_SETTINGS",
        action = action
      )
    } catch (e: Exception) {
      ActionExecutionResult.Failure("نەتوانرا ڕێکخستنەکانی بلوتوز بکرێتەوە", action)
    }
  }

  private fun handleOpenApplication(action: AndroidAction.OpenApplication): ActionExecutionResult {
    val pm = context.packageManager
    val nameLower = action.appName.lowercase().trim()

    val intent: Intent? = when {
      nameLower in listOf("camera", "کامێرا", "كاميرا") -> {
        Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("phone", "تەلەفۆن", "هاتف", "پەیوەندی") -> {
        Intent(Intent.ACTION_DIAL).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("settings", "ڕێکخستنەکان", "ڕێکخستن", "الإعدادات") -> {
        Intent(Settings.ACTION_SETTINGS).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("youtube", "یوتیوب") -> {
        pm.getLaunchIntentForPackage("com.google.android.youtube")?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("whatsapp", "واتسئەپ", "واتساب") -> {
        pm.getLaunchIntentForPackage("com.whatsapp")?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("telegram", "تێلەگرام", "تلیگرام") -> {
        pm.getLaunchIntentForPackage("org.telegram.messenger")?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("chrome", "کرۆم") -> {
        pm.getLaunchIntentForPackage("com.android.chrome")?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      nameLower in listOf("maps", "نەخشە", "خرائط") -> {
        pm.getLaunchIntentForPackage("com.google.android.apps.maps")?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      action.packageName != null -> {
        pm.getLaunchIntentForPackage(action.packageName)?.apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
      }
      else -> null
    }

    if (intent != null) {
      return try {
        context.startActivity(intent)
        ActionExecutionResult.IntentLaunched(
          message = "ئەپی ${action.appName} کرایەوە 📱",
          intentName = "OpenApplication",
          action = action
        )
      } catch (e: Exception) {
        ActionExecutionResult.Failure("نەتوانرا ئەپی ${action.appName} بکرێتەوە", action)
      }
    }

    return ActionExecutionResult.Failure("ئەپی ${action.appName} لەسەر ئامێرەکەت نەدۆزرایەوە", action)
  }

  private fun handleAdjustVolume(action: AndroidAction.AdjustVolume): ActionExecutionResult {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
      ?: return ActionExecutionResult.Failure("خزمەتگوزاری دەنگ بەردەست نییە", action)

    return try {
      when (action.direction) {
        com.example.domain.model.VolumeDirection.UP -> {
          audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
          ActionExecutionResult.Success(
            message = "دەنگی مۆبایل بەرزکرایەوە 🔊",
            action = action
          )
        }
        com.example.domain.model.VolumeDirection.DOWN -> {
          audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
          ActionExecutionResult.Success(
            message = "دەنگی مۆبایل کەمکرایەوە 🔉",
            action = action
          )
        }
        com.example.domain.model.VolumeDirection.MUTE -> {
          audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
          ActionExecutionResult.Success(
            message = "دەنگی مۆبایل بێدەنگکرا 🔇",
            action = action
          )
        }
      }
    } catch (e: Exception) {
      ActionExecutionResult.Failure("نەتوانرا دەنگی مۆبایل بگۆڕدرێت: ${e.localizedMessage}", action)
    }
  }

  /**
   * Helper to open the respective permission or setting screen directly when guided by Baso AI.
   */
  fun openPermissionSettings(permissionType: RequiredPermissionType) {
    val intent = when (permissionType) {
      RequiredPermissionType.NOTIFICATION_POLICY -> {
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
      }
      RequiredPermissionType.WRITE_SETTINGS -> {
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
          data = Uri.parse("package:${context.packageName}")
        }
      }
      RequiredPermissionType.CAMERA -> {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.parse("package:${context.packageName}")
        }
      }
    }.apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Cannot launch settings for $permissionType", e)
      // Fallback to general settings
      try {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
      } catch (_: Exception) {}
    }
  }

  fun hasPermission(permissionType: RequiredPermissionType): Boolean {
    return when (permissionType) {
      RequiredPermissionType.CAMERA -> {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
      }
      RequiredPermissionType.NOTIFICATION_POLICY -> {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.isNotificationPolicyAccessGranted == true
      }
      RequiredPermissionType.WRITE_SETTINGS -> {
        Settings.System.canWrite(context)
      }
    }
  }
}
