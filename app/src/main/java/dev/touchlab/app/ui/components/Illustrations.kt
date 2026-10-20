package dev.touchlab.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.touchlab.app.ui.theme.DustLavender
import dev.touchlab.app.ui.theme.JellyMint
import dev.touchlab.app.ui.theme.MarbleCoral
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Soft radial glows used behind home and play content. */
@Composable
fun AmbientBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val mintCenter = Offset(size.width * 0.86f, size.height * 0.08f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(JellyMint.copy(alpha = 0.10f), Color.Transparent),
                center = mintCenter,
                radius = size.width * 0.72f,
            ),
            radius = size.width * 0.72f,
            center = mintCenter,
        )
        val dustCenter = Offset(size.width * 0.08f, size.height * 0.88f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(DustLavender.copy(alpha = 0.09f), Color.Transparent),
                center = dustCenter,
                radius = size.width * 0.70f,
            ),
            radius = size.width * 0.70f,
            center = dustCenter,
        )
    }
}

/** A mint blob with a soft highlight: the Jelly card illustration. */
@Composable
fun JellyIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = min(w, h) * 0.30f
        val c = Offset(w * 0.55f, h * 0.50f)

        drawOval(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset(c.x - r * 0.72f, c.y + r * 0.72f),
            size = Size(r * 1.44f, r * 0.34f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(JellyMint, JellyMint.copy(alpha = 0.52f)),
                center = Offset(c.x - r * 0.32f, c.y - r * 0.38f),
                radius = r * 1.55f,
            ),
            radius = r,
            center = c,
        )
        drawOval(
            color = Color.White.copy(alpha = 0.42f),
            topLeft = Offset(c.x - r * 0.50f, c.y - r * 0.62f),
            size = Size(r * 0.62f, r * 0.36f),
        )
        drawCircle(
            color = JellyMint.copy(alpha = 0.20f),
            radius = r * 0.16f,
            center = Offset(w * 0.15f, h * 0.24f),
        )
        drawCircle(
            color = JellyMint.copy(alpha = 0.14f),
            radius = r * 0.09f,
            center = Offset(w * 0.88f, h * 0.74f),
        )
    }
}

/** A small lavender galaxy: the Magnet Dust card illustration. */
@Composable
fun DustIllustration(modifier: Modifier = Modifier) {
    val dots = remember {
        List(34) { i ->
            val t = i + 0.5f
            val radius = 0.10f + 0.44f * (t / 34f)
            val angle = i * 2.399963f
            Triple(radius * cos(angle), radius * sin(angle), 0.30f + 0.70f * ((i * 37) % 11) / 11f)
        }
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w * 0.52f, h * 0.50f)
        val unit = min(w, h)
        for ((cosA, sinA, alpha) in dots) {
            val p = Offset(c.x + cosA * unit * 0.86f, c.y + sinA * unit * 0.62f)
            drawCircle(
                color = DustLavender.copy(alpha = alpha * 0.85f),
                radius = unit * (0.008f + 0.012f * alpha),
                center = p,
            )
        }
        drawArc(
            color = DustLavender.copy(alpha = 0.25f),
            startAngle = 200f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(c.x - unit * 0.26f, c.y - unit * 0.20f),
            size = Size(unit * 0.52f, unit * 0.40f),
            style = Stroke(width = 1.4.dp.toPx()),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.85f), DustLavender.copy(alpha = 0f)),
                center = c,
                radius = unit * 0.10f,
            ),
            radius = unit * 0.10f,
            center = c,
        )
    }
}

/** Three coral marbles with a light trail: the Marbles card illustration. */
@Composable
fun MarblesIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val unit = min(w, h)
        val trail = Path().apply {
            moveTo(w * 0.18f, h * 0.62f)
            quadraticTo(w * 0.40f, h * 0.24f, w * 0.64f, h * 0.46f)
            quadraticTo(w * 0.78f, h * 0.58f, w * 0.88f, h * 0.42f)
        }
        drawPath(
            path = trail,
            color = MarbleCoral.copy(alpha = 0.28f),
            style = Stroke(width = 2.dp.toPx()),
        )
        data class Ball(val x: Float, val y: Float, val r: Float, val tint: Color)
        val balls = listOf(
            Ball(w * 0.26f, h * 0.68f, unit * 0.135f, MarbleCoral),
            Ball(w * 0.58f, h * 0.48f, unit * 0.105f, MarbleCoral.copy(alpha = 0.88f)),
            Ball(w * 0.83f, h * 0.34f, unit * 0.075f, MarbleCoral.copy(alpha = 0.76f)),
        )
        balls.forEach { ball ->
            val center = Offset(ball.x, ball.y)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f), ball.tint),
                    center = Offset(ball.x - ball.r * 0.35f, ball.y - ball.r * 0.38f),
                    radius = ball.r * 1.45f,
                ),
                radius = ball.r,
                center = center,
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = ball.r * 0.17f,
                center = Offset(ball.x - ball.r * 0.34f, ball.y - ball.r * 0.38f),
            )
        }
    }
}

@Composable
fun CardSheen(modifier: Modifier = Modifier, accent: Color) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                start = Offset.Zero,
                end = Offset(900f, 700f),
            ),
        ),
    )
}
