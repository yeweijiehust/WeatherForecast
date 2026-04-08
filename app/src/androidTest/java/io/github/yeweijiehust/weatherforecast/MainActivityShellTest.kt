package io.github.yeweijiehust.weatherforecast

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityShellTest {
    companion object {
        private const val WAIT_TIMEOUT_MS = 15_000L
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launch_showsHomeScaffold() {
        assertAnyDisplayed("Home", "首页")
        assertAnyHomeStateVisible()
        assertAnyDisplayed("Search Cities", "搜索城市")
        assertAnyDisplayed("Settings", "设置")
    }

    @Test
    fun navigationShell_allowsMovingBetweenScreens() {
        composeTestRule.onNodeWithText(existingText("Search Cities", "搜索城市")).performClick()
        assertAnyDisplayed("Find a city", "查找城市")

        composeTestRule.onNodeWithText(existingText("Back", "返回")).performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) { hasAnyHomeStateText() }
        assertAnyHomeStateVisible()

        composeTestRule.onNodeWithText(existingText("Settings", "设置")).performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
            hasAnyText(
                "Control units, language, and cache settings.",
                "管理单位、语言和缓存设置。",
            )
        }
        assertAnyDisplayed(
            "Control units, language, and cache settings.",
            "管理单位、语言和缓存设置。",
        )
    }

    @Test
    fun settingsClearCache_showsSnackbarMessage() {
        composeTestRule.onNodeWithText(existingText("Settings", "设置")).performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
            hasAnyText(
                "Control units, language, and cache settings.",
                "管理单位、语言和缓存设置。",
            )
        }
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
            hasAnyText("Clear cached weather", "清除天气缓存")
        }
        composeTestRule.onNodeWithText(
            existingText("Clear cached weather", "清除天气缓存"),
        ).performClick()

        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
            hasAnyText("Cached weather cleared.", "已清除天气缓存。")
        }
        assertAnyDisplayed("Cached weather cleared.", "已清除天气缓存。")
    }

    @Test
    fun changingLanguage_updatesVisibleUiWithoutRestartFlow() {
        composeTestRule.onNodeWithText(existingText("Settings", "设置")).performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
            hasAnyText(
                "Control units, language, and cache settings.",
                "管理单位、语言和缓存设置。",
            )
        }
        if (hasAnyText("管理单位、语言和缓存设置。")) {
            composeTestRule.onNodeWithText(existingText("English", "英语")).performClick()
            composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
                hasAnyText("Control units, language, and cache settings.")
            }
            assertAnyDisplayed("Control units, language, and cache settings.")
        }
        composeTestRule.onNodeWithText(existingText("Simplified Chinese", "简体中文")).performClick()
        composeTestRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MS) {
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

    private fun hasAnyHomeStateText(): Boolean {
        return hasAnyText(
            "No city selected yet.",
            "还没有选择城市。",
            "Add your first city",
            "添加第一个城市",
            "Manage saved cities",
            "管理已保存城市",
            "Loading weather…",
            "正在加载天气…",
        )
    }

    private fun assertAnyHomeStateVisible() {
        assertAnyDisplayed(
            "No city selected yet.",
            "还没有选择城市。",
            "Add your first city",
            "添加第一个城市",
            "Manage saved cities",
            "管理已保存城市",
            "Loading weather…",
            "正在加载天气…",
        )
    }

    private fun existingText(vararg texts: String): String {
        return texts.firstOrNull { text ->
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        } ?: error("None of the expected texts were found: ${texts.joinToString()}")
    }
}
