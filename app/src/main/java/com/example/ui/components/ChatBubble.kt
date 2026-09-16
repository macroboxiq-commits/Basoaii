package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.domain.model.RequiredPermissionType
import com.example.ui.localization.AppStrings
import com.example.ui.localization.LocalizedStrings
import com.example.ui.theme.BasoAiBubble
import com.example.ui.theme.BasoAiBubbleBorder
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextTertiary
import com.example.ui.theme.BasoUserBubble
import com.example.ui.theme.BasoUserBubbleTop
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
  message: ChatMessage,
  isRtl: Boolean = false,
  strings: AppStrings = LocalizedStrings.get(AppLanguage.ENGLISH),
  modifier: Modifier = Modifier,
  onOpenPermission: ((RequiredPermissionType) -> Unit)? = null,
  onReexecuteAction: ((String) -> Unit)? = null
) {
  val isUser = message.sender == MessageSender.USER
  val timeString = rememberFormattedTime(message.timestamp)
  val context = LocalContext.current

  Row(
    modifier = modifier
      .fillMaxWidth()
      .testTag(if (isUser) "user_bubble_row" else "ai_bubble_row")
      .padding(horizontal = 14.dp, vertical = 4.dp),
    horizontalArrangement = if (isUser) {
      if (isRtl) Arrangement.Start else Arrangement.End
    } else {
      if (isRtl) Arrangement.End else Arrangement.Start
    },
    verticalAlignment = Alignment.Bottom
  ) {
    // If Baso AI message and LTR, show avatar at start
    if (!isUser && !isRtl) {
      BasoLogoAvatar(size = 30.dp, showGlow = false)
      Spacer(modifier = Modifier.width(8.dp))
    }

    // Message Container
    Column(
      horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
      modifier = Modifier
        .widthIn(max = 340.dp)
        .testTag(if (isUser) "user_bubble" else "ai_bubble")
    ) {
      val userShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isRtl) 4.dp else 18.dp,
        bottomEnd = if (isRtl) 18.dp else 4.dp
      )

      val assistantShape = RoundedCornerShape(
        topStart = if (isRtl) 18.dp else 4.dp,
        topEnd = if (isRtl) 4.dp else 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 18.dp
      )

      Box(
        modifier = Modifier
          .testTag(if (isUser) "user_message_bubble" else "ai_message_bubble")
          .clip(if (isUser) userShape else assistantShape)
          .then(
            if (isUser) {
              Modifier.background(
                Brush.verticalGradient(listOf(BasoUserBubbleTop, BasoUserBubble))
              )
            } else {
              Modifier
                .background(BasoAiBubble)
                .border(BorderStroke(1.dp, BasoAiBubbleBorder), assistantShape)
            }
          )
          .padding(horizontal = 14.dp, vertical = 11.dp)
      ) {
        Column {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              lineHeight = 22.sp,
              fontSize = 15.sp,
              fontWeight = FontWeight.Normal
            ),
            color = if (isUser) Color.White else BasoTextPrimary
          )

          // If there is an associated native system action, show the rich ActionExecutionCard
          if (message.systemAction != null) {
            ActionExecutionCard(
              action = message.systemAction,
              result = message.actionResult,
              strings = strings,
              onOpenPermission = { perm ->
                onOpenPermission?.invoke(perm)
              },
              onReexecute = {
                onReexecuteAction?.invoke(message.id)
              }
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Bottom metadata row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = timeString,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = if (isUser) Color(0xCCFFFFFF) else BasoTextTertiary
            )

            if (!isUser) {
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Baso AI", message.text)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "کۆپیکرا", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Outlined.ContentCopy,
                  contentDescription = "Copy message",
                  tint = BasoTextTertiary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }
    }

    // If Baso AI message and RTL, show avatar at end
    if (!isUser && isRtl) {
      Spacer(modifier = Modifier.width(8.dp))
      BasoLogoAvatar(size = 30.dp, showGlow = false)
    }
  }
}

@Composable
private fun rememberFormattedTime(timestamp: Long): String {
  return androidx.compose.runtime.remember(timestamp) {
    try {
      val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
      sdf.format(Date(timestamp))
    } catch (e: Exception) {
      ""
    }
  }
}
