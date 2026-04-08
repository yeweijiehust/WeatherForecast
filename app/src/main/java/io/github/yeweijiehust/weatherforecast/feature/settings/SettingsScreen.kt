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
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.localization.LocalWeatherForecastContext
import io.github.yeweijiehust.weatherforecast.core.localization.localizedStringResource
import io.github.yeweijiehust.weatherforecast.core.ui.resolve
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem

@Composable
fun SettingsRoute(
    onShowMessage: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalWeatherForecastContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowMessage -> onShowMessage(event.message.resolve(context))
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
            text = localizedStringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = localizedStringResource(R.string.settings_description),
            style = MaterialTheme.typography.bodyLarge,
        )

        SettingsSection(title = localizedStringResource(R.string.settings_section_units)) {
            UnitSystem.entries.forEach { unitSystem ->
                SettingsOptionRow(
                    label = localizedStringResource(unitSystem.labelResId()),
                    selected = uiState.settings.unitSystem == unitSystem,
                    onClick = { onSelectUnitSystem(unitSystem) },
                )
            }
        }

        SettingsSection(title = localizedStringResource(R.string.settings_section_language)) {
            AppLanguage.entries.forEach { language ->
                SettingsOptionRow(
                    label = localizedStringResource(language.labelResId()),
                    selected = uiState.settings.language == language,
                    onClick = { onSelectLanguage(language) },
                )
            }
        }

        SettingsSection(title = localizedStringResource(R.string.settings_section_data)) {
            Text(
                text = localizedStringResource(R.string.settings_data_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onClearWeatherCache) {
                Text(text = localizedStringResource(R.string.settings_clear_cache))
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

private fun AppLanguage.labelResId(): Int {
    return when (this) {
        AppLanguage.English -> R.string.settings_language_english
        AppLanguage.SimplifiedChinese -> R.string.settings_language_simplified_chinese
    }
}

private fun UnitSystem.labelResId(): Int {
    return when (this) {
        UnitSystem.Metric -> R.string.settings_unit_metric
        UnitSystem.Imperial -> R.string.settings_unit_imperial
    }
}
