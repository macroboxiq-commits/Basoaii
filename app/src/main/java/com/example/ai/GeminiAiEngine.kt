package com.example.ai

import android.util.Log
import com.example.domain.model.AndroidAction
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.services.CreativeApiClient
import com.example.services.SystemActionParser
import com.example.tools.ToolRegistry
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gemini AI Engine supporting Google Gemini API with native Function Calling / Tools.
 * Automatically resolves tools or falls back gracefully to LocalAssistantEngine.
 */
class GeminiAiEngine(
  private val fallbackEngine: AiEngine = LocalAssistantEngine(),
  private val toolRegistry: ToolRegistry = ToolRegistry()
) : AiEngine {

  private val TAG = "GeminiAiEngine"

  val isConfigured: Boolean
    get() = CreativeApiClient.isKeyConfigured()

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
    // 1. Direct fast-path check for native system commands
    val localAction = SystemActionParser.parsePrompt(prompt, language)
    if (localAction != null) {
      return AssistantResponse(
        text = localAction.feedbackMessage,
        systemAction = localAction.action
      )
    }

    // 2. If Gemini API Key is configured, make real Gemini API call with function declarations
    if (isConfigured) {
      try {
        val geminiResponse = callGeminiWithTools(prompt, history, language)
        if (geminiResponse != null) {
          return geminiResponse
        }
      } catch (e: Exception) {
        Log.w(TAG, "Gemini API call failed, falling back to local engine", e)
      }
    }

    // 3. Fallback to local assistant engine
    return fallbackEngine.processPrompt(prompt, history, language)
  }

  private suspend fun callGeminiWithTools(
    prompt: String,
    history: List<ChatMessage>,
    language: AppLanguage
  ): AssistantResponse? {
    val endpoint = "models/gemini-3.5-flash:generateContent"

    val requestJson = JSONObject().apply {
      // System Instruction
      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().apply {
            put(
              "text",
              "You are Baso AI, an intelligent personal assistant capable of performing real Android device actions " +
                  "such as scheduling meetings in the calendar, setting alarms, starting timers, toggling the flashlight, " +
                  "adjusting silent mode, and changing brightness. When a user asks for one of these, invoke the matching function declaration. " +
                  "Always answer kindly in the user's language: Kurdish Sorani, Arabic, or English."
            )
          })
        })
      })

      // Contents history
      val contentsArray = JSONArray()
      // Send last 6 conversation turns
      val recentHistory = history.takeLast(6)
      for (msg in recentHistory) {
        val role = if (msg.sender == com.example.domain.model.MessageSender.USER) "user" else "model"
        contentsArray.put(JSONObject().apply {
          put("role", role)
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", msg.text) })
          })
        })
      }

      // Add current user prompt
      contentsArray.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().apply {
          put(JSONObject().apply { put("text", prompt) })
        })
      })
      put("contents", contentsArray)

      // Tools declarations
      val toolsArray = JSONArray().apply {
        put(JSONObject().apply {
          put("functionDeclarations", toolRegistry.getGeminiFunctionDeclarations())
        })
      }
      put("tools", toolsArray)
    }

    val (success, responseBody) = CreativeApiClient.postJson(endpoint, requestJson.toString())
    if (!success || responseBody.isBlank()) {
      return null
    }

    val root = JSONObject(responseBody)
    val candidates = root.optJSONArray("candidates") ?: return null
    val firstCandidate = candidates.optJSONObject(0) ?: return null
    val content = firstCandidate.optJSONObject("content") ?: return null
    val parts = content.optJSONArray("parts") ?: return null

    var responseText = ""
    var extractedAction: AndroidAction? = null

    for (i in 0 until parts.length()) {
      val part = parts.optJSONObject(i) ?: continue

      if (part.has("text")) {
        responseText += part.optString("text", "")
      }

      if (part.has("functionCall")) {
        val funcCall = part.getJSONObject("functionCall")
        val funcName = funcCall.optString("name", "")
        val args = funcCall.optJSONObject("args") ?: JSONObject()
        extractedAction = toolRegistry.parseFunctionCallToAction(funcName, args)

        if (responseText.isBlank()) {
          responseText = when (language) {
            AppLanguage.KURDISH_SORANI -> "کرداری $funcName جێبەجێ دەکرێت لەسەر ئامێرەکەت..."
            AppLanguage.ARABIC -> "جارِ تنفيذ أمر $funcName على جهازك..."
            AppLanguage.ENGLISH -> "Executing $funcName on your device..."
          }
        }
      }
    }

    if (responseText.isNotBlank() || extractedAction != null) {
      return AssistantResponse(
        text = responseText.ifBlank { "Done" },
        systemAction = extractedAction
      )
    }

    return null
  }
}
