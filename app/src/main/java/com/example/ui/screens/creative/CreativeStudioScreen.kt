package com.example.ui.screens.creative

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.domain.model.AppLanguage
import com.example.services.CreativeApiClient
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CreativeStudioTab
import com.example.ui.screens.CreativeViewModel

@Composable
fun CreativeStudioScreen(
  viewModel: CreativeViewModel,
  strings: AppStrings,
  currentLanguage: AppLanguage,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTab by viewModel.currentTab.collectAsState()
  val imageState by viewModel.imageState.collectAsState()
  val videoState by viewModel.videoState.collectAsState()
  val editState by viewModel.editState.collectAsState()
  val socialState by viewModel.socialState.collectAsState()
  val projects by viewModel.projects.collectAsState()
  val isKeyConfigured by viewModel.isApiKeyConfiguredFlow.collectAsState()

  var showKeyDialog by remember { mutableStateOf(false) }

  if (showKeyDialog) {
    ApiKeyInputDialog(
      currentKey = CreativeApiClient.getApiKey(),
      onDismiss = { showKeyDialog = false },
      onSaveKey = { newKey ->
        viewModel.updateCustomApiKey(newKey)
      }
    )
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      CreativeTopBar(
        strings = strings,
        isKeyConfigured = isKeyConfigured,
        onNavigateBack = onNavigateBack,
        onOpenKeyDialog = { showKeyDialog = true }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Tab Bar
      CreativeTabBar(
        currentTab = currentTab,
        strings = strings,
        onTabSelected = { viewModel.selectTab(it) }
      )

      // Content for active tab
      AnimatedContent(
        targetState = currentTab,
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
        label = "tabContentTransition",
        modifier = Modifier.fillMaxSize()
      ) { tab ->
        when (tab) {
          CreativeStudioTab.CREATE_IMAGE -> {
            CreateImageTab(
              viewModel = viewModel,
              state = imageState,
              strings = strings,
              currentLanguage = currentLanguage
            )
          }

          CreativeStudioTab.CREATE_VIDEO -> {
            CreateVideoTab(
              viewModel = viewModel,
              state = videoState,
              strings = strings,
              currentLanguage = currentLanguage
            )
          }

          CreativeStudioTab.EDIT_IMAGE -> {
            EditImageTab(
              viewModel = viewModel,
              state = editState,
              strings = strings,
              currentLanguage = currentLanguage
            )
          }

          CreativeStudioTab.SOCIAL_MEDIA -> {
            SocialMediaTab(
              viewModel = viewModel,
              state = socialState,
              strings = strings,
              currentLanguage = currentLanguage
            )
          }

          CreativeStudioTab.HISTORY -> {
            CreativeHistoryTab(
              viewModel = viewModel,
              projects = projects,
              strings = strings,
              onNavigateToTab = { viewModel.selectTab(it) }
            )
          }
        }
      }
    }
  }
}
