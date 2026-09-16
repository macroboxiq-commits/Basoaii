package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Animated moving background component that seamlessly covers the entire screen
 * featuring Killua Zoldyck overlooking the cyberpunk city with the moon,
 * dynamic electric lightning arcs, and looping ambient video playback.
 */
@Composable
fun AnimatedChatBackground(
  modifier: Modifier = Modifier,
  enableVideoLoop: Boolean = true,
  enableLightningArcs: Boolean = true,
  dimLevel: Float = 0.28f
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // State to hold MediaPlayer reference for lifecycle control
  var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
  var isVideoReady by remember { mutableStateOf(false) }

  // Manage Android lifecycle for MediaPlayer
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_PAUSE -> {
          mediaPlayerRef?.let {
            if (it.isPlaying) {
              it.pause()
            }
          }
        }
        Lifecycle.Event.ON_RESUME -> {
          mediaPlayerRef?.let {
            try {
              it.start()
            } catch (_: Exception) {}
          }
        }
        Lifecycle.Event.ON_DESTROY -> {
          try {
            mediaPlayerRef?.stop()
            mediaPlayerRef?.release()
          } catch (_: Exception) {}
          mediaPlayerRef = null
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      try {
        mediaPlayerRef?.stop()
        mediaPlayerRef?.release()
      } catch (_: Exception) {}
      mediaPlayerRef = null
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Layer 1: Instant high-res crisp still image background (never any black flash or letterboxing)
    Image(
      painter = painterResource(id = R.drawable.img_killua_bg),
      contentDescription = "Anime Killua moving background",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )

    // Layer 2: Moving / Animated Looping Video via TextureView (Scale to Fill Screen perfectly)
    if (enableVideoLoop) {
      AndroidView(
        factory = { ctx ->
          val frameLayout = FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
          }

          val textureView = TextureView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
              FrameLayout.LayoutParams.MATCH_PARENT,
              FrameLayout.LayoutParams.MATCH_PARENT
            )
          }

          textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
              try {
                val mp = MediaPlayer().apply {
                  val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.bg_killua_animated}")
                  setDataSource(ctx, uri)
                  setSurface(Surface(surface))
                  isLooping = true
                  setVolume(0f, 0f) // Silent ambient background
                  setOnPreparedListener { player ->
                    // Calculate aspect fill transformation matrix to prevent any letterboxing
                    val videoWidth = 720f
                    val videoHeight = 1280f
                    val viewWidth = width.toFloat()
                    val viewHeight = height.toFloat()

                    val scaleX: Float
                    val scaleY: Float
                    if (viewWidth / viewHeight > videoWidth / videoHeight) {
                      scaleX = 1f
                      scaleY = (viewWidth / videoWidth) * (videoHeight / viewHeight)
                    } else {
                      scaleX = (viewHeight / videoHeight) * (videoWidth / viewWidth)
                      scaleY = 1f
                    }

                    val matrix = Matrix()
                    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                    textureView.setTransform(matrix)

                    player.start()
                    isVideoReady = true
                  }
                  prepareAsync()
                }
                mediaPlayerRef = mp
              } catch (e: Exception) {
                // Graceful fallback to static high-res image and Compose lightning animation
              }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
              val mp = mediaPlayerRef ?: return
              try {
                val videoWidth = 720f
                val videoHeight = 1280f
                val viewWidth = width.toFloat()
                val viewHeight = height.toFloat()

                val scaleX: Float
                val scaleY: Float
                if (viewWidth / viewHeight > videoWidth / videoHeight) {
                  scaleX = 1f
                  scaleY = (viewWidth / videoWidth) * (videoHeight / viewHeight)
                } else {
                  scaleX = (viewHeight / videoHeight) * (videoWidth / viewWidth)
                  scaleY = 1f
                }

                val matrix = Matrix()
                matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                textureView.setTransform(matrix)
              } catch (_: Exception) {}
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
              try {
                mediaPlayerRef?.stop()
                mediaPlayerRef?.release()
              } catch (_: Exception) {}
              mediaPlayerRef = null
              return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
          }

          frameLayout.addView(textureView)
          frameLayout
        },
        modifier = Modifier.fillMaxSize()
      )
    }

    // Layer 3: Dynamic Animated Lightning Sparks & Electric Arcs overlay
    if (enableLightningArcs) {
      DynamicElectricSkyOverlay()
    }

    // Layer 4: Cinematic Vignette & Readability Gradient Overlay
    // Ensures text, buttons, and input bars are 100% legible while showing the wallpaper
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            0.0f to Color(0x99030B17), // Soft top tint for topbar & system status bar
            0.20f to Color(0x33051329),
            0.50f to Color(0x18000000).copy(alpha = dimLevel * 0.5f), // Clearest in center
            0.75f to Color(0x4D051021),
            1.0f to Color(0xD9030A14)  // Darker at bottom for the chat input bar
          )
        )
    )
  }
}

