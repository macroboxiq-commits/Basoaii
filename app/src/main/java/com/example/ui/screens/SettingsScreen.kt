package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.SettingsState
import com.example.domain.model.SpeechRate
import com.example.domain.model.VoiceLanguage
import com.example.services.BasoVoiceBackgroundService
import com.example.ui.components.BasoLogoAvatar
import com.example.ui.components.GeminiKeyActivationCard
import com.example.ui.localization.AppStrings
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkBackground
import com.example.ui.theme.BasoDarkBorder
import com.example.ui.theme.BasoDarkSurface
import com.example.ui.theme.BasoDarkSurfaceHigh
import com.example.ui.theme.BasoDarkSurfaceVariant
import com.example.ui.theme.BasoTextPrimary
import com.example.ui.theme.BasoTextSecondary
import com.example.ui.theme.BasoTextTertiary

@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  strings: AppStrings,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settingsState.collectAsState()
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = BasoDarkBackground,
    topBar = {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BasoDarkSurface,
        tonalElevation = 6.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .testTag("settings_back_button")
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(BasoDarkSurfaceHigh)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = strings.back,
              tint = BasoTextPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Text(
            text = strings.settingsTitle,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            ),
            color = BasoTextPrimary
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(BasoDarkBackground),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 680.dp)
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {

        // 1. Language Section
        SettingsGroup(
          icon = Icons.Outlined.Language,
          title = strings.languageCategory,
          subtitle = strings.languageSubtitle
        ) {
          AppLanguage.entries.forEach { lang ->
            val isSelected = lang == settings.language
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.setLanguage(lang) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = lang.nativeName,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                  ),
                  color = if (isSelected) BasoCyan else BasoTextPrimary
                )
                Text(
                  text = lang.englishName,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = BasoTextTertiary
                )
              }

              RadioButton(
                selected = isSelected,
                onClick = { viewModel.setLanguage(lang) },
                colors = RadioButtonDefaults.colors(
                  selectedColor = BasoCyan,
                  unselectedColor = BasoTextTertiary
                )
              )
            }
          }
        }

        // 2. Gemini API Key & Direct Activation Section
        GeminiKeyActivationCard(
          settings = settings,
          viewModel = viewModel,
          strings = strings
        )

        // 3. Appearance Section
        SettingsGroup(
          icon = Icons.Outlined.ColorLens,
          title = strings.appearanceCategory,
          subtitle = strings.appearanceSubtitle
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.darkThemeLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = BasoTextPrimary
              )
              Text(
                text = "Default modern futuristic dark canvas",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = BasoTextTertiary
              )
            }

            Switch(
              checked = settings.isDarkTheme,
              onCheckedChange = { viewModel.setDarkTheme(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = BasoCyan,
                checkedTrackColor = BasoDarkSurfaceHigh
              )
            )
          }
        }

        // 3. Voice Section
        val voiceSettings = settings.voiceSettings
        SettingsGroup(
          icon = Icons.Outlined.Mic,
          title = strings.voiceCategory,
          subtitle = strings.voiceSubtitle
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Voice Input Switch
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.voiceInputOption,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                  color = BasoTextPrimary
                )
                Text(
                  text = "Speech-to-text recognition",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = BasoTextTertiary
                )
              }

              Switch(
                checked = voiceSettings.voiceInputEnabled,
                onCheckedChange = { viewModel.setVoiceInputEnabled(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = BasoCyan,
                  checkedTrackColor = BasoDarkSurfaceHigh
                )
              )
            }

            // Auto Send Switch
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.autoSendOption,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                  color = BasoTextPrimary
                )
                Text(
                  text = "Send immediately after recognition finishes",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = BasoTextTertiary
                )
              }

              Switch(
                checked = voiceSettings.autoSend,
                onCheckedChange = { viewModel.setAutoSend(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = BasoCyan,
                  checkedTrackColor = BasoDarkSurfaceHigh
                )
              )
            }

            // Speak AI Responses Switch
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.speakAiResponsesOption,
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                  color = BasoTextPrimary
                )
                Text(
                  text = "Read assistant responses aloud with TTS",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = BasoTextTertiary
                )
              }

              Switch(
                checked = voiceSettings.speakAiResponses,
                onCheckedChange = { viewModel.setSpeakAiResponses(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = BasoCyan,
                  checkedTrackColor = BasoDarkSurfaceHigh
                )
              )
            }

            // Speech Rate selector
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = strings.speechRateOption,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = BasoTextPrimary
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                SpeechRate.entries.forEach { rate ->
                  val isSelected = voiceSettings.speechRate == rate
                  val label = when (settings.language) {
                    AppLanguage.KURDISH_SORANI -> rate.labelKurdish
                    AppLanguage.ARABIC -> rate.labelArabic
                    AppLanguage.ENGLISH -> rate.labelEnglish
                  }
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(if (isSelected) BasoCyan else BasoDarkSurfaceHigh)
                      .clickable { viewModel.setSpeechRate(rate) }
                      .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                      ),
                      color = if (isSelected) BasoDarkBackground else BasoTextSecondary
                    )
                  }
                }
              }
            }

            // Voice Language Selector
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = strings.voiceLanguageOption,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = BasoTextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              VoiceLanguage.entries.forEach { vLang ->
                val isSelected = voiceSettings.voiceLanguage == vLang
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.setVoiceLanguage(vLang) }
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "${vLang.nativeName} (${vLang.englishName})",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) BasoCyan else BasoTextSecondary
                  )

                  RadioButton(
                    selected = isSelected,
                    onClick = { viewModel.setVoiceLanguage(vLang) },
                    colors = RadioButtonDefaults.colors(
                      selectedColor = BasoCyan,
                      unselectedColor = BasoTextTertiary
                    )
                  )
                }
              }
            }
          }
        }

        // 3b. Background Voice Assistant ("سڵاو باسۆ")
        val isBgServiceRunning by BasoVoiceBackgroundService.isServiceRunning.collectAsState()
        val isActivelyListening by BasoVoiceBackgroundService.isActivelyListening.collectAsState()
        val lastCommand by BasoVoiceBackgroundService.lastRecognizedCommand.collectAsState()
        val lastFeedback by BasoVoiceBackgroundService.lastFeedback.collectAsState()

        SettingsGroup(
          icon = Icons.Outlined.GraphicEq,
          title = strings.bgVoiceCategory,
          subtitle = strings.bgVoiceSubtitle
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Enable switch row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.bgVoiceEnableLabel,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = BasoTextPrimary
                )
                Text(
                  text = if (isBgServiceRunning) strings.bgVoiceActiveStatus else strings.bgVoiceInactiveStatus,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = if (isBgServiceRunning) BasoCyan else BasoTextTertiary
                )
              }

              Switch(
                checked = isBgServiceRunning,
                onCheckedChange = { enable ->
                  if (enable) {
                    BasoVoiceBackgroundService.startService(context)
                  } else {
                    BasoVoiceBackgroundService.stopService(context)
                  }
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = BasoCyan,
                  checkedTrackColor = BasoDarkSurfaceHigh
                ),
                modifier = Modifier.testTag("bg_voice_switch")
              )
            }

            // Real-time status info card
            if (isBgServiceRunning) {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = Color(0xFF0F2332)
                ),
                border = BorderStroke(1.dp, Color(0x3300E5FF)),
                shape = RoundedCornerShape(10.dp)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = if (isActivelyListening) "🎙️ ئێستا گوێ دەگرێت..." else "🟢 ئامادەیە بۆ گوێگرتن",
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                      ),
                      color = if (isActivelyListening) Color(0xFF38BDF8) else Color(0xFF34D399)
                    )
                    OutlinedButton(
                      onClick = {
                        BasoVoiceBackgroundService.triggerDirectListen(context)
                      },
                      modifier = Modifier
                        .height(32.dp)
                        .testTag("bg_voice_test_button"),
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                      border = BorderStroke(1.dp, BasoCyan)
                    ) {
                      Text(
                        text = strings.bgVoiceQuickTestAction,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = BasoCyan
                      )
                    }
                  }

                  if (lastCommand.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "دواین فەرمان: \"$lastCommand\"",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                      color = BasoTextSecondary
                    )
                  }
                  if (lastFeedback.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "وەڵامی باسۆ: $lastFeedback",
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                      color = BasoCyan
                    )
                  }
                }
              }
            }

            // Explanatory note
            Text(
              text = strings.bgVoiceExplanation,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
              color = BasoTextTertiary,
              modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Example voice commands
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "نموونەی فەرمانە دەنگییەکان کاتێک ئەپەکە داخراوە:",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = BasoTextSecondary
              )
              listOf(
                "«سڵاو باسۆ وایفای دابخە» (کردنەوەی پەنجەرەی خێرا 📶)",
                "«سڵاو باسۆ فلاش داگیرسێنە» (کوژاندنەوە و پێکردن 🔦)",
                "«سڵاو باسۆ کاتژمێر ٧ زەنگ دابنێ ⏰»",
                "«سڵاو باسۆ یوتیوب بکەرەوە 📱»"
              ).forEach { example ->
                Text(
                  text = "• $example",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = BasoCyan
                )
              }
            }
          }
        }

        // 4. AI Section
        SettingsGroup(
          icon = Icons.Outlined.AutoAwesome,
          title = strings.aiCategory,
          subtitle = strings.aiSubtitle
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Model Engine",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = BasoTextPrimary
              )
              Text(
                text = settings.aiModelName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = BasoCyan)
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x2200E5FF))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = strings.statusActive,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = BasoCyan
                )
              )
            }
          }
        }

        // 5. Memory Section
        SettingsGroup(
          icon = Icons.Outlined.Psychology,
          title = strings.memoryCategory,
          subtitle = strings.memorySubtitle
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.memoryEnabledLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = BasoTextPrimary
              )
              Text(
                text = strings.statusPlaceholder,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = BasoTextTertiary
              )
            }

            Switch(
              checked = settings.memoryEnabled,
              onCheckedChange = { viewModel.setMemoryEnabled(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = BasoCyan,
                checkedTrackColor = BasoDarkSurfaceHigh
              )
            )
          }
        }

        // 6. Permissions Section
        SettingsGroup(
          icon = Icons.Outlined.Security,
          title = strings.permissionsCategory,
          subtitle = strings.permissionsSubtitle
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = strings.micPermissionLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = BasoTextSecondary
              )
              Text(
                text = strings.statusPlaceholder,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = BasoTextTertiary
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = strings.storagePermissionLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = BasoTextSecondary
              )
              Text(
                text = strings.statusPlaceholder,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = BasoTextTertiary
              )
            }
          }
        }

        // 7. About Section
        SettingsGroup(
          icon = Icons.Outlined.Info,
          title = strings.aboutCategory,
          subtitle = strings.aboutSubtitle
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            BasoLogoAvatar(size = 56.dp, showGlow = true)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = strings.appName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              ),
              color = BasoTextPrimary
            )
            Text(
              text = strings.tagline,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = BasoTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = strings.versionLabel,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = BasoCyan
            )
            Text(
              text = strings.developerLabel,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = BasoTextTertiary
            )
          }
        }

        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
private fun SettingsGroup(
  icon: ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = BasoDarkSurface
    ),
    border = BorderStroke(1.dp, BasoDarkBorder),
    shape = RoundedCornerShape(18.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BasoDarkSurfaceHigh),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BasoCyan,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            ),
            color = BasoTextPrimary
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = BasoTextSecondary
          )
        }
      }

      content()
    }
  }
}
