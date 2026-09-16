package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.services.CreativeApiClient
import com.example.ui.navigation.BasoApp
import com.example.ui.screens.ChatViewModel
import com.example.ui.screens.SettingsViewModel
import com.example.ui.theme.BasoAiTheme
import com.example.ui.theme.BasoDarkBackground

class MainActivity : ComponentActivity() {
  private val chatViewModel: ChatViewModel by viewModels()
  private val settingsViewModel: SettingsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CreativeApiClient.initialize(applicationContext)
    settingsViewModel.refreshApiKeyStatus(applicationContext)
    enableEdgeToEdge()
    setContent {
      val settings by settingsViewModel.settingsState.collectAsState()
      BasoAiTheme(darkTheme = settings.isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = BasoDarkBackground
        ) {
          BasoApp(
            chatViewModel = chatViewModel,
            settingsViewModel = settingsViewModel
          )
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  BasoAiTheme { Greeting("Baso AI") }
}
