package dev.touchlab.app.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import dev.touchlab.app.R
import dev.touchlab.app.ui.theme.DustLavender
import dev.touchlab.app.ui.theme.JellyMint
import dev.touchlab.app.ui.theme.MarbleCoral

enum class Toy(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val number: String,
    val index: String,
    @StringRes val hintRes: Int,
    @StringRes val accessibilityHintRes: Int,
    val accent: Color,
) {
    Jelly(
        titleRes = R.string.toy_title_jelly,
        subtitleRes = R.string.toy_subtitle_jelly,
        number = "01",
        index = "01 / 03",
        hintRes = R.string.toy_hint_jelly,
        accessibilityHintRes = R.string.toy_a11y_jelly,
        accent = JellyMint,
    ),
    Dust(
        titleRes = R.string.toy_title_dust,
        subtitleRes = R.string.toy_subtitle_dust,
        number = "02",
        index = "02 / 03",
        hintRes = R.string.toy_hint_dust,
        accessibilityHintRes = R.string.toy_a11y_dust,
        accent = DustLavender,
    ),
    Marbles(
        titleRes = R.string.toy_title_marbles,
        subtitleRes = R.string.toy_subtitle_marbles,
        number = "03",
        index = "03 / 03",
        hintRes = R.string.toy_hint_marbles,
        accessibilityHintRes = R.string.toy_a11y_marbles,
        accent = MarbleCoral,
    ),
}