package com.example.domain.model

enum class MessageSender {
  USER,
  ASSISTANT
}

enum class MessageStatus {
  SENDING,
  SENT,
  ERROR
}

data class ChatAttachment(
  val id: String,
  val fileName: String,
  val mimeType: String,
  val sizeBytes: Long = 0L
)

data class ChatMessage(
  val id: String,
  val text: String,
  val sender: MessageSender,
  val timestamp: Long = System.currentTimeMillis(),
  val status: MessageStatus = MessageStatus.SENT,
  val attachments: List<ChatAttachment> = emptyList(),
  val systemAction: AndroidAction? = null,
  val actionResult: ActionExecutionResult? = null
)
