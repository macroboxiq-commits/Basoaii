package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.CreativeApiClient
import com.example.ui.localization.AppStrings
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary

@Composable
fun BasoTopBar(
  strings: AppStrings,
  onNewChatClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onVoiceModeClicked: (() -> Unit)? = null,
  onCreativeStudioClicked: (() -> Unit)? = null,
  onGeminiActivationClicked: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isKeyActive = CreativeApiClient.isKeyConfigured()
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = Color(0xD9060F1E),
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left/Start: Avatar + Title & Tagline
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        BasoLogoAvatar(
          size = 40.dp,
          showGlow = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(verticalArrangement = Arrangement.Center) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = strings.appName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 0.3.sp
              ),
              color = BasoTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Futuristic online indicator dot
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(BasoCyan)
            )
          }

          Text(
            text = strings.tagline,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.Normal
            ),
            color = BasoTextSecondary,
            maxLines = 1
          )
        }
      }

      // Actions: Creative Studio + Voice Mode + New Chat + Settings
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        if (onCreativeStudioClicked != null) {
          IconButton(
            onClick = onCreativeStudioClicked,
            modifier = Modifier
              .testTag("creative_studio_button")
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(BasoDarkSurfaceHigh)
          ) {
            Icon(
              imageVector = Icons.Outlined.AutoAwesome,
              contentDescription = strings.creativeStudioTitle,
              tint = Color(0xFFA855F7), // Creative Purple
              modifier = Modifier.size(20.dp)
            )
          }
        }

        if (onVoiceModeClicked != null) {
          IconButton(
            onClick = onVoiceModeClicked,
            modifier = Modifier
              .testTag("voice_mode_button")
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(BasoDarkSurfaceHigh)
          ) {
            Icon(
              imageVector = Icons.Outlined.GraphicEq,
              contentDescription = strings.voiceModeTitle,
              tint = BasoCyan,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        IconButton(
          onClick = onNewChatClicked,
          modifier = Modifier
            .testTag("new_chat_button")
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BasoDarkSurfaceHigh)
        ) {
          Icon(
            imageVector = Icons.Outlined.AddComment,
            contentDescription = strings.newChat,
            tint = BasoTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        if (onGeminiActivationClicked != null) {
          IconButton(
            onClick = onGeminiActivationClicked,
            modifier = Modifier
              .testTag("gemini_key_topbar_button")
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isKeyActive) Color(0x2210B981) else Color(0x22F59E0B))
          ) {
            Icon(
              imageVector = Icons.Outlined.Key,
              contentDescription = strings.geminiKeySectionTitle,
              tint = if (isKeyActive) Color(0xFF10B981) else Color(0xFFF59E0B),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        IconButton(
          onClick = onSettingsClicked,
          modifier = Modifier
            .testTag("settings_button")
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BasoDarkSurfaceHigh)
        ) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = strings.settingsTitle,
            tint = BasoTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
