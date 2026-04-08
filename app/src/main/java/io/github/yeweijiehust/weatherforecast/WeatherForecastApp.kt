package io.github.yeweijiehust.weatherforecast

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.localization.localizedStringResource
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.feature.home.HomeRoute
import io.github.yeweijiehust.weatherforecast.feature.search.CitySearchRoute
import io.github.yeweijiehust.weatherforecast.feature.settings.SettingsRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun WeatherForecastApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination =
        WeatherForecastDestination.fromRoute(backStackEntry?.destination?.route)

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        navigationSuiteItems = {
            WeatherForecastDestination.entries.forEach { destination ->
                item(
                    selected = destination == currentDestination,
                    onClick = {
                        navController.navigateToTopLevelDestination(destination)
                    },
                    icon = {
                        Text(
                            text = when (destination) {
                                WeatherForecastDestination.Home -> "H"
                                WeatherForecastDestination.Search -> "C"
                                WeatherForecastDestination.Settings -> "S"
                            }
                        )
                    },
                    label = { Text(text = localizedStringResource(destination.titleResId)) },
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = localizedStringResource(currentDestination.titleResId)) },
                    navigationIcon = {
                        if (currentDestination != WeatherForecastDestination.Home) {
                            TextButton(onClick = navController::navigateUp) {
                                Text(text = localizedStringResource(R.string.action_back))
                            }
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = WeatherForecastDestination.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(WeatherForecastDestination.Home.route) {
                    HomeRoute(
                        onManageCitiesClick = {
                            navController.navigateToTopLevelDestination(WeatherForecastDestination.Search)
                        },
                        onShowMessage = { message, actionLabel, onAction ->
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = actionLabel,
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onAction?.invoke()
                                }
                            }
                        },
                    )
                }
                composable(WeatherForecastDestination.Search.route) {
                    CitySearchRoute(
                        onShowMessage = { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                    )
                }
                composable(WeatherForecastDestination.Settings.route) {
                    SettingsRoute(
                        onShowMessage = { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun NavHostController.navigateToTopLevelDestination(
    destination: WeatherForecastDestination,
) {
    navigate(destination.route) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
