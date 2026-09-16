package com.example.domain.model

enum class SilentModeType {
  SILENT,
  NORMAL,
  VIBRATE,
  TOGGLE
}

enum class BrightnessMode {
  SET_LEVEL,
  INCREASE,
  DECREASE,
  OPEN_SETTINGS
}

enum class SettingsTarget {
  GENERAL,
  DISPLAY,
  SOUND,
  NOTIFICATION_POLICY,
  WRITE_SETTINGS,
  ALARM,
  APPLICATION_DETAILS
}

enum class RequiredPermissionType {
  CAMERA,
  NOTIFICATION_POLICY,
  WRITE_SETTINGS
}

enum class VolumeDirection {
  UP,
  DOWN,
  MUTE
}

/**
 * Domain model representing actionable native Android system operations
 * triggered by Gemini tool calls or natural language commands.
 */
sealed interface AndroidAction {

  data class CreateCalendarEvent(
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startEpochMillis: Long? = null,
    val endEpochMillis: Long? = null,
    val allDay: Boolean = false,
    val formattedTimeDisplay: String? = null
  ) : AndroidAction

  data class SetAlarm(
    val hour: Int,
    val minutes: Int,
    val label: String? = null,
    val skipUi: Boolean = false
  ) : AndroidAction

  data class SetTimer(
    val lengthSeconds: Int,
    val label: String? = null,
    val skipUi: Boolean = false
  ) : AndroidAction

  data class ToggleFlashlight(
    val enabled: Boolean
  ) : AndroidAction

  data class SetSilentMode(
    val mode: SilentModeType
  ) : AndroidAction

  data class AdjustBrightness(
    val levelPercent: Int? = null,
    val mode: BrightnessMode = BrightnessMode.SET_LEVEL
  ) : AndroidAction

  data class OpenSystemSettings(
    val target: SettingsTarget
  ) : AndroidAction

  data class ToggleWifi(
    val enabled: Boolean? = null,
    val openPanel: Boolean = true
  ) : AndroidAction

  data class ToggleBluetooth(
    val enabled: Boolean? = null,
    val openPanel: Boolean = true
  ) : AndroidAction

  data class OpenApplication(
    val appName: String,
    val packageName: String? = null
  ) : AndroidAction

  data class AdjustVolume(
    val direction: VolumeDirection
  ) : AndroidAction
}

/**
 * Result returned after executing an AndroidAction via AndroidActionHandler.
 */
sealed interface ActionExecutionResult {

  data class Success(
    val message: String,
    val action: AndroidAction,
    val details: String? = null
  ) : ActionExecutionResult

  data class IntentLaunched(
    val message: String,
    val intentName: String,
    val action: AndroidAction
  ) : ActionExecutionResult

  data class PermissionRequired(
    val message: String,
    val permissionType: RequiredPermissionType,
    val action: AndroidAction,
    val guidance: String
  ) : ActionExecutionResult

  data class Failure(
    val error: String,
    val action: AndroidAction? = null
  ) : ActionExecutionResult
}
