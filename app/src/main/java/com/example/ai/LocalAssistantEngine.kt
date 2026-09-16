package com.example.ai

import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.services.SystemActionParser
import kotlinx.coroutines.delay

/**
 * Local AI Assistant engine for Baso AI.
 * Provides thoughtful, multilingual responses in Kurdish Sorani, Arabic, and English,
 * and parses native system action requests.
 */
class LocalAssistantEngine : AiEngine {

  override suspend fun generateResponse(
    prompt: String,
    history: List<ChatMessage>,
    language: AppLanguage
  ): String {
    return processPrompt(prompt, history, language).text
  }

  override suspend fun processPrompt(
    prompt: String,
    history: List<ChatMessage>,
    language: AppLanguage
  ): AssistantResponse {
    // 1. Check if the user is requesting a system action or device control
    val parsedAction = SystemActionParser.parsePrompt(prompt, language)
    if (parsedAction != null) {
      delay(300)
      return AssistantResponse(
        text = parsedAction.feedbackMessage,
        systemAction = parsedAction.action
      )
    }

    // 2. Otherwise simulate conversational assistant reasoning
    delay(650)

    val cleanPrompt = prompt.trim()
    val lower = cleanPrompt.lowercase()

    val responseText = when (language) {
      AppLanguage.KURDISH_SORANI -> generateKurdishResponse(cleanPrompt, lower)
      AppLanguage.ARABIC -> generateArabicResponse(cleanPrompt, lower)
      AppLanguage.ENGLISH -> generateEnglishResponse(cleanPrompt, lower)
    }

    return AssistantResponse(
      text = responseText,
      systemAction = null
    )
  }

  private fun generateKurdishResponse(prompt: String, lower: String): String {
    return when {
      lower.contains("سڵاو") || lower.contains("سلاو") || lower.contains("hello") || lower.contains("چۆنی") ->
        "سڵاو! زۆر خۆشحاڵم بە بینینت. من Baso AI ـم، یاریدەدەری زیرەکی تایبەتیت. دەتوانم یارمەتیت بدەم لە وەڵامدانەوەی پرسیارەکان، بەڕێوەبردنی کارەکان، دانانی زەنگ و ڕۆژمێر، و کۆنترۆڵکردنی ئامێرەکەت. چیت پێویستە ئەمڕۆ؟"

      lower.contains("کێیت") || lower.contains("ناوی تۆ") || lower.contains("baso") ->
        "من **Baso AI** ـم — یاریدەدەرێکی کەسیی زیرەک کە دروستکراوم بۆ ئەوەی ڕۆژەکەت ئاسانتر، خێراتر و بەرهەمدارتر بکەم. ئێستا توانای ڕێکخستنی ڕۆژمێر، زەنگ، فلاش و دەنگی مۆبایلەکەتم هەیە لەگەڵ وەڵامدانەوە بە زمانی کوردی (سۆرانی)، عەرەبی و ئینگلیزی."

      lower.contains("کەش") || lower.contains("weather") ->
        "بەپێی پێشبینییە سەرەتاییەکان، ئاسمان ساماڵ و پلەی گەرما لە ئاستێکی گونجاودایە. لە وەشانی داهاتوودا خزمەتگوزاری ڕاستەوخۆی کەشوهەوا چالاک دەبێت!"

      lower.contains("بیرخستنەوە") || lower.contains("تێبینی") || lower.contains("reminder") || lower.contains("note") ->
        "دەتوانیت بڵێیت: \"دانیشتنێکم بۆ سبەینێ سەعات 10 تۆمار بکە\" یان \"زەنگ دابنێ بۆ سەعات 7\" بۆ ئەوەی ڕاستەوخۆ لە سیستەمەکەدا جێبەجێی بکەم!"

      lower.contains("تەکنەلۆژیا") || lower.contains("کۆد") || lower.contains("code") || lower.contains("بەرنامە") ->
        "تەکنەلۆژیا بە خێرایی لە پێشکەوتندایە! من دەتوانم یارمەتیت بدەم لە داڕشتنی کۆد، شیکردنەوەی داتا، و ئامۆژگاری بۆ پرۆژەکانی بەرنامەسازی."

      else ->
        "سوپاس بۆ پرسیارەکەت دەربارەی: \"$prompt\"\n\nوەک Baso AI، من ئامادەم بۆ یارمەتیدانت لە گفتوگۆ یان ئەنجامدانی کردارە ئەندرۆیدییەکان (وەک ڕۆژژمێر، زەنگ، فلاش، و بێدەنگکردن)."
    }
  }

  private fun generateArabicResponse(prompt: String, lower: String): String {
    return when {
      lower.contains("مرحبا") || lower.contains("سلام") || lower.contains("أهلا") || lower.contains("كيفك") ->
        "أهلاً وسهلاً بك! 👋 أنا Baso AI، مساعدك الشخصي الذكي. يسعدني مساعدتك في الإجابة عن استفساراتك، تنظيم مواعيدك وضبط المنبه والتحكم بخصائص الهاتف. بمَ يمكنني مساعدتك الآن؟"

      lower.contains("من أنت") || lower.contains("اسمك") || lower.contains("baso") ->
        "أنا **Baso AI** — مساعدك الشخصي الذكي، مع قدرات جديدة للتحكم بالنظام، تسجيل المواعيد في التقويم، تشغيل الفلاش، وضبط المنبه باللغات الكردية، العربية والإنجليزية."

      lower.contains("طقس") || lower.contains("weather") ->
        "وفق المؤشرات الحالية، الأجواء مستقرة ومعتدلة. سيتم تفعيل ميزة الطقس المباشرة قريباً!"

      lower.contains("تذكير") || lower.contains("ملاحظة") || lower.contains("note") ->
        "يمكنك القول: \"سجل اجتماع غداً الساعة 10\" أو \"اضبط منبه الساعة 7\" وسأقوم بتنفيذ الأمر على الفور!"

      else ->
        "شكراً على استفسارك حول: \"$prompt\"\n\nبصفتي Baso AI، يسعدني التعمق أكثر في هذا الموضوع أو تنفيذ أوامر النظام الخاصة بجهازك."
    }
  }

  private fun generateEnglishResponse(prompt: String, lower: String): String {
    return when {
      lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
        "Hello! 👋 I am **Baso AI**, your smart personal assistant. I can answer questions, schedule calendar events, set alarms, toggle the flashlight, adjust silent mode, and keep you productive. How can I assist you today?"

      lower.contains("who are you") || lower.contains("what is baso") ->
        "I am **Baso AI** — an intelligent personal assistant capable of executing real Android device actions (Calendar, Alarms, Flashlight, Ringer mode) and conversing naturally in Kurdish Sorani, Arabic, and English."

      lower.contains("weather") ->
        "Current forecasts show pleasant conditions with clear skies. Real-time telemetry widgets will be integrated in upcoming releases!"

      lower.contains("reminder") || lower.contains("note") ->
        "You can say: \"Schedule a meeting tomorrow at 10 am\" or \"Set an alarm for 7 am\" and I'll execute it immediately!"

      lower.contains("code") || lower.contains("android") || lower.contains("tech") ->
        "Technology and mobile engineering are exciting domains! I can help you architect components, write Kotlin & Compose code, and structure scalable applications."

      else ->
        "Thank you for sharing: \"$prompt\"\n\nAs Baso AI, I'm ready to assist you or perform device commands whenever you need."
    }
  }
}
