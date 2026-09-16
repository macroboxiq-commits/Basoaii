package com.example

import com.example.ai.LocalAssistantEngine
import com.example.data.repository.ChatRepositoryImpl
import com.example.domain.model.ActionExecutionResult
import com.example.domain.model.AndroidAction
import com.example.domain.model.AppLanguage
import com.example.domain.model.BrightnessMode
import com.example.domain.model.RequiredPermissionType
import com.example.domain.model.SilentModeType
import com.example.services.AndroidActionHandler
import com.example.services.SystemActionParser
import com.example.tools.CalendarAndRemindersTool
import com.example.tools.DeviceControlsTool
import com.example.tools.ToolRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidActionsTest {

  // -------------------------------------------------------------
  // 1. Parsing Kurdish, Arabic, and English System Requests
  // -------------------------------------------------------------

  @Test
  fun testKurdishCalendarParsing_TomorrowAt10() {
    val prompt = "دانیشتنێکم بۆ سبەینێ سەعات 10 تۆمار بکە"
    val parsed = SystemActionParser.parsePrompt(prompt, AppLanguage.KURDISH_SORANI)

    assertNotNull("Should parse Kurdish calendar request", parsed)
    val action = parsed!!.action as AndroidAction.CreateCalendarEvent
    assertEquals("دانیشتن (کۆبوونەوە)", action.title)
    assertNotNull(action.startEpochMillis)
    assertTrue("Feedback should mention calendar", parsed.feedbackMessage.contains("ڕۆژمێر"))
  }

  @Test
  fun testKurdishAlarmParsing() {
    val prompt = "کاتژمێر 7 ی بەیانی زەنگم بۆ لێدە"
    val parsed = SystemActionParser.parsePrompt(prompt, AppLanguage.KURDISH_SORANI)

    assertNotNull(parsed)
    val action = parsed!!.action as AndroidAction.SetAlarm
    assertEquals(7, action.hour)
    assertEquals(0, action.minutes)
    assertTrue(parsed.feedbackMessage.contains("زەنگ"))
  }

  @Test
  fun testKurdishFlashlightParsing_OnAndOff() {
    val promptOn = "فلاش دابگیرسێنە"
    val parsedOn = SystemActionParser.parsePrompt(promptOn, AppLanguage.KURDISH_SORANI)
    assertNotNull(parsedOn)
    assertTrue((parsedOn!!.action as AndroidAction.ToggleFlashlight).enabled)

    val promptOff = "فلاش بکوژێنەرەوە"
    val parsedOff = SystemActionParser.parsePrompt(promptOff, AppLanguage.KURDISH_SORANI)
    assertNotNull(parsedOff)
    assertTrue(!(parsedOff!!.action as AndroidAction.ToggleFlashlight).enabled)
  }

  @Test
  fun testKurdishSilentModeParsing() {
    val prompt = "مۆبایلەکەم بێدەنگ بکە"
    val parsed = SystemActionParser.parsePrompt(prompt, AppLanguage.KURDISH_SORANI)
    assertNotNull(parsed)
    val action = parsed!!.action as AndroidAction.SetSilentMode
    assertEquals(SilentModeType.SILENT, action.mode)
  }

  @Test
  fun testKurdishBrightnessParsing() {
    val prompt = "ڕووناکی شاشە 80%"
    val parsed = SystemActionParser.parsePrompt(prompt, AppLanguage.KURDISH_SORANI)
    assertNotNull(parsed)
    val action = parsed!!.action as AndroidAction.AdjustBrightness
    assertEquals(80, action.levelPercent)
    assertEquals(BrightnessMode.SET_LEVEL, action.mode)
  }

  @Test
  fun testArabicRequestsParsing() {
    val calendarPrompt = "سجل اجتماع غداً الساعة 10"
    val calendarParsed = SystemActionParser.parsePrompt(calendarPrompt, AppLanguage.ARABIC)
    assertNotNull(calendarParsed)
    assertTrue(calendarParsed!!.action is AndroidAction.CreateCalendarEvent)

    val flashPrompt = "شغل الفلاش"
    val flashParsed = SystemActionParser.parsePrompt(flashPrompt, AppLanguage.ARABIC)
    assertNotNull(flashParsed)
    assertTrue((flashParsed!!.action as AndroidAction.ToggleFlashlight).enabled)

    val silentPrompt = "الوضع الصامت"
    val silentParsed = SystemActionParser.parsePrompt(silentPrompt, AppLanguage.ARABIC)
    assertNotNull(silentParsed)
    assertEquals(SilentModeType.SILENT, (silentParsed!!.action as AndroidAction.SetSilentMode).mode)
  }

  @Test
  fun testEnglishRequestsParsing() {
    val calendarPrompt = "Schedule a meeting tomorrow at 10 am"
    val calendarParsed = SystemActionParser.parsePrompt(calendarPrompt, AppLanguage.ENGLISH)
    assertNotNull(calendarParsed)
    assertTrue(calendarParsed!!.action is AndroidAction.CreateCalendarEvent)

    val alarmPrompt = "Set an alarm for 7:30"
    val alarmParsed = SystemActionParser.parsePrompt(alarmPrompt, AppLanguage.ENGLISH)
    assertNotNull(alarmParsed)
    val alarm = alarmParsed!!.action as AndroidAction.SetAlarm
    assertEquals(7, alarm.hour)
    assertEquals(30, alarm.minutes)

    val flashPrompt = "Turn on flashlight"
    val flashParsed = SystemActionParser.parsePrompt(flashPrompt, AppLanguage.ENGLISH)
    assertNotNull(flashParsed)
    assertTrue((flashParsed!!.action as AndroidAction.ToggleFlashlight).enabled)
  }

  // -------------------------------------------------------------
  // 2. ToolRegistry & BasoTool Execution
  // -------------------------------------------------------------

  @Test
  fun testToolRegistryDeclarations() {
    val registry = ToolRegistry(actionHandler = null)
    val declarations = registry.getGeminiFunctionDeclarations()
    assertTrue(declarations.length() >= 5)

    val registeredTools = registry.getRegisteredTools()
    assertTrue(registeredTools.any { it.id == "reminders" && it.isEnabled })
    assertTrue(registeredTools.any { it.id == "android_actions" && it.isEnabled })
  }

  @Test
  fun testToolExecutionDirectly() = runBlocking {
    val registry = ToolRegistry(actionHandler = null)
    val params = mapOf<String, Any>(
      "type" to "alarm",
      "hour" to 7,
      "minutes" to 0,
      "label" to "Work"
    )
    val result = registry.executeTool("reminders", params)
    assertNotNull(result)
  }

  // -------------------------------------------------------------
  // 3. ChatRepository & AI Engine Workflow
  // -------------------------------------------------------------

  @Test
  fun testLocalAssistantEngineSystemAction() = runBlocking {
    val engine = LocalAssistantEngine()
    val response = engine.processPrompt(
      prompt = "دانیشتنێکم بۆ سبەینێ سەعات 10 تۆمار بکە",
      history = emptyList(),
      language = AppLanguage.KURDISH_SORANI
    )

    assertNotNull("Should identify system action in assistant response", response.systemAction)
    assertTrue(response.systemAction is AndroidAction.CreateCalendarEvent)
    assertTrue(response.text.contains("ڕۆژمێر"))
  }

  @Test
  fun testChatRepositorySystemActionEndToEnd() = runBlocking {
    val repo = ChatRepositoryImpl(
      aiEngine = LocalAssistantEngine(),
      actionHandler = null
    )

    repo.sendMessage("دانیشتنێکم بۆ سبەینێ سەعات 10 تۆمار بکە", AppLanguage.KURDISH_SORANI)
    val messages = repo.messages.first()

    assertEquals(2, messages.size) // User message + Assistant message
    val assistantMsg = messages[1]
    assertNotNull("Assistant message should attach system action", assistantMsg.systemAction)
    assertTrue(assistantMsg.systemAction is AndroidAction.CreateCalendarEvent)
  }
}
