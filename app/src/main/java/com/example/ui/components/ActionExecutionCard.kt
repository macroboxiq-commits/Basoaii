package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActionExecutionResult
import com.example.domain.model.AndroidAction
import com.example.domain.model.BrightnessMode
import com.example.domain.model.RequiredPermissionType
import com.example.domain.model.SilentModeType
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionExecutionCard(
  action: AndroidAction,
  result: ActionExecutionResult?,
  strings: AppStrings,
  onOpenPermission: (RequiredPermissionType) -> Unit,
  onReexecute: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (actionIcon, actionLabel, actionColor) = getActionDisplayDetails(action, strings)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 8.dp)
      .testTag("action_execution_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = BasoDarkSurface
    ),
    border = BorderStroke(1.dp, actionColor.copy(alpha = 0.35f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              actionColor.copy(alpha = 0.08f),
              BasoDarkSurface,
              BasoDarkBackground
            )
          )
        )
        .padding(14.dp)
    ) {
      // 1. Header Row: Action Badge + Status Chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left: Action Type Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(actionColor.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = actionIcon,
            contentDescription = actionLabel,
            tint = actionColor,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            ),
            color = actionColor
          )
        }

        // Right: Execution Status Chip
        StatusChip(result = result, strings = strings)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Action Summary & Content Details
      ActionDetailsView(action = action)

      // 3. Permission Notice & Guidance (if permission required)
      if (result is ActionExecutionResult.PermissionRequired) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF59E0B).copy(alpha = 0.12f),
          border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Permission Needed",
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = strings.actionPermissionRequired,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                ),
                color = Color(0xFFFBBF24)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = result.guidance,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = BasoTextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Interactive Action Buttons
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (result is ActionExecutionResult.PermissionRequired) {
          // Direct Permission Grant Button
          Button(
            onClick = { onOpenPermission(result.permissionType) },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFF59E0B),
              contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("action_grant_permission_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Security,
              contentDescription = strings.grantPermissionAction,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.grantPermissionAction,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        // Secondary Action Button: Launch Native App / Retrigger
        OutlinedButton(
          onClick = onReexecute,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, BasoDarkBorder),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = BasoCyan
          ),
          modifier = Modifier.testTag("action_reexecute_button")
        ) {
          val (btnIcon, btnText) = when (action) {
            is AndroidAction.CreateCalendarEvent -> Pair(Icons.AutoMirrored.Outlined.OpenInNew, strings.openCalendarAction)
            is AndroidAction.SetAlarm -> Pair(Icons.AutoMirrored.Outlined.OpenInNew, strings.openAlarmAction)
            is AndroidAction.SetTimer -> Pair(Icons.Outlined.Timer, "تایمەر")
            is AndroidAction.ToggleFlashlight -> Pair(Icons.Outlined.FlashlightOn, strings.toggleFlashlightAction)
            is AndroidAction.SetSilentMode -> Pair(Icons.AutoMirrored.Outlined.VolumeUp, "گۆڕینی دۆخ")
            is AndroidAction.AdjustBrightness -> Pair(Icons.Outlined.BrightnessMedium, "ڕێکخستنی شاشە")
            is AndroidAction.OpenSystemSettings -> Pair(Icons.Outlined.Settings, "ڕێکخستنەکان")
            is AndroidAction.ToggleWifi -> Pair(Icons.Outlined.Wifi, "وایفای")
            is AndroidAction.ToggleBluetooth -> Pair(Icons.Outlined.Bluetooth, "بلوتوز")
            is AndroidAction.OpenApplication -> Pair(Icons.Outlined.Apps, action.appName)
            is AndroidAction.AdjustVolume -> Pair(Icons.AutoMirrored.Outlined.VolumeUp, "دەنگ")
          }
          Icon(
            imageVector = btnIcon,
            contentDescription = btnText,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = btnText,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
          )
        }
      }
    }
  }
}

@Composable
private fun StatusChip(
  result: ActionExecutionResult?,
  strings: AppStrings
) {
  if (result == null) return

  val (statusText, statusColor, statusIcon) = when (result) {
    is ActionExecutionResult.Success -> Triple(
      strings.actionCompleted,
      Color(0xFF10B981),
      Icons.Outlined.CheckCircle
    )
    is ActionExecutionResult.IntentLaunched -> Triple(
      strings.actionIntentLaunched,
      BasoCyan,
      Icons.AutoMirrored.Outlined.OpenInNew
    )
    is ActionExecutionResult.PermissionRequired -> Triple(
      strings.actionPermissionRequired,
      Color(0xFFF59E0B),
      Icons.Outlined.Warning
    )
    is ActionExecutionResult.Failure -> Triple(
      "هەڵە",
      Color(0xFFEF4444),
      Icons.Outlined.ErrorOutline
    )
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(CircleShape)
      .background(statusColor.copy(alpha = 0.15f))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Icon(
      imageVector = statusIcon,
      contentDescription = statusText,
      tint = statusColor,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = statusText,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
      ),
      color = statusColor
    )
  }
}

