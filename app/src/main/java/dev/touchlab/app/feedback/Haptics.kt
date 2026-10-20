package dev.touchlab.app.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Thin wrapper around [View.performHapticFeedback] so every toy uses the same
 * restrained vocabulary. Callers must gate calls with the user setting.
 */
class Haptics(private val view: View) {

    fun press() {
        perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun release() {
        perform(
            if (Build.VERSION.SDK_INT >= 30) {
                HapticFeedbackConstants.GESTURE_END
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            },
        )
    }

    fun impact() {
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
