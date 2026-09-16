package com.example.domain.repository

import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
  val messages: Flow<List<ChatMessage>>
  val isThinking: Flow<Boolean>

  suspend fun sendMessage(content: String, language: AppLanguage)
  suspend fun clearChat()
  suspend fun startNewConversation()
  suspend fun reexecuteAction(messageId: String)
}
