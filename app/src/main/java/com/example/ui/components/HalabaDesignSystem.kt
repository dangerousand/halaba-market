package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.random.Random

// 1. Subtle Halaba traditional geometric border or pattern ("Tilet")
@Composable
fun HalabaPatternDivider(
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    alpha: Float = 0.35f
) {
    val colorGreen = HalabaSoftGreen.copy(alpha = alpha)
    val colorYellow = HalabaGold.copy(alpha = alpha)
    val colorRed = HalabaCrimson.copy(alpha = alpha)
    
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val patternWidth = 24.dp.toPx()
        val numRepeats = (size.width / patternWidth).toInt() + 1
        
        for (i in 0 until numRepeats) {
            val startX = i * patternWidth
            // Draw traditional geometric triangles
            // Triangle 1: Green
            val greenPath = Path().apply {
                moveTo(startX, 0f)
                lineTo(startX + patternWidth / 3f, size.height)
                lineTo(startX + (patternWidth / 3f) * 2f, 0f)
                close()
            }
            drawPath(greenPath, color = colorGreen)

            // Triangle 2: Yellow diamond inside
            val yellowPath = Path().apply {
                moveTo(startX + patternWidth / 3f, size.height)
                lineTo(startX + patternWidth / 2f, 0f)
                lineTo(startX + (patternWidth / 3f) * 2f, size.height)
                close()
            }
            drawPath(yellowPath, color = colorYellow)

            // Chevron 3: Red side lines
            val redPath = Path().apply {
                moveTo(startX + (patternWidth / 3f) * 2f, 0f)
                lineTo(startX + patternWidth, size.height / 2f)
                lineTo(startX + (patternWidth / 3f) * 2f + patternWidth / 3f, 0f)
                close()
            }
            drawPath(redPath, color = colorRed)
        }
    }
}

// 2. Glassmorphic Elevated Card with soft colors and subtle borders
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
    content: @Composable ColumnScope.() -> Unit
) {
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    )
    Column(
        modifier = modifier
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderColor, shape)
            .padding(16.dp),
        content = content
    )
}

// 3. Shimmer skeleton loader animation for images/lists
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// 4. Animated vibrating Notification Bell
@Composable
fun PulseNotificationBell(
    hasNotifications: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "bell")
    val angle by if (hasNotifications) {
        transition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 150, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wiggle"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val scale by if (hasNotifications) {
        transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    IconButton(onClick = onClick, modifier = modifier) {
        Box {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = if (hasNotifications) HalabaGold else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .scale(scale)
                    .rotate(angle)
            )
            if (hasNotifications) {
                // Little red dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(HalabaCrimson)
                        .align(androidx.compose.ui.Alignment.TopEnd)
                )
            }
        }
    }
}

// 5. Confetti animation overlay for successful creation
@Composable
fun ConfettiOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val transition = rememberInfiniteTransition(label = "confetti")
    val fallPercentage by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    // Generate stable random particles once
    val particles = remember {
        List(40) {
            ConfettiParticle(
                xPercent = Random.nextFloat(),
                speedMultiplier = Random.nextFloat() * 0.4f + 0.8f,
                size = Random.nextInt(12, 32).toFloat(),
                color = when (Random.nextInt(3)) {
                    0 -> HalabaSoftGreen
                    1 -> HalabaGold
                    else -> HalabaCrimson
                },
                shapeType = Random.nextInt(3)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        for (particle in particles) {
            val yPos = (fallPercentage * size.height * particle.speedMultiplier) % size.height
            val xPos = particle.xPercent * size.width

            when (particle.shapeType) {
                0 -> drawRect(
                    color = particle.color,
                    topLeft = Offset(xPos, yPos),
                    size = Size(particle.size, particle.size)
                )
                1 -> drawCircle(
                    color = particle.color,
                    center = Offset(xPos, yPos),
                    radius = particle.size / 2f
                )
                else -> {
                    val path = Path().apply {
                        moveTo(xPos, yPos - particle.size / 2f)
                        lineTo(xPos + particle.size / 2f, yPos + particle.size / 2f)
                        lineTo(xPos - particle.size / 2f, yPos + particle.size / 2f)
                        close()
                    }
                    drawPath(path, color = particle.color)
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val xPercent: Float,
    val speedMultiplier: Float,
    val size: Float,
    val color: Color,
    val shapeType: Int
)
