package io.github.yeweijiehust.weatherforecast.core.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailNavigationFlowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigateDetail_thenBack_returnsToHome() {
        composeTestRule.setContent {
            TestDetailNavHost()
        }

        composeTestRule.onNodeWithText("Open detail").performClick()
        composeTestRule.onNodeWithText("Detail city: 101020100").assertIsDisplayed()

        composeTestRule.onNodeWithText("Back to home").performClick()
        composeTestRule.onNodeWithText("Open detail").assertIsDisplayed()
    }
}

@Composable
private fun TestDetailNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = WeatherForecastDestination.Home.route,
    ) {
        composable(WeatherForecastDestination.Home.route) {
            Button(
                onClick = {
                    navController.navigate(
                        WeatherForecastDestination.Detail.routeForCity("101020100"),
                    )
                },
            ) {
                Text("Open detail")
            }
        }
        composable(
            route = WeatherForecastDestination.Detail.route,
            arguments = listOf(navArgument(WeatherForecastDestination.Detail.cityIdArg) {}),
        ) { backStackEntry ->
            val cityId =
                backStackEntry.arguments?.getString(WeatherForecastDestination.Detail.cityIdArg)
                    .orEmpty()
            Button(onClick = navController::navigateUp) {
                Text("Back to home")
            }
            Text("Detail city: $cityId")
        }
    }
}
