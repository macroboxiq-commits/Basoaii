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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.services.CreativeApiClient
import com.example.ui.localization.AppStrings
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBackground
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary
import com.example.ui.theme.BasoTextTertiary
import kotlinx.coroutines.launch

/**
 * Direct In-App Gemini API Key Activation Dialog.
 * Allows users to paste, test, and activate their Google Gemini API key immediately.
 */
@Composable
fun GeminiActivationDialog(
  strings: AppStrings,
  onDismiss: () -> Unit,
  onKeyActivated: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val uriHandler = LocalUriHandler.current
  val scope = rememberCoroutineScope()

  var inputKey by remember { mutableStateOf("") }
  var isKeyVisible by remember { mutableStateOf(false) }
  var isTesting by remember { mutableStateOf(false) }
  var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

  val isConfigured = CreativeApiClient.isKeyConfigured()
  val currentMaskedKey = CreativeApiClient.getMaskedApiKey()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .testTag("gemini_activation_dialog"),
      shape = RoundedCornerShape(24.dp),
      color = BasoDarkSurface,
      border = BorderStroke(1.dp, BasoCyan.copy(alpha = 0.4f)),
      tonalElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Top Title Row
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
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x2200E5FF)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                tint = BasoCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = strings.geminiKeySectionTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                ),
                color = BasoTextPrimary
              )
              Text(
                text = strings.geminiKeySectionSubtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = BasoTextTertiary
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(BasoDarkSurfaceHigh)
          ) {
            Icon(
              imageVector = Icons.Outlined.Close,
              contentDescription = strings.cancelAction,
              tint = BasoTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Current status banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isConfigured) Color(0x2210B981) else Color(0x22F59E0B))
            .border(
              1.dp,
              if (isConfigured) Color(0x5510B981) else Color(0x55F59E0B),
              RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (isConfigured) strings.geminiKeyStatusActive else strings.geminiKeyStatusInactive,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              ),
              color = if (isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)
            )

            if (isConfigured && currentMaskedKey.isNotBlank()) {
              Text(
                text = currentMaskedKey,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp
                ),
                color = BasoTextSecondary
              )
            }
          }
        }

        // Description
        Text(
          text = strings.geminiKeyHelpDescription,
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
          color = BasoTextSecondary
        )

        // API Key Input Field
        OutlinedTextField(
          value = inputKey,
          onValueChange = { inputKey = it },
          label = { Text(strings.geminiKeyInputLabel) },
          placeholder = { Text(strings.geminiKeyPlaceholder) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_gemini_key_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.AutoAwesome,
              contentDescription = null,
              tint = BasoCyan,
              modifier = Modifier.size(18.dp)
            )
          },
          trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

        // Result / Feedback Banner
        AnimatedVisibility(
          visible = testResult != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          testResult?.let { (success, msg) ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (success) Color(0x2210B981) else Color(0x22EF4444))
                .border(
                  1.dp,
                  if (success) Color(0x6610B981) else Color(0x66EF4444),
                  RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  imageVector = if (success) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                  contentDescription = null,
                  tint = if (success) Color(0xFF10B981) else Color(0xFFEF4444),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (success) strings.geminiKeySuccessNotice else "${strings.geminiKeyInvalidNotice}\n$msg",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = if (success) Color(0xFF10B981) else Color(0xFFEF4444)
                  )
                )
              }
            }
          }
        }

        // Link for Google AI Studio
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              try {
                uriHandler.openUri("https://aistudio.google.com/app/apikey")
              } catch (_: Exception) {}
            }
            .padding(vertical = 4.dp),
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
              fontSize = 11.sp
            )
          )
        }

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (isConfigured) {
            TextButton(
              onClick = {
                CreativeApiClient.setCustomApiKey(context, "")
                onKeyActivated?.invoke()
                onDismiss()
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = strings.geminiKeyRemoveAction,
                color = Color(0xFFEF4444),
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.weight(1f))

          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BasoTextSecondary)
          ) {
            Text(strings.cancelAction, fontSize = 12.sp)
          }

          Button(
            onClick = {
              val clean = inputKey.trim()
              if (clean.isNotBlank()) {
                scope.launch {
                  isTesting = true
                  val result = CreativeApiClient.testApiKey(clean)
                  testResult = result
                  isTesting = false
                  if (result.first) {
                    CreativeApiClient.setCustomApiKey(context, clean)
                    onKeyActivated?.invoke()
                  }
                }
              }
            },
            enabled = inputKey.isNotBlank() && !isTesting,
            modifier = Modifier.testTag("dialog_activate_gemini_button"),
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
              Spacer(modifier = Modifier.width(6.dp))
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
      }
    }
  }
}
