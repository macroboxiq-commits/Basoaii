package com.example.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CreativeDatabase
import com.example.data.repository.CreativeRepositoryImpl
import com.example.domain.model.AppLanguage
import com.example.domain.model.AspectRatioOption
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeStatus
import com.example.domain.model.CreativeType
import com.example.domain.model.GenerationResult
import com.example.domain.model.ImageGenerationEngine
import com.example.domain.model.ImageStyle
import com.example.domain.model.SocialFormat
import com.example.domain.model.SocialGeneratedContent
import com.example.domain.model.SocialPlatform
import com.example.domain.model.VideoDuration
import com.example.domain.model.VideoStyle
import com.example.domain.repository.CreativeRepository
import com.example.domain.service.CreativePromptService
import com.example.domain.service.ImageEditingService
import com.example.domain.service.ImageGenerationService
import com.example.domain.service.MediaStorageService
import com.example.domain.service.VideoGenerationService
import com.example.services.CreativeApiClient
import com.example.services.GoogleCreativePromptService
import com.example.services.GoogleImageEditingServiceImpl
import com.example.services.GoogleImageGenerationServiceImpl
import com.example.services.GoogleVideoGenerationServiceImpl
import com.example.services.MediaStorageServiceImpl
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CreativeStudioTab {
  CREATE_IMAGE,
  CREATE_VIDEO,
  EDIT_IMAGE,
  SOCIAL_MEDIA,
  HISTORY
}

data class ImageCreationState(
  val prompt: String = "",
  val selectedEngine: ImageGenerationEngine = ImageGenerationEngine.UNLIMITED_AI,
  val selectedStyle: ImageStyle = ImageStyle.NONE,
  val selectedAspectRatio: AspectRatioOption = AspectRatioOption.SQUARE,
  val numberOfImages: Int = 1,
  val isEnhancePromptEnabled: Boolean = true,
  val enhancedPrompt: String? = null,
  val status: CreativeStatus = CreativeStatus.IDLE,
  val generatedUri: Uri? = null,
  val statusMessage: String? = null,
  val errorMessage: String? = null,
  val isConfigurationRequired: Boolean = false,
  val activeProjectId: String? = null
)

data class ImageEditingState(
  val sourceUri: Uri? = null,
  val sourceBytes: ByteArray? = null,
  val instruction: String = "",
  val status: CreativeStatus = CreativeStatus.IDLE,
  val editedUri: Uri? = null,
  val statusMessage: String? = null,
  val errorMessage: String? = null,
  val isConfigurationRequired: Boolean = false,
  val activeProjectId: String? = null
)

data class VideoCreationState(
  val prompt: String = "",
  val referenceImageUri: Uri? = null,
  val referenceImageBytes: ByteArray? = null,
  val selectedStyle: VideoStyle = VideoStyle.CINEMATIC,
  val selectedAspectRatio: AspectRatioOption = AspectRatioOption.LANDSCAPE_16_9,
  val selectedDuration: VideoDuration = VideoDuration.SHORT_5S,
  val status: CreativeStatus = CreativeStatus.IDLE,
  val operationName: String? = null,
  val generatedVideoUri: String? = null,
  val statusMessage: String? = null,
  val errorMessage: String? = null,
  val isConfigurationRequired: Boolean = false,
  val activeProjectId: String? = null
)

data class SocialCreatorState(
  val platform: SocialPlatform = SocialPlatform.INSTAGRAM,
  val format: SocialFormat = SocialFormat.POST,
  val topic: String = "",
  val isGenerating: Boolean = false,
  val generatedContent: SocialGeneratedContent? = null,
  val errorMessage: String? = null,
  val isConfigurationRequired: Boolean = false
)

