package io.github.yeweijiehust.weatherforecast.feature.detail

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
fun WeatherDetailRoute(
    viewModel: WeatherDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeatherDetailScreen(
        uiState = uiState,
        onRetry = {},
    )
}

@Composable
fun WeatherDetailScreen(
    uiState: WeatherDetailUiState,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val state = uiState.state) {
            WeatherDetailState.Loading -> {
                Text(
                    text = localizedStringResource(R.string.detail_loading),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            is WeatherDetailState.Content -> {
                Text(
                    text = localizedStringResource(R.string.detail_title_for_city, state.city.name),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = localizedStringResource(R.string.detail_placeholder_body),
                    style = MaterialTheme.typography.bodyLarge,
                )
                AlertSection(
                    alertCount = state.alerts.size,
                    isUnavailable = false,
                )
                AirQualitySection(
                    aqi = state.airQuality?.aqi,
                    category = state.airQuality?.category,
                    isUnsupported = state.isAirQualityUnsupported,
                    isUnavailable = false,
                )
            }

            is WeatherDetailState.PartialContent -> {
                Text(
                    text = localizedStringResource(R.string.detail_title_for_city, state.city.name),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = localizedStringResource(R.string.detail_partial_content),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.unavailableSections.joinToString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AlertSection(
                    alertCount = state.alerts.size,
                    isUnavailable = WeatherDetailSection.Alerts in state.unavailableSections,
                )
                AirQualitySection(
                    aqi = state.airQuality?.aqi,
                    category = state.airQuality?.category,
                    isUnsupported = state.isAirQualityUnsupported,
                    isUnavailable = WeatherDetailSection.AirQuality in state.unavailableSections,
                )
            }

            is WeatherDetailState.ErrorNoData -> {
                Text(
                    text = localizedStringResource(R.string.detail_error_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                if (state.cityId.isNotBlank()) {
                    Text(
                        text = localizedStringResource(R.string.detail_city_id, state.cityId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = onRetry) {
                    Text(text = localizedStringResource(R.string.action_retry))
                }
            }
        }
    }
}

@Composable
private fun AlertSection(
    alertCount: Int,
    isUnavailable: Boolean,
) {
    Text(
        text = localizedStringResource(R.string.detail_alert_section_title),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = when {
            isUnavailable -> localizedStringResource(R.string.detail_alert_unavailable)
            alertCount > 0 -> localizedStringResource(R.string.detail_alert_count, alertCount)
            else -> localizedStringResource(R.string.detail_alert_empty)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AirQualitySection(
    aqi: String?,
    category: String?,
    isUnsupported: Boolean,
    isUnavailable: Boolean,
) {
    Text(
        text = localizedStringResource(R.string.detail_aqi_section_title),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = when {
            isUnavailable -> localizedStringResource(R.string.detail_aqi_unavailable)
            isUnsupported -> localizedStringResource(R.string.detail_aqi_unsupported_region)
            !aqi.isNullOrBlank() -> localizedStringResource(
                R.string.detail_aqi_value,
                aqi,
                category.orEmpty().ifBlank { "--" },
            )
            else -> localizedStringResource(R.string.detail_aqi_empty)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
