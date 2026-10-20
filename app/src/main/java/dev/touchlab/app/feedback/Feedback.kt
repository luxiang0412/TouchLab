package dev.touchlab.app.feedback

import dev.touchlab.app.settings.Settings

/** Shared, rate-limited feedback for buttons, cards and settings toggles. */
fun uiFeedback(sound: SoundEngine, settings: Settings, haptics: Haptics) {
    sound.play(SoundCue.UiTap, settings.soundEnabled)
    if (settings.hapticsEnabled) haptics.press()
}
