package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.localization.LocalizedStrings
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBackground
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary
import com.example.ui.theme.BasoTextTertiary

@Composable
fun ChatInputBar(
  inputText: String,
  onInputChanged: (String) -> Unit,
  onSendClicked: () -> Unit,
  onMicClicked: () -> Unit = {},
  onAttachmentClicked: () -> Unit = {},
  isListening: Boolean = false,
  strings: AppStrings = LocalizedStrings.get(AppLanguage.ENGLISH),
  modifier: Modifier = Modifier
) {
  val hasText = inputText.isNotBlank()
  val sendButtonBg by animateColorAsState(
    targetValue = if (hasText) BasoCyan else BasoDarkSurfaceHigh,
    animationSpec = tween(durationMillis = 200),
    label = "sendBg"
  )
  val sendButtonIconTint by animateColorAsState(
    targetValue = if (hasText) BasoDarkBackground else BasoTextTertiary,
    animationSpec = tween(durationMillis = 200),
    label = "sendTint"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
  val micScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = if (isListening) 1.16f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "micScale"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("chat_input_bar")
      .navigationBarsPadding()
      .imePadding(),
    color = Color(0xD9060F1E),
    tonalElevation = 0.dp
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      AnimatedVisibility(
        visible = isListening,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(BasoCyan)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.voiceListening,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = BasoCyan
          )
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Attachment button
        IconButton(
          onClick = onAttachmentClicked,
          modifier = Modifier
            .testTag("attachment_button")
            .size(42.dp)
            .clip(CircleShape)
            .background(BasoDarkSurfaceHigh)
        ) {
          Icon(
            imageVector = Icons.Outlined.AttachFile,
            contentDescription = strings.attachFile,
            tint = BasoTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Input Field Container
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(BasoDarkSurfaceHigh)
            .border(BorderStroke(1.dp, BasoDarkBorder), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 11.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          if (inputText.isEmpty()) {
            Text(
              text = if (isListening) strings.voiceListening else strings.inputPlaceholder,
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
              color = if (isListening) BasoCyan else BasoTextTertiary,
              maxLines = 1
            )
          }

          BasicTextField(
            value = inputText,
            onValueChange = onInputChanged,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("chat_input_field"),
            textStyle = TextStyle(
              color = BasoTextPrimary,
              fontSize = 14.sp
            ),
            cursorBrush = SolidColor(BasoCyan),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
              onSend = {
                if (hasText) {
                  onSendClicked()
                }
              }
            ),
            maxLines = 4
          )
        }

        // Microphone button
        IconButton(
          onClick = onMicClicked,
          modifier = Modifier
            .testTag("mic_button")
            .scale(if (isListening) micScale else 1.0f)
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isListening) Color(0x3300E5FF) else BasoDarkSurfaceHigh)
            .border(
              if (isListening) BorderStroke(1.5.dp, BasoCyan) else BorderStroke(0.dp, Color.Transparent),
              CircleShape
            )
        ) {
          Icon(
            imageVector = if (isListening) Icons.Outlined.MicOff else Icons.Outlined.Mic,
            contentDescription = strings.voiceInput,
            tint = if (isListening) BasoCyan else BasoTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Send button
        IconButton(
          onClick = {
            if (hasText) {
              onSendClicked()
            }
          },
          enabled = hasText,
          modifier = Modifier
            .testTag("send_button")
            .size(42.dp)
            .clip(CircleShape)
            .background(sendButtonBg)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = strings.send,
            tint = sendButtonIconTint,
            modifier = Modifier.size(19.dp)
          )
        }
      }
    }
  }
}
