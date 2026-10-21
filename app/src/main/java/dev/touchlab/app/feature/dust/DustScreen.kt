package dev.touchlab.app.feature.dust

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import dev.touchlab.app.ui.theme.DustLavender
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun DustScreen(
    settings: Settings,
    sound: SoundEngine,
    externallyPaused: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val a11y = stringResource(Toy.Dust.accessibilityHintRes)
    val physics = remember { DustPhysics() }
    val haptics = rememberHaptics()
    var paused by rememberSaveable { mutableStateOf(false) }
    val isPaused = paused || externallyPaused
    var manualTick by remember { mutableIntStateOf(0) }

    val running = rememberSimulationRunning(isPaused)
    val motionScale = if (settings.reduceMotion) 0.35f else 1f
    val frame = rememberPhysicsFrameCounter(running) { dt -> physics.step(dt, motionScale) }

    PlayScaffold(
        toy = Toy.Dust,
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
                .semantics { contentDescription = a11y }
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
                        sound.play(SoundCue.DustTouch, settings.soundEnabled)
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
                            if (settings.hapticsEnabled) haptics.release()
                        }
                    }
                },
        ) {
            @Suppress("UNUSED_EXPRESSION")
            frame.value
            @Suppress("UNUSED_EXPRESSION")
            manualTick
            drawDust(physics, settings.reduceMotion)
        }
    }
}

private fun DrawScope.drawDust(physics: DustPhysics, reduceMotion: Boolean) {
    if (physics.width <= 0f || physics.height <= 0f) return
    val unit = min(physics.width, physics.height)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(DustLavender.copy(alpha = 0.07f), Color.Transparent),
            center = Offset(physics.width * 0.5f, physics.height * 0.5f),
            radius = unit * 0.55f,
        ),
        radius = unit * 0.55f,
        center = Offset(physics.width * 0.5f, physics.height * 0.5f),
    )

    if (physics.touchActive) {
        val halo = 96.dp.toPx()
        val center = Offset(physics.touchX, physics.touchY)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    DustLavender.copy(alpha = if (reduceMotion) 0.08f else 0.13f),
                    Color.Transparent,
                ),
                center = center,
                radius = halo,
            ),
            radius = halo,
            center = center,
        )
        drawCircle(
            color = DustLavender.copy(alpha = 0.28f),
            radius = halo * 0.24f,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
    }

    val trailFactor = if (reduceMotion) 0.018f else 0.032f
    for (i in 0 until DustPhysics.PARTICLE_COUNT) {
        val p = physics.particle(i)
        val speed = hypot(p.vx, p.vy)
        if (speed > 18f) {
            drawLine(
                color = DustLavender.copy(alpha = p.brightness * 0.16f),
                start = Offset(p.x - p.vx * trailFactor, p.y - p.vy * trailFactor),
                end = Offset(p.x, p.y),
                strokeWidth = p.radius * 0.9f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = DustLavender.copy(alpha = p.brightness * 0.95f),
            radius = p.radius,
            center = Offset(p.x, p.y),
        )
        if (!reduceMotion && p.brightness > 0.82f) {
            drawCircle(
                color = Color.White.copy(alpha = 0.38f),
                radius = p.radius * 0.55f,
                center = Offset(p.x, p.y),
            )
        }
    }

    if (physics.touchActive) {
        drawCircle(
            color = Color.White.copy(alpha = 0.82f),
            radius = 3.5f,
            center = Offset(physics.touchX, physics.touchY),
        )
    }
}