/**
 * Animated dynamic electric arcs and lightning flashes drawn over the sky
 */
@Composable
private fun DynamicElectricSkyOverlay() {
  val infiniteTransition = rememberInfiniteTransition(label = "electricPulse")

  // Ambient pulsing glow around the moon / upper sky
  val auraGlow by infiniteTransition.animateFloat(
    initialValue = 0.15f,
    targetValue = 0.45f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "auraGlow"
  )

  // Subtle floating neon spark particles
  val particleShift by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "particleShift"
  )

  // Randomized lightning strike state
  var lightningFlashAlpha by remember { mutableFloatStateOf(0f) }
  var lightningSeed by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(Unit) {
    while (true) {
      // Wait between 2.5 and 5 seconds between strikes
      delay(Random.nextLong(2200, 4800))
      lightningSeed = Random.nextFloat()

      // Quick double strike flash
      lightningFlashAlpha = 0.75f
      delay(70)
      lightningFlashAlpha = 0.1f
      delay(50)
      lightningFlashAlpha = 0.9f
      delay(90)
      lightningFlashAlpha = 0.25f
      delay(80)
      lightningFlashAlpha = 0f
    }
  }

  Canvas(modifier = Modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    // 1. Ambient Moon Glow aura in the upper region
    val moonCenter = Offset(width * 0.52f, height * 0.19f)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x6600E5FF).copy(alpha = auraGlow * 0.8f),
          Color(0x3300B0FF).copy(alpha = auraGlow * 0.4f),
          Color.Transparent
        ),
        center = moonCenter,
        radius = width * 0.45f
      ),
      center = moonCenter,
      radius = width * 0.45f
    )

    // 2. Dynamic Lightning Arcs when strike triggers
    if (lightningFlashAlpha > 0.05f) {
      // Sky flash illumination
      drawRect(
        color = Color(0x3300E5FF).copy(alpha = lightningFlashAlpha * 0.35f)
      )

      // Main branching lightning bolt from clouds
      val startX = width * (0.35f + (lightningSeed * 0.3f))
      val startY = height * 0.04f
      val boltPath = Path().apply {
        moveTo(startX, startY)
        var currentX = startX
        var currentY = startY
        val segments = 8
        val stepY = (height * 0.45f) / segments

        for (i in 1..segments) {
          val offsetX = (Random(lightningSeed.toLong() + i * 17).nextFloat() - 0.5f) * width * 0.12f
          currentX += offsetX
          currentY += stepY
          lineTo(currentX, currentY)
        }
      }

      // Outer glow of lightning bolt
      drawPath(
        path = boltPath,
        color = Color(0x8000E5FF).copy(alpha = lightningFlashAlpha),
        style = Stroke(width = 6f)
      )
      // Core bright white-blue bolt
      drawPath(
        path = boltPath,
        color = Color(0xFFFFFFFF).copy(alpha = lightningFlashAlpha),
        style = Stroke(width = 2.5f)
      )
    }

    // 3. Floating electric embers rising from the cityscape
    val particleCount = 14
    for (i in 0 until particleCount) {
      val pSeed = (i * 97) % 100 / 100f
      val px = (pSeed * width * 0.9f) + (width * 0.05f)
      val baseY = height * 0.88f
      val py = baseY - ((particleShift + pSeed) % 1f) * (height * 0.65f)
      val radius = 1.5f + (pSeed * 2.2f)
      val pAlpha = (1f - ((baseY - py) / (height * 0.65f))) * 0.6f

      drawCircle(
        color = if (i % 2 == 0) Color(0xFF00E5FF).copy(alpha = pAlpha) else Color(0xFF80D8FF).copy(alpha = pAlpha),
        radius = radius,
        center = Offset(px, py)
      )
    }
  }
}
