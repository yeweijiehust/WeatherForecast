package io.github.yeweijiehust.weatherforecast

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityShellTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_showsHomeScaffold() {
        assertAnyDisplayed("Home", "首页")
        assertAnyDisplayed("No city selected yet.", "还没有选择城市。")
        assertAnyDisplayed("Search Cities", "搜索城市")
        assertAnyDisplayed("Settings", "设置")
    }

    @Test
    fun navigationShell_allowsMovingBetweenScreens() {
        composeTestRule.onNodeWithText(existingText("Search Cities", "搜索城市")).performClick()
        assertAnyDisplayed("Find a city", "查找城市")

        composeTestRule.onNodeWithText(existingText("Back", "返回")).performClick()
        assertAnyDisplayed("No city selected yet.", "还没有选择城市。")

        composeTestRule.onNodeWithText(existingText("Settings", "设置")).performClick()
        assertAnyDisplayed(
            "Control units, language, and cache settings.",
            "管理单位、语言和缓存设置。",
        )
    }

    @Test
    fun changingLanguage_updatesVisibleUiWithoutRestartFlow() {
        composeTestRule.onNodeWithText(existingText("Settings", "设置")).performClick()
        if (hasAnyText("管理单位、语言和缓存设置。")) {
            composeTestRule.onNodeWithText(existingText("English", "英语")).performClick()
            assertAnyDisplayed("Control units, language, and cache settings.")
        }
        composeTestRule.onNodeWithText(existingText("Simplified Chinese", "简体中文")).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            hasAnyText("管理单位、语言和缓存设置。")
        }

        composeTestRule.onNodeWithText("管理单位、语言和缓存设置。").assertIsDisplayed()
        composeTestRule.onNodeWithText("返回").assertIsDisplayed()
    }

    private fun assertAnyDisplayed(vararg texts: String) {
        composeTestRule.onNodeWithText(existingText(*texts)).assertIsDisplayed()
    }

    private fun hasAnyText(vararg texts: String): Boolean {
        return texts.any { text ->
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun existingText(vararg texts: String): String {
        return texts.firstOrNull { text ->
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        } ?: error("None of the expected texts were found: ${texts.joinToString()}")
    }
}
