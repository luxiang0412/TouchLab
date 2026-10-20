package dev.touchlab.app.ui

import androidx.compose.ui.graphics.Color
import dev.touchlab.app.ui.theme.DustLavender
import dev.touchlab.app.ui.theme.JellyMint
import dev.touchlab.app.ui.theme.MarbleCoral

enum class Toy(
    val title: String,
    val subtitle: String,
    val number: String,
    val index: String,
    val hint: String,
    val accessibilityHint: String,
    val accent: Color,
) {
    Jelly(
        title = "Jelly",
        subtitle = "Squish it. Stretch it. Let it bounce.",
        number = "01",
        index = "01 / 03",
        hint = "Press and drag the jelly",
        accessibilityHint = "Interactive jelly toy. Touch and drag to squish and move it.",
        accent = JellyMint,
    ),
    Dust(
        title = "Magnet Dust",
        subtitle = "Gather a little universe.",
        number = "02",
        index = "02 / 03",
        hint = "Hold to pull the dust",
        accessibilityHint = "Interactive magnet dust toy. Touch and hold to gather the particles.",
        accent = DustLavender,
    ),
    Marbles(
        title = "Marbles",
        subtitle = "Flick. Bounce. Repeat.",
        number = "03",
        index = "03 / 03",
        hint = "Drag a marble and flick it",
        accessibilityHint = "Interactive marble toy. Drag and flick the marbles around the tray.",
        accent = MarbleCoral,
    ),
}
