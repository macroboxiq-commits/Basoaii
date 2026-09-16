package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeStatus
import com.example.domain.model.CreativeType

@Entity(tableName = "creative_projects")
data class CreativeProjectEntity(
  @PrimaryKey val id: String,
  val title: String,
  val type: String, // "IMAGE", "VIDEO", "EDITED_IMAGE", "SOCIAL_MEDIA"
  val prompt: String,
  val enhancedPrompt: String? = null,
  val mediaUri: String? = null,
  val thumbnailUri: String? = null,
  val style: String? = null,
  val aspectRatio: String? = null,
  val socialPlatform: String? = null,
  val socialFormat: String? = null,
  val metadataJson: String? = null,
  val status: String = "IDLE", // "IDLE", "PREPARING", "GENERATING", "PROCESSING", "COMPLETED", "FAILED"
  val errorMessage: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) {
  fun toDomain(): CreativeProject {
    val creativeType = try {
      CreativeType.valueOf(type)
    } catch (e: Exception) {
      CreativeType.IMAGE
    }
    val creativeStatus = try {
      CreativeStatus.valueOf(status)
    } catch (e: Exception) {
      CreativeStatus.IDLE
    }

    return CreativeProject(
      id = id,
      title = title,
      type = creativeType,
      prompt = prompt,
      enhancedPrompt = enhancedPrompt,
      mediaUri = mediaUri,
      thumbnailUri = thumbnailUri,
      style = style,
      aspectRatio = aspectRatio,
      socialPlatform = socialPlatform,
      socialFormat = socialFormat,
      metadataJson = metadataJson,
      status = creativeStatus,
      errorMessage = errorMessage,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }

  companion object {
    fun fromDomain(project: CreativeProject): CreativeProjectEntity {
      return CreativeProjectEntity(
        id = project.id,
        title = project.title,
        type = project.type.name,
        prompt = project.prompt,
        enhancedPrompt = project.enhancedPrompt,
        mediaUri = project.mediaUri,
        thumbnailUri = project.thumbnailUri,
        style = project.style,
        aspectRatio = project.aspectRatio,
        socialPlatform = project.socialPlatform,
        socialFormat = project.socialFormat,
        metadataJson = project.metadataJson,
        status = project.status.name,
        errorMessage = project.errorMessage,
        createdAt = project.createdAt,
        updatedAt = project.updatedAt
      )
    }
  }
}
