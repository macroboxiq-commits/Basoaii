package com.example.services

import android.util.Base64
import android.util.Log
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.GenerationResult
import com.example.domain.model.ImageGenerationEngine
import com.example.domain.model.ImageStyle
import com.example.domain.service.GeneratedImageResult
import com.example.domain.service.ImageGenerationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GoogleImageGenerationServiceImpl : ImageGenerationService {
  private val tag = "ImageGenerationService"
  private val primaryModel = "models/gemini-2.5-flash-image"
  private val fallbackModel = "models/gemini-3.1-flash-image-preview"

  override suspend fun generateImage(
    prompt: String,
    style: ImageStyle,
    aspectRatio: AspectRatioOption,
    numberOfImages: Int,
    engine: ImageGenerationEngine
  ): GenerationResult<GeneratedImageResult> = withContext(Dispatchers.IO) {
    // 1. If Unlimited engine is requested directly, use UnlimitedImageEngine
    if (engine == ImageGenerationEngine.UNLIMITED_AI) {
      Log.i(tag, "Generating image via Unlimited AI Engine directly (no quota/rate limits)")
      return@withContext UnlimitedImageEngine.generateImage(prompt, style, aspectRatio)
    }

    // 2. Google Gemini Engine: check if key is configured, fallback to Unlimited if not
    if (!CreativeApiClient.isKeyConfigured()) {
      Log.w(tag, "Gemini API key is not configured, automatically falling back to Unlimited AI Engine")
      return@withContext UnlimitedImageEngine.generateImage(prompt, style, aspectRatio)
    }

    val finalPrompt = if (style.promptModifier.isNotBlank()) {
      "$prompt, in ${style.labelEn} style: ${style.promptModifier}"
    } else {
      prompt
    }

    val ratioString = when (aspectRatio) {
      AspectRatioOption.SQUARE -> "1:1"
      AspectRatioOption.PORTRAIT_4_5 -> "3:4"
      AspectRatioOption.PORTRAIT_9_16 -> "9:16"
      AspectRatioOption.LANDSCAPE_16_9 -> "16:9"
      AspectRatioOption.LANDSCAPE_4_3 -> "4:3"
    }

    val payload = JSONObject().apply {
      put("contents", JSONArray().apply {
        put(JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", finalPrompt) })
          })
        })
      })
      put("generationConfig", JSONObject().apply {
        put("responseModalities", JSONArray().apply {
          put("TEXT")
          put("IMAGE")
        })
        put("imageConfig", JSONObject().apply {
          put("aspectRatio", ratioString)
        })
      })
    }

    // Try primary model first, fallback if not available
    var (success, responseStr) = CreativeApiClient.postJson(
      endpoint = "$primaryModel:generateContent",
      jsonPayload = payload.toString()
    )

    if (!success && (responseStr.contains("404") || responseStr.contains("NOT_FOUND"))) {
      Log.w(tag, "Primary image model returned 404, attempting fallback model $fallbackModel")
      val fallbackResult = CreativeApiClient.postJson(
        endpoint = "$fallbackModel:generateContent",
        jsonPayload = payload.toString()
      )
      success = fallbackResult.first
      responseStr = fallbackResult.second
    }

    // Automatic quota exceeded / rate limit fallback
    if (!success) {
      val isRateLimitedOrExhausted = responseStr.contains("429") ||
        responseStr.contains("RESOURCE_EXHAUSTED") ||
        responseStr.contains("Quota exceeded") ||
        responseStr.contains("403") ||
        responseStr.contains("PERMISSION_DENIED")

      if (isRateLimitedOrExhausted) {
        Log.i(tag, "Gemini rate limited or quota exceeded ($responseStr). Seamlessly falling back to Unlimited AI Engine.")
        val unlimitedResult = UnlimitedImageEngine.generateImage(prompt, style, aspectRatio)
        if (unlimitedResult is GenerationResult.Success) {
          return@withContext unlimitedResult
        }
      }

      val isConfig = responseStr.contains("403") || responseStr.contains("PERMISSION_DENIED") || responseStr.contains("NOT_FOUND")
      val userMessage = when {
        responseStr.contains("429") || responseStr.contains("RESOURCE_EXHAUSTED") ->
          "ڕێژەی داواکارییەکان تێپەڕیوە (Quota exceeded). تکایە کەمێکی تر هەوڵ بدەرەوە."
        responseStr.contains("SAFETY") || responseStr.contains("blocked") ->
          "داواکارییەکەت ڕەتکرایەوە بەهۆی سیاسەتی پاراستنی ناوەڕۆک (Safety Filter)."
        isConfig ->
          "مۆدێلی دروستکردنی وێنەی Google AI پێویستی بە بەستنەوەی کلیلێکی ڕێپێدراوە لە AI Studio."
        else ->
          "هەڵە لە دروستکردنی وێنە: $responseStr"
      }
      return@withContext GenerationResult.Error(
        message = userMessage,
        technicalDetail = responseStr,
        isConfigurationRequired = isConfig
      )
    }

    try {
      val json = JSONObject(responseStr)
      val candidates = json.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        val promptFeedback = json.optJSONObject("promptFeedback")
        val blockReason = promptFeedback?.optString("blockReason")
        return@withContext GenerationResult.Error(
          message = "وێنە دروست نەکرا. هۆکار: ${blockReason ?: "داواکارییەکە ڕەتکرایەوە"}",
          technicalDetail = responseStr
        )
      }

      val content = candidates.getJSONObject(0).optJSONObject("content")
      val parts = content?.optJSONArray("parts")

      var imageBytes: ByteArray? = null
      var mimeType = "image/png"
      var textDescription: String? = null

      if (parts != null) {
        for (i in 0 until parts.length()) {
          val part = parts.getJSONObject(i)
          if (part.has("inlineData")) {
            val inlineData = part.getJSONObject("inlineData")
            mimeType = inlineData.optString("mimeType", "image/png")
            val base64Data = inlineData.getString("data")
            imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
            break
          } else if (part.has("text")) {
            textDescription = part.getString("text")
          }
        }
      }

      if (imageBytes != null && imageBytes.isNotEmpty()) {
        GenerationResult.Success(
          GeneratedImageResult(
            imageBytes = imageBytes,
            mimeType = mimeType,
            revisedPrompt = textDescription
          )
        )
      } else {
        GenerationResult.Error(
          message = "مۆدێلەکە وەڵامی وێنەی نەگەڕاندەوە (تەنها دەق: ${textDescription?.take(100) ?: "هیچ"}).",
          technicalDetail = responseStr
        )
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to parse image response", e)
      GenerationResult.Error("هەڵە لە خوێندنەوەی داتای وێنەکە: ${e.localizedMessage}")
    }
  }
}
