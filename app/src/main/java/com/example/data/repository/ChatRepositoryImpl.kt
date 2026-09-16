package com.example.data.repository

import com.example.ai.AiEngine
import com.example.ai.GeminiAiEngine
import com.example.domain.model.ActionExecutionResult
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.domain.model.MessageStatus
import com.example.domain.repository.ChatRepository
import com.example.services.AndroidActionHandler
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepositoryImpl(
  private val aiEngine: AiEngine = GeminiAiEngine(),
  private val actionHandler: AndroidActionHandler? = null
) : ChatRepository {

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  override val messages: Flow<List<ChatMessage>> = _messages.asStateFlow()

  private val _isThinking = MutableStateFlow(false)
  override val isThinking: Flow<Boolean> = _isThinking.asStateFlow()

  override suspend fun sendMessage(content: String, language: AppLanguage) {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) return

    val userMessage = ChatMessage(
      id = UUID.randomUUID().toString(),
      text = trimmed,
      sender = MessageSender.USER,
      status = MessageStatus.SENT,
      timestamp = System.currentTimeMillis()
    )

    val currentList = _messages.value
    _messages.value = currentList + userMessage

    _isThinking.value = true
    try {
      val assistantOutput = aiEngine.processPrompt(
        prompt = trimmed,
        history = _messages.value,
        language = language
      )

      var actionResult: ActionExecutionResult? = null
      val action = assistantOutput.systemAction
      if (action != null && actionHandler != null) {
        actionResult = actionHandler.executeAction(action)
      }

      val assistantMessage = ChatMessage(
        id = UUID.randomUUID().toString(),
        text = assistantOutput.text,
        sender = MessageSender.ASSISTANT,
        status = MessageStatus.SENT,
        timestamp = System.currentTimeMillis(),
        systemAction = action,
        actionResult = actionResult
      )

      _messages.value = _messages.value + assistantMessage
    } catch (e: Exception) {
      val errorMessage = ChatMessage(
        id = UUID.randomUUID().toString(),
        text = "Error generating response: ${e.localizedMessage ?: "Unknown error"}",
        sender = MessageSender.ASSISTANT,
        status = MessageStatus.ERROR,
        timestamp = System.currentTimeMillis()
      )
      _messages.value = _messages.value + errorMessage
    } finally {
      _isThinking.value = false
    }
  }

  override suspend fun reexecuteAction(messageId: String) {
    val current = _messages.value
    val msg = current.firstOrNull { it.id == messageId } ?: return
    val action = msg.systemAction ?: return
    val handler = actionHandler ?: return

    val newResult = handler.executeAction(action)
    _messages.value = current.map {
      if (it.id == messageId) {
        it.copy(actionResult = newResult)
      } else {
        it
      }
    }
  }

  override suspend fun clearChat() {
    _messages.value = emptyList()
    _isThinking.value = false
  }

  override suspend fun startNewConversation() {
    clearChat()
  }
}
