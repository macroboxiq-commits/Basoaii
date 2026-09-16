package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BasoAiBubble
import com.example.ui.theme.BasoAiBubbleBorder
import com.example.ui.theme.BasoCyan
import com.example.ui.theme.BasoTextSecondary
import kotlinx.coroutines.delay

@Composable
fun ThinkingIndicator(
  thinkingText: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .testTag("thinking_indicator")
      .padding(horizontal = 16.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BasoLogoAvatar(size = 28.dp, showGlow = true)

    Spacer(modifier = Modifier.width(10.dp))

    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
        .background(BasoAiBubble)
        .border(BorderStroke(1.dp, BasoAiBubbleBorder), RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = thinkingText,
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp
        ),
        color = BasoTextSecondary
      )

      Spacer(modifier = Modifier.width(2.dp))

      // Animated 3 pulsing dots
      PulsingDot(delayMillis = 0)
      PulsingDot(delayMillis = 200)
      PulsingDot(delayMillis = 400)
    }
  }
}

@Composable
private fun PulsingDot(delayMillis: Int) {
  val scale = remember { Animatable(0.4f) }

  LaunchedEffect(Unit) {
    delay(delayMillis.toLong())
    scale.animateTo(
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  Box(
    modifier = Modifier
      .size(6.dp)
      .scale(scale.value)
      .clip(CircleShape)
      .background(BasoCyan)
  )
}
