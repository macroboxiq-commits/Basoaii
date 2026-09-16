package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.VoiceAssistantState
import com.example.ui.localization.AppStrings
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBackground
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoPurple
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary
import com.example.ui.theme.BasoTextTertiary

@Composable
fun VoiceModeModal(
  state: VoiceAssistantState,
  soundLevel: Float,
  recognizedText: String,
  lastSpokenText: String,
  errorMessage: String?,
  strings: AppStrings,
  onStartListening: () -> Unit,
  onStopListening: () -> Unit,
  onStopSpeaking: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  Dialog(
    onDismissRequest = onClose,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false
    )
  ) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF030712),
              Color(0xFF0A0F1D),
              Color(0xFF02040A)
            )
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 560.dp)
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            BasoLogoAvatar(size = 32.dp, showGlow = false)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.voiceModeTitle,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              ),
              color = BasoTextPrimary
            )
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier
              .testTag("close_voice_mode")
              .size(40.dp)
              .clip(CircleShape)
              .background(BasoDarkSurfaceHigh)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = strings.cancelAction,
              tint = BasoTextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Center Voice Orb and State Indicator
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.weight(1f)
        ) {
          VoiceOrb(
            state = state,
            soundLevel = soundLevel,
            size = 190.dp
          )

          Spacer(modifier = Modifier.height(28.dp))

          // State Title Text
          val statusText = when (state) {
            VoiceAssistantState.IDLE -> strings.voiceIdlePrompt
            VoiceAssistantState.LISTENING -> strings.voiceListening
            VoiceAssistantState.THINKING -> strings.voiceThinking
            VoiceAssistantState.SPEAKING -> strings.voiceSpeaking
            VoiceAssistantState.PROCESSING -> strings.voiceThinking
            VoiceAssistantState.ERROR -> errorMessage ?: "هەڵەیەک ڕوویدا"
          }

          val statusColor = when (state) {
            VoiceAssistantState.IDLE -> BasoTextPrimary
            VoiceAssistantState.LISTENING -> BasoCyan
            VoiceAssistantState.THINKING -> BasoPurple
            VoiceAssistantState.SPEAKING -> BasoCyan
            VoiceAssistantState.PROCESSING -> BasoCyan
            VoiceAssistantState.ERROR -> Color(0xFFFF6E6E)
          }

          Text(
            text = statusText,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            ),
            color = statusColor,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Live Recognized text or spoken response preview card
          val displayText = if (state == VoiceAssistantState.SPEAKING && lastSpokenText.isNotBlank()) {
            lastSpokenText
          } else if (recognizedText.isNotBlank()) {
            "\"$recognizedText\""
          } else {
            ""
          }

          if (displayText.isNotBlank()) {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
              colors = CardDefaults.cardColors(containerColor = BasoDarkSurface),
              border = BorderStroke(1.dp, BasoDarkBorder),
              shape = RoundedCornerShape(16.dp)
            ) {
              Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = BasoTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                maxLines = 4
              )
            }
          }
        }

        // Bottom Action Bar
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          when (state) {
            VoiceAssistantState.SPEAKING -> {
              // Interrupt TTS button
              IconButton(
                onClick = onStopSpeaking,
                modifier = Modifier
                  .testTag("stop_speech_button")
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE53935))
              ) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = strings.stopSpeakingAction,
                  tint = Color.White,
                  modifier = Modifier.size(36.dp)
                )
              }
              Text(
                text = strings.stopSpeakingAction,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = BasoTextSecondary
              )
            }

            VoiceAssistantState.LISTENING -> {
              // Tap to finish listening
              IconButton(
                onClick = onStopListening,
                modifier = Modifier
                  .testTag("stop_listening_button")
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(BasoCyan)
              ) {
                Icon(
                  imageVector = Icons.Outlined.MicOff,
                  contentDescription = "Stop listening",
                  tint = BasoDarkBackground,
                  modifier = Modifier.size(34.dp)
                )
              }
              Text(
                text = "لێبدە بۆ تەواوکردنی قسەکردن",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = BasoCyan
              )
            }

            else -> {
              // Idle or error: Tap to start speaking
              IconButton(
                onClick = onStartListening,
                modifier = Modifier
                  .testTag("voice_mode_mic_button")
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(BasoCyan)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Mic,
                  contentDescription = strings.tapToSpeakPrompt,
                  tint = BasoDarkBackground,
                  modifier = Modifier.size(34.dp)
                )
              }
              Text(
                text = strings.tapToSpeakPrompt,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = BasoTextSecondary
              )
            }
          }
        }
      }
    }
  }
}
