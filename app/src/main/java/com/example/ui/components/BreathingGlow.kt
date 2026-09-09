package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StatusUrgentCrimson
import com.example.ui.theme.VioletNeon

/**
 * Creates a pulsating respiratory glow effect on borders for items with critical deadlines (< 1 hour).
 */
fun Modifier.breathingGlow(
    isCritical: Boolean,
    cornerRadius: Dp = 14.dp,
    strokeWidth: Dp = 1.5.dp
): Modifier = composed {
    if (!isCritical) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "breathing_glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    this.border(
        width = strokeWidth,
        brush = Brush.horizontalGradient(
            listOf(
                StatusUrgentCrimson.copy(alpha = alpha),
                VioletNeon.copy(alpha = alpha * 0.75f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
}