@Composable
private fun ActionDetailsView(action: AndroidAction) {
  when (action) {
    is AndroidAction.CreateCalendarEvent -> {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = action.title,
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          ),
          color = BasoTextPrimary
        )
        val timeDisplay = action.formattedTimeDisplay ?: "ئەندرۆید کاتەکەی ڕێکدەخات"
        Text(
          text = "📅 $timeDisplay",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
          color = BasoCyan
        )
        action.description?.let {
          Text(
            text = it,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = BasoTextTertiary
          )
        }
      }
    }

    is AndroidAction.SetAlarm -> {
      val timeStr = String.format("%02d:%02d", action.hour, action.minutes)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(
            text = "کاتژمێر: $timeStr",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = BasoTextPrimary
          )
          action.label?.let {
            Text(
              text = it,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
              color = BasoTextSecondary
            )
          }
        }
      }
    }

    is AndroidAction.SetTimer -> {
      val min = action.lengthSeconds / 60
      val sec = action.lengthSeconds % 60
      val formatted = if (min > 0) "$min خولەک" + if (sec > 0) " و $sec چرکە" else "" else "$sec چرکە"
      Text(
        text = "ماوەی تایمەر: $formatted",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp
        ),
        color = BasoTextPrimary
      )
    }

    is AndroidAction.ToggleFlashlight -> {
      val stateText = if (action.enabled) "فلاش داگیرساوە (ON)" else "فلاش کوژاوەتەوە (OFF)"
      Text(
        text = stateText,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = if (action.enabled) Color(0xFFFBBF24) else BasoTextSecondary
      )
    }

    is AndroidAction.SetSilentMode -> {
      val modeName = when (action.mode) {
        SilentModeType.SILENT -> "دۆخی بێدەنگ (Silent) 🔕"
        SilentModeType.NORMAL -> "دۆخی ئاسایی (Normal) 🔔"
        SilentModeType.VIBRATE -> "دۆخی لەرزین (Vibrate) 📳"
        SilentModeType.TOGGLE -> "گۆڕینی دۆخی دەنگ"
      }
      Text(
        text = modeName,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = BasoPurple
      )
    }

    is AndroidAction.AdjustBrightness -> {
      val desc = when (action.mode) {
        BrightnessMode.SET_LEVEL -> "ڕووناکی: ${action.levelPercent}%"
        BrightnessMode.INCREASE -> "زیادکردنی ڕووناکی"
        BrightnessMode.DECREASE -> "کەمکردنەوەی ڕووناکی"
        BrightnessMode.OPEN_SETTINGS -> "ڕێکخستنەکانی پیشاندانی شاشە"
      }
      Text(
        text = desc,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = Color(0xFFFBBF24)
      )
    }

    is AndroidAction.OpenSystemSettings -> {
      Text(
        text = "ڕێکخستنەکانی سیستەم",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = BasoTextPrimary
      )
    }

    is AndroidAction.ToggleWifi -> {
      Text(
        text = "پەنجەرەی خێرای وایفای (Wi-Fi) 📶",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = BasoCyan
      )
    }

    is AndroidAction.ToggleBluetooth -> {
      Text(
        text = "ڕێکخستنەکانی بلوتوز (Bluetooth) 📡",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = Color(0xFF38BDF8)
      )
    }

    is AndroidAction.OpenApplication -> {
      Text(
        text = "کردنەوەی ئەپی: ${action.appName} 📱",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = BasoPurple
      )
    }

    is AndroidAction.AdjustVolume -> {
      val dirText = when (action.direction) {
        com.example.domain.model.VolumeDirection.UP -> "بەرزکردنەوەی دەنگ 🔊"
        com.example.domain.model.VolumeDirection.DOWN -> "کەمکردنەوەی دەنگ 🔉"
        com.example.domain.model.VolumeDirection.MUTE -> "بێدەنگکردنی دەنگ 🔇"
      }
      Text(
        text = dirText,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = Color(0xFF10B981)
      )
    }
  }
}

private fun getActionDisplayDetails(
  action: AndroidAction,
  strings: AppStrings
): Triple<ImageVector, String, Color> {
  return when (action) {
    is AndroidAction.CreateCalendarEvent -> Triple(
      Icons.Outlined.Event,
      strings.calendarEventLabel,
      BasoCyan
    )
    is AndroidAction.SetAlarm -> Triple(
      Icons.Outlined.Alarm,
      strings.alarmLabel,
      Color(0xFF38BDF8)
    )
    is AndroidAction.SetTimer -> Triple(
      Icons.Outlined.Timer,
      strings.timerLabel,
      BasoCyan
    )
    is AndroidAction.ToggleFlashlight -> Triple(
      if (action.enabled) Icons.Outlined.FlashlightOn else Icons.Outlined.FlashlightOff,
      strings.flashlightLabel,
      Color(0xFFF59E0B)
    )
    is AndroidAction.SetSilentMode -> Triple(
      if (action.mode == SilentModeType.SILENT) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
      strings.silentModeLabel,
      BasoPurple
    )
    is AndroidAction.AdjustBrightness -> Triple(
      Icons.Outlined.BrightnessMedium,
      strings.brightnessLabel,
      Color(0xFFFBBF24)
    )
    is AndroidAction.OpenSystemSettings -> Triple(
      Icons.Outlined.Settings,
      "ڕێکخستنەکانی سیستەم",
      BasoCyan
    )
    is AndroidAction.ToggleWifi -> Triple(
      Icons.Outlined.Wifi,
      "وایفای (Wi-Fi)",
      BasoCyan
    )
    is AndroidAction.ToggleBluetooth -> Triple(
      Icons.Outlined.Bluetooth,
      "بلوتوز (Bluetooth)",
      Color(0xFF38BDF8)
    )
    is AndroidAction.OpenApplication -> Triple(
      Icons.Outlined.Apps,
      action.appName,
      BasoPurple
    )
    is AndroidAction.AdjustVolume -> Triple(
      Icons.AutoMirrored.Outlined.VolumeUp,
      "دەنگی مۆبایل",
      Color(0xFF10B981)
    )
  }
}
