package com.example.domain.model

enum class CreativeType(val titleEn: String, val titleKu: String) {
  IMAGE("Create Image", "دروستکردنی وێنە"),
  VIDEO("Create Video", "دروستکردنی ڤیدیۆ"),
  EDITED_IMAGE("Edit Image", "دەستکاریکردنی وێنە"),
  SOCIAL_MEDIA("Social Media", "تۆڕە کۆمەڵایەتییەکان")
}

enum class CreativeStatus(val labelKu: String, val labelEn: String) {
  IDLE("ئامادەیە", "Idle"),
  PREPARING("🧠 Baso ئامادەی دەکات...", "Preparing..."),
  GENERATING("🎨 وێنەکە دروست دەکرێت...", "Generating..."),
  PROCESSING("🎬 ڤیدیۆکە لە پرۆسەدایە...", "Processing..."),
  COMPLETED("✓ تەواو بوو", "Completed"),
  FAILED("هەڵەیەک ڕوویدا", "Failed")
}

enum class ImageStyle(val id: String, val labelKu: String, val labelEn: String, val promptModifier: String) {
  NONE("none", "ئاسایی", "Natural", ""),
  REALISTIC("realistic", "ڕیالیستیک", "Photorealistic", "ultra photorealistic, 8k resolution, highly detailed, realistic lighting, shot on 35mm lens"),
  CINEMATIC("cinematic", "سینەمایی", "Cinematic", "cinematic lighting, dramatic composition, film still, atmospheric, depth of field, anamorphic lens"),
  ANIME("anime", "ئەنیمێ", "Anime", "modern anime style, vibrant colors, makoto shinkai aesthetic, crisp line art"),
  DIGITAL_ART("digital_art", "هونەری دیجیتاڵی", "Digital Art", "concept art, digital illustration, trending on ArtStation, dynamic lighting"),
  RENDER_3D("render_3d", "سێ دووری", "3D Render", "octane render, 3D blender masterpiece, ray-traced reflections, volumetric lighting"),
  OIL_PAINTING("oil_painting", "تابلۆی ڕۆنی", "Oil Painting", "classical oil painting style, visible brush strokes, rich textured canvas, impressionistic"),
  WATERCOLOR("watercolor", "ئاوی", "Watercolor", "delicate watercolor wash, soft pigment bleeding, artistic hand-painted paper"),
  MINIMALIST("minimalist", "سادە", "Minimalist", "minimalist aesthetic, clean composition, elegant negative space, Bauhaus inspired")
}

enum class ImageGenerationEngine(
  val id: String,
  val labelKu: String,
  val labelAr: String,
  val labelEn: String,
  val isUnlimited: Boolean
) {
  UNLIMITED_AI(
    id = "unlimited_ai",
    labelKu = "بێسنوور (Unlimited AI)",
    labelAr = "الوضع اللامحدود (Unlimited AI)",
    labelEn = "Unlimited AI",
    isUnlimited = true
  ),
  GOOGLE_GEMINI(
    id = "google_gemini",
    labelKu = "گووگڵ جێمینای (Gemini Flash)",
    labelAr = "جوجل جيميني (Gemini Flash)",
    labelEn = "Google Gemini Flash",
    isUnlimited = false
  )
}

enum class AspectRatioOption(val ratio: String, val labelKu: String, val labelEn: String, val widthWeight: Float, val heightWeight: Float) {
  SQUARE("1:1", "چوارگۆشە (1:1)", "Square (1:1)", 1f, 1f),
  PORTRAIT_4_5("4:5", "پۆستی مۆبایل (4:5)", "Portrait (4:5)", 4f, 5f),
  PORTRAIT_9_16("9:16", "ستۆری و ڕیلز (9:16)", "Story/Reels (9:16)", 9f, 16f),
  LANDSCAPE_16_9("16:9", "سینەمایی و یوتیوب (16:9)", "Landscape (16:9)", 16f, 9f),
  LANDSCAPE_4_3("4:3", "کلاسیک (4:3)", "Standard (4:3)", 4f, 3f)
}

enum class VideoStyle(val id: String, val labelKu: String, val labelEn: String, val promptModifier: String) {
  CINEMATIC("cinematic", "سینەمایی", "Cinematic", "cinematic 4k motion, smooth camera panning, cinematic color grade"),
  DRONE_SHOT("drone", "دیمەنی فڕۆکە", "Drone Aerial", "scenic drone aerial sweeping shot, high altitude, 4k landscape cinematography"),
  SLOW_MOTION("slowmo", "جوڵەی هێواش", "Slow Motion", "crisp 120fps slow motion, ultra smooth fluid movement"),
  HYPERLAPSE("hyperlapse", "هایپەرلاپس", "Hyperlapse", "fast-paced seamless hyperlapse motion, dynamic city atmosphere"),
  ANIMATED("animated", "ئەنیمەیشن", "Animated", "fluid stylized 3D animation, expressive character movements")
}

enum class VideoDuration(val seconds: Int, val labelKu: String, val labelEn: String) {
  SHORT_5S(5, "٥ چرکە", "5 seconds"),
  MEDIUM_10S(10, "١٠ چرکە", "10 seconds")
}

enum class SocialPlatform(val id: String, val labelKu: String, val labelEn: String, val colorHex: Long) {
  INSTAGRAM("instagram", "ئینستاگرام", "Instagram", 0xFFE1306C),
  TIKTOK("tiktok", "تیکتۆک", "TikTok", 0xFF00F2FE),
  YOUTUBE("youtube", "یوتیوب", "YouTube", 0xFFFF0000),
  FACEBOOK("facebook", "فەیسبووک", "Facebook", 0xFF1877F2)
}

enum class SocialFormat(val id: String, val labelKu: String, val labelEn: String, val defaultRatio: AspectRatioOption) {
  POST("post", "پۆستی ئاسایی", "Feed Post", AspectRatioOption.SQUARE),
  STORY("story", "ستۆری", "Story", AspectRatioOption.PORTRAIT_9_16),
  REEL("reel", "ڕیلز / کورتەڤیدیۆ", "Reel / Short Video", AspectRatioOption.PORTRAIT_9_16),
  SHORT("short", "شۆرتس", "Short", AspectRatioOption.PORTRAIT_9_16),
  THUMBNAIL("thumbnail", "وێنەی کەڤەر (Thumbnail)", "Thumbnail", AspectRatioOption.LANDSCAPE_16_9)
}

data class SocialGeneratedContent(
  val visualConcept: String,
  val generationPrompt: String,
  val title: String,
  val caption: String,
  val hashtags: List<String>,
  val script: String? = null
)

data class CreativeProject(
  val id: String,
  val title: String,
  val type: CreativeType,
  val prompt: String,
  val enhancedPrompt: String? = null,
  val mediaUri: String? = null,
  val thumbnailUri: String? = null,
  val style: String? = null,
  val aspectRatio: String? = null,
  val socialPlatform: String? = null,
  val socialFormat: String? = null,
  val metadataJson: String? = null,
  val status: CreativeStatus = CreativeStatus.IDLE,
  val errorMessage: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

sealed class GenerationResult<out T> {
  data class Success<out T>(val data: T) : GenerationResult<T>()
  data class Error(
    val message: String,
    val technicalDetail: String? = null,
    val isConfigurationRequired: Boolean = false
  ) : GenerationResult<Nothing>()
}
