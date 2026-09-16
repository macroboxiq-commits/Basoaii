package com.example

import com.example.data.local.CreativeProjectEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeStatus
import com.example.domain.model.CreativeType
import com.example.domain.model.ImageStyle
import com.example.domain.model.SocialFormat
import com.example.domain.model.SocialPlatform
import com.example.domain.model.VideoDuration
import com.example.domain.model.VideoStyle
import com.example.ui.localization.LocalizedStrings
import com.example.ui.screens.CreativeStudioTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreativeStudioTest {

  @Test
  fun testCreativeStudioTabs() {
    val tabs = CreativeStudioTab.entries
    assertEquals(5, tabs.size)
    assertTrue(tabs.contains(CreativeStudioTab.CREATE_IMAGE))
    assertTrue(tabs.contains(CreativeStudioTab.CREATE_VIDEO))
    assertTrue(tabs.contains(CreativeStudioTab.EDIT_IMAGE))
    assertTrue(tabs.contains(CreativeStudioTab.SOCIAL_MEDIA))
    assertTrue(tabs.contains(CreativeStudioTab.HISTORY))
  }

  @Test
  fun testAspectRatioOptions() {
    assertEquals("1:1", AspectRatioOption.SQUARE.ratio)
    assertEquals("16:9", AspectRatioOption.LANDSCAPE_16_9.ratio)
    assertEquals("9:16", AspectRatioOption.PORTRAIT_9_16.ratio)
    assertEquals("4:5", AspectRatioOption.PORTRAIT_4_5.ratio)
    assertEquals("4:3", AspectRatioOption.LANDSCAPE_4_3.ratio)
  }

  @Test
  fun testCreativeStatusCycle() {
    val statuses = CreativeStatus.entries
    assertTrue(statuses.contains(CreativeStatus.IDLE))
    assertTrue(statuses.contains(CreativeStatus.PREPARING))
    assertTrue(statuses.contains(CreativeStatus.GENERATING))
    assertTrue(statuses.contains(CreativeStatus.PROCESSING))
    assertTrue(statuses.contains(CreativeStatus.COMPLETED))
    assertTrue(statuses.contains(CreativeStatus.FAILED))
  }

  @Test
  fun testImageStylesAvailable() {
    val styles = ImageStyle.entries
    assertTrue(styles.contains(ImageStyle.REALISTIC))
    assertTrue(styles.contains(ImageStyle.CINEMATIC))
    assertTrue(styles.contains(ImageStyle.ANIME))
    assertTrue(styles.contains(ImageStyle.DIGITAL_ART))
    assertTrue(styles.contains(ImageStyle.RENDER_3D))
  }

  @Test
  fun testVideoStylesAndDurations() {
    val videoStyles = VideoStyle.entries
    assertTrue(videoStyles.contains(VideoStyle.CINEMATIC))
    assertTrue(videoStyles.contains(VideoStyle.DRONE_SHOT))
    assertTrue(videoStyles.contains(VideoStyle.SLOW_MOTION))

    assertEquals(5, VideoDuration.SHORT_5S.seconds)
    assertEquals(10, VideoDuration.MEDIUM_10S.seconds)
  }

  @Test
  fun testSocialPlatformsAndFormats() {
    val platforms = SocialPlatform.entries
    assertTrue(platforms.contains(SocialPlatform.INSTAGRAM))
    assertTrue(platforms.contains(SocialPlatform.TIKTOK))
    assertTrue(platforms.contains(SocialPlatform.YOUTUBE))
    assertTrue(platforms.contains(SocialPlatform.FACEBOOK))

    val formats = SocialFormat.entries
    assertTrue(formats.contains(SocialFormat.POST))
    assertTrue(formats.contains(SocialFormat.STORY))
    assertTrue(formats.contains(SocialFormat.REEL))
    assertTrue(formats.contains(SocialFormat.SHORT))
  }

  @Test
  fun testCreativeProjectEntityMapping() {
    val entity = CreativeProjectEntity(
      id = "proj-123",
      title = "Erbil Sunset",
      type = "IMAGE",
      prompt = "A sunset over Erbil Citadel",
      mediaUri = "file:///data/user/0/com.example/files/image.png",
      thumbnailUri = null,
      aspectRatio = "16:9",
      style = "CINEMATIC",
      metadataJson = "{}",
      status = "COMPLETED",
      createdAt = 1700000000000L,
      updatedAt = 1700000000000L
    )

    val domain = entity.toDomain()
    assertEquals("proj-123", domain.id)
    assertEquals("Erbil Sunset", domain.title)
    assertEquals(CreativeType.IMAGE, domain.type)
    assertEquals("A sunset over Erbil Citadel", domain.prompt)
    assertEquals("file:///data/user/0/com.example/files/image.png", domain.mediaUri)
    assertEquals("16:9", domain.aspectRatio)

    val backToEntity = CreativeProjectEntity.fromDomain(domain)
    assertEquals(entity.id, backToEntity.id)
    assertEquals(entity.title, backToEntity.title)
    assertEquals(entity.type, backToEntity.type)
  }

  @Test
  fun testCreativeStudioKurdishLocalization() {
    val kurdish = LocalizedStrings.get(AppLanguage.KURDISH_SORANI)
    assertEquals("ستۆدیۆی داهێنان", kurdish.creativeStudioTitle)
    assertEquals("دروستکردنی وێنە", kurdish.tabCreateImage)
    assertEquals("دروستکردنی ڤیدیۆ", kurdish.tabCreateVideo)
    assertEquals("دەستکاریکردنی وێنە", kurdish.tabEditImage)
    assertEquals("تۆڕە کۆمەڵایەتییەکان", kurdish.tabSocialMedia)
    assertEquals("مێژووی داهێنان", kurdish.tabCreativeHistory)
    assertNotNull(kurdish.imagePromptPlaceholder)
    assertNotNull(kurdish.videoPromptPlaceholder)
  }

  @Test
  fun testCreativeStudioArabicAndEnglishLocalization() {
    val arabic = LocalizedStrings.get(AppLanguage.ARABIC)
    assertEquals("استوديو الإبداع", arabic.creativeStudioTitle)
    assertEquals("إنشاء صورة", arabic.tabCreateImage)
    assertEquals("إنشاء فيديو", arabic.tabCreateVideo)

    val english = LocalizedStrings.get(AppLanguage.ENGLISH)
    assertEquals("Creative Studio", english.creativeStudioTitle)
    assertEquals("Create Image", english.tabCreateImage)
    assertEquals("Create Video", english.tabCreateVideo)
  }
}
