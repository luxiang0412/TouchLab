package dev.touchlab.app.feature.jelly

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.touchlab.app.feature.play.rememberPhysicsFrameCounter
import dev.touchlab.app.feature.play.rememberSimulationRunning
import dev.touchlab.app.feedback.SoundCue
import dev.touchlab.app.feedback.SoundEngine
import dev.touchlab.app.feedback.rememberHaptics
import dev.touchlab.app.feedback.uiFeedback
import dev.touchlab.app.settings.Settings
import dev.touchlab.app.ui.Toy
import dev.touchlab.app.ui.components.PlayScaffold
import dev.touchlab.app.ui.theme.JellyMint

@Composable
fun JellyScreen(
    settings: Settings,
    sound: SoundEngine,
    externallyPaused: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val density = LocalDensity.current
    val desiredRadius = with(density) { 112.dp.toPx() }
    val physics = remember(desiredRadius) { JellyPhysics(desiredRadiusPx = desiredRadius) }
    val haptics = rememberHaptics()
    var paused by rememberSaveable { mutableStateOf(false) }
    val isPaused = paused || externallyPaused
    var manualTick by remember { mutableIntStateOf(0) }
    val running = rememberSimulationRunning(isPaused)
    val frame = rememberPhysicsFrameCounter(running) { dt ->
        physics.step(dt, motionScale = if (settings.reduceMotion) 0.55f else 1f)
    }

    val path = remember { Path() }
    val xs = remember { FloatArray(JellyPhysics.POINT_COUNT) }
    val ys = remember { FloatArray(JellyPhysics.POINT_COUNT) }

    PlayScaffold(
        toy = Toy.Jelly,
        paused = isPaused,
        reduceMotion = settings.reduceMotion,
        onBack = onBack,
        onTogglePause = { paused = !paused },
        onReset = {
            physics.reset()
            manualTick++
        },
        onSettings = onOpenSettings,
        onUiFeedback = { uiFeedback(sound, settings, haptics) },
    ) { notifyInteraction ->
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("play_canvas")
                .semantics { contentDescription = Toy.Jelly.accessibilityHint }
                .onSizeChanged { size ->
                    physics.resize(size.width.toFloat(), size.height.toFloat())
                    manualTick++
                }
                .pointerInput(isPaused, settings.hapticsEnabled) {
                    if (isPaused) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        physics.beginTouch(down.position.x, down.position.y)
                        notifyInteraction()
                        sound.play(SoundCue.JellyPress, settings.soundEnabled)
                        if (settings.hapticsEnabled) haptics.press()
                        var pressed = true
                        try {
                            while (pressed) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null) {
                                    pressed = false
                                } else if (!change.pressed) {
                                    change.consume()
                                    pressed = false
                                } else {
                                    physics.moveTouch(change.position.x, change.position.y)
                                    change.consume()
                                }
                            }
                        } finally {
                            physics.endTouch()
                            sound.play(SoundCue.JellyRelease, settings.soundEnabled)
                            if (settings.hapticsEnabled) haptics.release()
                        }
                    }
                },
        ) {
            // Reading the frame counter in the draw phase invalidates only the
            // drawing, never the composition tree.
            @Suppress("UNUSED_EXPRESSION")
            frame.value
            @Suppress("UNUSED_EXPRESSION")
            manualTick
            drawJelly(physics, path, xs, ys)
        }
    }
}

private fun DrawScope.drawJelly(
    physics: JellyPhysics,
    path: Path,
    xs: FloatArray,
    ys: FloatArray,
) {
    if (physics.width <= 0f || physics.height <= 0f) return
    val n = JellyPhysics.POINT_COUNT
    physics.copyOutline(xs, ys)

    path.reset()
    path.moveTo((xs[n - 1] + xs[0]) * 0.5f, (ys[n - 1] + ys[0]) * 0.5f)
    for (i in 0 until n) {
        val p1x = xs[i]
        val p1y = ys[i]
        val p2x = xs[(i + 1) % n]
        val p2y = ys[(i + 1) % n]
        val p0x = xs[(i - 1 + n) % n]
        val p0y = ys[(i - 1 + n) % n]
        val p3x = xs[(i + 2) % n]
        val p3y = ys[(i + 2) % n]

        val c1x = p1x + (p2x - p0x) / 6f
        val c1y = p1y + (p2y - p0y) / 6f
        val c2x = p2x - (p3x - p1x) / 6f
        val c2y = p2y - (p3y - p1y) / 6f
        path.cubicTo(c1x, c1y, c2x, c2y, p2x, p2y)
    }
    path.close()

    val r = physics.radius
    val cx = physics.centerX
    val cy = physics.centerY

    withTransform({ translate(0f, r * 0.13f) }) {
        drawPath(path = path, color = Color.Black.copy(alpha = 0.34f))
    }

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFD8FBEA),
                JellyMint,
                Color(0xFF6AC79B),
            ),
            center = Offset(cx - r * 0.30f, cy - r * 0.38f),
            radius = r * 2.0f,
        ),
    )

    clipPath(path) {
        drawOval(
            color = Color.White.copy(alpha = 0.34f),
            topLeft = Offset(cx - r * 0.58f, cy - r * 0.68f),
            size = Size(r * 0.82f, r * 0.40f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.055f),
            radius = r * 0.66f,
            center = Offset(cx + r * 0.34f, cy + r * 0.42f),
        )
        if (physics.touchActive) {
            drawCircle(
                color = Color.White.copy(alpha = 0.11f),
                radius = r * 0.30f,
                center = Offset(physics.touchX, physics.touchY),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = r * 0.045f,
                center = Offset(physics.touchX, physics.touchY),
            )
        }
    }

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.16f),
        style = Stroke(width = 1.2.dp.toPx()),
    )
}
