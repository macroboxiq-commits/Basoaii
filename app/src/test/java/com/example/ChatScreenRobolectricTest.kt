package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.BasoAiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatScreenRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testChatScreenLazyColumnAndDistinctBubbles() {
    val sampleMessages = listOf(
      ChatMessage(
        id = "msg_1",
        text = "Hello Baso, can you help me?",
        sender = MessageSender.USER
      ),
      ChatMessage(
        id = "msg_2",
        text = "Hello! I am Baso AI, how can I assist you today?",
        sender = MessageSender.ASSISTANT
      )
    )

    var sentMessage = ""

    composeTestRule.setContent {
      BasoAiTheme {
        ChatScreen(
          messages = sampleMessages,
          onSendMessage = { sentMessage = it }
        )
      }
    }

    // Verify LazyColumn is present
    composeTestRule.onNodeWithTag("chat_lazy_column").assertIsDisplayed()

    // Verify distinct bubble designs exist
    composeTestRule.onNodeWithTag("user_message_bubble").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ai_message_bubble").assertIsDisplayed()

    // Verify message texts are displayed
    composeTestRule.onNodeWithText("Hello Baso, can you help me?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello! I am Baso AI, how can I assist you today?").assertIsDisplayed()

    // Verify persistent input field is displayed
    composeTestRule.onNodeWithTag("chat_input_field").assertIsDisplayed()
  }

  @Test
  fun testChatScreenPersistentInputAndSendFunctionality() {
    val messages = mutableListOf<ChatMessage>()
    var lastSentMessage = ""

    composeTestRule.setContent {
      BasoAiTheme {
        ChatScreen(
          messages = messages,
          onSendMessage = { text ->
            lastSentMessage = text
          }
        )
      }
    }

    // Input field is visible
    val inputNode = composeTestRule.onNodeWithTag("chat_input_field")
    inputNode.assertIsDisplayed()

    // Send button starts disabled when empty
    val sendButton = composeTestRule.onNodeWithTag("send_button")
    sendButton.assertIsNotEnabled()

    // Type text into persistent input field
    inputNode.performTextInput("Remind me to call Mom at 5pm")

    // Send button is now enabled
    sendButton.assertIsEnabled()

    // Click send
    sendButton.performClick()

    // Verify send functionality executed
    assertEquals("Remind me to call Mom at 5pm", lastSentMessage)
  }
}
