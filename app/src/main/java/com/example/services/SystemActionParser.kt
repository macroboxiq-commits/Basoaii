package com.example.services

import com.example.domain.model.AndroidAction
import com.example.domain.model.AppLanguage
import com.example.domain.model.BrightnessMode
import com.example.domain.model.SilentModeType
import java.util.Calendar

data class ParsedActionResult(
  val action: AndroidAction,
  val feedbackMessage: String
)

/**
 * Intelligent parser for natural language device actions supporting Kurdish Sorani, Arabic, and English.
 */
object SystemActionParser {

  fun parsePrompt(prompt: String, language: AppLanguage): ParsedActionResult? {
    val clean = prompt.trim()
    var lower = clean.lowercase()

    // Strip optional wake word prefixes so "سڵاو باسۆ وایفای دابخە" matches "وایفای دابخە"
    val wakeWords = listOf("سڵاو باسۆ", "سلاو باسو", "سڵاو باسو", "سڵاو", "باسۆ", "باسو", "hey baso", "hello baso", "ok baso", "مرحبا باسو")
    for (wake in wakeWords) {
      if (lower.startsWith(wake)) {
        lower = lower.removePrefix(wake).trim().removePrefix("،").removePrefix(",").trim()
        break
      }
    }

    // 0. Wi-Fi (Quick Panel / Action)
    parseWifi(lower, language)?.let { return it }

    // 0b. Bluetooth
    parseBluetooth(lower, language)?.let { return it }

    // 0c. Open Application
    parseOpenApp(lower, clean, language)?.let { return it }

    // 0d. Volume Control
    parseVolume(lower, language)?.let { return it }

    // 1. Flashlight
    parseFlashlight(lower, language)?.let { return it }

    // 2. Silent Mode / Ringer
    parseSilentMode(lower, language)?.let { return it }

    // 3. Brightness
    parseBrightness(lower, language)?.let { return it }

    // 4. Timer
    parseTimer(lower, clean, language)?.let { return it }

    // 5. Alarm
    parseAlarm(lower, clean, language)?.let { return it }

    // 6. Calendar Event & Reminder
    parseCalendar(lower, clean, language)?.let { return it }

    return null
  }

  // --- Wi-Fi (Modern Android Quick Panel) ---
  private fun parseWifi(lower: String, language: AppLanguage): ParsedActionResult? {
    val isWifi = lower.contains("وایفای") || lower.contains("وای فای") ||
        lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("وايفاي") ||
        lower.contains("الواي فاي")
    if (!isWifi) return null

    val isTurnOff = lower.contains("دابخە") || lower.contains("بکوژێنەوە") ||
        lower.contains("خامۆش") || lower.contains("اطفئ") || lower.contains("off") ||
        lower.contains("کوژانەوە")

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> if (isTurnOff) {
        "پەنجەرەی وایفای (Wi-Fi) کرایەوە بۆ کوژاندنەوە 📶"
      } else {
        "پەنجەرەی وایفای (Wi-Fi) کرایەوە بۆ هەڵکردن 📶"
      }
      AppLanguage.ARABIC -> "جارِ فتح لوحة الواي فاي (Wi-Fi) للتحكم السريع 📶"
      AppLanguage.ENGLISH -> "Opening Wi-Fi control panel 📶"
    }

