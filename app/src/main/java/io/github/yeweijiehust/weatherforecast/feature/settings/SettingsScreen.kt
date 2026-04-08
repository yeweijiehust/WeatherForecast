package io.github.yeweijiehust.weatherforecast.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem

@Composable
fun SettingsRoute(
    onShowMessage: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowMessage -> onShowMessage(event.message)
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        onSelectLanguage = viewModel::selectLanguage,
        onSelectUnitSystem = viewModel::selectUnitSystem,
        onClearWeatherCache = viewModel::clearWeatherCache,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectUnitSystem: (UnitSystem) -> Unit,
    onClearWeatherCache: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Control units, language, and cache settings.",
            style = MaterialTheme.typography.bodyLarge,
        )

        SettingsSection(title = "Units") {
            UnitSystem.entries.forEach { unitSystem ->
                SettingsOptionRow(
                    label = unitSystem.displayName,
                    selected = uiState.settings.unitSystem == unitSystem,
                    onClick = { onSelectUnitSystem(unitSystem) },
                )
            }
        }

        SettingsSection(title = "Language") {
            AppLanguage.entries.forEach { language ->
                SettingsOptionRow(
                    label = language.displayName,
                    selected = uiState.settings.language == language,
                    onClick = { onSelectLanguage(language) },
                )
            }
        }

        SettingsSection(title = "Data") {
            Text(
                text = "Clears cached weather data only. Saved cities and settings stay intact.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onClearWeatherCache) {
                Text(text = "Clear cached weather")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

@Composable
private fun SettingsOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
