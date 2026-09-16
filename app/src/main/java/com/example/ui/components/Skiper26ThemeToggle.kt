package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Skiper26 Inspired Theme Toggle:
 * Ultra-fluid, hardware-accelerated theme toggle button featuring snappy
 * spring morphing between Sun and Moon states with rotation and ray expansion.
 */
@Composable
fun Skiper26ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var buttonCenter by remember { mutableStateOf(Offset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile spring press scale with snappy medium stiffness
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "skiper26_button_scale"
    )

    // Fluid icon rotation between light (0 deg) and dark (360 deg)
    val rotationAngle by animateFloatAsState(
        targetValue = if (isDarkTheme) 360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "skiper26_rotation"
    )

    // Morph progress: 0f = Pure Sun (Light), 1f = Pure Moon (Dark)
    val morphProgress by animateFloatAsState(
        targetValue = if (isDarkTheme) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "skiper26_morph"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInRoot()
                buttonCenter = Offset(bounds.center.x, bounds.center.y)
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onToggle(buttonCenter)
            }
            .testTag("theme_toggle_button"),
        shape = RoundedCornerShape(12.dp),
        color = surfaceVariantColor.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, outlineVariantColor.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width * 0.26f

                val sunColor = Color(0xFFF59E0B) // Solar Amber
                val moonColor = primaryColor    // Neon / Royal Purple Moon

                val bodyColor = androidx.compose.ui.graphics.lerp(sunColor, moonColor, morphProgress)

                // 1. Sun Rays: smooth expansion / contraction
                val rayAlpha = (1f - morphProgress * 1.5f).coerceIn(0f, 1f)
                if (rayAlpha > 0f) {
                    val rayLength = 3.5.dp.toPx() * (1f - morphProgress * 0.5f)
                    val rayInnerRadius = baseRadius + 2.5.dp.toPx()
                    val rayOuterRadius = rayInnerRadius + rayLength

                    for (i in 0 until 8) {
                        val angle = (i * 45.0) * (Math.PI / 180.0)
                        val startX = center.x + (rayInnerRadius * cos(angle)).toFloat()
                        val startY = center.y + (rayInnerRadius * sin(angle)).toFloat()
                        val endX = center.x + (rayOuterRadius * cos(angle)).toFloat()
                        val endY = center.y + (rayOuterRadius * sin(angle)).toFloat()

                        drawLine(
                            color = sunColor.copy(alpha = rayAlpha),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 1.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2. Center Celestial Body: Morph between Sun disk and Crescent Moon
                val mainCirclePath = Path().apply {
                    addOval(
                        Rect(
                            center.x - baseRadius,
                            center.y - baseRadius,
                            center.x + baseRadius,
                            center.y + baseRadius
                        )
                    )
                }

                if (morphProgress > 0.01f) {
                    val cutoutRadius = baseRadius * 0.92f
                    val cutoutOffsetX = (center.x - baseRadius * 0.72f * morphProgress)
                    val cutoutOffsetY = (center.y - baseRadius * 0.68f * morphProgress)

                    val cutoutPath = Path().apply {
                        addOval(
                            Rect(
                                cutoutOffsetX - cutoutRadius,
                                cutoutOffsetY - cutoutRadius,
                                cutoutOffsetX + cutoutRadius,
                                cutoutOffsetY + cutoutRadius
                            )
                        )
                    }

                    val crescentPath = Path().apply {
                        op(mainCirclePath, cutoutPath, PathOperation.Difference)
                    }
                    drawPath(path = crescentPath, color = bodyColor)

                    val sparkleAlpha = ((morphProgress - 0.5f) * 2f).coerceIn(0f, 1f)
                    if (sparkleAlpha > 0f) {
                        drawCircle(
                            color = bodyColor.copy(alpha = sparkleAlpha * 0.8f),
                            radius = 1.2.dp.toPx(),
                            center = Offset(center.x + baseRadius * 0.95f, center.y - baseRadius * 0.6f)
                        )
                    }
                } else {
                    drawPath(path = mainCirclePath, color = bodyColor)
                }
            }
        }
    }
}

/**
 * Full-screen circular reveal wave (inspired by Skiper26 View Transition API).
 * Runs strictly in the draw phase without triggering parent recomposition.
 */
@Composable
fun Skiper26CircularRevealOverlay(
    isRevealing: Boolean,
    origin: Offset,
    progressProvider: () -> Float,
    targetIsDark: Boolean
) {
    if (!isRevealing) return

    val waveColor = if (targetIsDark) {
        Color(0xFFA78BFA) // Luminescent purple wave
    } else {
        Color(0xFFFBBF24) // Solar warm golden/amber wave
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val progress = progressProvider()
        if (progress <= 0f || progress >= 1f) return@Canvas

        val maxRadius = hypot(size.width, size.height) * 1.15f
        val currentRadius = progress * maxRadius

        if (currentRadius > 0f) {
            val alpha = ((1f - progress) * 0.75f).coerceIn(0f, 0.75f)

            // Expanding wave fill
            drawCircle(
                color = waveColor.copy(alpha = alpha * 0.15f),
                center = origin,
                radius = currentRadius
            )

            // High-contrast leading ripple edge
            drawCircle(
                color = waveColor.copy(alpha = alpha * 0.85f),
                center = origin,
                radius = currentRadius,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
