package com.example.services

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.domain.service.MediaStorageService
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStorageServiceImpl(
  private val context: Context
) : MediaStorageService {

  private val tag = "MediaStorageService"

  private val mediaDir: File by lazy {
    File(context.filesDir, "creative_media").apply {
      if (!exists()) mkdirs()
    }
  }

  override suspend fun saveToInternalStorage(
    bytes: ByteArray,
    filenamePrefix: String,
    extension: String
  ): Uri? = withContext(Dispatchers.IO) {
    try {
      val filename = "${filenamePrefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension"
      val file = File(mediaDir, filename)
      FileOutputStream(file).use { it.write(bytes) }
      FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
    } catch (e: Exception) {
      Log.e(tag, "Failed to save media internally", e)
      null
    }
  }

  override suspend fun saveMediaToGallery(
    uri: Uri,
    title: String,
    isVideo: Boolean
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val contentResolver = context.contentResolver
      val mimeType = if (isVideo) "video/mp4" else "image/png"
      val extension = if (isVideo) "mp4" else "png"
      val displayName = "${title.replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06FF]"), "_")}_${System.currentTimeMillis()}.$extension"

      val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val relativePath = if (isVideo) {
            "${Environment.DIRECTORY_MOVIES}/BasoAI"
          } else {
            "${Environment.DIRECTORY_PICTURES}/BasoAI"
          }
          put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
          put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
      }

      val collection = if (isVideo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
      }

      val itemUri = contentResolver.insert(collection, values) ?: return@withContext false

      var success = false
      var inputStream: InputStream? = null
      var outputStream: OutputStream? = null

      try {
        inputStream = contentResolver.openInputStream(uri)
        outputStream = contentResolver.openOutputStream(itemUri)
        if (inputStream != null && outputStream != null) {
          inputStream.copyTo(outputStream)
          success = true
        }
      } finally {
        inputStream?.close()
        outputStream?.close()
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && itemUri != null) {
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        contentResolver.update(itemUri, values, null, null)
      }

      success
    } catch (e: Exception) {
      Log.e(tag, "Failed to save to gallery", e)
      false
    }
  }

  override fun createShareIntent(
    uri: Uri,
    mimeType: String,
    text: String?
  ): Intent? {
    return try {
      Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        if (!text.isNullOrBlank()) {
          putExtra(Intent.EXTRA_TEXT, text)
        }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to create share intent", e)
      null
    }
  }

  override suspend fun deleteMediaFile(uriString: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val uri = Uri.parse(uriString)
      if (uri.scheme == "file") {
        uri.path?.let { File(it).delete() } ?: false
      } else {
        // Search in mediaDir if filename matches
        val lastSegment = uri.lastPathSegment ?: return@withContext false
        val file = File(mediaDir, lastSegment)
        if (file.exists()) file.delete() else true
      }
    } catch (e: Exception) {
      Log.e(tag, "Failed to delete file", e)
      false
    }
  }
}
