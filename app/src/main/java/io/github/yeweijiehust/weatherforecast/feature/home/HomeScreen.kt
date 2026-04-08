package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.localization.localizedStringResource

@Composable
fun HomeRoute(
    onManageCitiesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onManageCitiesClick = onManageCitiesClick,
        onSettingsClick = onSettingsClick,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onManageCitiesClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.defaultCity == null) {
            Text(
                text = localizedStringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = localizedStringResource(R.string.home_empty_body),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onManageCitiesClick) {
                Text(text = localizedStringResource(R.string.home_add_first_city))
            }
        } else {
            Text(
                text = localizedStringResource(R.string.home_current_city_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = uiState.defaultCity.name,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = listOf(
                    uiState.defaultCity.adm1,
                    uiState.defaultCity.country,
                ).filter { it.isNotBlank() }.distinct().joinToString(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onManageCitiesClick) {
                Text(text = localizedStringResource(R.string.home_manage_saved_cities))
            }
        }
        Button(onClick = onSettingsClick) {
            Text(text = localizedStringResource(R.string.home_open_settings))
        }
    }
}
