package dev.touchlab.app.feature.play

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlinx.coroutines.isActive

const val PHYSICS_FIXED_STEP = 1f / 120f
private const val MAX_FRAME_SECONDS = 0.05f
private const val MAX_SUBSTEPS = 6

/** True only while the screen is visible and the user has not paused the toy. */
@Composable
fun rememberSimulationRunning(paused: Boolean): Boolean {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    return !paused && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
}

/**
 * Fixed-step frame driver shared by all toys. The returned counter is meant to
 * be read inside a Canvas draw lambda so frames invalidate drawing only.
 *
 * Real frame time is clamped to 50ms and at most 6 physics steps run per frame,
 * preventing a spiral of death after backgrounding or a long jank.
 */
@Composable
fun rememberPhysicsFrameCounter(running: Boolean, step: (Float) -> Unit): State<Int> {
    val frame = remember { mutableIntStateOf(0) }
    val currentStep by rememberUpdatedState(step)

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var lastFrameNanos = 0L
        var accumulator = 0f
        while (isActive) {
            withFrameNanos { now ->
                if (lastFrameNanos == 0L) lastFrameNanos = now
                var realDt = (now - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = now
                realDt = realDt.coerceIn(0f, MAX_FRAME_SECONDS)
                accumulator += realDt
                var steps = 0
                while (accumulator >= PHYSICS_FIXED_STEP && steps < MAX_SUBSTEPS) {
                    currentStep(PHYSICS_FIXED_STEP)
                    accumulator -= PHYSICS_FIXED_STEP
                    steps++
                }
                if (steps == MAX_SUBSTEPS) accumulator = 0f
                frame.intValue++
            }
        }
    }
    return frame
}
