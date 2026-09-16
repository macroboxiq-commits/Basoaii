package com.example.ai

import com.example.domain.model.AndroidAction
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage

data class AssistantResponse(
  val text: String,
  val systemAction: AndroidAction? = null
)

interface AiEngine {
  suspend fun generateResponse(
    prompt: String,
    history: List<ChatMessage>,
    language: AppLanguage
  ): String

  suspend fun processPrompt(
    prompt: String,
    history: List<ChatMessage>,
    language: AppLanguage
  ): AssistantResponse {
    val text = generateResponse(prompt, history, language)
    return AssistantResponse(text = text, systemAction = null)
  }
}
