package io.github.yeweijiehust.weatherforecast

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityShellTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_showsHomeScaffold() {
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Project foundation is ready.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search Cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun navigationShell_allowsMovingBetweenScreens() {
        composeTestRule.onNodeWithText("Search Cities").performClick()
        composeTestRule.onNodeWithText("Find a city").assertIsDisplayed()

        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.onNodeWithText("Project foundation is ready.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings screen shell").assertIsDisplayed()
    }
}