class CreativeViewModel(
  application: Application,
  private val repository: CreativeRepository,
  private val imageGenerationService: ImageGenerationService,
  private val imageEditingService: ImageEditingService,
  private val videoGenerationService: VideoGenerationService,
  private val promptService: CreativePromptService,
  val mediaStorageService: MediaStorageService
) : AndroidViewModel(application) {

  constructor(application: Application) : this(
    application = application,
    repository = CreativeRepositoryImpl(
      CreativeDatabase.getDatabase(application).creativeProjectDao()
    ),
    imageGenerationService = GoogleImageGenerationServiceImpl(),
    imageEditingService = GoogleImageEditingServiceImpl(),
    videoGenerationService = GoogleVideoGenerationServiceImpl(),
    promptService = GoogleCreativePromptService(),
    mediaStorageService = MediaStorageServiceImpl(application)
  )

  init {
    CreativeApiClient.initialize(application)
  }

  private val tag = "CreativeViewModel"

  private val _currentTab = MutableStateFlow(CreativeStudioTab.CREATE_IMAGE)
  val currentTab: StateFlow<CreativeStudioTab> = _currentTab.asStateFlow()

  val projects: StateFlow<List<CreativeProject>> = repository.getAllProjects()
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  private val _isApiKeyConfiguredState = MutableStateFlow(CreativeApiClient.isKeyConfigured())
  val isApiKeyConfiguredFlow: StateFlow<Boolean> = _isApiKeyConfiguredState.asStateFlow()

  val isApiKeyConfigured: Boolean
    get() = _isApiKeyConfiguredState.value

  fun updateCustomApiKey(key: String) {
    CreativeApiClient.setCustomApiKey(getApplication(), key)
    _isApiKeyConfiguredState.value = CreativeApiClient.isKeyConfigured()
    // Clear configuration warning from video or social states if now configured
    if (_isApiKeyConfiguredState.value) {
      if (_videoState.value.isConfigurationRequired) {
        _videoState.value = _videoState.value.copy(errorMessage = null, isConfigurationRequired = false)
      }
      if (_socialState.value.isConfigurationRequired) {
        _socialState.value = _socialState.value.copy(errorMessage = null, isConfigurationRequired = false)
      }
    }
  }

  private val _imageState = MutableStateFlow(ImageCreationState())
  val imageState: StateFlow<ImageCreationState> = _imageState.asStateFlow()

  private val _editState = MutableStateFlow(ImageEditingState())
  val editState: StateFlow<ImageEditingState> = _editState.asStateFlow()

  private val _videoState = MutableStateFlow(VideoCreationState())
  val videoState: StateFlow<VideoCreationState> = _videoState.asStateFlow()

  private val _socialState = MutableStateFlow(SocialCreatorState())
  val socialState: StateFlow<SocialCreatorState> = _socialState.asStateFlow()

  private var videoPollingJob: Job? = null

  fun setTab(tab: CreativeStudioTab) {
    _currentTab.value = tab
  }

  fun selectTab(tab: CreativeStudioTab) {
    _currentTab.value = tab
  }

  // ==========================================
  // 1. Image Creation
  // ==========================================

  fun onImagePromptChanged(prompt: String) {
    _imageState.value = _imageState.value.copy(prompt = prompt, errorMessage = null)
  }

  fun onImageEngineSelected(engine: ImageGenerationEngine) {
    _imageState.value = _imageState.value.copy(selectedEngine = engine, errorMessage = null)
  }

  fun onImageStyleSelected(style: ImageStyle) {
    _imageState.value = _imageState.value.copy(selectedStyle = style)
  }

  fun onImageAspectRatioSelected(ratio: AspectRatioOption) {
    _imageState.value = _imageState.value.copy(selectedAspectRatio = ratio)
  }

  fun onImageCountChanged(count: Int) {
    _imageState.value = _imageState.value.copy(numberOfImages = count.coerceIn(1, 4))
  }

  fun togglePromptEnhancer(enabled: Boolean) {
    _imageState.value = _imageState.value.copy(isEnhancePromptEnabled = enabled)
  }

  fun generateImage(language: AppLanguage) {
    val state = _imageState.value
    val basePrompt = state.prompt.trim()
    if (basePrompt.isBlank()) {
      _imageState.value = state.copy(
        errorMessage = "تکایە سەرەتا داواکارییەک بنووسە بۆ وێنەکە."
      )
      return
    }

    viewModelScope.launch {
      val projectId = UUID.randomUUID().toString()
      _imageState.value = state.copy(
        status = CreativeStatus.PREPARING,
        statusMessage = "🧠 Baso ئامادەی دەکات...",
        errorMessage = null,
        isConfigurationRequired = false,
        activeProjectId = projectId
      )

      var finalPromptToGenerate = basePrompt
      var enhancedPromptResult: String? = null

      if (state.isEnhancePromptEnabled) {
        val enhanceResult = promptService.enhancePrompt(basePrompt, CreativeType.IMAGE, language)
        if (enhanceResult is GenerationResult.Success) {
          enhancedPromptResult = enhanceResult.data
          finalPromptToGenerate = enhanceResult.data
          _imageState.value = _imageState.value.copy(
            enhancedPrompt = enhanceResult.data
          )
        }
      }

      _imageState.value = _imageState.value.copy(
        status = CreativeStatus.GENERATING,
        statusMessage = "🎨 وێنەکە دروست دەکرێت..."
      )

      val genResult = imageGenerationService.generateImage(
        prompt = finalPromptToGenerate,
        style = state.selectedStyle,
        aspectRatio = state.selectedAspectRatio,
        numberOfImages = state.numberOfImages,
        engine = state.selectedEngine
      )

      when (genResult) {
        is GenerationResult.Success -> {
          val savedUri = mediaStorageService.saveToInternalStorage(
            bytes = genResult.data.imageBytes,
            filenamePrefix = "baso_img",
            extension = if (genResult.data.mimeType.contains("jpeg")) "jpg" else "png"
          )

          val project = CreativeProject(
            id = projectId,
            title = basePrompt.take(30),
            type = CreativeType.IMAGE,
            prompt = basePrompt,
            enhancedPrompt = enhancedPromptResult,
            mediaUri = savedUri?.toString(),
            thumbnailUri = savedUri?.toString(),
            style = state.selectedStyle.id,
            aspectRatio = state.selectedAspectRatio.ratio,
            status = CreativeStatus.COMPLETED
          )
          repository.saveProject(project)

          _imageState.value = _imageState.value.copy(
            status = CreativeStatus.COMPLETED,
            statusMessage = "✓ تەواو بوو",
            generatedUri = savedUri,
            activeProjectId = projectId
          )
        }

        is GenerationResult.Error -> {
          _imageState.value = _imageState.value.copy(
            status = CreativeStatus.FAILED,
            errorMessage = genResult.message,
            isConfigurationRequired = genResult.isConfigurationRequired
          )
        }
      }
    }
  }

  // ==========================================
  // 2. Image Editing
  // ==========================================

  fun onEditInstructionChanged(text: String) {
    _editState.value = _editState.value.copy(instruction = text, errorMessage = null)
  }

  fun setEditSourceImage(context: Context, uri: Uri) {
    viewModelScope.launch {
      try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        _editState.value = _editState.value.copy(
          sourceUri = uri,
          sourceBytes = bytes,
          errorMessage = null
        )
      } catch (e: Exception) {
        Log.e(tag, "Failed to read image bytes", e)
        _editState.value = _editState.value.copy(
          errorMessage = "نەتوانرا وێنەکە بخوێنرێتەوە: ${e.localizedMessage}"
        )
      }
    }
  }

  fun generateImageEdit() {
    val state = _editState.value
    val bytes = state.sourceBytes
    val instruction = state.instruction.trim()

    if (bytes == null || bytes.isEmpty()) {
      _editState.value = state.copy(errorMessage = "تکایە سەرەتا وێنەیەک هەڵبژێرە بۆ دەستکاریکردن.")
      return
    }

    if (instruction.isBlank()) {
      _editState.value = state.copy(errorMessage = "تکایە ڕێنمایی دەستکاریکردن بنووسە.")
      return
    }

    viewModelScope.launch {
      val projectId = UUID.randomUUID().toString()
      _editState.value = state.copy(
        status = CreativeStatus.PROCESSING,
        statusMessage = "🎨 وێنەکە بە پێی ڕێنماییەکان دەستکاری دەکرێت...",
        errorMessage = null,
        isConfigurationRequired = false,
        activeProjectId = projectId
      )

      val editResult = imageEditingService.editImage(
        originalImageBytes = bytes,
        mimeType = "image/jpeg",
        instruction = instruction
      )

      when (editResult) {
        is GenerationResult.Success -> {
          val savedUri = mediaStorageService.saveToInternalStorage(
            bytes = editResult.data.imageBytes,
            filenamePrefix = "baso_edit",
            extension = "png"
          )

          val project = CreativeProject(
            id = projectId,
            title = "دەستکاری: ${instruction.take(25)}",
            type = CreativeType.EDITED_IMAGE,
            prompt = instruction,
            mediaUri = savedUri?.toString(),
            thumbnailUri = savedUri?.toString(),
            status = CreativeStatus.COMPLETED
          )
          repository.saveProject(project)

          _editState.value = _editState.value.copy(
            status = CreativeStatus.COMPLETED,
            statusMessage = "✓ تەواو بوو",
            editedUri = savedUri,
            activeProjectId = projectId
          )
        }

        is GenerationResult.Error -> {
          _editState.value = _editState.value.copy(
            status = CreativeStatus.FAILED,
            errorMessage = editResult.message,
            isConfigurationRequired = editResult.isConfigurationRequired
          )
        }
      }
    }
  }

  // ==========================================
  // 3. Video Creation (Text-to-Video & Image-to-Video)
  // ==========================================

  fun onVideoPromptChanged(prompt: String) {
    _videoState.value = _videoState.value.copy(prompt = prompt, errorMessage = null)
  }

  fun onVideoStyleSelected(style: VideoStyle) {
    _videoState.value = _videoState.value.copy(selectedStyle = style)
  }

  fun onVideoAspectRatioSelected(ratio: AspectRatioOption) {
    _videoState.value = _videoState.value.copy(selectedAspectRatio = ratio)
  }

  fun onVideoDurationSelected(duration: VideoDuration) {
    _videoState.value = _videoState.value.copy(selectedDuration = duration)
  }

  fun setVideoReferenceImage(context: Context, uri: Uri?) {
    if (uri == null) {
      _videoState.value = _videoState.value.copy(referenceImageUri = null, referenceImageBytes = null)
      return
    }
    viewModelScope.launch {
      try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        _videoState.value = _videoState.value.copy(
          referenceImageUri = uri,
          referenceImageBytes = bytes,
          errorMessage = null
        )
      } catch (e: Exception) {
        Log.e(tag, "Failed to load video reference image", e)
      }
    }
  }

  fun generateVideo(language: AppLanguage) {
    val state = _videoState.value
    val prompt = state.prompt.trim()
    if (prompt.isBlank() && state.referenceImageBytes == null) {
      _videoState.value = state.copy(errorMessage = "تکایە وەسفێک بۆ ڤیدیۆکە بنووسە یان وێنەیەک هەڵبژێرە.")
      return
    }

    videoPollingJob?.cancel()

    viewModelScope.launch {
      val projectId = UUID.randomUUID().toString()
      _videoState.value = state.copy(
        status = CreativeStatus.PREPARING,
        statusMessage = "🧠 Baso ئامادەی دەکات...",
        errorMessage = null,
        isConfigurationRequired = false,
        activeProjectId = projectId
      )

      val result = videoGenerationService.createVideo(
        prompt = prompt,
        referenceImageBytes = state.referenceImageBytes,
        referenceMimeType = "image/jpeg",
        style = state.selectedStyle,
        aspectRatio = state.selectedAspectRatio,
        duration = state.selectedDuration
      )

      when (result) {
        is GenerationResult.Success -> {
          val opName = result.data.operationName
          if (opName != null && !result.data.isDone) {
            _videoState.value = _videoState.value.copy(
              status = CreativeStatus.GENERATING,
              statusMessage = "🎬 ڤیدیۆکە لە پرۆسەدایە...",
              operationName = opName
            )
            startPollingVideo(opName, projectId, prompt)
          } else if (result.data.videoUri != null) {
            _videoState.value = _videoState.value.copy(
              status = CreativeStatus.COMPLETED,
              statusMessage = "✓ تەواو بوو",
              generatedVideoUri = result.data.videoUri
            )
          }
        }

        is GenerationResult.Error -> {
          _videoState.value = _videoState.value.copy(
            status = CreativeStatus.FAILED,
            errorMessage = result.message,
            isConfigurationRequired = result.isConfigurationRequired
          )
        }
      }
    }
  }

  private fun startPollingVideo(operationName: String, projectId: String, prompt: String) {
    videoPollingJob = viewModelScope.launch {
      var attempts = 0
      val maxAttempts = 30 // Poll for up to 2.5 minutes
      while (attempts < maxAttempts) {
        delay(5000)
        attempts++

        val pollResult = videoGenerationService.pollVideoOperation(operationName)
        when (pollResult) {
          is GenerationResult.Success -> {
            if (pollResult.data.isDone && pollResult.data.videoUri != null) {
              val project = CreativeProject(
                id = projectId,
                title = prompt.take(30),
                type = CreativeType.VIDEO,
                prompt = prompt,
                mediaUri = pollResult.data.videoUri,
                status = CreativeStatus.COMPLETED
              )
              repository.saveProject(project)

              _videoState.value = _videoState.value.copy(
                status = CreativeStatus.COMPLETED,
                statusMessage = "✓ تەواو بوو",
                generatedVideoUri = pollResult.data.videoUri
              )
              return@launch
            } else {
              _videoState.value = _videoState.value.copy(
                status = CreativeStatus.PROCESSING,
                statusMessage = pollResult.data.statusMessage ?: "🎬 ڤیدیۆکە لە پرۆسەدایە..."
              )
            }
          }

          is GenerationResult.Error -> {
            _videoState.value = _videoState.value.copy(
              status = CreativeStatus.FAILED,
              errorMessage = pollResult.message,
              isConfigurationRequired = pollResult.isConfigurationRequired
            )
            return@launch
          }
        }
      }

      _videoState.value = _videoState.value.copy(
        status = CreativeStatus.FAILED,
        errorMessage = "پرۆسەی دروستکردنی ڤیدیۆ کاتێکی زۆری خایاند (Timeout). تکایە دواتر هەوڵ بدەرەوە."
      )
    }
  }

  // ==========================================
  // 4. Social Media Creator
  // ==========================================

  fun onSocialPlatformSelected(platform: SocialPlatform) {
    _socialState.value = _socialState.value.copy(platform = platform)
  }

  fun onSocialFormatSelected(format: SocialFormat) {
    _socialState.value = _socialState.value.copy(format = format)
  }

  fun onSocialTopicChanged(topic: String) {
    _socialState.value = _socialState.value.copy(topic = topic, errorMessage = null)
  }

  fun generateSocialPackage(language: AppLanguage) {
    val state = _socialState.value
    val topic = state.topic.trim()
    if (topic.isBlank()) {
      _socialState.value = state.copy(errorMessage = "تکایە سەرەتا بیرۆکە یان بابەتی پۆستەکە بنووسە.")
      return
    }

    viewModelScope.launch {
      _socialState.value = state.copy(
        isGenerating = true,
        errorMessage = null,
        isConfigurationRequired = false
      )

      val result = promptService.generateSocialContent(
        platform = state.platform,
        format = state.format,
        topic = topic,
        language = language
      )

      when (result) {
        is GenerationResult.Success -> {
          val content = result.data
          _socialState.value = _socialState.value.copy(
            isGenerating = false,
            generatedContent = content
          )

          // Save to creative history
          val projectId = UUID.randomUUID().toString()
          val project = CreativeProject(
            id = projectId,
            title = content.title,
            type = CreativeType.SOCIAL_MEDIA,
            prompt = topic,
            socialPlatform = state.platform.id,
            socialFormat = state.format.id,
            metadataJson = "${content.caption}\n\n${content.hashtags.joinToString(" ")}",
            status = CreativeStatus.COMPLETED
          )
          repository.saveProject(project)
        }

        is GenerationResult.Error -> {
          _socialState.value = _socialState.value.copy(
            isGenerating = false,
            errorMessage = result.message,
            isConfigurationRequired = result.isConfigurationRequired
          )
        }
      }
    }
  }

  fun transferPromptToImage(prompt: String) {
    _imageState.value = _imageState.value.copy(prompt = prompt)
    _currentTab.value = CreativeStudioTab.CREATE_IMAGE
  }

  fun transferPromptToVideo(prompt: String) {
    _videoState.value = _videoState.value.copy(prompt = prompt)
    _currentTab.value = CreativeStudioTab.CREATE_VIDEO
  }

  fun useAsReferenceInVideo(imageUri: Uri) {
    _videoState.value = _videoState.value.copy(referenceImageUri = imageUri)
    _currentTab.value = CreativeStudioTab.CREATE_VIDEO
  }

  fun useInImageEdit(imageUri: Uri) {
    _editState.value = _editState.value.copy(sourceUri = imageUri)
    _currentTab.value = CreativeStudioTab.EDIT_IMAGE
  }

  // ==========================================
  // 5. Creative History Actions
  // ==========================================

  fun deleteProject(project: CreativeProject) {
    viewModelScope.launch {
      project.mediaUri?.let { mediaStorageService.deleteMediaFile(it) }
      repository.deleteProject(project.id)
    }
  }

  fun renameProject(project: CreativeProject, newTitle: String) {
    if (newTitle.isBlank()) return
    viewModelScope.launch {
      repository.renameProject(project.id, newTitle.trim())
    }
  }

  fun saveProjectToGallery(project: CreativeProject, onResult: (Boolean) -> Unit) {
    val uriStr = project.mediaUri ?: return onResult(false)
    val uri = Uri.parse(uriStr)
    viewModelScope.launch {
      val isVideo = project.type == CreativeType.VIDEO
      val success = mediaStorageService.saveMediaToGallery(uri, project.title, isVideo)
      onResult(success)
    }
  }
}
