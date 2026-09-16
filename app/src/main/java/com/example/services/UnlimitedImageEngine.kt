package com.example.services

import android.util.Log
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.GenerationResult
import com.example.domain.model.ImageStyle
import com.example.domain.service.GeneratedImageResult
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object UnlimitedImageEngine {
  private const val TAG = "UnlimitedImageEngine"

  // Dedicated client with realistic headers and generous timeouts
  private val unlimitedHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(45, TimeUnit.SECONDS)
      .readTimeout(90, TimeUnit.SECONDS)
      .writeTimeout(45, TimeUnit.SECONDS)
      .followRedirects(true)
      .followSslRedirects(true)
      .build()
  }

  suspend fun generateImage(
    prompt: String,
    style: ImageStyle = ImageStyle.NONE,
    aspectRatio: AspectRatioOption = AspectRatioOption.SQUARE
  ): GenerationResult<GeneratedImageResult> = withContext(Dispatchers.IO) {
    try {
      val enrichedPrompt = enrichPrompt(prompt.trim(), style)
      val (width, height) = calculateDimensions(aspectRatio)
      val seed = (System.currentTimeMillis() % 10000000) + Random.nextInt(100000)
      val encodedPrompt = URLEncoder.encode(enrichedPrompt, "UTF-8")

      // URLs to try: 1) Flux model, 2) Standard model
      val candidateUrls = listOf(
        "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&seed=$seed&model=flux&nologo=true",
        "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&seed=$seed&nologo=true"
      )

      var lastError = ""
      for (url in candidateUrls) {
        val request = Request.Builder()
          .url(url)
          .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36 BasoAI/1.0")
          .header("Accept", "image/jpeg, image/png, image/webp, */*")
          .get()
          .build()

        try {
          unlimitedHttpClient.newCall(request).execute().use { response ->
            val code = response.code
            if (response.isSuccessful) {
              val body = response.body
              val bytes = body?.bytes()
              if (bytes != null && bytes.size > 1500) {
                val mimeType = response.header("Content-Type")?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
                Log.d(TAG, "Unlimited image generated successfully: ${bytes.size} bytes from $url")
                return@withContext GenerationResult.Success(
                  GeneratedImageResult(
                    imageBytes = bytes,
                    mimeType = mimeType,
                    revisedPrompt = "⚡ مۆدی بێسنوور (Unlimited Engine) • $enrichedPrompt"
                  )
                )
              } else {
                lastError = "Response body was too small or empty (${bytes?.size ?: 0} bytes)"
              }
            } else {
              lastError = "HTTP $code: ${response.message}"
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Request to $url failed, will try next: ${e.localizedMessage}")
          lastError = e.localizedMessage ?: "Connection error"
        }
      }

      GenerationResult.Error(
        message = "نەتوانرا وێنەکە بە مۆدی بێسنوور دروست بکرێت: $lastError. تکایە پاش کەمێکی تر هەوڵ بدەرەوە.",
        technicalDetail = lastError
      )
    } catch (e: Exception) {
      Log.e(TAG, "Failed in generateImage", e)
      GenerationResult.Error("هەڵە لە دروستکردنی وێنەی بێسنوور: ${e.localizedMessage}")
    }
  }

  private fun calculateDimensions(aspectRatio: AspectRatioOption): Pair<Int, Int> {
    return when (aspectRatio) {
      AspectRatioOption.SQUARE -> Pair(1024, 1024)
      AspectRatioOption.PORTRAIT_4_5 -> Pair(768, 1024)
      AspectRatioOption.PORTRAIT_9_16 -> Pair(576, 1024)
      AspectRatioOption.LANDSCAPE_16_9 -> Pair(1024, 576)
      AspectRatioOption.LANDSCAPE_4_3 -> Pair(1024, 768)
    }
  }

  fun enrichPrompt(userPrompt: String, style: ImageStyle): String {
    val clean = userPrompt.trim()
    val isKurdishOrArabic = clean.any { it in '\u0600'..'\u06FF' }

    val visualKeywords = mutableListOf<String>()

    if (isKurdishOrArabic) {
      val lower = clean.lowercase()
      // Landmarks & Cities
      if (lower.contains("هەولێر") || lower.contains("اربيل") || lower.contains("erbil")) visualKeywords.add("Erbil Citadel historical Kurdish fortress")
      if (lower.contains("قەڵا") || lower.contains("قلعة")) visualKeywords.add("ancient stone fortress citadel castle")
      if (lower.contains("سلێمانی") || lower.contains("سليمانية")) visualKeywords.add("Sulaymaniyah mountains cityscape")
      if (lower.contains("دهۆک") || lower.contains("دهوك")) visualKeywords.add("Duhok valley scenery")
      if (lower.contains("کوردستان") || lower.contains("كردستان")) visualKeywords.add("Kurdistan landscape mountains scenery")

      // Nature & Weather
      if (lower.contains("شاخ") || lower.contains("چیا") || lower.contains("جبل")) visualKeywords.add("magnificent mountains landscape, dramatic peaks")
      if (lower.contains("بەفر") || lower.contains("ثلج")) visualKeywords.add("snowy winter wonderland, crisp snow")
      if (lower.contains("باران") || lower.contains("مطر")) visualKeywords.add("rainy atmospheric wet reflections")
      if (lower.contains("پایز") || lower.contains("خريف")) visualKeywords.add("autumn golden foliage, falling leaves")
      if (lower.contains("بەهار") || lower.contains("ربيع")) visualKeywords.add("spring blooming blossom nature")
      if (lower.contains("دارستان") || lower.contains("غابة")) visualKeywords.add("lush green forest trees")
      if (lower.contains("دەریا") || lower.contains("بحر")) visualKeywords.add("vast sparkling sea ocean waves")
      if (lower.contains("ڕووبار") || lower.contains("نهر")) visualKeywords.add("clear flowing river stream")
      if (lower.contains("گوڵ") || lower.contains("ورد")) visualKeywords.add("vibrant blooming flowers garden")
      if (lower.contains("خۆر") || lower.contains("ڕۆژئاوا") || lower.contains("غروب")) visualKeywords.add("golden hour warm sunset dramatic glow")
      if (lower.contains("شەو") || lower.contains("ئەستێرە") || lower.contains("ليل")) visualKeywords.add("starry night sky, celestial glow")
      if (lower.contains("مانگ") || lower.contains("قمر")) visualKeywords.add("glowing full moon")

      // Animals
      if (lower.contains("شێر") || lower.contains("اسد")) visualKeywords.add("majestic lion portrait, detailed mane")
      if (lower.contains("پڵنگ") || lower.contains("نمر")) visualKeywords.add("wild leopard panther")
      if (lower.contains("ئەسپ") || lower.contains("حصان")) visualKeywords.add("majestic galloping horse")
      if (lower.contains("پشیلە") || lower.contains("قطة")) visualKeywords.add("cute adorable cat kitten portrait")
      if (lower.contains("سەگ") || lower.contains("كلب")) visualKeywords.add("friendly dog puppy")
      if (lower.contains("هەڵۆ") || lower.contains("نسر")) visualKeywords.add("soaring eagle with sharp gaze")
      if (lower.contains("گورگ") || lower.contains("ذئب")) visualKeywords.add("wild wolf in wilderness")

      // People & Culture
      if (lower.contains("جلوبەرگی کوردی") || lower.contains("جلی کوردی") || lower.contains("کوردی")) visualKeywords.add("traditional vibrant Kurdish ethnic attire and dress")
      if (lower.contains("کچ") || lower.contains("فتاة") || lower.contains("ژن")) visualKeywords.add("portrait of a graceful woman")
      if (lower.contains("کوڕ") || lower.contains("شاب") || lower.contains("پیاو")) visualKeywords.add("portrait of a handsome man")
      if (lower.contains("منداڵ") || lower.contains("طفل")) visualKeywords.add("cheerful cute child portrait")

      // Tech & Vehicles
      if (lower.contains("سایبەرفانک") || lower.contains("سايبربانك")) visualKeywords.add("cyberpunk neon lighting, futuristic city")
      if (lower.contains("سەیارە") || lower.contains("ئۆتۆمبێل") || lower.contains("سيارة")) visualKeywords.add("sleek modern luxury sports car")
      if (lower.contains("ڕۆبۆت") || lower.contains("روبوت")) visualKeywords.add("futuristic sleek robot android")
      if (lower.contains("بۆشایی") || lower.contains("فضاء")) visualKeywords.add("deep space galaxy cosmic nebula")
    }

    val styleDetail = when {
      style.promptModifier.isNotBlank() -> style.promptModifier
      else -> "highly detailed, 8k resolution, crisp focus, cinematic atmospheric lighting, artistic composition"
    }

    val keywordsStr = if (visualKeywords.isNotEmpty()) visualKeywords.joinToString(", ") else ""
    return buildString {
      append(clean)
      if (keywordsStr.isNotBlank()) {
        append(", ")
        append(keywordsStr)
      }
      append(", ")
      append(styleDetail)
    }
  }
}