    return ParsedActionResult(
      action = AndroidAction.ToggleWifi(enabled = !isTurnOff, openPanel = true),
      feedbackMessage = msg
    )
  }

  // --- Bluetooth ---
  private fun parseBluetooth(lower: String, language: AppLanguage): ParsedActionResult? {
    val isBt = lower.contains("بلوتوز") || lower.contains("بلوتوث") || lower.contains("bluetooth")
    if (!isBt) return null

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> "ڕێکخستنەکانی بلوتوز کرایەوە 📡"
      AppLanguage.ARABIC -> "جارِ فتح إعدادات البلوتوث 📡"
      AppLanguage.ENGLISH -> "Opening Bluetooth settings 📡"
    }

    return ParsedActionResult(
      action = AndroidAction.ToggleBluetooth(),
      feedbackMessage = msg
    )
  }

  // --- Open App ---
  private fun parseOpenApp(lower: String, original: String, language: AppLanguage): ParsedActionResult? {
    val isOpenCommand = lower.contains("بکەرەوە") || lower.contains("کردنەوە") ||
        lower.contains("افتح") || lower.contains("فتح") || lower.contains("open") || lower.contains("launch")

    if (!isOpenCommand) return null

    val appName = when {
      lower.contains("یوتیوب") || lower.contains("يوتيوب") || lower.contains("youtube") -> "YouTube"
      lower.contains("کامێرا") || lower.contains("كاميرا") || lower.contains("camera") -> "کامێرا"
      lower.contains("واتسئەپ") || lower.contains("واتساب") || lower.contains("whatsapp") -> "WhatsApp"
      lower.contains("تێلەگرام") || lower.contains("تلیگرام") || lower.contains("telegram") -> "Telegram"
      lower.contains("تەلەفۆن") || lower.contains("هاتف") || lower.contains("phone") || lower.contains("dialer") -> "تەلەفۆن"
      lower.contains("کرۆم") || lower.contains("كروم") || lower.contains("chrome") -> "Chrome"
      lower.contains("نەخشە") || lower.contains("خرائط") || lower.contains("maps") -> "Google Maps"
      lower.contains("ڕێکخستن") || lower.contains("إعدادات") || lower.contains("settings") -> "ڕێکخستنەکان"
      else -> return null
    }

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> "ئەپی $appName دەکرێتەوە 📱"
      AppLanguage.ARABIC -> "جارِ فتح تطبيق $appName 📱"
      AppLanguage.ENGLISH -> "Opening $appName 📱"
    }

    return ParsedActionResult(
      action = AndroidAction.OpenApplication(appName = appName),
      feedbackMessage = msg
    )
  }

  // --- Volume Control ---
  private fun parseVolume(lower: String, language: AppLanguage): ParsedActionResult? {
    val isVolumeQuery = lower.contains("دەنگ") || lower.contains("صوت") || lower.contains("volume")
    if (!isVolumeQuery) return null

    val isUp = lower.contains("بەرز") || lower.contains("زیاد") || lower.contains("ارفع") ||
        lower.contains("زيادة") || lower.contains("up") || lower.contains("raise")
    val isDown = lower.contains("کەم") || lower.contains("دابەز") || lower.contains("اخفض") ||
        lower.contains("خفض") || lower.contains("down") || lower.contains("lower")

    if (!isUp && !isDown) return null

    val direction = if (isUp) com.example.domain.model.VolumeDirection.UP else com.example.domain.model.VolumeDirection.DOWN
    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> if (isUp) "دەنگی مۆبایل بەرزکرایەوە 🔊" else "دەنگی مۆبایل کەمکرایەوە 🔉"
      AppLanguage.ARABIC -> if (isUp) "جارِ رفع صوت الجهاز 🔊" else "جارِ خفض صوت الجهاز 🔉"
      AppLanguage.ENGLISH -> if (isUp) "Increasing device volume 🔊" else "Decreasing device volume 🔉"
    }

    return ParsedActionResult(
      action = AndroidAction.AdjustVolume(direction = direction),
      feedbackMessage = msg
    )
  }

  // --- Flashlight ---
  private fun parseFlashlight(lower: String, language: AppLanguage): ParsedActionResult? {
    val isTurnOff = lower.contains("بکوژێنەرەوە") || lower.contains("خامۆش") ||
        lower.contains("اطفئ") || lower.contains("أطفئ") || lower.contains("off") ||
        lower.contains("داخە") || lower.contains("کوژانەوە")

    val isFlashlightQuery = lower.contains("فلاش") || lower.contains("لایت") ||
        lower.contains("کاشف") || lower.contains("كشاف") || lower.contains("flashlight") ||
        lower.contains("torch")

    if (!isFlashlightQuery) return null

    val isTurnOn = lower.contains("داگیرسێن") || lower.contains("هەڵکە") ||
        lower.contains("شغل") || lower.contains("تفعيل") || lower.contains("on") ||
        lower.contains("رووناک") || !isTurnOff

    val enabled = !isTurnOff && isTurnOn

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> if (enabled) "فلاش دابگیرسێنم 🔦" else "فلاش دەکوژێنمەوە 🔦"
      AppLanguage.ARABIC -> if (enabled) "جارِ تشغيل الفلاش (الكشاف) 🔦" else "جارِ إطفاء الفلاش 🔦"
      AppLanguage.ENGLISH -> if (enabled) "Turning on flashlight 🔦" else "Turning off flashlight 🔦"
    }

    return ParsedActionResult(
      action = AndroidAction.ToggleFlashlight(enabled = enabled),
      feedbackMessage = msg
    )
  }

  // --- Silent Mode ---
  private fun parseSilentMode(lower: String, language: AppLanguage): ParsedActionResult? {
    val isSilent = lower.contains("بێدەنگ") || lower.contains("صامت") ||
        lower.contains("silent") || lower.contains("mute") || lower.contains("كتم")

    val isUnmute = lower.contains("دەنگدار") || lower.contains("بێدەنگی لابدە") ||
        lower.contains("إلغاء الصامت") || lower.contains("تفعيل الصوت") ||
        lower.contains("unmute") || lower.contains("normal sound")

    val isVibrate = lower.contains("لەرزین") || lower.contains("ڤایبرەیشن") ||
        lower.contains("اهتزاز") || lower.contains("vibrate")

    if (!isSilent && !isUnmute && !isVibrate) return null

    val mode = when {
      isUnmute -> SilentModeType.NORMAL
      isVibrate -> SilentModeType.VIBRATE
      else -> SilentModeType.SILENT
    }

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> when (mode) {
        SilentModeType.SILENT -> "مۆبایلەکەت دەخرێتە دۆخی بێدەنگەوە 🔕"
        SilentModeType.NORMAL -> "دەنگی مۆبایل خرایەوە دۆخی ئاسایی 🔔"
        SilentModeType.VIBRATE -> "مۆبایلەکەت خرایە سەر دۆخی لەرزین 📳"
        SilentModeType.TOGGLE -> "دۆخی دەنگی مۆبایل گۆڕدرا"
      }
      AppLanguage.ARABIC -> when (mode) {
        SilentModeType.SILENT -> "تم تحويل الهاتف إلى الوضع الصامت 🔕"
        SilentModeType.NORMAL -> "تم تفعيل الوضع العام (رنين) 🔔"
        SilentModeType.VIBRATE -> "تم تفعيل وضع الاهتزاز 📳"
        SilentModeType.TOGGLE -> "تم تغيير وضع الصوت"
      }
      AppLanguage.ENGLISH -> when (mode) {
        SilentModeType.SILENT -> "Setting phone to Silent mode 🔕"
        SilentModeType.NORMAL -> "Restoring normal ringer volume 🔔"
        SilentModeType.VIBRATE -> "Setting phone to Vibrate mode 📳"
        SilentModeType.TOGGLE -> "Toggling silent mode"
      }
    }

    return ParsedActionResult(
      action = AndroidAction.SetSilentMode(mode),
      feedbackMessage = msg
    )
  }

  // --- Brightness ---
  private fun parseBrightness(lower: String, language: AppLanguage): ParsedActionResult? {
    val isBrightnessQuery = lower.contains("ڕووناکی") || lower.contains("رووناکی") ||
        lower.contains("سطوع") || lower.contains("إضاءة") || lower.contains("brightness")

    if (!isBrightnessQuery) return null

    val percentRegex = Regex("(\\d{1,3})\\s*%")
    val match = percentRegex.find(lower)
    val level = match?.groupValues?.get(1)?.toIntOrNull()

    val mode = when {
      level != null -> BrightnessMode.SET_LEVEL
      lower.contains("زیاد") || lower.contains("بەرز") || lower.contains("رفع") ||
          lower.contains("زيادة") || lower.contains("increase") || lower.contains("up") -> BrightnessMode.INCREASE
      lower.contains("کەم") || lower.contains("دابەز") || lower.contains("خفض") ||
          lower.contains("تقليل") || lower.contains("decrease") || lower.contains("down") -> BrightnessMode.DECREASE
      else -> BrightnessMode.OPEN_SETTINGS
    }

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> when (mode) {
        BrightnessMode.SET_LEVEL -> "ڕووناکی شاشە دەکرێتە $level%"
        BrightnessMode.INCREASE -> "ڕووناکی شاشە زیاد دەکرێت ☀️"
        BrightnessMode.DECREASE -> "ڕووناکی شاشە کەم دەکرێتەوە 🔅"
        BrightnessMode.OPEN_SETTINGS -> "کردنەوەی ڕێکخستنەکانی ڕووناکی شاشە"
      }
      AppLanguage.ARABIC -> when (mode) {
        BrightnessMode.SET_LEVEL -> "جارِ ضبط السطوع على $level%"
        BrightnessMode.INCREASE -> "جارِ زيادة سطوع الشاشة ☀️"
        BrightnessMode.DECREASE -> "جارِ خفض سطوع الشاشة 🔅"
        BrightnessMode.OPEN_SETTINGS -> "فتح إعدادات سطوع الشاشة"
      }
      AppLanguage.ENGLISH -> when (mode) {
        BrightnessMode.SET_LEVEL -> "Adjusting brightness to $level%"
        BrightnessMode.INCREASE -> "Increasing screen brightness ☀️"
        BrightnessMode.DECREASE -> "Lowering screen brightness 🔅"
        BrightnessMode.OPEN_SETTINGS -> "Opening display brightness settings"
      }
    }

    return ParsedActionResult(
      action = AndroidAction.AdjustBrightness(levelPercent = level, mode = mode),
      feedbackMessage = msg
    )
  }

  // --- Timer ---
  private fun parseTimer(lower: String, original: String, language: AppLanguage): ParsedActionResult? {
    val isTimer = lower.contains("تایمەر") || lower.contains("مؤقت") || lower.contains("timer")
    if (!isTimer) return null

    // Extract numbers and units (minutes / seconds)
    val minutesMatch = Regex("(\\d+)\\s*(خولەک|دەقیقە|دقيقة|دقائق|min|minute|minutes)").find(lower)
    val secondsMatch = Regex("(\\d+)\\s*(چرکە|ثانية|ثواني|sec|second|seconds)").find(lower)

    val minutes = minutesMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
    val seconds = secondsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

    val totalSeconds = if (minutes == 0 && seconds == 0) {
      // Look for a raw number
      val rawNum = Regex("(\\d+)").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 5
      rawNum * 60
    } else {
      (minutes * 60) + seconds
    }

    val displayMinutes = totalSeconds / 60
    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> "تایمەر دادەنێم بۆ ماوەی $displayMinutes خولەک ⏱️"
      AppLanguage.ARABIC -> "جارِ ضبط المؤقت لمدة $displayMinutes دقيقة ⏱️"
      AppLanguage.ENGLISH -> "Setting a timer for $displayMinutes minutes ⏱️"
    }

    return ParsedActionResult(
      action = AndroidAction.SetTimer(lengthSeconds = totalSeconds, label = "Baso AI Timer"),
      feedbackMessage = msg
    )
  }

  // --- Alarm ---
  private fun parseAlarm(lower: String, original: String, language: AppLanguage): ParsedActionResult? {
    val isAlarm = lower.contains("زەنگ") || lower.contains("منبه") || lower.contains("alarm") ||
        lower.contains("ئاگادارم بکەرەوە") || lower.contains("wake me")

    if (!isAlarm) return null

    // Extract time (e.g. 7, 7:30, 07:00, 10)
    var hour = 7
    var minute = 0

    val timeRegex = Regex("(\\d{1,2})[:.](\\d{2})")
    val timeMatch = timeRegex.find(lower)
    if (timeMatch != null) {
      hour = timeMatch.groupValues[1].toIntOrNull() ?: 7
      minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
    } else {
      val hourRegex = Regex("(سەعات|کاتژمێر|ساعة|الساعة|at)\\s*(\\d{1,2})")
      val hourMatch = hourRegex.find(lower)
      if (hourMatch != null) {
        hour = hourMatch.groupValues[2].toIntOrNull() ?: 7
      } else {
        val anyNum = Regex("(\\d{1,2})").find(lower)?.groupValues?.get(1)?.toIntOrNull()
        if (anyNum != null && anyNum in 1..24) {
          hour = anyNum
        }
      }
    }

    // PM / evening adjustment
    val isPm = lower.contains("ئێوارە") || lower.contains("شەو") ||
        lower.contains("مساء") || lower.contains("ليلا") || lower.contains("pm")
    if (isPm && hour in 1..11) {
      hour += 12
    }

    val formattedTime = String.format("%02d:%02d", hour, minute)
    val msg = when (language) {
      AppLanguage.KURDISH_SORANI -> "زەنگی کاتژمێر دادەنێم بۆ $formattedTime ⏰"
      AppLanguage.ARABIC -> "جارِ ضبط المنبه على الساعة $formattedTime ⏰"
      AppLanguage.ENGLISH -> "Setting an alarm for $formattedTime ⏰"
    }

    return ParsedActionResult(
      action = AndroidAction.SetAlarm(
        hour = hour,
        minutes = minute,
        label = "Baso AI Alarm",
        skipUi = false
      ),
      feedbackMessage = msg
    )
  }

  // --- Calendar Event & Reminder ---
  private fun parseCalendar(lower: String, original: String, language: AppLanguage): ParsedActionResult? {
    val isCalendar = lower.contains("دانیشتن") || lower.contains("کۆبوونەوە") ||
        lower.contains("تۆمار بکە") || lower.contains("ڕۆژمێر") || lower.contains("اجتماع") ||
        lower.contains("موعد") || lower.contains("التقويم") || lower.contains("calendar") ||
        lower.contains("meeting") || lower.contains("appointment") || lower.contains("schedule")

    if (!isCalendar) return null

    // Determine target day: tomorrow vs today vs specific
    val calendar = Calendar.getInstance()
    val isTomorrow = lower.contains("سبەی") || lower.contains("سبەینێ") ||
        lower.contains("غداً") || lower.contains("بكرة") || lower.contains("tomorrow")

    if (isTomorrow) {
      calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    // Extract hour
    var eventHour = 10
    var eventMinute = 0
    val timeRegex = Regex("(\\d{1,2})[:.](\\d{2})")
    val timeMatch = timeRegex.find(lower)
    if (timeMatch != null) {
      eventHour = timeMatch.groupValues[1].toIntOrNull() ?: 10
      eventMinute = timeMatch.groupValues[2].toIntOrNull() ?: 0
    } else {
      val hourRegex = Regex("(سەعات|کاتژمێر|ساعة|الساعة|at)\\s*(\\d{1,2})")
      val hourMatch = hourRegex.find(lower)
      if (hourMatch != null) {
        eventHour = hourMatch.groupValues[2].toIntOrNull() ?: 10
      } else {
        val anyNum = Regex("(\\d{1,2})").find(lower)?.groupValues?.get(1)?.toIntOrNull()
        if (anyNum != null && anyNum in 1..24) {
          eventHour = anyNum
        }
      }
    }

    val isPm = lower.contains("ئێوارە") || lower.contains("شەو") ||
        lower.contains("مساء") || lower.contains("pm")
    if (isPm && eventHour in 1..11) {
      eventHour += 12
    }

    calendar.set(Calendar.HOUR_OF_DAY, eventHour)
    calendar.set(Calendar.MINUTE, eventMinute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startEpoch = calendar.timeInMillis
    val endEpoch = startEpoch + (60 * 60 * 1000) // 1 hour duration

    // Extract title or default based on query
    val title = when {
      lower.contains("دانیشتن") -> "دانیشتن (کۆبوونەوە)"
      lower.contains("کۆبوونەوە") -> "کۆبوونەوە"
      lower.contains("اجتماع") -> "اجتماع عمل"
      lower.contains("موعد") -> "موعد"
      lower.contains("meeting") -> "Meeting"
      lower.contains("doctor") -> "Doctor Appointment"
      else -> "تۆماری Baso AI"
    }

    val timeDisplay = if (isTomorrow) {
      "سبەینێ کاتژمێر %02d:%02d".format(eventHour, eventMinute)
    } else {
      "کاتژمێر %02d:%02d".format(eventHour, eventMinute)
    }

    val msg = when (language) {
      AppLanguage.KURDISH_SORANI ->
        "ڕۆژمێری ئەندرۆید دەکەمەوە بۆ تۆمارکردنی \"$title\" بۆ $timeDisplay 📅"
      AppLanguage.ARABIC ->
        "جارِ فتح التقويم لتسجيل \"$title\" غداً الساعة %02d:%02d 📅".format(eventHour, eventMinute)
      AppLanguage.ENGLISH ->
        "Opening Android Calendar to schedule \"$title\" for %s at %02d:%02d 📅".format(
          if (isTomorrow) "tomorrow" else "today",
          eventHour,
          eventMinute
        )
    }

    return ParsedActionResult(
      action = AndroidAction.CreateCalendarEvent(
        title = title,
        description = "تۆمارکراو لە ڕێگەی Baso AI",
        startEpochMillis = startEpoch,
        endEpochMillis = endEpoch,
        allDay = false,
        formattedTimeDisplay = timeDisplay
      ),
      feedbackMessage = msg
    )
  }
}
