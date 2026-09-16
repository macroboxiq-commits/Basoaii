package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.domain.model.RequiredPermissionType
import com.example.domain.model.VoiceAssistantState
import com.example.domain.model.VoiceSettings
import com.example.ui.components.AnimatedChatBackground
import com.example.ui.components.BasoTopBar
import com.example.ui.components.ChatBubble
import com.example.ui.components.ChatInputBar
import com.example.ui.components.EmptyChatView
import com.example.ui.components.GeminiActivationDialog
import com.example.ui.components.MicPermissionDialog
import com.example.ui.components.SpeakingBanner
import com.example.ui.components.ThinkingIndicator
import com.example.ui.components.VoiceModeModal
import com.example.ui.localization.AppStrings
import com.example.ui.localization.LocalizedStrings
import com.example.ui.theme.BasoDarkBackground

/**
 * Primary ChatScreen composable powered by ChatViewModel and optional VoiceViewModel.
 */
@Composable
fun ChatScreen(
  viewModel: ChatViewModel,
  voiceViewModel: VoiceViewModel? = null,
  voiceSettings: VoiceSettings = VoiceSettings(),
  currentLanguage: AppLanguage = AppLanguage.ENGLISH,
  strings: AppStrings = LocalizedStrings.get(currentLanguage),
  onNavigateToSettings: () -> Unit = {},
  onNavigateToCreativeStudio: (CreativeStudioTab?) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val messages by viewModel.messages.collectAsState()
  val isThinking by viewModel.isThinking.collectAsState()
  val inputText by viewModel.inputText.collectAsState()
  val context = LocalContext.current

  // Voice Assistant states (handled gracefully if voiceViewModel is null)
  val assistantState by voiceViewModel?.assistantState?.collectAsState() ?: remember { mutableStateOf(VoiceAssistantState.IDLE) }
  val isSpeaking by voiceViewModel?.isSpeaking?.collectAsState() ?: remember { mutableStateOf(false) }
  val soundLevel by voiceViewModel?.soundLevel?.collectAsState() ?: remember { mutableStateOf(0f) }
  val recognizedText by voiceViewModel?.recognizedText?.collectAsState() ?: remember { mutableStateOf("") }
  val errorMessage by voiceViewModel?.errorMessage?.collectAsState() ?: remember { mutableStateOf<String?>(null) }
  val isVoiceModeActive by voiceViewModel?.isVoiceModeActive?.collectAsState() ?: remember { mutableStateOf(false) }
  val isListening = assistantState == VoiceAssistantState.LISTENING

  var showPermissionDialog by remember { mutableStateOf(false) }
  var showGeminiActivationDialog by remember { mutableStateOf(false) }

  // Text-To-Speech: When a new assistant message arrives, read it aloud if enabled
  var lastSpokenMessageId by remember { mutableStateOf<String?>(null) }
  LaunchedEffect(messages.size, voiceSettings.speakAiResponses) {
    val lastMessage = messages.lastOrNull()
    if (lastMessage != null &&
      lastMessage.sender == MessageSender.ASSISTANT &&
      lastMessage.id != lastSpokenMessageId &&
      voiceSettings.speakAiResponses
    ) {
      lastSpokenMessageId = lastMessage.id
      voiceViewModel?.speakResponse(
        text = lastMessage.text,
        voiceSettings = voiceSettings,
        appLanguage = currentLanguage
      )
    }
  }

  // Toast for voice errors if present
  LaunchedEffect(errorMessage) {
    errorMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
  }

  // Microphone permission launcher
  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      voiceViewModel?.startListening(
        voiceSettings = voiceSettings,
        appLanguage = currentLanguage,
        onResult = { text ->
          if (voiceSettings.autoSend) {
            viewModel.sendQuickPrompt(text, currentLanguage)
          } else {
            viewModel.onInputChanged(text)
          }
        }
      )
    } else {
      showPermissionDialog = true
    }
  }

  fun handleMicClick() {
    if (voiceViewModel == null) return

    if (isSpeaking) {
      voiceViewModel.stopSpeaking()
    }

    if (isListening) {
      voiceViewModel.stopListening()
      return
    }

    val hasPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
      voiceViewModel.startListening(
        voiceSettings = voiceSettings,
        appLanguage = currentLanguage,
        onResult = { text ->
          if (voiceSettings.autoSend) {
            viewModel.sendQuickPrompt(text, currentLanguage)
          } else {
            viewModel.onInputChanged(text)
          }
        }
      )
    } else {
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  ChatScreen(
    messages = messages,
    onSendMessage = { text ->
      voiceViewModel?.stopSpeaking()
      viewModel.onInputChanged(text)
      viewModel.sendMessage(currentLanguage)
    },
    modifier = modifier,
    isThinking = isThinking,
    inputText = inputText,
    onInputChanged = viewModel::onInputChanged,
    currentLanguage = currentLanguage,
    strings = strings,
    onMicClicked = { handleMicClick() },
    isListening = isListening,
    isSpeaking = isSpeaking,
    onStopSpeaking = { voiceViewModel?.stopSpeaking() },
    onAttachmentClicked = {
      Toast.makeText(context, "Attachment feature coming soon", Toast.LENGTH_SHORT).show()
    },
    onNewChatClicked = {
      voiceViewModel?.stopSpeaking()
      viewModel.startNewConversation()
      Toast.makeText(context, strings.newChat, Toast.LENGTH_SHORT).show()
    },
    onSettingsClicked = {
      voiceViewModel?.stopSpeaking()
      onNavigateToSettings()
    },
    onVoiceModeClicked = {
      handleMicClick()
      voiceViewModel?.openVoiceMode()
    },
    onCreativeStudioClicked = {
      voiceViewModel?.stopSpeaking()
      onNavigateToCreativeStudio(null)
    },
    onOpenPermission = { perm ->
      viewModel.openPermissionSettings(perm)
    },
    onReexecuteAction = { messageId ->
      viewModel.reexecuteAction(messageId)
    },
    onGeminiActivationClicked = {
      showGeminiActivationDialog = true
    }
  )

  // Gemini API Key Direct Activation Dialog
  if (showGeminiActivationDialog) {
    GeminiActivationDialog(
      strings = strings,
      onDismiss = { showGeminiActivationDialog = false },
      onKeyActivated = { showGeminiActivationDialog = false }
    )
  }

  // Dedicated Voice Mode Modal
  if (isVoiceModeActive && voiceViewModel != null) {
    VoiceModeModal(
      state = if (isThinking) VoiceAssistantState.THINKING else assistantState,
      soundLevel = soundLevel,
      recognizedText = recognizedText,
      lastSpokenText = messages.lastOrNull { it.sender == MessageSender.ASSISTANT }?.text ?: "",
      errorMessage = errorMessage,
      strings = strings,
      onStartListening = { handleMicClick() },
      onStopListening = { voiceViewModel.stopListening() },
      onStopSpeaking = { voiceViewModel.stopSpeaking() },
      onClose = { voiceViewModel.closeVoiceMode() }
    )
  }

  // Permission Denial Dialog
  if (showPermissionDialog) {
    MicPermissionDialog(
      strings = strings,
      onDismiss = { showPermissionDialog = false }
    )
  }
}

