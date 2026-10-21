package dev.touchlab.app.feature.marbles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.touchlab.app.feature.play.rememberPhysicsFrameCounter
import dev.touchlab.app.feature.play.rememberSimulationRunning
import dev.touchlab.app.feedback.SoundCue
import dev.touchlab.app.feedback.SoundEngine
import dev.touchlab.app.R
import dev.touchlab.app.feedback.rememberHaptics
import dev.touchlab.app.feedback.uiFeedback
import dev.touchlab.app.settings.Settings
import dev.touchlab.app.ui.Toy
import dev.touchlab.app.ui.components.PlayScaffold
import dev.touchlab.app.ui.components.SmallTogglePill
import dev.touchlab.app.ui.theme.MarbleCoral
import dev.touchlab.app.ui.theme.Surface
import kotlin.math.hypot

private data class MarbleColors(val light: Color, val deep: Color)

private val MARBLE_PALETTE = listOf(
    MarbleColors(Color(0xFFFFD7CB), Color(0xFFF3866D)),
    MarbleColors(Color(0xFFFFE2BC), Color(0xFFEEA253)),
    MarbleColors(Color(0xFFFFCCD2), Color(0xFFE57884)),
    MarbleColors(Color(0xFFFFEBC7), Color(0xFFE5A955)),
    MarbleColors(Color(0xFFFFF4D8), Color(0xFFDDBB6E)),
    MarbleColors(Color(0xFFFFCCB1), Color(0xFFE58466)),
)

@Composable
fun MarblesScreen(
    settings: Settings,
    sound: SoundEngine,
    externallyPaused: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val a11y = stringResource(Toy.Marbles.accessibilityHintRes)
    val physics = remember { MarblesPhysics() }
    val haptics = rememberHaptics()
    var paused by rememberSaveable { mutableStateOf(false) }
    var gravityOn by rememberSaveable { mutableStateOf(true) }
    val isPaused = paused || externallyPaused
    var manualTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(gravityOn) { physics.setGravity(gravityOn) }

    val running = rememberSimulationRunning(isPaused)
    val frame = rememberPhysicsFrameCounter(running) { dt ->
        physics.step(dt)
        val impact = physics.consumeImpact()
        if (impact > 160f) {
            sound.play(SoundCue.MarbleHit, settings.soundEnabled)
            if (settings.hapticsEnabled && impact > 240f) haptics.impact()
        }
    }

    PlayScaffold(
        toy = Toy.Marbles,
        paused = isPaused,
        reduceMotion = settings.reduceMotion,
        onBack = onBack,
        onTogglePause = { paused = !paused },
        onReset = {
            physics.reset()
            physics.setGravity(gravityOn)
            manualTick++
        },
        onSettings = onOpenSettings,
        onUiFeedback = { uiFeedback(sound, settings, haptics) },
        extraControls = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                SmallTogglePill(
                    label = stringResource(if (gravityOn) R.string.gravity_on else R.string.gravity_off),
                    checked = gravityOn,
                    accent = MarbleCoral,
                    onToggle = {
                        gravityOn = !gravityOn
                        physics.setGravity(gravityOn)
                        uiFeedback(sound, settings, haptics)
                    },
                    tag = "marbles_gravity_toggle",
                )
            }
        },
    ) { notifyInteraction ->
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp)
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
                        notifyInteraction()
                        val grabbed = physics.beginDrag(down.position.x, down.position.y)
                        var lastUptime = down.uptimeMillis
                        if (grabbed) {
                            sound.play(SoundCue.UiTap, settings.soundEnabled)
                            if (settings.hapticsEnabled) haptics.press()
                        }
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
                                    val deltaSeconds = if (lastUptime > 0L) {
                                        ((change.uptimeMillis - lastUptime) / 1000f)
                                            .coerceIn(0f, 0.2f)
                                    } else {
                                        0f
                                    }
                                    lastUptime = change.uptimeMillis
                                    physics.dragTo(change.position.x, change.position.y, deltaSeconds)
                                    change.consume()
                                }
                            }
                        } finally {
                            val speed = physics.lastDragSpeed()
                            if (physics.draggedIndex >= 0) {
                                physics.endDrag()
                                sound.play(
                                    if (speed > 350f) SoundCue.MarbleHit else SoundCue.UiTap,
                                    settings.soundEnabled,
                                )
                                if (settings.hapticsEnabled) {
                                    if (speed > 350f) haptics.impact() else haptics.release()
                                }
                            }
                        }
                    }
                },
        ) {
            @Suppress("UNUSED_EXPRESSION")
            frame.value
            @Suppress("UNUSED_EXPRESSION")
            manualTick
            drawMarbles(physics, settings.reduceMotion)
        }
    }
}

private fun DrawScope.drawMarbles(physics: MarblesPhysics, reduceMotion: Boolean) {
    if (physics.width <= 0f || physics.height <= 0f) return
    val corner = CornerRadius(30.dp.toPx(), 30.dp.toPx())

    drawRoundRect(
        color = Surface.copy(alpha = 0.36f),
        size = Size(physics.width, physics.height),
        cornerRadius = corner,
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.055f),
        size = Size(physics.width, physics.height),
        cornerRadius = corner,
        style = Stroke(width = 1.dp.toPx()),
    )
    drawLine(
        color = Color.White.copy(alpha = 0.07f),
        start = Offset(physics.width * 0.18f, 1.dp.toPx() * 2f),
        end = Offset(physics.width * 0.50f, 1.dp.toPx() * 2f),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
    )

    for (i in 0 until MarblesPhysics.MARBLE_COUNT) {
        val m = physics.marble(i)
        val colors = MARBLE_PALETTE[m.colorIndex % MARBLE_PALETTE.size]
        val speed = hypot(m.vx, m.vy)

        if (speed > 70f) {
            drawLine(
                color = MarbleCoral.copy(alpha = if (reduceMotion) 0.08f else 0.14f),
                start = Offset(m.x - m.vx * 0.022f, m.y - m.vy * 0.022f),
                end = Offset(m.x, m.y),
                strokeWidth = m.radius * 0.55f,
                cap = StrokeCap.Round,
            )
        }

        drawCircle(
            color = Color.Black.copy(alpha = 0.24f),
            radius = m.radius * 1.01f,
            center = Offset(m.x, m.y + m.radius * 0.16f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.78f), colors.light, colors.deep),
                center = Offset(m.x - m.radius * 0.34f, m.y - m.radius * 0.38f),
                radius = m.radius * 1.55f,
            ),
            radius = m.radius,
            center = Offset(m.x, m.y),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.14f),
            radius = m.radius,
            center = Offset(m.x, m.y),
            style = Stroke(width = 1.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.62f),
            radius = m.radius * 0.19f,
            center = Offset(m.x - m.radius * 0.34f, m.y - m.radius * 0.38f),
        )

        if (i == physics.draggedIndex) {
            drawCircle(
                color = MarbleCoral.copy(alpha = 0.55f),
                radius = m.radius + 6.dp.toPx(),
                center = Offset(m.x, m.y),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
