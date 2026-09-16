package com.example

import com.example.ai.LocalAssistantEngine
import com.example.domain.model.AppLanguage
import com.example.tools.ToolRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testAppLanguageDefaultIsKurdish() {
    assertEquals(AppLanguage.KURDISH_SORANI, AppLanguage.DEFAULT)
    assertTrue(AppLanguage.DEFAULT.isRtl)
    assertEquals("ckb", AppLanguage.DEFAULT.code)
  }

  @Test
  fun testLocalAssistantGeneratesKurdishResponse() = runTest {
    val engine = LocalAssistantEngine()
    val response = engine.generateResponse(
      prompt = "سڵاو",
      history = emptyList(),
      language = AppLanguage.KURDISH_SORANI
    )
    assertNotNull(response)
    assertTrue(response.isNotEmpty())
    assertTrue(response.contains("Baso AI"))
  }

  @Test
  fun testToolRegistryPrepared() {
    val registry = ToolRegistry()
    val tools = registry.getRegisteredTools()
    assertTrue(tools.isNotEmpty())
    assertTrue(tools.any { it.id == "web_search" })
    assertTrue(tools.any { it.id == "memory" })
    assertTrue(tools.any { it.id == "image_understanding" })
  }
}