/**
 * Convenience overload for ChatScreen with default ViewModel initialization.
 */
@Composable
fun ChatScreen(
  modifier: Modifier = Modifier
) {
  val viewModel: ChatViewModel = viewModel()
  ChatScreen(
    viewModel = viewModel,
    modifier = modifier
  )
}

/**
 * Core state-hoisted ChatScreen composable featuring:
 * - LazyColumn for message display with auto-scroll
 * - Distinct bubble designs for user and AI messages
 * - Persistent input field at the bottom with send functionality
 */
@Composable
fun ChatScreen(
  messages: List<ChatMessage>,
  onSendMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
  isThinking: Boolean = false,
  inputText: String = "",
  onInputChanged: ((String) -> Unit)? = null,
  currentLanguage: AppLanguage = AppLanguage.ENGLISH,
  strings: AppStrings = LocalizedStrings.get(currentLanguage),
  onMicClicked: (() -> Unit)? = null,
  isListening: Boolean = false,
  isSpeaking: Boolean = false,
  onStopSpeaking: (() -> Unit)? = null,
  onAttachmentClicked: (() -> Unit)? = null,
  onNewChatClicked: (() -> Unit)? = null,
  onSettingsClicked: (() -> Unit)? = null,
  onVoiceModeClicked: (() -> Unit)? = null,
  onCreativeStudioClicked: ((CreativeStudioTab?) -> Unit)? = null,
  onOpenPermission: ((RequiredPermissionType) -> Unit)? = null,
  onReexecuteAction: ((String) -> Unit)? = null,
  onGeminiActivationClicked: (() -> Unit)? = null
) {
  val listState = rememberLazyListState()

  // Manage internal input state when no external state holder is provided
  var localInputText by remember { mutableStateOf("") }
  val effectiveInput = if (onInputChanged != null) inputText else localInputText
  val updateInput: (String) -> Unit = { newText ->
    if (onInputChanged != null) {
      onInputChanged(newText)
    } else {
      localInputText = newText
    }
  }

  // Auto-scroll to bottom when new messages arrive or thinking state starts
  LaunchedEffect(messages.size, isThinking) {
    val totalCount = messages.size + if (isThinking) 1 else 0
    if (totalCount > 0) {
      listState.animateScrollToItem(totalCount - 1)
    }
  }

  fun performSend() {
    val trimmed = effectiveInput.trim()
    if (trimmed.isNotEmpty()) {
      onSendMessage(trimmed)
      updateInput("")
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("chat_screen_root")
  ) {
    // Moving animated Killua anime wallpaper with dynamic lightning arcs covering the whole screen
    AnimatedChatBackground(
      modifier = Modifier.fillMaxSize()
    )

    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .testTag("chat_screen"),
      containerColor = Color.Transparent,
      topBar = {
        BasoTopBar(
          strings = strings,
          onNewChatClicked = { onNewChatClicked?.invoke() },
          onSettingsClicked = { onSettingsClicked?.invoke() },
          onVoiceModeClicked = { onVoiceModeClicked?.invoke() },
          onCreativeStudioClicked = { onCreativeStudioClicked?.invoke(null) },
          onGeminiActivationClicked = { onGeminiActivationClicked?.invoke() }
        )
      },
      bottomBar = {
        // Persistent input field bar with send functionality
        ChatInputBar(
          inputText = effectiveInput,
          onInputChanged = updateInput,
          onSendClicked = { performSend() },
          onMicClicked = { onMicClicked?.invoke() },
          onAttachmentClicked = { onAttachmentClicked?.invoke() },
          isListening = isListening,
          strings = strings,
          modifier = Modifier.testTag("persistent_chat_input_bar")
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
      ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 680.dp) // Tablet & foldable friendly constraint
      ) {
        // Floating Speaking Banner when reading response aloud
        if (isSpeaking && onStopSpeaking != null) {
          SpeakingBanner(
            isSpeaking = isSpeaking,
            strings = strings,
            onStopSpeaking = onStopSpeaking
          )
        }

        // LazyColumn for chat messages
        LazyColumn(
          state = listState,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("chat_lazy_column"),
          contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
          if (messages.isEmpty() && !isThinking) {
            item(key = "empty_chat_view") {
              EmptyChatView(
                strings = strings,
                onSuggestionClicked = { prompt ->
                  onSendMessage(prompt)
                },
                onNavigateToCreativeStudio = onCreativeStudioClicked,
                onActivateGeminiClicked = onGeminiActivationClicked,
                modifier = Modifier.fillParentMaxSize()
              )
            }
          } else {
            items(
              items = messages,
              key = { it.id }
            ) { message ->
              // Distinct bubble designs for user and AI messages
              ChatBubble(
                message = message,
                isRtl = currentLanguage.isRtl,
                strings = strings,
                onOpenPermission = onOpenPermission,
                onReexecuteAction = onReexecuteAction
              )
            }

            if (isThinking) {
              item(key = "thinking_indicator_item") {
                ThinkingIndicator(
                  thinkingText = strings.thinking
                )
              }
            }
          }
        }
      }
    }
  }
}
}
