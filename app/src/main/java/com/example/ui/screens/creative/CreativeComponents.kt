package com.example.ui.screens.creative

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.AppLanguage
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.CreativeStatus
import com.example.domain.model.ImageGenerationEngine
import com.example.domain.model.ImageStyle
import com.example.ui.localization.AppStrings
import com.example.ui.screens.CreativeStudioTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeTopBar(
  strings: AppStrings,
  isKeyConfigured: Boolean,
  onNavigateBack: () -> Unit,
  onOpenKeyDialog: (() -> Unit)? = null
) {
  TopAppBar(
    title = {
      Column {
        Text(
          text = strings.creativeStudioTitle,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          ),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = strings.creativeStudioTagline,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    },
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = strings.back,
          tint = MaterialTheme.colorScheme.onBackground
        )
      }
    },
    actions = {
      // API Key Status Badge & Setup Button
      Surface(
        onClick = { onOpenKeyDialog?.invoke() },
        shape = RoundedCornerShape(20.dp),
        color = if (isKeyConfigured) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isKeyConfigured) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
        ),
        modifier = Modifier.padding(end = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = if (isKeyConfigured) Icons.Default.Key else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isKeyConfigured) Color(0xFF10B981) else Color(0xFFF59E0B),
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = if (isKeyConfigured) "کلیل دانراوە ✓" else "دانانی کلیل 🔑",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isKeyConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background
    )
  )
}

@Composable
fun CreativeTabBar(
  currentTab: CreativeStudioTab,
  strings: AppStrings,
  onTabSelected: (CreativeStudioTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val tabs = listOf(
    Triple(CreativeStudioTab.CREATE_IMAGE, strings.tabCreateImage, Icons.Default.Image),
    Triple(CreativeStudioTab.CREATE_VIDEO, strings.tabCreateVideo, Icons.Default.Movie),
    Triple(CreativeStudioTab.EDIT_IMAGE, strings.tabEditImage, Icons.Default.Edit),
    Triple(CreativeStudioTab.SOCIAL_MEDIA, strings.tabSocialMedia, Icons.Default.AutoAwesome),
    Triple(CreativeStudioTab.HISTORY, strings.tabCreativeHistory, Icons.Default.History)
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    tabs.forEach { (tab, label, icon) ->
      val isSelected = currentTab == tab
      val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "tabBg"
      )
      val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

      Surface(
        onClick = { onTabSelected(tab) },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = if (!isSelected) {
          androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        } else null,
        modifier = Modifier.height(42.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
          )
          Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor
          )
        }
      }
    }
  }
}

@Composable
fun StatusProgressCard(
  status: CreativeStatus,
  statusMessage: String?,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      Brush.horizontalGradient(
        listOf(
          MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
          MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(68.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(46.dp),
          color = MaterialTheme.colorScheme.primary,
          strokeWidth = 3.dp
        )
      }

      Text(
        text = statusMessage ?: status.labelKu,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )

      Text(
        text = "داواکارییەکەت لە ڕێگەی مۆدێلی فەرمی Google AI پرۆسێس دەکرێت...",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun ConfigRequiredCard(
  message: String?,
  modifier: Modifier = Modifier,
  onOpenKeyDialog: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFFF59E0B).copy(alpha = 0.12f)
    ),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = null,
          tint = Color(0xFFF59E0B),
          modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "پێویستی بە ڕێکخستنی کلیل هەیە (API Setup Required)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFF59E0B)
          )
          Text(
            text = message ?: "تکایە کلیلی مۆڵەتپێدراوی Google AI Studio لە بەشی نهێنییەکان (Secrets panel) یان ڕاستەوخۆ لێرە دابنێ.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      if (onOpenKeyDialog != null) {
        Button(
          onClick = onOpenKeyDialog,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF59E0B)
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
          Spacer(Modifier.width(8.dp))
          Text(
            text = "لێرە کلیلەکەت بنووسە یان دابنێ (Enter API Key)",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
          )
        }
      }
    }
  }
}

