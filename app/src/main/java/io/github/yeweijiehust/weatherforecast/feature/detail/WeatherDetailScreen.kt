package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
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
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert

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
                alerts = state.alerts,
                isUnavailable = false,
                onRetry = onRetryAlerts,
            )
            AirQualitySection(
                airQuality = state.airQuality,
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
                alerts = state.alerts,
                isUnavailable = WeatherDetailSection.Alerts in unavailableSections,
                onRetry = onRetryAlerts,
            )
            AirQualitySection(
                airQuality = state.airQuality,
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
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = localizedStringResource(
                                R.string.detail_hourly_item,
                                item.forecastTime,
                                item.temperature,
                                item.conditionText,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = localizedStringResource(
                                R.string.detail_hourly_precip,
                                item.precipitation,
                                item.precipitationProbability,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = localizedStringResource(
                                R.string.detail_hourly_wind,
                                item.windDirection,
                                item.windScale,
                                item.windSpeed,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
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
                        )
                        Text(
                            text = localizedStringResource(
                                R.string.detail_daily_precip,
                                item.precipitation,
                                item.precipitationProbability,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = localizedStringResource(
                                R.string.detail_daily_wind,
                                item.windDirectionDay,
                                item.windScaleDay,
                                item.windSpeedDay,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertSection(
    alerts: List<WeatherAlert>,
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
    if (alerts.isEmpty()) {
        Text(
            text = localizedStringResource(R.string.detail_alert_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Text(
        text = localizedStringResource(R.string.detail_alert_count, alerts.size),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    alerts.forEach { alert ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = alert.title.ifBlank { alert.typeName.ifBlank { localizedStringResource(R.string.detail_fallback_dash) } },
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = localizedStringResource(
                        R.string.detail_alert_meta,
                        alert.severity.ifBlank { localizedStringResource(R.string.detail_fallback_dash) },
                        alert.status.ifBlank { localizedStringResource(R.string.detail_fallback_dash) },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = localizedStringResource(
                        R.string.detail_alert_time,
                        alert.startTime.ifBlank { localizedStringResource(R.string.detail_fallback_dash) },
                        alert.endTime.ifBlank { localizedStringResource(R.string.detail_fallback_dash) },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (alert.sender.isNotBlank()) {
                    Text(
                        text = localizedStringResource(R.string.detail_alert_sender, alert.sender),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (alert.text.isNotBlank()) {
                    Text(
                        text = alert.text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AirQualitySection(
    airQuality: AirQuality?,
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
            !airQuality?.aqi.isNullOrBlank() -> localizedStringResource(
                R.string.detail_aqi_value,
                airQuality.aqi,
                airQuality.category.ifBlank {
                    localizedStringResource(R.string.detail_fallback_dash)
                },
            )

            else -> localizedStringResource(R.string.detail_aqi_empty)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (airQuality == null || isUnsupported || isUnavailable) {
        return
    }
    Text(
        text = localizedStringResource(
            R.string.detail_aqi_primary,
            airQuality.primary.ifBlank { localizedStringResource(R.string.detail_fallback_dash) },
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = localizedStringResource(R.string.detail_aqi_pollutants_title),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PollutantMetricCard(
            modifier = Modifier.weight(1f),
            label = "PM2.5",
            value = airQuality.pm2p5,
        )
        PollutantMetricCard(
            modifier = Modifier.weight(1f),
            label = "PM10",
            value = airQuality.pm10,
        )
        PollutantMetricCard(
            modifier = Modifier.weight(1f),
            label = "NO2",
            value = airQuality.no2,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PollutantMetricCard(
            modifier = Modifier.weight(1f),
            label = "SO2",
            value = airQuality.so2,
        )
        PollutantMetricCard(
            modifier = Modifier.weight(1f),
            label = "CO",
            value = airQuality.co,
        )
        PollutantMetricCard(
            modifier = Modifier.weight(1f),
            label = "O3",
            value = airQuality.o3,
        )
    }
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

@Composable
private fun PollutantMetricCard(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.ifBlank { localizedStringResource(R.string.detail_fallback_dash) },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
