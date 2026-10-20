package dev.touchlab.app.feature.play

import dev.touchlab.app.feature.jelly.JellyScreen
import dev.touchlab.app.settings.Settings
import dev.touchlab.app.ui.Toy
import dev.touchlab.app.ui.components.DustIllustration
import dev.touchlab.app.ui.components.JellyIllustration
import dev.touchlab.app.ui.components.MarblesIllustration
import dev.touchlab.app.ui.components.PlayScaffold
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun PlayShell(
    toy: Toy,
    settings: Settings,
    externallyPaused: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (toy == Toy.Jelly) {
        JellyScreen(
            settings = settings,
            externallyPaused = externallyPaused,
            onBack = onBack,
            onOpenSettings = onOpenSettings,
        )
        return
    }
    PlaceholderToy(toy, settings, externallyPaused, onBack, onOpenSettings)
}

/**
 * Temporary Phase 1 shell for Dust and Marbles, replaced by their physics
 * screens in Phases 3-4.
 */
@Composable
private fun PlaceholderToy(
    toy: Toy,
    settings: Settings,
    externallyPaused: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var paused by rememberSaveable(toy) { mutableStateOf(false) }
    var resetCount by rememberSaveable(toy) { mutableIntStateOf(0) }

    PlayScaffold(
        toy = toy,
        paused = paused || externallyPaused,
        reduceMotion = settings.reduceMotion,
        onBack = onBack,
        onTogglePause = { paused = !paused },
        onReset = { resetCount++ },
        onSettings = onOpenSettings,
    ) { notifyInteraction ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("play_canvas")
                .pointerInput(Unit) { detectTapGestures { notifyInteraction() } },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 24.dp),
            ) {
                when (toy) {
                    Toy.Jelly -> JellyIllustration(Modifier.fillMaxSize())
                    Toy.Dust -> DustIllustration(Modifier.fillMaxSize())
                    Toy.Marbles -> MarblesIllustration(Modifier.fillMaxSize())
                }
            }
        }
    }
}
