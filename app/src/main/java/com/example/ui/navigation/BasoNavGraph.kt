package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.localization.LocalizedStrings
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ChatViewModel
import com.example.ui.screens.CreativeViewModel
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettingsViewModel
import com.example.ui.screens.VoiceViewModel
import com.example.ui.screens.creative.CreativeStudioScreen

@Composable
fun BasoApp(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  chatViewModel: ChatViewModel = viewModel(),
  settingsViewModel: SettingsViewModel = viewModel(),
  voiceViewModel: VoiceViewModel = viewModel(),
  creativeViewModel: CreativeViewModel = viewModel()
) {
  val settings by settingsViewModel.settingsState.collectAsState()
  val currentLanguage = settings.language
  val strings = LocalizedStrings.get(currentLanguage)
  val layoutDirection = if (currentLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    NavHost(
      navController = navController,
      startDestination = NavDestinations.CHAT,
      modifier = modifier
    ) {
      composable(
        route = NavDestinations.CHAT,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
      ) {
        ChatScreen(
          viewModel = chatViewModel,
          voiceViewModel = voiceViewModel,
          voiceSettings = settings.voiceSettings,
          currentLanguage = currentLanguage,
          strings = strings,
          onNavigateToSettings = {
            navController.navigate(NavDestinations.SETTINGS)
          },
          onNavigateToCreativeStudio = { tab ->
            if (tab != null) {
              creativeViewModel.selectTab(tab)
            }
            navController.navigate(NavDestinations.CREATIVE_STUDIO)
          }
        )
      }

      composable(
        route = NavDestinations.CREATIVE_STUDIO,
        enterTransition = {
          slideInHorizontally(initialOffsetX = { if (currentLanguage.isRtl) -it else it }) + fadeIn()
        },
        exitTransition = {
          slideOutHorizontally(targetOffsetX = { if (currentLanguage.isRtl) -it else it }) + fadeOut()
        }
      ) {
        CreativeStudioScreen(
          viewModel = creativeViewModel,
          strings = strings,
          currentLanguage = currentLanguage,
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }

      composable(
        route = NavDestinations.SETTINGS,
        enterTransition = {
          slideInHorizontally(initialOffsetX = { if (currentLanguage.isRtl) -it else it }) + fadeIn()
        },
        exitTransition = {
          slideOutHorizontally(targetOffsetX = { if (currentLanguage.isRtl) -it else it }) + fadeOut()
        }
      ) {
        SettingsScreen(
          viewModel = settingsViewModel,
          strings = strings,
          onNavigateBack = {
            navController.popBackStack()
          }
        )
      }
    }
  }
}
