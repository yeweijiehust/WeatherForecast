package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.localization.localizedStringResource
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast

@Composable
fun WeatherDetailRoute(
    viewModel: WeatherDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WeatherDetailScreen(
        uiState = uiState,
        onRetryHourly = viewModel::retryHourlySection,
        onRetryDaily = viewModel::retryDailySection,
        onRetryAlerts = viewModel::retryAlertsSection,
        onRetryAirQuality = viewModel::retryAirQualitySection,
        onRetryAll = {
            viewModel.retryHourlySection()
            viewModel.retryDailySection()
            viewModel.retryAlertsSection()
            viewModel.retryAirQualitySection()
        },
    )
}

@Composable
fun WeatherDetailScreen(
    uiState: WeatherDetailUiState,
    onRetryHourly: () -> Unit,
    onRetryDaily: () -> Unit,
    onRetryAlerts: () -> Unit,
    onRetryAirQuality: () -> Unit,
    onRetryAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                DetailSections(
                    state = state,
                    unavailableSections = emptySet(),
                    onRetryHourly = onRetryHourly,
                    onRetryDaily = onRetryDaily,
                    onRetryAlerts = onRetryAlerts,
                    onRetryAirQuality = onRetryAirQuality,
                )
            }

            is WeatherDetailState.PartialContent -> {
                Text(
                    text = localizedStringResource(R.string.detail_partial_content),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DetailSections(
                    state = state,
                    unavailableSections = state.unavailableSections,
                    onRetryHourly = onRetryHourly,
                    onRetryDaily = onRetryDaily,
                    onRetryAlerts = onRetryAlerts,
                    onRetryAirQuality = onRetryAirQuality,
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
                Button(onClick = onRetryAll) {
                    Text(text = localizedStringResource(R.string.action_retry))
                }
            }
        }
    }
}

@Composable
private fun DetailSections(
    state: WeatherDetailState,
    unavailableSections: Set<WeatherDetailSection>,
    onRetryHourly: () -> Unit,
    onRetryDaily: () -> Unit,
    onRetryAlerts: () -> Unit,
    onRetryAirQuality: () -> Unit,
) {
    val cityName = when (state) {
        is WeatherDetailState.Content -> state.city.name
        is WeatherDetailState.PartialContent -> state.city.name
        WeatherDetailState.Loading -> ""
        is WeatherDetailState.ErrorNoData -> ""
    }
    Text(
        text = localizedStringResource(R.string.detail_title_for_city, cityName),
        style = MaterialTheme.typography.headlineSmall,
    )

    when (state) {
        is WeatherDetailState.Content -> {
            HourlySection(
                hourlyForecast = state.hourlyForecast,
                isUnavailable = false,
                onRetry = onRetryHourly,
            )
            DailySection(
                dailyForecast = state.dailyForecast,
                isUnavailable = false,
                onRetry = onRetryDaily,
            )
            AlertSection(
                alertCount = state.alerts.size,
                isUnavailable = false,
                onRetry = onRetryAlerts,
            )
            AirQualitySection(
                aqi = state.airQuality?.aqi,
                category = state.airQuality?.category,
                isUnsupported = state.isAirQualityUnsupported,
                isUnavailable = false,
                onRetry = onRetryAirQuality,
            )
        }

        is WeatherDetailState.PartialContent -> {
            HourlySection(
                hourlyForecast = state.hourlyForecast,
                isUnavailable = WeatherDetailSection.HourlyForecast in unavailableSections,
                onRetry = onRetryHourly,
            )
            DailySection(
                dailyForecast = state.dailyForecast,
                isUnavailable = WeatherDetailSection.DailyForecast in unavailableSections,
                onRetry = onRetryDaily,
            )
            AlertSection(
                alertCount = state.alerts.size,
                isUnavailable = WeatherDetailSection.Alerts in unavailableSections,
                onRetry = onRetryAlerts,
            )
            AirQualitySection(
                aqi = state.airQuality?.aqi,
                category = state.airQuality?.category,
                isUnsupported = state.isAirQualityUnsupported,
                isUnavailable = WeatherDetailSection.AirQuality in unavailableSections,
                onRetry = onRetryAirQuality,
            )
        }

        else -> Unit
    }
}

@Composable
private fun HourlySection(
    hourlyForecast: List<HourlyForecast>,
    isUnavailable: Boolean,
    onRetry: () -> Unit,
) {
    Text(
        text = localizedStringResource(R.string.detail_hourly_title),
        style = MaterialTheme.typography.titleMedium,
    )
    when {
        isUnavailable -> SectionUnavailable(
            message = localizedStringResource(R.string.detail_hourly_unavailable),
            onRetry = onRetry,
        )

        hourlyForecast.isEmpty() -> {
            Text(
                text = localizedStringResource(R.string.detail_hourly_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        else -> {
            hourlyForecast.forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = localizedStringResource(
                            R.string.detail_hourly_item,
                            item.forecastTime,
                            item.temperature,
                            item.conditionText,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailySection(
    dailyForecast: List<DailyForecast>,
    isUnavailable: Boolean,
    onRetry: () -> Unit,
) {
    Text(
        text = localizedStringResource(R.string.detail_daily_title),
        style = MaterialTheme.typography.titleMedium,
    )
    when {
        isUnavailable -> SectionUnavailable(
            message = localizedStringResource(R.string.detail_daily_unavailable),
            onRetry = onRetry,
        )

        dailyForecast.isEmpty() -> {
            Text(
                text = localizedStringResource(R.string.detail_daily_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        else -> {
            dailyForecast.forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 1.dp,
                ) {
                    Text(
                        text = localizedStringResource(
                            R.string.detail_daily_item,
                            item.forecastDate,
                            item.tempMax,
                            item.tempMin,
                            item.conditionTextDay,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertSection(
    alertCount: Int,
    isUnavailable: Boolean,
    onRetry: () -> Unit,
) {
    Text(
        text = localizedStringResource(R.string.detail_alert_section_title),
        style = MaterialTheme.typography.titleMedium,
    )
    if (isUnavailable) {
        SectionUnavailable(
            message = localizedStringResource(R.string.detail_alert_unavailable),
            onRetry = onRetry,
        )
        return
    }
    Text(
        text = if (alertCount > 0) {
            localizedStringResource(R.string.detail_alert_count, alertCount)
        } else {
            localizedStringResource(R.string.detail_alert_empty)
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
    onRetry: () -> Unit,
) {
    Text(
        text = localizedStringResource(R.string.detail_aqi_section_title),
        style = MaterialTheme.typography.titleMedium,
    )
    if (isUnavailable) {
        SectionUnavailable(
            message = localizedStringResource(R.string.detail_aqi_unavailable),
            onRetry = onRetry,
        )
        return
    }
    Text(
        text = when {
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

@Composable
private fun SectionUnavailable(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = onRetry) {
            Text(text = localizedStringResource(R.string.action_retry))
        }
    }
}
