package com.example.tools

import com.example.domain.model.ActionExecutionResult
import com.example.domain.model.AndroidAction
import com.example.domain.model.BrightnessMode
import com.example.domain.model.SilentModeType
import com.example.services.AndroidActionHandler
import org.json.JSONArray
import org.json.JSONObject

/**
 * Concrete BasoTool implementation for Reminders, Alarms, and Calendar Events.
 */
class CalendarAndRemindersTool(
  private val actionHandler: AndroidActionHandler
) : BasoTool {

  override val definition: ToolDefinition = ToolDefinition(
    id = "reminders",
    name = "Reminders & Calendar",
    description = "Schedule calendar meetings, set alarms, and start timers on the Android device",
    isEnabled = true
  )

  override suspend fun execute(parameters: Map<String, Any>): ToolResult {
    val actionType = parameters["type"]?.toString() ?: "calendar"
    val action: AndroidAction = when (actionType) {
      "alarm" -> {
        val hour = (parameters["hour"] as? Number)?.toInt() ?: 7
        val minutes = (parameters["minutes"] as? Number)?.toInt() ?: 0
        val label = parameters["label"]?.toString() ?: "Baso AI Alarm"
        AndroidAction.SetAlarm(hour = hour, minutes = minutes, label = label)
      }
      "timer" -> {
        val seconds = (parameters["seconds"] as? Number)?.toInt() ?: 300
        val label = parameters["label"]?.toString() ?: "Baso AI Timer"
        AndroidAction.SetTimer(lengthSeconds = seconds, label = label)
      }
      else -> {
        val title = parameters["title"]?.toString() ?: "دانیشتن"
        val startMillis = (parameters["start_epoch_ms"] as? Number)?.toLong()
        val endMillis = (parameters["end_epoch_ms"] as? Number)?.toLong()
        AndroidAction.CreateCalendarEvent(
          title = title,
          description = parameters["description"]?.toString(),
          location = parameters["location"]?.toString(),
          startEpochMillis = startMillis,
          endEpochMillis = endMillis
        )
      }
    }

    val result = actionHandler.executeAction(action)
    return when (result) {
      is ActionExecutionResult.Success -> ToolResult.Success(result.message)
      is ActionExecutionResult.IntentLaunched -> ToolResult.Success(result.message)
      is ActionExecutionResult.PermissionRequired -> ToolResult.Failure("Permission required: ${result.permissionType}")
      is ActionExecutionResult.Failure -> ToolResult.Failure(result.error)
    }
  }
}

/**
 * Concrete BasoTool implementation for Device Controls (Flashlight, Silent Mode, Brightness).
 */
class DeviceControlsTool(
  private val actionHandler: AndroidActionHandler
) : BasoTool {

  override val definition: ToolDefinition = ToolDefinition(
    id = "android_actions",
    name = "Device Controls",
    description = "Control hardware toggles: flashlight on/off, silent mode, and screen brightness",
    isEnabled = true
  )

  override suspend fun execute(parameters: Map<String, Any>): ToolResult {
    val command = parameters["command"]?.toString() ?: "flashlight"
    val action: AndroidAction = when (command) {
      "flashlight" -> {
        val enabled = parameters["enabled"] as? Boolean ?: true
        AndroidAction.ToggleFlashlight(enabled = enabled)
      }
      "silent_mode" -> {
        val modeStr = parameters["mode"]?.toString()?.uppercase() ?: "SILENT"
        val mode = when (modeStr) {
          "NORMAL", "UNMUTE" -> SilentModeType.NORMAL
          "VIBRATE" -> SilentModeType.VIBRATE
          "TOGGLE" -> SilentModeType.TOGGLE
          else -> SilentModeType.SILENT
        }
        AndroidAction.SetSilentMode(mode)
      }
      "brightness" -> {
        val level = (parameters["level"] as? Number)?.toInt()
        val modeStr = parameters["mode"]?.toString()?.uppercase() ?: "SET_LEVEL"
        val mode = when (modeStr) {
          "INCREASE" -> BrightnessMode.INCREASE
          "DECREASE" -> BrightnessMode.DECREASE
          "OPEN_SETTINGS" -> BrightnessMode.OPEN_SETTINGS
          else -> BrightnessMode.SET_LEVEL
        }
        AndroidAction.AdjustBrightness(levelPercent = level, mode = mode)
      }
      else -> {
        return ToolResult.Failure("Unknown device command: $command")
      }
    }

    val result = actionHandler.executeAction(action)
    return when (result) {
      is ActionExecutionResult.Success -> ToolResult.Success(result.message)
      is ActionExecutionResult.IntentLaunched -> ToolResult.Success(result.message)
      is ActionExecutionResult.PermissionRequired -> ToolResult.Failure(result.guidance)
      is ActionExecutionResult.Failure -> ToolResult.Failure(result.error)
    }
  }
}
