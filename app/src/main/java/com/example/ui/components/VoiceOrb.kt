package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.VoiceAssistantState
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoiceOrb(
  state: VoiceAssistantState,
  soundLevel: Float = 0f,
  modifier: Modifier = Modifier,
  size: Dp = 160.dp
) {
  val infiniteTransition = rememberInfiniteTransition(label = "voiceOrbTransition")

  // Idle pulse
  val idleScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "idleScale"
  )

  // Rotation for thinking
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotationAngle"
  )

  // Speaking sound-wave ripple
  val wavePulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "wavePulse"
  )

  val dynamicScale = when (state) {
    VoiceAssistantState.IDLE -> idleScale
    VoiceAssistantState.LISTENING -> (1.0f + (soundLevel * 0.35f)).coerceIn(1.0f, 1.4f)
    VoiceAssistantState.PROCESSING,
    VoiceAssistantState.THINKING -> idleScale * 0.98f
    VoiceAssistantState.SPEAKING -> wavePulse
    VoiceAssistantState.ERROR -> 0.92f
  }

  val primaryColor = when (state) {
    VoiceAssistantState.IDLE -> BasoCyan
    VoiceAssistantState.LISTENING -> Color(0xFF00F0FF)
    VoiceAssistantState.PROCESSING,
    VoiceAssistantState.THINKING -> BasoPurple
    VoiceAssistantState.SPEAKING -> Color(0xFF00E5FF)
    VoiceAssistantState.ERROR -> Color(0xFFFF5252)
  }

  val secondaryColor = when (state) {
    VoiceAssistantState.IDLE -> BasoPurple
    VoiceAssistantState.LISTENING -> Color(0xFF7000FF)
    VoiceAssistantState.PROCESSING,
    VoiceAssistantState.THINKING -> Color(0xFF00E5FF)
    VoiceAssistantState.SPEAKING -> Color(0xFF7C4DFF)
    VoiceAssistantState.ERROR -> Color(0xFFFF8A80)
  }

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    // Outer animated canvas
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          scaleX = dynamicScale
          scaleY = dynamicScale
          if (state == VoiceAssistantState.THINKING) {
            rotationZ = rotationAngle
          }
        }
    ) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val radius = this.size.minDimension / 2.6f

      // Outer aura glow
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            primaryColor.copy(alpha = 0.45f),
            secondaryColor.copy(alpha = 0.2f),
            Color.Transparent
          ),
          center = center,
          radius = radius * 1.6f
        ),
        radius = radius * 1.6f,
        center = center
      )

      // Speaking or Listening acoustic ripples
      if (state == VoiceAssistantState.LISTENING || state == VoiceAssistantState.SPEAKING) {
        val rippleCount = 3
        for (i in 1..rippleCount) {
          val rippleAlpha = (0.35f / i)
          val rippleRadius = radius * (1f + (i * 0.18f * dynamicScale))
          drawCircle(
            color = primaryColor.copy(alpha = rippleAlpha),
            radius = rippleRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
          )
        }
      }

      // Thinking orbiting nodes
      if (state == VoiceAssistantState.THINKING) {
        val orbitRadius = radius * 1.25f
        for (i in 0 until 4) {
          val angleRad = Math.toRadians((i * 90.0) + rotationAngle.toDouble())
          val nodeX = (center.x + orbitRadius * cos(angleRad)).toFloat()
          val nodeY = (center.y + orbitRadius * sin(angleRad)).toFloat()
          drawCircle(
            color = if (i % 2 == 0) BasoCyan else BasoPurple,
            radius = 5.dp.toPx(),
            center = Offset(nodeX, nodeY)
          )
        }
      }

      // Core neural sphere
      drawCircle(
        brush = Brush.linearGradient(
          colors = listOf(
            primaryColor,
            secondaryColor,
            Color(0xFF0A0F1D)
          ),
          start = Offset(center.x - radius, center.y - radius),
          end = Offset(center.x + radius, center.y + radius)
        ),
        radius = radius,
        center = center
      )

      // Inner specular gleam
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.6f),
            Color.Transparent
          ),
          center = Offset(center.x - (radius * 0.35f), center.y - (radius * 0.35f)),
          radius = radius * 0.5f
        ),
        radius = radius * 0.5f,
        center = Offset(center.x - (radius * 0.35f), center.y - (radius * 0.35f))
      )
    }
  }
}
