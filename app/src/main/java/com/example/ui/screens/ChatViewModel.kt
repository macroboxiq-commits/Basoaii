package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepositoryImpl
import com.example.domain.model.AndroidAction
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.domain.model.RequiredPermissionType
import com.example.domain.repository.ChatRepository
import com.example.services.AndroidActionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
  application: Application,
  val actionHandler: AndroidActionHandler?,
  private val repository: ChatRepository
) : AndroidViewModel(application) {

  constructor(application: Application) : this(
    application = application,
    actionHandler = AndroidActionHandler(application),
    repository = ChatRepositoryImpl(
      actionHandler = AndroidActionHandler(application)
    )
  )

  constructor(repository: ChatRepository) : this(
    application = Application(),
    actionHandler = null,
    repository = repository
  )

  constructor() : this(ChatRepositoryImpl())

  val messages: StateFlow<List<ChatMessage>> = repository.messages
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val isThinking: StateFlow<Boolean> = repository.isThinking
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = false
    )

  private val _inputText = MutableStateFlow("")
  val inputText: StateFlow<String> = _inputText.asStateFlow()

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  fun onInputChanged(text: String) {
    _inputText.value = text
  }

  fun sendMessage(language: AppLanguage) {
    val text = _inputText.value.trim()
    if (text.isEmpty()) return

    _inputText.value = ""
    viewModelScope.launch {
      repository.sendMessage(text, language)
    }
  }

  fun sendQuickPrompt(prompt: String, language: AppLanguage) {
    viewModelScope.launch {
      repository.sendMessage(prompt, language)
    }
  }

  fun startNewConversation() {
    viewModelScope.launch {
      repository.startNewConversation()
    }
  }

  fun toggleMic() {
    _isListening.value = !_isListening.value
  }

  fun reexecuteAction(messageId: String) {
    viewModelScope.launch {
      repository.reexecuteAction(messageId)
    }
  }

  fun openPermissionSettings(permissionType: RequiredPermissionType) {
    actionHandler?.openPermissionSettings(permissionType)
  }

  fun executeActionDirectly(action: AndroidAction) {
    actionHandler?.executeAction(action)
  }
}
