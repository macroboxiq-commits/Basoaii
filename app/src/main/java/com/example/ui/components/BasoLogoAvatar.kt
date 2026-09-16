package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.BasoBlue
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoDarkSurfaceHigh

@Composable
fun BasoLogoAvatar(
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  showGlow: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = if (showGlow) 1.08f else 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glowScale"
  )

  val glowBrush = Brush.sweepGradient(
    listOf(
      BasoCyan,
      BasoBlue,
      Color(0xFF8B5CF6),
      BasoCyan
    )
  )

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(size)
      .scale(pulseScale)
  ) {
    // Outer glow ring
    Box(
      modifier = Modifier
        .size(size)
        .shadow(
          elevation = if (showGlow) 12.dp else 4.dp,
          shape = CircleShape,
          spotColor = BasoCyan,
          ambientColor = BasoBlue
        )
        .border(
          border = BorderStroke(1.5.dp, glowBrush),
          shape = CircleShape
        )
        .clip(CircleShape)
        .background(BasoDarkSurfaceHigh),
      contentAlignment = Alignment.Center
    ) {
      val context = LocalContext.current
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(R.drawable.baso_logo)
          .crossfade(true)
          .build(),
        contentDescription = "Baso AI Avatar",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(size - 3.dp)
          .clip(CircleShape)
      )
    }
  }
}
