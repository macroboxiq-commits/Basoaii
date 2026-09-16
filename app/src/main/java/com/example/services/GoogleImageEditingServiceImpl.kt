package com.example.services

import android.util.Base64
import android.util.Log
import com.example.domain.model.GenerationResult
import com.example.domain.service.GeneratedImageResult
import com.example.domain.service.ImageEditingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GoogleImageEditingServiceImpl : ImageEditingService {
  private val tag = "ImageEditingService"
  private val primaryModel = "models/gemini-2.5-flash-image"
  private val fallbackModel = "models/gemini-3.1-flash-image-preview"

  override suspend fun editImage(
    originalImageBytes: ByteArray,
    mimeType: String,
    instruction: String
  ): GenerationResult<GeneratedImageResult> = withContext(Dispatchers.IO) {
    if (!CreativeApiClient.isKeyConfigured()) {
      return@withContext GenerationResult.Error(
        message = "تکایە کلیلی دروستی Google AI/Gemini لە AI Studio Secrets دابنێ بۆ دەستکاریکردنی وێنە.",
        technicalDetail = "API key missing in BuildConfig.GEMINI_API_KEY.",
        isConfigurationRequired = true
      )
    }

    if (originalImageBytes.isEmpty()) {
      return@withContext GenerationResult.Error("تکایە سەرەتا وێنەیەک هەڵبژێرە بۆ دەستکاریکردن.")
    }

    val base64Data = Base64.encodeToString(originalImageBytes, Base64.NO_WRAP)
    val cleanMime = if (mimeType.isBlank()) "image/jpeg" else mimeType

    val payload = JSONObject().apply {
      put("contents", JSONArray().apply {
        put(JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply {
              put("text", "Perform the following image modification or editing task on the provided image: $instruction. Output the resulting edited image.")
            })
            put(JSONObject().apply {
              put("inlineData", JSONObject().apply {
                put("mimeType", cleanMime)
                put("data", base64Data)
              })
            })
          })
        })
      })
      put("generationConfig", JSONObject().apply {
        put("responseModalities", JSONArray().apply {
          put("TEXT")
          put("IMAGE")
        })
      })
    }

    var (success, responseStr) = CreativeApiClient.postJson(
      endpoint = "$primaryModel:generateContent",
      jsonPayload = payload.toString()
    )

    if (!success && (responseStr.contains("404") || responseStr.contains("NOT_FOUND"))) {
      Log.w(tag, "Primary editing model 404, attempting fallback model $fallbackModel")
      val fallbackResult = CreativeApiClient.postJson(
        endpoint = "$fallbackModel:generateContent",
        jsonPayload = payload.toString()
      )
      success = fallbackResult.first
      responseStr = fallbackResult.second
    }

    if (!success) {
      val isConfig = responseStr.contains("403") || responseStr.contains("PERMISSION_DENIED") || responseStr.contains("NOT_FOUND")
      return@withContext GenerationResult.Error(
        message = if (isConfig) "دەستکاریکردنی وێنە پێویستی بە مۆدێلی وێنەی ڕێپێدراو هەیە لە Google AI Studio."
        else "هەڵە لە دەستکاریکردنی وێنە: $responseStr",
        technicalDetail = responseStr,
        isConfigurationRequired = isConfig
      )
    }

    try {
      val json = JSONObject(responseStr)
      val candidates = json.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        return@withContext GenerationResult.Error(
          message = "مۆدێلەکە نەیتوانی وێنەکە دەستکاری بکات بەهۆی سیاسەتی پاراستن یان داواکاری نادروست.",
          technicalDetail = responseStr
        )
      }

      val content = candidates.getJSONObject(0).optJSONObject("content")
      val parts = content?.optJSONArray("parts")

      var editedBytes: ByteArray? = null
      var outMime = "image/png"
      var responseText: String? = null

      if (parts != null) {
        for (i in 0 until parts.length()) {
          val part = parts.getJSONObject(i)
          if (part.has("inlineData")) {
            val inlineData = part.getJSONObject("inlineData")
            outMime = inlineData.optString("mimeType", "image/png")
            val base64Out = inlineData.getString("data")
            editedBytes = Base64.decode(base64Out, Base64.DEFAULT)
            break
          } else if (part.has("text")) {
            responseText = part.getString("text")
          }
        }
      }

      if (editedBytes != null && editedBytes.isNotEmpty()) {
        GenerationResult.Success(
          GeneratedImageResult(
            imageBytes = editedBytes,
            mimeType = outMime,
            revisedPrompt = responseText
          )
        )
      } else {
        GenerationResult.Error(
          message = "مۆدێلەکە وەڵامی وێنەی نوێی نەگەڕاندەوە: ${responseText?.take(120) ?: "تەنها دەق"}",
          technicalDetail = responseStr
        )
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to parse editing response", e)
      GenerationResult.Error("هەڵە لە خوێندنەوەی داتای وێنەی دەستکاریکراو: ${e.localizedMessage}")
    }
  }
}
