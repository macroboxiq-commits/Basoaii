package com.example.domain.service

import android.content.Intent
import android.net.Uri
import com.example.domain.model.AppLanguage
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.CreativeType
import com.example.domain.model.GenerationResult
import com.example.domain.model.ImageGenerationEngine
import com.example.domain.model.ImageStyle
import com.example.domain.model.SocialFormat
import com.example.domain.model.SocialGeneratedContent
import com.example.domain.model.SocialPlatform
import com.example.domain.model.VideoDuration
import com.example.domain.model.VideoStyle

data class GeneratedImageResult(
  val imageBytes: ByteArray,
  val mimeType: String,
  val revisedPrompt: String? = null
)

data class GeneratedVideoResult(
  val videoBytes: ByteArray? = null,
  val videoUri: String? = null,
  val operationName: String? = null,
  val isDone: Boolean = true,
  val statusMessage: String? = null
)

interface ImageGenerationService {
  suspend fun generateImage(
    prompt: String,
    style: ImageStyle = ImageStyle.NONE,
    aspectRatio: AspectRatioOption = AspectRatioOption.SQUARE,
    numberOfImages: Int = 1,
    engine: ImageGenerationEngine = ImageGenerationEngine.UNLIMITED_AI
  ): GenerationResult<GeneratedImageResult>
}

interface ImageEditingService {
  suspend fun editImage(
    originalImageBytes: ByteArray,
    mimeType: String,
    instruction: String
  ): GenerationResult<GeneratedImageResult>
}

interface VideoGenerationService {
  suspend fun createVideo(
    prompt: String,
    referenceImageBytes: ByteArray? = null,
    referenceMimeType: String? = null,
    style: VideoStyle = VideoStyle.CINEMATIC,
    aspectRatio: AspectRatioOption = AspectRatioOption.LANDSCAPE_16_9,
    duration: VideoDuration = VideoDuration.SHORT_5S
  ): GenerationResult<GeneratedVideoResult>

  suspend fun pollVideoOperation(
    operationName: String
  ): GenerationResult<GeneratedVideoResult>
}

interface CreativePromptService {
  suspend fun enhancePrompt(
    userPrompt: String,
    type: CreativeType,
    language: AppLanguage
  ): GenerationResult<String>

  suspend fun generateSocialContent(
    platform: SocialPlatform,
    format: SocialFormat,
    topic: String,
    language: AppLanguage
  ): GenerationResult<SocialGeneratedContent>
}

interface MediaStorageService {
  suspend fun saveToInternalStorage(
    bytes: ByteArray,
    filenamePrefix: String,
    extension: String
  ): Uri?

  suspend fun saveMediaToGallery(
    uri: Uri,
    title: String,
    isVideo: Boolean
  ): Boolean

  fun createShareIntent(
    uri: Uri,
    mimeType: String,
    text: String? = null
  ): Intent?

  suspend fun deleteMediaFile(uriString: String): Boolean
}
