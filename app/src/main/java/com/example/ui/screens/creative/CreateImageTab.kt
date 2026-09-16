package com.example.ui.screens.creative

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.CreativeStatus
import com.example.domain.model.ImageGenerationEngine
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CreativeViewModel
import com.example.ui.screens.ImageCreationState

@Composable
fun CreateImageTab(
  viewModel: CreativeViewModel,
  state: ImageCreationState,
  strings: AppStrings,
  currentLanguage: AppLanguage,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  var showKeyDialog by remember { mutableStateOf(false) }

  if (showKeyDialog) {
    ApiKeyInputDialog(
      currentKey = com.example.services.CreativeApiClient.getApiKey(),
      onDismiss = { showKeyDialog = false },
      onSaveKey = { newKey ->
        viewModel.updateCustomApiKey(newKey)
      }
    )
  }

  val promptSuggestions = listOf(
    "قەڵای دێرینی هەولێر لە کاتی دابەزینی باران",
    "کۆڵانێکی مۆدێرنی ژێر ئاو بە ستایلی سایبەرفانک",
    "پشیلەیەکی بچووک بە جلوبەرگی ئاسمانی لەسەر مانگ",
    "سروشتی چیای سەفین لە وەرزی پایزدا",
    "ئۆتۆمبێلێکی کلاسیکی وەرزشی لە ناو بیابان بە ڕووناکی نەئۆن"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Prompt Input Box
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = strings.promptLabel,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          if (state.prompt.isNotBlank()) {
            IconButton(
              onClick = { viewModel.onImagePromptChanged("") },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                Icons.Default.Clear,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        OutlinedTextField(
          value = state.prompt,
          onValueChange = { viewModel.onImagePromptChanged(it) },
          placeholder = {
            Text(
              text = strings.imagePromptPlaceholder,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )
        )

        // Prompt Suggestions Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
              modifier = Modifier.padding(end = 4.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  Icons.Default.Lightbulb,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "پێشنیارەکان:",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          items(promptSuggestions) { suggestion ->
            FilterChip(
              selected = false,
              onClick = { viewModel.onImagePromptChanged(suggestion) },
              label = { Text(suggestion, maxLines = 1) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }

        // Toggle: "✨ Improve my prompt"
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Text(
                text = strings.improvePromptToggle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Switch(
              checked = state.isEnhancePromptEnabled,
              onCheckedChange = { viewModel.togglePromptEnhancer(it) }
            )
          }
        }

        // Show enhanced prompt if available
        AnimatedVisibility(
          visible = !state.enhancedPrompt.isNullOrBlank(),
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut()
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = strings.enhancedPromptLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(Modifier.height(4.dp))
              Text(
                text = state.enhancedPrompt ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // 1.5 Engine Selector (Unlimited AI vs Gemini Flash)
    EngineSelectorCard(
      selectedEngine = state.selectedEngine,
      strings = strings,
      currentLanguage = currentLanguage,
      onEngineSelected = { viewModel.onImageEngineSelected(it) }
    )

    // 2. Style Selector
    StyleSelectorRow(
      selectedStyle = state.selectedStyle,
      strings = strings,
      onStyleSelected = { viewModel.onImageStyleSelected(it) }
    )

    // 3. Aspect Ratio Selector
    AspectRatioSelectorRow(
      selectedRatio = state.selectedAspectRatio,
      strings = strings,
      onRatioSelected = { viewModel.onImageAspectRatioSelected(it) }
    )

    // 4. Image Count Selector (1, 2, 4)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "ژمارەی وێنەکان:",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1, 2, 4).forEach { count ->
          FilterChip(
            selected = state.numberOfImages == count,
            onClick = { viewModel.onImageCountChanged(count) },
            label = { Text("$count") },
            shape = RoundedCornerShape(10.dp)
          )
        }
      }
    }

    // 5. Generate Button
    val isBusy = state.status == CreativeStatus.PREPARING || state.status == CreativeStatus.GENERATING
    Button(
      onClick = { viewModel.generateImage(currentLanguage) },
      enabled = !isBusy && state.prompt.isNotBlank(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(54.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
      )
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = if (isBusy) state.statusMessage ?: strings.generateAction else strings.generateAction,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }

    // 6. Loading State Card
    if (isBusy) {
      StatusProgressCard(
        status = state.status,
        statusMessage = state.statusMessage,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // 7. Error & Config Messages
    if (!state.errorMessage.isNullOrBlank()) {
      if (state.isConfigurationRequired && state.selectedEngine == ImageGenerationEngine.GOOGLE_GEMINI) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ConfigRequiredCard(
            message = state.errorMessage,
            onOpenKeyDialog = { showKeyDialog = true }
          )
          Button(
            onClick = {
              viewModel.onImageEngineSelected(ImageGenerationEngine.UNLIMITED_AI)
              viewModel.generateImage(currentLanguage)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
              text = "دروستکردن بە مۆدی بێسنوور (Unlimited AI)",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      } else {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = state.errorMessage,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (state.selectedEngine != ImageGenerationEngine.UNLIMITED_AI) {
              Button(
                onClick = {
                  viewModel.onImageEngineSelected(ImageGenerationEngine.UNLIMITED_AI)
                  viewModel.generateImage(currentLanguage)
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                  text = "دروستکردن بە مۆدی بێسنوور (Unlimited Mode)",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    }

    // 8. Result Image Display Card
    if (state.generatedUri != null) {
      GeneratedImagePreviewCard(
        imageUri = state.generatedUri,
        revisedPrompt = state.enhancedPrompt,
        strings = strings,
        onSaveToGallery = {
          viewModel.saveProjectToGallery(
            project = com.example.domain.model.CreativeProject(
              id = state.activeProjectId ?: "",
              title = state.prompt.take(30),
              type = com.example.domain.model.CreativeType.IMAGE,
              prompt = state.prompt,
              mediaUri = state.generatedUri.toString()
            )
          ) { success ->
            val msg = if (success) strings.savedSuccessNotice else strings.savedFailNotice
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          }
        },
        onShare = {
          val shareIntent = viewModel.mediaStorageService.createShareIntent(
            uri = state.generatedUri,
            mimeType = "image/png",
            text = state.prompt
          )
          if (shareIntent != null) {
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Image"))
          }
        },
        onRegenerate = { viewModel.generateImage(currentLanguage) },
        onEditImage = { viewModel.useInImageEdit(state.generatedUri) },
        onUseAsVideoRef = { viewModel.useAsReferenceInVideo(state.generatedUri) }
      )
    }
  }
}
