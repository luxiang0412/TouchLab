package dev.touchlab.app.feature.play

import androidx.compose.runtime.Composable
import dev.touchlab.app.feature.dust.DustScreen
import dev.touchlab.app.feature.jelly.JellyScreen
import dev.touchlab.app.feature.marbles.MarblesScreen
import dev.touchlab.app.feedback.SoundEngine
import dev.touchlab.app.settings.Settings
import dev.touchlab.app.ui.Toy

@Composable
fun PlayShell(
    toy: Toy,
    settings: Settings,
    sound: SoundEngine,
    externallyPaused: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    when (toy) {
        Toy.Jelly -> JellyScreen(
            settings = settings,
            sound = sound,
            externallyPaused = externallyPaused,
            onBack = onBack,
            onOpenSettings = onOpenSettings,
        )
        Toy.Dust -> DustScreen(
            settings = settings,
            sound = sound,
            externallyPaused = externallyPaused,
            onBack = onBack,
            onOpenSettings = onOpenSettings,
        )
        Toy.Marbles -> MarblesScreen(
            settings = settings,
            sound = sound,
            externallyPaused = externallyPaused,
            onBack = onBack,
            onOpenSettings = onOpenSettings,
        )
    }
}
