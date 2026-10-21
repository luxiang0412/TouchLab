package dev.touchlab.app

import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.touchlab.app.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TouchLabUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(@StringRes id: Int): String = composeRule.activity.getString(id)

    private fun openToy(tag: String) {
        // Physics toys animate every frame, so the shared test clock must not
        // auto-advance or waitForIdle would never settle. Advance manually.
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.mainClock.advanceTimeBy(600)
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
    }

    private fun returnHome() {
        composeRule.onNodeWithTag("play_back").performClick()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun homeShowsThreePlayableCards() {
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_jelly_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_dust_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_marbles_card").assertIsDisplayed()
        composeRule.onNode(hasText(text(R.string.toy_title_jelly))).assertIsDisplayed()
        composeRule.onNode(hasText(text(R.string.toy_title_dust))).assertIsDisplayed()
        composeRule.onNode(hasText(text(R.string.toy_title_marbles))).assertIsDisplayed()
    }

    @Test
    fun jellyOpensAndBackReturnsHome() {
        openToy("home_jelly_card")
        composeRule.onNodeWithTag("play_back").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun dustOpensAndResetDoesNotCrash() {
        openToy("home_dust_card")
        composeRule.onNodeWithTag("play_reset").performClick()
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun marblesGravityToggleAndReset() {
        openToy("home_marbles_card")
        composeRule.onNodeWithText(text(R.string.gravity_on)).assertIsDisplayed()
        composeRule.onNodeWithTag("marbles_gravity_toggle").performClick()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText(text(R.string.gravity_off)).assertIsDisplayed()
        composeRule.onNodeWithTag("play_reset").performClick()
        composeRule.onNodeWithText(text(R.string.gravity_off)).assertIsDisplayed()
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun marblesCanvasAcceptsDragGesture() {
        openToy("home_marbles_card")
        composeRule.onNodeWithTag("play_canvas").performTouchInput {
            down(center)
            moveBy(Offset(0f, -180f))
            moveBy(Offset(120f, -80f))
            up()
        }
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun pauseResumeChangesLabel() {
        openToy("home_jelly_card")
        composeRule.onNodeWithText(text(R.string.pause)).assertIsDisplayed()
        composeRule.onNodeWithTag("play_pause").performClick()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText(text(R.string.resume)).assertIsDisplayed()
        composeRule.onNodeWithTag("play_pause").performClick()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText(text(R.string.pause)).assertIsDisplayed()
        returnHome()
    }

    private fun isSwitchOn(tag: String): Boolean {
        val config = composeRule.onNodeWithTag(tag).fetchSemanticsNode().config
        return config[SemanticsProperties.ToggleableState] == ToggleableState.On
    }

    @Test
    fun allThreeSettingsToggleAndPersistWithinSession() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()

        listOf("setting_sound", "setting_haptic", "setting_reduce_motion").forEach { tag ->
            val initial = isSwitchOn(tag)
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.waitForIdle()
            check(isSwitchOn(tag) != initial) { "$tag did not toggle" }
            // Restore so the test leaves no surprising state behind.
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.waitForIdle()
            check(isSwitchOn(tag) == initial) { "$tag did not restore" }
        }

        val beforeClose = isSwitchOn("setting_reduce_motion")
        composeRule.onNodeWithTag("setting_reduce_motion").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_done").performClick()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        check(isSwitchOn("setting_reduce_motion") != beforeClose) {
            "setting did not survive closing and reopening the sheet"
        }
        composeRule.onNodeWithTag("setting_reduce_motion").performClick()
        composeRule.waitForIdle()
        check(isSwitchOn("setting_reduce_motion") == beforeClose)
        composeRule.onNodeWithTag("settings_done").performClick()
    }

    @Test
    fun canvasAcceptsDownMoveUpGesture() {
        openToy("home_jelly_card")
        composeRule.onNodeWithTag("play_canvas").performTouchInput {
            down(center)
            moveBy(Offset(140f, -90f))
            moveBy(Offset(-60f, 40f))
            up()
        }
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun systemBackReturnsHome() {
        openToy("home_marbles_card")
        Espresso.pressBack()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun languageSwitchWorksAndIsReversible() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("setting_language_zh").performClick()
        // The activity recreates; the sheet comes back in Chinese.
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        composeRule.onNode(hasText("软糖")).assertIsDisplayed()
        // Back to the system language again.
        composeRule.onNodeWithTag("setting_language_system").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_done").performClick()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }
}
