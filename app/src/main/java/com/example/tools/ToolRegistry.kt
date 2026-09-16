package com.example.tools

import com.example.domain.model.ActionExecutionResult
import com.example.domain.model.AndroidAction
import com.example.domain.model.BrightnessMode
import com.example.domain.model.SilentModeType
import com.example.services.AndroidActionHandler
import org.json.JSONArray
import org.json.JSONObject

/**
 * Registry holding tools for Baso AI:
 * Reminders & Alarms, Device Controls / Android Actions, Long-Term Memory, etc.
 */
class ToolRegistry(
  private val actionHandler: AndroidActionHandler? = null
) {

  private val tools: MutableMap<String, BasoTool> = mutableMapOf()

  init {
    if (actionHandler != null) {
      val calendarTool = CalendarAndRemindersTool(actionHandler)
      val deviceTool = DeviceControlsTool(actionHandler)
      tools[calendarTool.definition.id] = calendarTool
      tools[deviceTool.definition.id] = deviceTool
    }
  }

  fun getRegisteredTools(): List<ToolDefinition> {
    return listOf(
      ToolDefinition(
        id = "web_search",
        name = "Web Search",
        description = "Search the live web for real-time information",
        isEnabled = false
      ),
      ToolDefinition(
        id = "image_understanding",
        name = "Image Understanding",
        description = "Analyze visuals and multimodal queries",
        isEnabled = false
      ),
      ToolDefinition(
        id = "file_analysis",
        name = "File Analysis",
        description = "Inspect and summarize attached documents",
        isEnabled = false
      ),
      ToolDefinition(
        id = "memory",
        name = "Long-Term Memory",
        description = "Store and retrieve personal assistant context and preferences",
        isEnabled = true
      ),
      ToolDefinition(
        id = "reminders",
        name = "Reminders & Alarms",
        description = "Schedule calendar events, alarms, and timers on Android",
        isEnabled = true
      ),
      ToolDefinition(
        id = "android_actions",
        name = "Android Actions & Device Controls",
        description = "Control hardware toggles (flashlight, silent mode, brightness)",
        isEnabled = true
      )
    )
  }

  suspend fun executeTool(toolId: String, parameters: Map<String, Any>): ToolResult {
    val tool = tools[toolId] ?: return ToolResult.Failure("Tool not found or not initialized: $toolId")
    return tool.execute(parameters)
  }

  /**
   * Generates Gemini Tools JSON array structure for function calling.
   */
  fun getGeminiFunctionDeclarations(): JSONArray {
    val declarations = JSONArray()

    // 1. create_calendar_event
    declarations.put(
      JSONObject().apply {
        put("name", "create_calendar_event")
        put("description", "Create or schedule a calendar meeting or event on the Android device")
        put("parameters", JSONObject().apply {
          put("type", "OBJECT")
          put("properties", JSONObject().apply {
            put("title", JSONObject().apply {
              put("type", "STRING")
              put("description", "Title or subject of the meeting/event (e.g. دانیشتن, Meeting)")
            })
            put("description", JSONObject().apply {
              put("type", "STRING")
              put("description", "Optional notes or details for the event")
            })
            put("start_time_iso", JSONObject().apply {
              put("type", "STRING")
              put("description", "Optional ISO-8601 start time or relative indicator")
            })
          })
          put("required", JSONArray().apply { put("title") })
        })
      }
    )

    // 2. set_alarm
    declarations.put(
      JSONObject().apply {
        put("name", "set_alarm")
        put("description", "Set an alarm clock on Android")
        put("parameters", JSONObject().apply {
          put("type", "OBJECT")
          put("properties", JSONObject().apply {
            put("hour", JSONObject().apply {
              put("type", "INTEGER")
              put("description", "Hour of the alarm (0-23)")
            })
            put("minutes", JSONObject().apply {
              put("type", "INTEGER")
              put("description", "Minute of the alarm (0-59)")
            })
            put("label", JSONObject().apply {
              put("type", "STRING")
              put("description", "Label for the alarm")
            })
          })
          put("required", JSONArray().apply { put("hour"); put("minutes") })
        })
      }
    )

    // 3. set_timer
    declarations.put(
      JSONObject().apply {
        put("name", "set_timer")
        put("description", "Set a countdown timer on Android")
        put("parameters", JSONObject().apply {
          put("type", "OBJECT")
          put("properties", JSONObject().apply {
            put("seconds", JSONObject().apply {
              put("type", "INTEGER")
              put("description", "Timer duration in seconds")
            })
            put("label", JSONObject().apply {
              put("type", "STRING")
              put("description", "Timer label")
            })
          })
          put("required", JSONArray().apply { put("seconds") })
        })
      }
    )

    // 4. toggle_flashlight
    declarations.put(
      JSONObject().apply {
        put("name", "toggle_flashlight")
        put("description", "Turn the device camera flashlight / torch on or off")
        put("parameters", JSONObject().apply {
          put("type", "OBJECT")
          put("properties", JSONObject().apply {
            put("enabled", JSONObject().apply {
              put("type", "BOOLEAN")
              put("description", "true to turn on, false to turn off")
            })
          })
          put("required", JSONArray().apply { put("enabled") })
        })
      }
    )

    // 5. set_silent_mode
    declarations.put(
      JSONObject().apply {
        put("name", "set_silent_mode")
        put("description", "Set phone ringer mode to SILENT, NORMAL, or VIBRATE")
        put("parameters", JSONObject().apply {
          put("type", "OBJECT")
          put("properties", JSONObject().apply {
            put("mode", JSONObject().apply {
              put("type", "STRING")
              put("description", "Ringer mode: 'SILENT', 'NORMAL', 'VIBRATE', or 'TOGGLE'")
            })
          })
          put("required", JSONArray().apply { put("mode") })
        })
      }
    )

    // 6. adjust_brightness
    declarations.put(
      JSONObject().apply {
        put("name", "adjust_brightness")
        put("description", "Adjust screen brightness level or open display settings")
        put("parameters", JSONObject().apply {
          put("type", "OBJECT")
          put("properties", JSONObject().apply {
            put("level_percent", JSONObject().apply {
              put("type", "INTEGER")
              put("description", "Screen brightness percentage (0-100)")
            })
            put("mode", JSONObject().apply {
              put("type", "STRING")
              put("description", "Mode: 'SET_LEVEL', 'INCREASE', 'DECREASE', 'OPEN_SETTINGS'")
            })
          })
        })
      }
    )

    return declarations
  }

  /**
   * Dispatches a tool/function call output from Gemini into an AndroidAction.
   */
  fun parseFunctionCallToAction(name: String, args: JSONObject): AndroidAction? {
    return when (name) {
      "create_calendar_event" -> {
        val title = args.optString("title", "دانیشتن")
        val desc = if (args.has("description")) args.getString("description") else null
        AndroidAction.CreateCalendarEvent(
          title = title,
          description = desc
        )
      }
      "set_alarm" -> {
        val hour = args.optInt("hour", 7)
        val minutes = args.optInt("minutes", 0)
        val label = if (args.has("label")) args.getString("label") else "Baso AI Alarm"
        AndroidAction.SetAlarm(hour = hour, minutes = minutes, label = label)
      }
      "set_timer" -> {
        val seconds = args.optInt("seconds", 300)
        val label = if (args.has("label")) args.getString("label") else "Baso AI Timer"
        AndroidAction.SetTimer(lengthSeconds = seconds, label = label)
      }
      "toggle_flashlight" -> {
        val enabled = args.optBoolean("enabled", true)
        AndroidAction.ToggleFlashlight(enabled = enabled)
      }
      "set_silent_mode" -> {
        val modeStr = args.optString("mode", "SILENT").uppercase()
        val mode = when (modeStr) {
          "NORMAL", "UNMUTE" -> SilentModeType.NORMAL
          "VIBRATE" -> SilentModeType.VIBRATE
          "TOGGLE" -> SilentModeType.TOGGLE
          else -> SilentModeType.SILENT
        }
        AndroidAction.SetSilentMode(mode)
      }
      "adjust_brightness" -> {
        val level = if (args.has("level_percent")) args.getInt("level_percent") else null
        val modeStr = args.optString("mode", "SET_LEVEL").uppercase()
        val mode = when (modeStr) {
          "INCREASE" -> BrightnessMode.INCREASE
          "DECREASE" -> BrightnessMode.DECREASE
          "OPEN_SETTINGS" -> BrightnessMode.OPEN_SETTINGS
          else -> BrightnessMode.SET_LEVEL
        }
        AndroidAction.AdjustBrightness(levelPercent = level, mode = mode)
      }
      else -> null
    }
  }
}