@Composable
fun ApiKeyInputDialog(
  currentKey: String,
  onDismiss: () -> Unit,
  onSaveKey: (String) -> Unit
) {
  var keyText by remember { mutableStateOf(currentKey) }
  var isVisible by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Key,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp)
      )
    },
    title = {
      Text(
        text = "دانانی کلیلی Google AI (API Key)",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "دەتوانیت کلیلی Gemini / Google AI لێرە دابنێیت (Paste) بۆ بەکارهێنانی مۆدێلە پێشکەوتووەکانی وەک Google Veo یان مۆدێلی تر:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = keyText,
          onValueChange = { keyText = it },
          label = { Text("GEMINI_API_KEY") },
          placeholder = { Text("AIzaSy...") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          trailingIcon = {
            IconButton(onClick = { isVisible = !isVisible }) {
              Icon(
                imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (isVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation()
        )

        if (keyText.isNotBlank()) {
          TextButton(
            onClick = {
              keyText = ""
              onSaveKey("")
              onDismiss()
            },
            modifier = Modifier.align(Alignment.End)
          ) {
            Text("سڕینەوەی کلیل", color = MaterialTheme.colorScheme.error)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSaveKey(keyText.trim())
          onDismiss()
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("پاشەکەوتکردن (Save)")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("داخستن")
      }
    }
  )
}

@Composable
fun EngineSelectorCard(
  selectedEngine: ImageGenerationEngine,
  strings: AppStrings,
  currentLanguage: AppLanguage,
  onEngineSelected: (ImageGenerationEngine) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (selectedEngine == ImageGenerationEngine.UNLIMITED_AI)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
      else
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
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
            Icons.Default.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = strings.engineLabel,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (selectedEngine.isUnlimited) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
          ) {
            Text(
              text = strings.unlimitedModeBadge,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ImageGenerationEngine.entries.forEach { engine ->
          val isSelected = engine == selectedEngine
          val label = when (currentLanguage) {
            AppLanguage.KURDISH_SORANI -> engine.labelKu
            AppLanguage.ARABIC -> engine.labelAr
            AppLanguage.ENGLISH -> engine.labelEn
          }

          FilterChip(
            selected = isSelected,
            onClick = { onEngineSelected(engine) },
            leadingIcon = {
              if (engine.isUnlimited) {
                Icon(
                  Icons.Default.Bolt,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              } else {
                Icon(
                  Icons.Default.Stars,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            },
            label = {
              Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              )
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selectedEngine.isUnlimited)
          MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = if (selectedEngine.isUnlimited)
            "✨ " + strings.unlimitedModeNotice + " • بێ سنووردارکردنی کوۆتا و خێرا"
          else
            "💡 " + strings.engineGeminiSubtitle + " • لە کاتی پڕبوونی ڕێژەدا خۆکارانە دەگۆڕێت بۆ مۆدی بێسنوور",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
      }
    }
  }
}

@Composable
fun StyleSelectorRow(
  selectedStyle: ImageStyle,
  strings: AppStrings,
  onStyleSelected: (ImageStyle) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = strings.styleLabel,
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
      ImageStyle.entries.forEach { style ->
        FilterChip(
          selected = selectedStyle == style,
          onClick = { onStyleSelected(style) },
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
}

@Composable
fun AspectRatioSelectorRow(
  selectedRatio: AspectRatioOption,
  strings: AppStrings,
  onRatioSelected: (AspectRatioOption) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
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
      AspectRatioOption.entries.forEach { ratio ->
        FilterChip(
          selected = selectedRatio == ratio,
          onClick = { onRatioSelected(ratio) },
          label = { Text(ratio.labelKu) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          shape = RoundedCornerShape(12.dp)
        )
      }
    }
  }
}

@Composable
fun GeneratedImagePreviewCard(
  imageUri: Uri,
  revisedPrompt: String?,
  strings: AppStrings,
  onSaveToGallery: () -> Unit,
  onShare: () -> Unit,
  onRegenerate: () -> Unit,
  onEditImage: () -> Unit,
  onUseAsVideoRef: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Image Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
          .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
          .background(Color.Black),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build(),
          contentDescription = "Generated Image",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit
        )
      }

      // Details and Actions
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (!revisedPrompt.isNullOrBlank()) {
          Text(
            text = revisedPrompt,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Primary Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onSaveToGallery,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.saveToGalleryAction)
          }

          OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.shareAction)
          }
        }

        // Secondary Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onRegenerate,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(strings.regenerateAction)
          }

          OutlinedButton(
            onClick = onEditImage,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(strings.editAction)
          }

          OutlinedButton(
            onClick = onUseAsVideoRef,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("ڤیدیۆ")
          }
        }
      }
    }
  }
}

fun copyToClipboard(context: Context, label: String, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText(label, text)
  clipboard.setPrimaryClip(clip)
  Toast.makeText(context, "لە کلیپبۆرد کۆپیکرا ✓", Toast.LENGTH_SHORT).show()
}
