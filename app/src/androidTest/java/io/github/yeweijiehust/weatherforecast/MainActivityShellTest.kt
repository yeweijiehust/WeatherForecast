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
        composeTestRule.onNodeWithText("Manage Cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun navigationShell_allowsMovingBetweenScreens() {
        composeTestRule.onNodeWithText("Manage Cities").performClick()
        composeTestRule.onNodeWithText("Search screen shell").assertIsDisplayed()

        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.onNodeWithText("Project foundation is ready.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings screen shell").assertIsDisplayed()
    }
}
