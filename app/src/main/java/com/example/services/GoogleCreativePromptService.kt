package com.example.services

import android.util.Log
import com.example.domain.model.AppLanguage
import com.example.domain.model.CreativeType
import com.example.domain.model.GenerationResult
import com.example.domain.model.SocialFormat
import com.example.domain.model.SocialGeneratedContent
import com.example.domain.model.SocialPlatform
import com.example.domain.service.CreativePromptService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GoogleCreativePromptService : CreativePromptService {
  private val tag = "CreativePromptService"
  private val textModel = "models/gemini-3.5-flash"

  override suspend fun enhancePrompt(
    userPrompt: String,
    type: CreativeType,
    language: AppLanguage
  ): GenerationResult<String> = withContext(Dispatchers.IO) {
    if (!CreativeApiClient.isKeyConfigured()) {
      return@withContext GenerationResult.Error(
        message = "تکایە کلیلی Gemini API لە ڕێکخستنەکان یان AI Studio Secrets دابنێ.",
        technicalDetail = "GEMINI_API_KEY is missing or placeholder.",
        isConfigurationRequired = true
      )
    }

    try {
      val systemInstruction = """
        You are an expert AI art & video prompt director.
        The user provides a concept or prompt in Kurdish, Arabic, or English.
        Your task is to enrich and enhance it into a vivid, descriptive prompt in English optimized for modern image and video generative models (like Imagen, Gemini Flash Image, and Veo).
        Include key sensory and technical details: lighting, camera perspective, texture, depth, artistic atmosphere, and composition.
        Keep it natural and concise (under 75 words).
        IMPORTANT: Output ONLY the enhanced prompt. Do NOT add preamble, quotes, or markdown formatting.
      """.trimIndent()

      val payload = JSONObject().apply {
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", systemInstruction) })
          })
        })
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply {
                put("text", "Creative task type: ${type.name}. Enhance this user prompt: \"$userPrompt\"")
              })
            })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.7)
          put("maxOutputTokens", 256)
        })
      }

      val (success, responseStr) = CreativeApiClient.postJson(
        endpoint = "$textModel:generateContent",
        jsonPayload = payload.toString()
      )

      if (!success) {
        return@withContext GenerationResult.Error(
          message = "نەتوانرا داواکارییەکە باشتر بکرێت: $responseStr",
          technicalDetail = responseStr
        )
      }

      val json = JSONObject(responseStr)
      val candidates = json.optJSONArray("candidates")
      val content = candidates?.optJSONObject(0)?.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      val text = parts?.optJSONObject(0)?.optString("text")?.trim()

      if (!text.isNullOrBlank()) {
        GenerationResult.Success(text)
      } else {
        GenerationResult.Error("وەڵامی بەتاڵ لە مۆدێلی Gemini وەرگیرا.")
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to enhance prompt", e)
      GenerationResult.Error("هەڵە لە باشترکردنی داواکاری: ${e.localizedMessage}")
    }
  }

  override suspend fun generateSocialContent(
    platform: SocialPlatform,
    format: SocialFormat,
    topic: String,
    language: AppLanguage
  ): GenerationResult<SocialGeneratedContent> = withContext(Dispatchers.IO) {
    if (!CreativeApiClient.isKeyConfigured()) {
      return@withContext GenerationResult.Error(
        message = "تکایە کلیلی Gemini API دابنێ بۆ بەرهەمهێنانی ناوەڕۆک.",
        technicalDetail = "GEMINI_API_KEY is not configured.",
        isConfigurationRequired = true
      )
    }

    try {
      val langInstruction = when (language) {
        AppLanguage.KURDISH_SORANI -> "Generate caption, title, and script in natural Kurdish Sorani (سۆرانی). The generationPrompt MUST be in English for image/video models."
        AppLanguage.ARABIC -> "Generate caption, title, and script in natural Arabic (العربية). The generationPrompt MUST be in English for image/video models."
        AppLanguage.ENGLISH -> "Generate everything in English."
      }

      val prompt = """
        You are a top social media strategist and content producer.
        Create an engaging social media post package for:
        Platform: ${platform.labelEn}
        Format: ${format.labelEn}
        Topic/Idea: $topic
        
        Language rule: $langInstruction

        Respond ONLY with a valid JSON object matching this exact schema:
        {
          "visualConcept": "A brief description of what the visual/video represents",
          "generationPrompt": "Detailed English generation prompt for AI image or video creation",
          "title": "Catchy headline or video title",
          "caption": "Compelling caption with storytelling or call-to-action",
          "hashtags": ["#tag1", "#tag2", "#tag3", "#tag4", "#tag5"],
          "script": "Optional short 15-30 second video script or voiceover hook (or null if static post)"
        }
      """.trimIndent()

      val payload = JSONObject().apply {
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply { put("text", prompt) })
            })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("responseMimeType", "application/json")
          put("temperature", 0.8)
        })
      }

      val (success, responseStr) = CreativeApiClient.postJson(
        endpoint = "$textModel:generateContent",
        jsonPayload = payload.toString()
      )

      if (!success) {
        return@withContext GenerationResult.Error(
          message = "نەتوانرا ناوەڕۆکی تۆڕە کۆمەڵایەتییەکان دروست بکرێت: $responseStr",
          technicalDetail = responseStr
        )
      }

      val json = JSONObject(responseStr)
      val candidates = json.optJSONArray("candidates")
      val content = candidates?.optJSONObject(0)?.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      val rawText = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

      // Parse JSON from text
      val cleanJsonStr = if (rawText.startsWith("```json")) {
        rawText.removePrefix("```json").substringBeforeLast("```").trim()
      } else if (rawText.startsWith("```")) {
        rawText.removePrefix("```").substringBeforeLast("```").trim()
      } else {
        rawText
      }

      val resultObj = JSONObject(cleanJsonStr)
      val hashtagsArray = resultObj.optJSONArray("hashtags")
      val hashtagsList = mutableListOf<String>()
      if (hashtagsArray != null) {
        for (i in 0 until hashtagsArray.length()) {
          hashtagsList.add(hashtagsArray.getString(i))
        }
      }

      val result = SocialGeneratedContent(
        visualConcept = resultObj.optString("visualConcept", topic),
        generationPrompt = resultObj.optString("generationPrompt", topic),
        title = resultObj.optString("title", "پۆستی نوێ"),
        caption = resultObj.optString("caption", topic),
        hashtags = if (hashtagsList.isNotEmpty()) hashtagsList else listOf("#BasoAI", "#Creativity"),
        script = resultObj.optString("script", "").ifBlank { null }
      )

      GenerationResult.Success(result)
    } catch (e: Exception) {
      Log.e(tag, "Failed to generate social media content", e)
      GenerationResult.Error("هەڵە لە دروستکردنی پۆست: ${e.localizedMessage}")
    }
  }
}
