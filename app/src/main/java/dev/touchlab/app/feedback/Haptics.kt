package dev.touchlab.app.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Thin wrapper around [View.performHapticFeedback] so every toy uses the same
 * restrained vocabulary. Callers must gate calls with the user setting.
 */
class Haptics(private val view: View) {

    private var lastPressAt = 0L
    private var lastImpactAt = 0L

    fun press() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPressAt < 55L) return
        lastPressAt = now
        perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun release() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPressAt < 55L) return
        lastPressAt = now
        perform(
            if (Build.VERSION.SDK_INT >= 30) {
                HapticFeedbackConstants.GESTURE_END
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            },
        )
    }

    /** Rate-limited so collision-heavy frames cannot buzz continuously. */
    fun impact() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastImpactAt < 130L) return
        lastImpactAt = now
        perform(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    private fun perform(constant: Int) {
        view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}
