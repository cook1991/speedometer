package com.spritzlive.speedometer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val START_ANGLE = 135f   // degrees, 0 = 3 o'clock, CW positive
private const val SWEEP_ANGLE = 270f

/**
 * Analog speedometer arc. [speed] and [maxSpeed] are in the same (display) unit;
 * the needle position is speed / maxSpeed clamped to the arc.
 */
@Composable
fun SpeedGauge(
    speed: Float,
    maxScale: Float,
    accent: Color,
    warm: Color,
    trackColor: Color,
    tickColor: Color,
    modifier: Modifier = Modifier,
    /** In landscape the gauge is bounded by height rather than width. */
    fitToHeight: Boolean = false,
) {
    val fraction = (speed / maxScale).coerceIn(0f, 1f)
    val animated by animateFloatAsState(targetValue = fraction, label = "needle")
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = (if (fitToHeight) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
                .aspectRatio(1f)
                .padding(16.dp)
        ) {
            drawGauge(animated, maxScale, accent, warm, trackColor, tickColor, textMeasurer)
        }
    }
}

private fun DrawScope.drawGauge(
    fraction: Float,
    maxScale: Float,
    accent: Color,
    warm: Color,
    trackColor: Color,
    tickColor: Color,
    textMeasurer: TextMeasurer,
) {
    val stroke = size.minDimension * 0.07f
    val radius = (min(size.width, size.height) - stroke) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Background track.
    drawArc(
        color = trackColor,
        startAngle = START_ANGLE,
        sweepAngle = SWEEP_ANGLE,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )

    // Filled progress arc with an accent -> warm gradient.
    drawArc(
        brush = Brush.sweepGradient(listOf(accent, accent, warm, warm), center),
        startAngle = START_ANGLE,
        sweepAngle = SWEEP_ANGLE * fraction,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )

    // Tick marks + numeric labels every 1/6 of the scale.
    val majorTicks = 6
    for (i in 0..majorTicks) {
        val t = i / majorTicks.toFloat()
        val angleRad = Math.toRadians((START_ANGLE + SWEEP_ANGLE * t).toDouble())
        val outer = radius
        val inner = radius - stroke * 1.3f
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()
        drawLine(
            color = tickColor,
            start = Offset(center.x + cosA * outer, center.y + sinA * outer),
            end = Offset(center.x + cosA * inner, center.y + sinA * inner),
            strokeWidth = stroke * 0.18f,
            cap = StrokeCap.Round,
        )
        val labelValue = (maxScale * t).roundToInt().toString()
        val labelRadius = radius - stroke * 2.4f
        val measured = textMeasurer.measure(labelValue)
        val lx = center.x + cosA * labelRadius - measured.size.width / 2f
        val ly = center.y + sinA * labelRadius - measured.size.height / 2f
        drawText(measured, color = Color(0xFF8899AA), topLeft = Offset(lx, ly))
    }
}
