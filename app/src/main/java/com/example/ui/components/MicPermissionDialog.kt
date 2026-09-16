package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.localization.AppStrings
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBackground
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary

@Composable
fun MicPermissionDialog(
  strings: AppStrings,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .padding(16.dp),
      colors = CardDefaults.cardColors(containerColor = BasoDarkSurface),
      border = BorderStroke(1.dp, BasoDarkBorder),
      shape = RoundedCornerShape(20.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Outlined.MicOff,
          contentDescription = null,
          tint = Color(0xFFFF5252),
          modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = strings.micPermissionLabel,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          ),
          color = BasoTextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = strings.micPermissionRequiredMessage,
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
          color = BasoTextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("permission_cancel_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BasoDarkBorder)
          ) {
            Text(
              text = strings.cancelAction,
              color = BasoTextSecondary,
              fontSize = 13.sp
            )
          }

          Button(
            onClick = {
              openAppSettings(context)
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .testTag("open_settings_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BasoCyan,
              contentColor = BasoDarkBackground
            )
          ) {
            Text(
              text = strings.openSettingsAction,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}

private fun openAppSettings(context: Context) {
  try {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", context.packageName, null)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  } catch (_: Exception) {
  }
}
