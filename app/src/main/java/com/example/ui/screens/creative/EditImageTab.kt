package com.example.ui.screens.creative

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeStatus
import com.example.domain.model.CreativeType
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CreativeViewModel
import com.example.ui.screens.ImageEditingState

@Composable
fun EditImageTab(
  viewModel: CreativeViewModel,
  state: ImageEditingState,
  strings: AppStrings,
  currentLanguage: AppLanguage,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.setEditSourceImage(context, uri)
    }
  }

  val editSuggestions = listOf(
    "باکگراوەند بگۆڕە بۆ مۆدێرن و سینەمایی",
    "ڕووناکی و وردەکارییەکان زیاد بکە",
    "ئەم وێنەیە بکە بە تابلۆی ڕۆنی کلاسیک",
    "فلتەرێکی ڕەنگی سایبەرفانک و نەئۆن دابنێ",
    "شێوازی ئەنیمێ بۆ کەسایەتییەکە دابنێ"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Source Image Picker & Preview
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
              text = "وێنەی سەرەکی بۆ دەستکاریکردن",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          if (state.sourceUri != null) {
            OutlinedButton(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("گۆڕینی وێنە")
            }
          }
        }

        if (state.sourceUri != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(state.sourceUri)
                .crossfade(true)
                .build(),
              contentDescription = "Source Image",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit
            )
          }
        } else {
          Surface(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
              Spacer(Modifier.height(8.dp))
              Text(
                text = "کلیک بکە بۆ هەڵبژاردنی وێنە لە گەلەری",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }

    // 2. Editing Instruction Input
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            text = "ڕێنمایی دەستکاریکردن (AI Instruction)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )

          if (state.instruction.isNotBlank()) {
            IconButton(
              onClick = { viewModel.onEditInstructionChanged("") },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        }

        OutlinedTextField(
          value = state.instruction,
          onValueChange = { viewModel.onEditInstructionChanged(it) },
          placeholder = {
            Text(
              text = strings.editInstructionPlaceholder,
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

        // Suggestions
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Text("نموونە:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
              }
            }
          }

          items(editSuggestions) { suggestion ->
            FilterChip(
              selected = false,
              onClick = { viewModel.onEditInstructionChanged(suggestion) },
              label = { Text(suggestion, maxLines = 1) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }
    }

    // 3. Edit Action Button
    val isBusy = state.status == CreativeStatus.PREPARING || state.status == CreativeStatus.PROCESSING
    Button(
      onClick = { viewModel.generateImageEdit() },
      enabled = !isBusy && state.sourceBytes != null && state.instruction.isNotBlank(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(54.dp),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = if (isBusy) state.statusMessage ?: "دەستکاریکردن..." else "دەستکاریکردنی وێنە (Edit Image)",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }

    // 4. Progress Card
    if (isBusy) {
      StatusProgressCard(
        status = state.status,
        statusMessage = state.statusMessage,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }

    // 5. Error & Config Messages
    if (!state.errorMessage.isNullOrBlank()) {
      if (state.isConfigurationRequired) {
        ConfigRequiredCard(
          message = state.errorMessage,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      } else {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
    }

    // 6. Edited Result Image Card
    if (state.editedUri != null) {
      GeneratedImagePreviewCard(
        imageUri = state.editedUri,
        revisedPrompt = "دەستکاریکراو بە پێی: ${state.instruction}",
        strings = strings,
        onSaveToGallery = {
          viewModel.saveProjectToGallery(
            project = CreativeProject(
              id = state.activeProjectId ?: "",
              title = "دەستکاری: ${state.instruction.take(25)}",
              type = CreativeType.EDITED_IMAGE,
              prompt = state.instruction,
              mediaUri = state.editedUri.toString()
            )
          ) { success ->
            val msg = if (success) strings.savedSuccessNotice else strings.savedFailNotice
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
          }
        },
        onShare = {
          val shareIntent = viewModel.mediaStorageService.createShareIntent(
            uri = state.editedUri,
            mimeType = "image/png",
            text = state.instruction
          )
          if (shareIntent != null) {
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Edited Image"))
          }
        },
        onRegenerate = { viewModel.generateImageEdit() },
        onEditImage = { viewModel.useInImageEdit(state.editedUri) },
        onUseAsVideoRef = { viewModel.useAsReferenceInVideo(state.editedUri) }
      )
    }
  }
}
