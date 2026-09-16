package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.CreativeApiClient
import com.example.ui.localization.AppStrings
import com.example.ui.theme.BasoBlue
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoDarkSurfaceVariant
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary

@Composable
fun EmptyChatView(
  strings: AppStrings,
  onSuggestionClicked: (String) -> Unit,
  onNavigateToCreativeStudio: ((com.example.ui.screens.CreativeStudioTab?) -> Unit)? = null,
  onActivateGeminiClicked: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val isKeyActive = CreativeApiClient.isKeyConfigured()

  Column(
    modifier = modifier
      .testTag("empty_chat_view")
      .fillMaxWidth()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Large glowing Baso AI Avatar
    BasoLogoAvatar(
      size = 86.dp,
      showGlow = true
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Greeting Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(
          Brush.verticalGradient(
            listOf(
              BasoDarkSurfaceHigh,
              BasoDarkSurfaceVariant
            )
          )
        )
        .border(BorderStroke(1.dp, BasoDarkBorder), RoundedCornerShape(22.dp))
        .padding(20.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // AI Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x2200E5FF))
            .border(BorderStroke(0.8.dp, BasoCyan.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = BasoCyan,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = strings.appName,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = BasoCyan
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prominent Greeting text
        Text(
          text = strings.greeting,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 28.sp
          ),
          color = BasoTextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = strings.greetingSubtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 19.sp
          ),
          color = BasoTextSecondary,
          textAlign = TextAlign.Center
        )
      }
    }

    // Gemini Activation Prompt Banner (when not configured)
    if (!isKeyActive && onActivateGeminiClicked != null) {
      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        onClick = onActivateGeminiClicked,
        shape = RoundedCornerShape(16.dp),
        color = Color(0x22F59E0B),
        border = BorderStroke(1.dp, Color(0x66F59E0B)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("empty_chat_gemini_activate_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33F59E0B)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = strings.geminiKeySectionTitle,
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                ),
                color = BasoTextPrimary
              )
              Text(
                text = strings.geminiKeySectionSubtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = BasoTextSecondary
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF59E0B)
          ) {
            Text(
              text = strings.geminiKeyActivateAction,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Black
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }
    }

    // Creative Studio Quick Access Card
    if (onNavigateToCreativeStudio != null) {
      Spacer(modifier = Modifier.height(18.dp))

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
          containerColor = BasoDarkSurfaceHigh.copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.35f)),
        shape = RoundedCornerShape(20.dp)
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
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFFA855F7).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.AutoAwesome,
                  contentDescription = null,
                  tint = Color(0xFFA855F7),
                  modifier = Modifier.size(16.dp)
                )
              }
              Text(
                text = strings.creativeStudioTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                ),
                color = BasoTextPrimary
              )
            }

            Surface(
              onClick = { onNavigateToCreativeStudio(null) },
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFA855F7).copy(alpha = 0.15f)
            ) {
              Text(
                text = "کردنەوە →",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFA855F7)
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          // 4 Options Grid: 2 rows of 2
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // 1. Create Image
            Surface(
              onClick = { onNavigateToCreativeStudio(com.example.ui.screens.CreativeStudioTab.CREATE_IMAGE) },
              shape = RoundedCornerShape(12.dp),
              color = BasoDarkSurfaceVariant,
              border = BorderStroke(1.dp, BasoDarkBorder),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(text = "🖼️", fontSize = 16.sp)
                Text(
                  text = strings.tabCreateImage,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = BasoTextPrimary,
                  maxLines = 1
                )
              }
            }

            // 2. Create Video
            Surface(
              onClick = { onNavigateToCreativeStudio(com.example.ui.screens.CreativeStudioTab.CREATE_VIDEO) },
              shape = RoundedCornerShape(12.dp),
              color = BasoDarkSurfaceVariant,
              border = BorderStroke(1.dp, BasoDarkBorder),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(text = "🎬", fontSize = 16.sp)
                Text(
                  text = strings.tabCreateVideo,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = BasoTextPrimary,
                  maxLines = 1
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // 3. Edit Image
            Surface(
              onClick = { onNavigateToCreativeStudio(com.example.ui.screens.CreativeStudioTab.EDIT_IMAGE) },
              shape = RoundedCornerShape(12.dp),
              color = BasoDarkSurfaceVariant,
              border = BorderStroke(1.dp, BasoDarkBorder),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(text = "✨", fontSize = 16.sp)
                Text(
                  text = strings.tabEditImage,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = BasoTextPrimary,
                  maxLines = 1
                )
              }
            }

            // 4. Social Media
            Surface(
              onClick = { onNavigateToCreativeStudio(com.example.ui.screens.CreativeStudioTab.SOCIAL_MEDIA) },
              shape = RoundedCornerShape(12.dp),
              color = BasoDarkSurfaceVariant,
              border = BorderStroke(1.dp, BasoDarkBorder),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(text = "📱", fontSize = 16.sp)
                Text(
                  text = strings.tabSocialMedia,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = BasoTextPrimary,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Suggestions Section Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.Lightbulb,
        contentDescription = null,
        tint = BasoCyan,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = strings.suggestionsHeader,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp
        ),
        color = BasoTextSecondary
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Suggestion chips
    strings.suggestions.forEach { suggestion ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clip(RoundedCornerShape(16.dp))
          .clickable { onSuggestionClicked(suggestion) },
        colors = CardDefaults.cardColors(
          containerColor = BasoDarkSurfaceHigh.copy(alpha = 0.8f)
        ),
        border = BorderStroke(1.dp, BasoDarkBorder),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(BasoCyan)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = suggestion,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            ),
            color = BasoTextPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
