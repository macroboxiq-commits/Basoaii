package com.example.ui.screens.creative

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeType
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CreativeStudioTab
import com.example.ui.screens.CreativeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreativeHistoryTab(
  viewModel: CreativeViewModel,
  projects: List<CreativeProject>,
  strings: AppStrings,
  onNavigateToTab: (CreativeStudioTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedFilter by remember { mutableStateOf<CreativeType?>(null) }
  var projectToRename by remember { mutableStateOf<CreativeProject?>(null) }
  var renameInputText by remember { mutableStateOf("") }

  val filteredProjects = if (selectedFilter == null) {
    projects
  } else {
    projects.filter { it.type == selectedFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(bottom = 24.dp)
  ) {
    // 1. Filter Chips Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedFilter == null,
        onClick = { selectedFilter = null },
        label = { Text("هەموو (${projects.size})") },
        shape = RoundedCornerShape(12.dp)
      )

      CreativeType.entries.forEach { type ->
        val count = projects.count { it.type == type }
        FilterChip(
          selected = selectedFilter == type,
          onClick = { selectedFilter = type },
          label = { Text("${type.titleKu} ($count)") },
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    // 2. Project List or Empty State
    if (filteredProjects.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
          )
          Text(
            text = strings.emptyHistoryTitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = strings.emptyHistorySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(Modifier.height(8.dp))
          Button(
            onClick = { onNavigateToTab(CreativeStudioTab.CREATE_IMAGE) },
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("دەستپێکردنی داهێنان")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredProjects, key = { it.id }) { project ->
          HistoryProjectCard(
            project = project,
            strings = strings,
            onSaveToGallery = {
              viewModel.saveProjectToGallery(project) { success ->
                val msg = if (success) strings.savedSuccessNotice else strings.savedFailNotice
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
              }
            },
            onShare = {
              if (project.mediaUri != null) {
                val shareIntent = viewModel.mediaStorageService.createShareIntent(
                  uri = Uri.parse(project.mediaUri),
                  mimeType = if (project.type == CreativeType.VIDEO) "video/mp4" else "image/png",
                  text = project.prompt
                )
                if (shareIntent != null) {
                  context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                }
              } else {
                val textIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, "${project.title}\n\n${project.prompt}\n\n${project.metadataJson ?: ""}")
                }
                context.startActivity(Intent.createChooser(textIntent, "Share Project"))
              }
            },
            onDelete = { viewModel.deleteProject(project) },
            onRename = {
              projectToRename = project
              renameInputText = project.title
            },
            onReusePrompt = {
              when (project.type) {
                CreativeType.IMAGE -> viewModel.transferPromptToImage(project.prompt)
                CreativeType.VIDEO -> viewModel.transferPromptToVideo(project.prompt)
                CreativeType.EDITED_IMAGE -> {
                  if (project.mediaUri != null) {
                    viewModel.useInImageEdit(Uri.parse(project.mediaUri))
                  } else {
                    viewModel.onEditInstructionChanged(project.prompt)
                    onNavigateToTab(CreativeStudioTab.EDIT_IMAGE)
                  }
                }
                CreativeType.SOCIAL_MEDIA -> {
                  viewModel.onSocialTopicChanged(project.prompt)
                  onNavigateToTab(CreativeStudioTab.SOCIAL_MEDIA)
                }
              }
            }
          )
        }
      }
    }

    // Rename Dialog
    projectToRename?.let { project ->
      AlertDialog(
        onDismissRequest = { projectToRename = null },
        title = { Text("گۆڕینی ناوی پرۆژە") },
        text = {
          OutlinedTextField(
            value = renameInputText,
            onValueChange = { renameInputText = it },
            label = { Text("ناوی نوێ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        },
        confirmButton = {
          Button(onClick = {
            viewModel.renameProject(project, renameInputText)
            projectToRename = null
          }) {
            Text("پاشەکەوتکردن")
          }
        },
        dismissButton = {
          TextButton(onClick = { projectToRename = null }) {
            Text(strings.cancelAction)
          }
        }
      )
    }
  }
}

@Composable
fun HistoryProjectCard(
  project: CreativeProject,
  strings: AppStrings,
  onSaveToGallery: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit,
  onRename: () -> Unit,
  onReusePrompt: () -> Unit
) {
  val context = LocalContext.current
  var menuExpanded by remember { mutableStateOf(false) }

  val formattedDate = remember(project.createdAt) {
    val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault())
    sdf.format(Date(project.createdAt))
  }

  val typeIcon = when (project.type) {
    CreativeType.IMAGE -> Icons.Default.Image
    CreativeType.VIDEO -> Icons.Default.Movie
    CreativeType.EDITED_IMAGE -> Icons.Default.Edit
    CreativeType.SOCIAL_MEDIA -> Icons.Default.AutoAwesome
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Media Thumbnail or Type Icon Box
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
      ) {
        if (!project.mediaUri.isNullOrBlank()) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(Uri.parse(project.mediaUri))
              .crossfade(true)
              .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
          if (project.type == CreativeType.VIDEO) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
          }
        } else {
          Icon(typeIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
      }

      // Information
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
          ) {
            Text(
              text = project.type.titleKu,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Text(
            text = formattedDate,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = project.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = project.prompt,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Actions Menu
      Box {
        IconButton(onClick = { menuExpanded = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "More")
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          DropdownMenuItem(
            text = { Text("دووبارە بەکارهێنانەوە") },
            onClick = {
              menuExpanded = false
              onReusePrompt()
            },
            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
          )

          if (project.mediaUri != null) {
            DropdownMenuItem(
              text = { Text(strings.saveToGalleryAction) },
              onClick = {
                menuExpanded = false
                onSaveToGallery()
              },
              leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) }
            )
          }

          DropdownMenuItem(
            text = { Text(strings.shareAction) },
            onClick = {
              menuExpanded = false
              onShare()
            },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
          )

          DropdownMenuItem(
            text = { Text("گۆڕینی ناو") },
            onClick = {
              menuExpanded = false
              onRename()
            },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) }
          )

          DropdownMenuItem(
            text = { Text("سڕینەوە", color = MaterialTheme.colorScheme.error) },
            onClick = {
              menuExpanded = false
              onDelete()
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
          )
        }
      }
    }
  }
}
