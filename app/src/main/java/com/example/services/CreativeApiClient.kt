package com.example.services

import android.util.Log
import com.example.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

object CreativeApiClient {
  private const val TAG = "CreativeApiClient"
  private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
  private const val PREFS_NAME = "creative_api_prefs"
  private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"

  @Volatile
  private var customApiKey: String? = null

  fun initialize(context: android.content.Context) {
    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
      val saved = prefs.getString(KEY_CUSTOM_API_KEY, null)?.trim()
      if (!saved.isNullOrBlank()) {
        customApiKey = saved
      }
    } catch (e: Throwable) {
      Log.e(TAG, "Failed to load custom API key", e)
    }
  }

  fun setCustomApiKey(context: android.content.Context, key: String?) {
    val cleanKey = key?.trim().orEmpty()
    customApiKey = if (cleanKey.isNotBlank()) cleanKey else null
    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
      if (cleanKey.isNotBlank()) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, cleanKey).apply()
      } else {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
      }
    } catch (e: Throwable) {
      Log.e(TAG, "Failed to save custom API key", e)
    }
  }

  val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(60, TimeUnit.SECONDS)
      .readTimeout(120, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .build()
  }

  fun getApiKey(): String {
    // 1. Check custom user-entered key first
    customApiKey?.let {
      if (it.isNotBlank()) return it
    }

    // 2. Check BuildConfig
    return try {
      val key = BuildConfig.GEMINI_API_KEY
      if (key.isNullOrBlank() || key == "PLACEHOLDER" || key.contains("MY_GEMINI_API_KEY")) {
        ""
      } else {
        key.trim()
      }
    } catch (e: Throwable) {
      ""
    }
  }

  fun isKeyConfigured(): Boolean {
    return getApiKey().isNotBlank()
  }

  fun getMaskedApiKey(): String {
    val key = getApiKey()
    if (key.isBlank()) return ""
    return if (key.length > 10) {
      "${key.take(6)}...${key.takeLast(4)}"
    } else {
      "********"
    }
  }

  suspend fun testApiKey(candidateKey: String): Pair<Boolean, String> {
    val clean = candidateKey.trim()
    if (clean.isBlank()) {
      return Pair(false, "API key is empty")
    }
    return withContext(Dispatchers.IO) {
      try {
        val testUrl = "$BASE_URL/models?key=$clean"
        val request = Request.Builder().url(testUrl).get().build()
        httpClient.newCall(request).execute().use { response ->
          val bodyStr = response.body?.string() ?: ""
          if (response.isSuccessful) {
            Pair(true, "Key is valid and active!")
          } else {
            val errorMsg = parseErrorDetails(response, bodyStr)
            Pair(false, errorMsg)
          }
        }
      } catch (e: Exception) {
        Pair(false, e.localizedMessage ?: "Network error connecting to Gemini API")
      }
    }
  }

  fun buildUrl(endpoint: String): String {
    val apiKey = getApiKey()
    val separator = if (endpoint.contains("?")) "&" else "?"
    return "$BASE_URL/$endpoint${separator}key=$apiKey"
  }

  fun parseErrorDetails(response: Response, responseBody: String?): String {
    return try {
      if (!responseBody.isNullOrBlank()) {
        val json = JSONObject(responseBody)
        if (json.has("error")) {
          val errorObj = json.getJSONObject("error")
          val message = errorObj.optString("message", "")
          val status = errorObj.optString("status", "")
          if (message.isNotBlank()) {
            return "[$status] $message"
          }
        }
      }
      "HTTP ${response.code}: ${response.message}"
    } catch (e: Exception) {
      "HTTP ${response.code}: ${response.message}"
    }
  }

  suspend fun postJson(endpoint: String, jsonPayload: String): Pair<Boolean, String> {
    val url = buildUrl(endpoint)
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = jsonPayload.toRequestBody(mediaType)

    val request = Request.Builder()
      .url(url)
      .post(requestBody)
      .build()

    return try {
      httpClient.newCall(request).execute().use { response ->
        val bodyStr = response.body?.string() ?: ""
        if (response.isSuccessful) {
          Pair(true, bodyStr)
        } else {
          val errorMsg = parseErrorDetails(response, bodyStr)
          Log.e(TAG, "Request to $endpoint failed: $errorMsg")
          Pair(false, errorMsg)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Network exception calling $endpoint", e)
      Pair(false, e.localizedMessage ?: "Network error")
    }
  }

  suspend fun getJson(endpoint: String): Pair<Boolean, String> {
    val url = buildUrl(endpoint)
    val request = Request.Builder()
      .url(url)
      .get()
      .build()

    return try {
      httpClient.newCall(request).execute().use { response ->
        val bodyStr = response.body?.string() ?: ""
        if (response.isSuccessful) {
          Pair(true, bodyStr)
        } else {
          val errorMsg = parseErrorDetails(response, bodyStr)
          Pair(false, errorMsg)
        }
      }
    } catch (e: Exception) {
      Pair(false, e.localizedMessage ?: "Network error")
    }
  }
}
