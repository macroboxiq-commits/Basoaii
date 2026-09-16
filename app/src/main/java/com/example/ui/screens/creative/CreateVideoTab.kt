package com.example.ui.screens.creative

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.AppLanguage
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeStatus
import com.example.domain.model.CreativeType
import com.example.domain.model.VideoDuration
import com.example.domain.model.VideoStyle
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CreativeViewModel
import com.example.ui.screens.VideoCreationState

@Composable
fun CreateVideoTab(
  viewModel: CreativeViewModel,
  state: VideoCreationState,
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

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.setVideoReferenceImage(context, uri)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 0. AI Studio Veo API Notice Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
      ),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "تایبەتمەندی دروستکردنی ڤیدیۆ (Google Veo)",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "مۆدێلی Google Veo پێویستی بە کلیل (API Key) هەیە. دەتوانیت بە دوگمەی خوارەوە ڕاستەوخۆ کلیلەکەت لەناو بەرنامەکە بنووسیت یان دابنێیت.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 18.sp
            )
          }
        }

        // Direct Key Setup Button
        Button(
          onClick = { showKeyDialog = true },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(8.dp))
          Text(
            text = if (viewModel.isApiKeyConfigured) "گۆڕینی کلیلی Google AI 🔑" else "دانانی کلیل ڕاستەوخۆ لە ئەپدا 🔑",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    // 1. Text Prompt Input
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              Icons.Default.Videocam,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "داواکاری ڤیدیۆ (Google Veo)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          if (state.prompt.isNotBlank()) {
            IconButton(
              onClick = { viewModel.onVideoPromptChanged("") },
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
          onValueChange = { viewModel.onVideoPromptChanged(it) },
          placeholder = {
            Text(
              text = strings.videoPromptPlaceholder,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )
        )

        // 2. Reference Image for Image-to-Video
        Text(
          text = "وێنەی سەرەکی بۆ ڤیدیۆ (Image to Video):",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )

        if (state.referenceImageUri != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(state.referenceImageUri)
                .crossfade(true)
                .build(),
              contentDescription = "Reference Image",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
            IconButton(
              onClick = { viewModel.setVideoReferenceImage(context, null) },
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
            ) {
              Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
            }
          }
        } else {
          OutlinedButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("دانانی وێنە بۆ دروستکردنی ڤیدیۆ لەسەری")
          }
        }
      }
    }

    // 3. Video Style Selector
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "شێوازی ڤیدیۆ:",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        VideoStyle.entries.forEach { style ->
          FilterChip(
            selected = state.selectedStyle == style,
            onClick = { viewModel.onVideoStyleSelected(style) },
            label = { Text(style.labelKu) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // 4. Video Aspect Ratio
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = strings.aspectRatioLabel,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          AspectRatioOption.LANDSCAPE_16_9,
          AspectRatioOption.PORTRAIT_9_16,
          AspectRatioOption.SQUARE
        ).forEach { ratio ->
          FilterChip(
            selected = state.selectedAspectRatio == ratio,
            onClick = { viewModel.onVideoAspectRatioSelected(ratio) },
            label = { Text(ratio.labelKu) },
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // 5. Video Duration
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = strings.durationLabel,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        VideoDuration.entries.forEach { dur ->
          FilterChip(
            selected = state.selectedDuration == dur,
            onClick = { viewModel.onVideoDurationSelected(dur) },
            label = { Text(dur.labelKu) },
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
    }

    // 6. Generate Video Button
    val isBusy = state.status == CreativeStatus.PREPARING ||
        state.status == CreativeStatus.GENERATING ||
        state.status == CreativeStatus.PROCESSING

    Button(
      onClick = { viewModel.generateVideo(currentLanguage) },
      enabled = !isBusy && (state.prompt.isNotBlank() || state.referenceImageBytes != null),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(54.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
      )
    ) {
      Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = if (isBusy) state.statusMessage ?: strings.generateAction else "دروستکردنی ڤیدیۆ (Generate Video)",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }

    // 7. Status Card
    if (isBusy) {
      StatusProgressCard(
        status = state.status,
        statusMessage = state.statusMessage,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // 8. Error / Config Notice
    if (!state.errorMessage.isNullOrBlank()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (state.isConfigurationRequired) {
          ConfigRequiredCard(
            message = state.errorMessage,
            onOpenKeyDialog = { showKeyDialog = true }
          )
        } else {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = state.errorMessage,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(14.dp)
            )
          }
        }

        // Quick fallback option: Generate high-resolution Unlimited AI visual
        if (state.prompt.isNotBlank()) {
          Button(
            onClick = {
              viewModel.onImagePromptChanged(state.prompt)
              viewModel.selectTab(com.example.ui.screens.CreativeStudioTab.CREATE_IMAGE)
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
              text = "دروستکردنی بە مۆدی وێنەی بێسنوور (بێ پێویستی بە کلیل)",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }

    // 9. Video Completed Result Card
    if (!state.generatedVideoUri.isNullOrBlank()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFF10B981))
            Text(
              text = "✓ ڤیدیۆکە بە سەرکەوتوویی دروستکرا",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Text(
            text = state.prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.generatedVideoUri))
                context.startActivity(intent)
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("کردنەوەی ڤیدیۆ")
            }

            OutlinedButton(
              onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, "Generated Video via Baso AI Veo:\n${state.generatedVideoUri}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text(strings.shareAction)
            }
          }
        }
      }
    }
  }
}
