package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SettingsState
import com.example.ui.localization.AppStrings
import com.example.ui.screens.SettingsViewModel
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBackground
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoDarkSurfaceVariant
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary
import com.example.ui.theme.BasoTextTertiary

/**
 * Dedicated Card component for setting and activating the Gemini API Key
 * directly inside the application.
 */
@Composable
fun GeminiKeyActivationCard(
  settings: SettingsState,
  viewModel: SettingsViewModel,
  strings: AppStrings,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val uriHandler = LocalUriHandler.current

  var inputKey by remember { mutableStateOf("") }
  var isKeyVisible by remember { mutableStateOf(false) }
  var isEditingMode by remember { mutableStateOf(false) }

  val isConfigured = settings.isGeminiKeyConfigured
  val isTesting = settings.isTestingApiKey
  val feedbackMsg = settings.apiKeyFeedbackMessage
  val isTestSuccess = settings.isApiKeyTestSuccess

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("gemini_key_activation_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = BasoDarkSurface),
    border = BorderStroke(
      width = 1.dp,
      brush = if (isConfigured) {
        Brush.linearGradient(listOf(Color(0xFF10B981).copy(alpha = 0.6f), BasoCyan.copy(alpha = 0.3f)))
      } else {
        Brush.linearGradient(listOf(BasoCyan.copy(alpha = 0.5f), Color(0xFFA855F7).copy(alpha = 0.4f)))
      }
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header: Icon + Title & Subtitle + Activation Status Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isConfigured) Color(0x2210B981) else Color(0x2200E5FF)
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isConfigured) Icons.Outlined.Key else Icons.Outlined.AutoAwesome,
              contentDescription = null,
              tint = if (isConfigured) Color(0xFF10B981) else BasoCyan,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = strings.geminiKeySectionTitle,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              color = BasoTextPrimary
            )
            Text(
              text = strings.geminiKeySectionSubtitle,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = BasoTextTertiary
            )
          }
        }

        // Status Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (isConfigured) Color(0x2210B981) else Color(0x22F59E0B)
            )
            .border(
              width = 1.dp,
              color = if (isConfigured) Color(0x6610B981) else Color(0x66F59E0B),
              shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (isConfigured) strings.geminiKeyStatusActive else strings.geminiKeyStatusInactive,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            color = if (isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)
          )
        }
      }

      // If configured and not in edit mode: show active status and actions
      if (isConfigured && !isEditingMode) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BasoDarkSurfaceHigh)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "کلیل چالاک کراوە / Active Key:",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = BasoTextTertiary
              )
              Text(
                text = settings.maskedApiKey.ifEmpty { "AIzaSy...****" },
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  letterSpacing = 1.sp
                ),
                color = Color(0xFF10B981)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              // Edit Key Button
              IconButton(
                onClick = { isEditingMode = true },
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(BasoDarkSurfaceVariant)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Edit,
                  contentDescription = strings.geminiKeyChangeAction,
                  tint = BasoCyan,
                  modifier = Modifier.size(18.dp)
                )
              }

              // Remove Key Button
              IconButton(
                onClick = { viewModel.removeApiKey(context) },
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0x22EF4444))
              ) {
                Icon(
                  imageVector = Icons.Outlined.Delete,
                  contentDescription = strings.geminiKeyRemoveAction,
                  tint = Color(0xFFEF4444),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // Test Button for active key
          OutlinedButton(
            onClick = {
              viewModel.testApiKey("")
            },
            enabled = !isTesting,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("test_active_key_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BasoCyan),
            border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.5f))
          ) {
            if (isTesting) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = BasoCyan,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(strings.geminiKeyTestingNotice, fontSize = 12.sp)
            } else {
              Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(strings.geminiKeyTestAction, fontSize = 12.sp)
            }
          }
        }
      } else {
        // Activation Input Mode (when not configured, or when editing)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = strings.geminiKeyHelpDescription,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = BasoTextSecondary
          )

          OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it },
            label = { Text(strings.geminiKeyInputLabel) },
            placeholder = { Text(strings.geminiKeyPlaceholder) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("gemini_key_input_field"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                tint = BasoCyan,
                modifier = Modifier.size(20.dp)
              )
            },
            trailingIcon = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                // Paste button
                IconButton(
                  onClick = {
                    val clip = clipboardManager.getText()?.text?.trim()
                    if (!clip.isNullOrBlank()) {
                      inputKey = clip
                    }
                  }
                ) {
                  Icon(
                    imageVector = Icons.Outlined.ContentPaste,
                    contentDescription = strings.geminiKeyPasteAction,
                    tint = BasoTextSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                }

                // Show/Hide password toggle
                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                  Icon(
                    imageVector = if (isKeyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = BasoTextSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BasoCyan,
              unfocusedBorderColor = BasoDarkBorder,
              focusedTextColor = BasoTextPrimary,
              unfocusedTextColor = BasoTextPrimary,
              focusedContainerColor = BasoDarkSurfaceHigh,
              unfocusedContainerColor = BasoDarkSurfaceHigh
            )
          )

          // Action Buttons: Activate & Cancel (if editing)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (isEditingMode) {
              OutlinedButton(
                onClick = {
                  isEditingMode = false
                  inputKey = ""
                  viewModel.clearApiKeyFeedback()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BasoTextSecondary)
              ) {
                Text(strings.cancelAction, fontSize = 12.sp)
              }
            }

            Button(
              onClick = {
                val clean = inputKey.trim()
                if (clean.isNotBlank()) {
                  viewModel.saveAndActivateApiKey(context, clean) { success, _ ->
                    if (success) {
                      isEditingMode = false
                      inputKey = ""
                    }
                  }
                }
              },
              enabled = inputKey.isNotBlank() && !isTesting,
              modifier = Modifier
                .weight(if (isEditingMode) 1.5f else 1f)
                .testTag("activate_gemini_key_button"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = BasoCyan,
                contentColor = BasoDarkBackground
              )
            ) {
              if (isTesting) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  color = BasoDarkBackground,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.geminiKeyTestingNotice, fontSize = 12.sp)
              } else {
                Icon(
                  imageVector = Icons.Outlined.CheckCircle,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = strings.geminiKeyActivateAction,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            }
          }

          // Link to obtain a free API key from Google AI Studio
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable {
                try {
                  uriHandler.openUri("https://aistudio.google.com/app/apikey")
                } catch (_: Exception) {}
              }
              .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.OpenInNew,
              contentDescription = null,
              tint = BasoCyan,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.geminiKeyGetFreeKeyHelp,
              style = MaterialTheme.typography.labelSmall.copy(
                color = BasoCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }
      }

      // Feedback / Result banner (Testing / Success / Error)
      AnimatedVisibility(
        visible = feedbackMsg != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        if (feedbackMsg != null) {
          val isSuccess = isTestSuccess == true
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSuccess) Color(0x2210B981) else Color(0x22EF4444))
              .border(
                1.dp,
                if (isSuccess) Color(0x6610B981) else Color(0x66EF4444),
                RoundedCornerShape(10.dp)
              )
              .padding(10.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isSuccess) strings.geminiKeySuccessNotice else "${strings.geminiKeyInvalidNotice}\n$feedbackMsg",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                )
              )
            }
          }
        }
      }
    }
  }
}
