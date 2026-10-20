package dev.touchlab.app

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TouchLabUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun openToy(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
    }

    private fun returnHome() {
        composeRule.onNodeWithTag("play_back").performClick()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun homeShowsThreePlayableCards() {
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_jelly_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_dust_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_marbles_card").assertIsDisplayed()
        composeRule.onNode(hasText("Jelly")).assertIsDisplayed()
        composeRule.onNode(hasText("Magnet Dust")).assertIsDisplayed()
        composeRule.onNode(hasText("Marbles")).assertIsDisplayed()
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
    fun marblesOpensResetAndBack() {
        openToy("home_marbles_card")
        composeRule.onNodeWithTag("play_reset").performClick()
        composeRule.onNodeWithTag("play_screen").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun pauseResumeChangesLabel() {
        openToy("home_jelly_card")
        composeRule.onNodeWithText("Pause").assertIsDisplayed()
        composeRule.onNodeWithTag("play_pause").performClick()
        composeRule.onNodeWithText("Resume").assertIsDisplayed()
        composeRule.onNodeWithTag("play_pause").performClick()
        composeRule.onNodeWithText("Pause").assertIsDisplayed()
        returnHome()
    }

    @Test
    fun settingsTogglePersistsWithinSession() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("setting_reduce_motion").assertIsOff()
        composeRule.onNodeWithTag("setting_reduce_motion").performClick()
        composeRule.onNodeWithTag("setting_reduce_motion").assertIsOn()
        composeRule.onNodeWithTag("settings_done").performClick()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("setting_reduce_motion").assertIsOn()
        // Restore the default so other tests see a stable baseline.
        composeRule.onNodeWithTag("setting_reduce_motion").performClick()
        composeRule.onNodeWithTag("setting_reduce_motion").assertIsOff()
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
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }
}
