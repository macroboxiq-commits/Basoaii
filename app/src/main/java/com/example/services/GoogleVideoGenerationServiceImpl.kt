package com.example.services

import android.util.Base64
import android.util.Log
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.GenerationResult
import com.example.domain.model.VideoDuration
import com.example.domain.model.VideoStyle
import com.example.domain.service.GeneratedVideoResult
import com.example.domain.service.VideoGenerationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GoogleVideoGenerationServiceImpl : VideoGenerationService {
  private val tag = "VideoGenerationService"
  private val primaryVeoModel = "models/veo-3.1-fast-generate-preview"
  private val fallbackVeoModel = "models/veo-3.1-generate-preview"

  override suspend fun createVideo(
    prompt: String,
    referenceImageBytes: ByteArray?,
    referenceMimeType: String?,
    style: VideoStyle,
    aspectRatio: AspectRatioOption,
    duration: VideoDuration
  ): GenerationResult<GeneratedVideoResult> = withContext(Dispatchers.IO) {
    if (!CreativeApiClient.isKeyConfigured()) {
      return@withContext GenerationResult.Error(
        message = "دروستکردنی ڤیدیۆ لە ڕێگەی مۆدێلی گووگڵ (Google Veo) پێویستی بە چالاککردنی کلیلی تایبەتی Google AI هەیە لە بەشی نهێنییەکان (AI Studio Secrets panel) لەبەر تێچوو و پرۆسێسکردنی قورسی سێرڤەر. دەتوانیت لە خوارەوە بە دوگمەی مۆدی بێسنوور هەمان وەسف ڕاستەوخۆ بکەیت بە وێنەی کوالیتی بەرز.",
        technicalDetail = "API key missing in BuildConfig.GEMINI_API_KEY.",
        isConfigurationRequired = true
      )
    }

    val finalPrompt = if (style.promptModifier.isNotBlank()) {
      "$prompt, style: ${style.promptModifier}"
    } else {
      prompt
    }

    val ratioString = when (aspectRatio) {
      AspectRatioOption.PORTRAIT_9_16, AspectRatioOption.PORTRAIT_4_5 -> "9:16"
      AspectRatioOption.SQUARE -> "1:1"
      else -> "16:9"
    }

    val configObj = JSONObject().apply {
      put("numberOfVideos", 1)
      put("aspectRatio", ratioString)
      put("durationSeconds", duration.seconds)
    }

    val payload = JSONObject().apply {
      put("prompt", finalPrompt)
      put("config", configObj)

      // Image to Video support
      if (referenceImageBytes != null && referenceImageBytes.isNotEmpty()) {
        val base64Image = Base64.encodeToString(referenceImageBytes, Base64.NO_WRAP)
        put("image", JSONObject().apply {
          put("inlineData", JSONObject().apply {
            put("mimeType", referenceMimeType ?: "image/jpeg")
            put("data", base64Image)
          })
        })
      }
    }

    // Call official Veo endpoint
    var (success, responseStr) = CreativeApiClient.postJson(
      endpoint = "$primaryVeoModel:generateVideos",
      jsonPayload = payload.toString()
    )

    if (!success && (responseStr.contains("404") || responseStr.contains("NOT_FOUND"))) {
      Log.w(tag, "Veo fast model not found (404), trying standard Veo preview: $fallbackVeoModel")
      val fallbackResult = CreativeApiClient.postJson(
        endpoint = "$fallbackVeoModel:generateVideos",
        jsonPayload = payload.toString()
      )
      success = fallbackResult.first
      responseStr = fallbackResult.second
    }

    if (!success) {
      val isConfig = responseStr.contains("404") ||
          responseStr.contains("NOT_FOUND") ||
          responseStr.contains("403") ||
          responseStr.contains("PERMISSION_DENIED") ||
          responseStr.contains("ACCESS_TOKEN_SCOPE_INSUFFICIENT")

      val userMessage = if (isConfig) {
        "مۆدێلی دروستکردنی ڤیدیۆی Google Veo (veo-3.1-fast-generate-preview) لە ئێستادا پێویستی بە چالاککردنی پرۆژە هەیە لە Google AI Studio بە بەشداریکردنی لە تاقیکاری تایبەتی (Early Access) یان بەستنەوەی هەژماری خاوەن کرێدیتی پەسەندکراو.\nتکایە دڵنیابە لەوەی دەستپێڕاگەیشتنت پێدراوە لە ناو Google AI Studio."
      } else {
        "هەڵە لە دروستکردنی ڤیدیۆ: $responseStr"
      }

      return@withContext GenerationResult.Error(
        message = userMessage,
        technicalDetail = responseStr,
        isConfigurationRequired = isConfig
      )
    }

    try {
      val json = JSONObject(responseStr)
      val operationName = json.optString("name", "")

      if (operationName.isNotBlank()) {
        GenerationResult.Success(
          GeneratedVideoResult(
            operationName = operationName,
            isDone = false,
            statusMessage = "🎬 پڕۆسەی دروستکردنی ڤیدیۆکە دەستی پێکرد..."
          )
        )
      } else {
        GenerationResult.Error(
          message = "وەڵامی چاوەڕواننەکراو لە مۆدێلی Veo وەرگیرا.",
          technicalDetail = responseStr
        )
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to parse video generation response", e)
      GenerationResult.Error("هەڵە لە دەستپێکردنی ڤیدیۆ: ${e.localizedMessage}")
    }
  }

  override suspend fun pollVideoOperation(
    operationName: String
  ): GenerationResult<GeneratedVideoResult> = withContext(Dispatchers.IO) {
    if (!CreativeApiClient.isKeyConfigured()) {
      return@withContext GenerationResult.Error(
        message = "کلیلی API بەردەست نییە.",
        isConfigurationRequired = true
      )
    }

    try {
      val (success, responseStr) = CreativeApiClient.getJson(operationName)
      if (!success) {
        return@withContext GenerationResult.Error(
          message = "نەتوانرا پشکنین بۆ دۆخی ڤیدیۆکە بکرێت: $responseStr",
          technicalDetail = responseStr
        )
      }

      val json = JSONObject(responseStr)
      val isDone = json.optBoolean("done", false)

      if (!isDone) {
        val metadata = json.optJSONObject("metadata")
        val progress = metadata?.optString("progress", "لە پرۆسەدایە")
        return@withContext GenerationResult.Success(
          GeneratedVideoResult(
            operationName = operationName,
            isDone = false,
            statusMessage = "🎬 ڤیدیۆکە لە پرۆسەدایە ($progress)..."
          )
        )
      }

      if (json.has("error")) {
        val errorObj = json.getJSONObject("error")
        val errorMsg = errorObj.optString("message", "هەڵە لە پرۆسەی دروستکردنی ڤیدیۆ")
        return@withContext GenerationResult.Error(
          message = "پرۆسەی دروستکردنی ڤیدیۆ سەرکەوتوو نەبوو: $errorMsg",
          technicalDetail = responseStr
        )
      }

      val responseObj = json.optJSONObject("response")
      val generatedVideos = responseObj?.optJSONArray("generatedVideos")
      val firstVideo = generatedVideos?.optJSONObject(0)?.optJSONObject("video")
      val videoUri = firstVideo?.optString("uri")

      if (!videoUri.isNullOrBlank()) {
        GenerationResult.Success(
          GeneratedVideoResult(
            videoUri = videoUri,
            operationName = operationName,
            isDone = true,
            statusMessage = "✓ ڤیدیۆکە ئامادەیە"
          )
        )
      } else {
        GenerationResult.Error(
          message = "ڤیدیۆ لە ئەنجامدا نەدۆزرایەوە.",
          technicalDetail = responseStr
        )
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to poll video operation", e)
      GenerationResult.Error("هەڵە لە وەرگرتنی ڤیدیۆ: ${e.localizedMessage}")
    }
  }
}
