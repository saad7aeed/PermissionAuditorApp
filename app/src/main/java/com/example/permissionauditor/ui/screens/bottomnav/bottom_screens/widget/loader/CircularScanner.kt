package com.example.permissionauditor.ui.screens.bottomnav.bottom_screens.widget.loader

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularScanner(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp
) {
    // ✨ Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")

    val sweepRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier.size(220.dp)
    ) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // ✨ Background ring (theme-aware)
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )

        // ✨ Progress ring (theme-aware)
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = stroke
        )

        // ✨ Scanning sweep arc (theme-aware with alpha)
        if (progress < 1f) {
            drawArc(
                color = primaryColor.copy(alpha = 0.6f),
                startAngle = sweepRotation,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

// ✨ Alternative: Gradient Scanner
@Composable
fun GradientCircularScanner(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp
) {
    // ✨ Theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    val infiniteTransition = rememberInfiniteTransition(label = "gradient_scanner")

    val sweepRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier.size(220.dp)
    ) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // Background ring
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )

        // Progress ring
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = stroke
        )

        // Animated gradient sweep (slower, more subtle)
        if (progress < 1f) {
            // Primary sweep
            drawArc(
                color = primaryColor.copy(alpha = 0.7f),
                startAngle = sweepRotation,
                sweepAngle = 40f,
                useCenter = false,
                style = stroke
            )

            // Secondary sweep (trailing)
            drawArc(
                color = secondaryColor.copy(alpha = 0.4f),
                startAngle = sweepRotation - 40f,
                sweepAngle = 30f,
                useCenter = false,
                style = stroke
            )
        }
    }
}

// ✨ Minimal Scanner (for minimal design preference)
@Composable
fun MinimalCircularScanner(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Canvas(
        modifier = modifier.size(180.dp)
    ) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // Background ring
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )

        // Progress ring (no sweep animation - cleaner)
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = stroke
        )
    }
}