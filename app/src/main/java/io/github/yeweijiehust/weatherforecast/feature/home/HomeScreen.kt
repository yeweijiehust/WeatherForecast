package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
        when (val state = uiState.state) {
            HomeState.EmptyNoCity -> EmptyState(onManageCitiesClick = onManageCitiesClick)
            is HomeState.Loading -> LoadingState(city = state.city)
            is HomeState.Content -> ContentState(
                city = state.city,
                currentWeather = state.currentWeather,
                onManageCitiesClick = onManageCitiesClick,
            )
            is HomeState.ErrorNoCache -> ErrorState(
                city = state.city,
                onManageCitiesClick = onManageCitiesClick,
            )
        }
        Button(onClick = onSettingsClick) {
            Text(text = localizedStringResource(R.string.home_open_settings))
        }
    }
}

@Composable
private fun EmptyState(
    onManageCitiesClick: () -> Unit,
) {
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
}

@Composable
private fun LoadingState(
    city: City,
) {
    Text(
        text = localizedStringResource(R.string.home_current_city_title),
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        text = city.name,
        style = MaterialTheme.typography.headlineMedium,
    )
    Text(
        text = localizedStringResource(R.string.home_loading),
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun ContentState(
    city: City,
    currentWeather: CurrentWeather,
    onManageCitiesClick: () -> Unit,
) {
    CurrentWeatherHeroCard(
        city = city,
        currentWeather = currentWeather,
    )
    SecondaryMetricsBlock(currentWeather = currentWeather)
    Button(onClick = onManageCitiesClick) {
        Text(text = localizedStringResource(R.string.home_manage_saved_cities))
    }
}

@Composable
private fun ErrorState(
    city: City,
    onManageCitiesClick: () -> Unit,
) {
    Text(
        text = city.name,
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        text = localizedStringResource(R.string.home_error_no_cache),
        style = MaterialTheme.typography.bodyLarge,
    )
    Button(onClick = onManageCitiesClick) {
        Text(text = localizedStringResource(R.string.home_manage_saved_cities))
    }
}

@Composable
private fun CurrentWeatherHeroCard(
    city: City,
    currentWeather: CurrentWeather,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = cityRegionLine(city),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = localizedStringResource(
                        R.string.home_hero_temperature,
                        currentWeather.temperature,
                    ),
                    style = MaterialTheme.typography.displaySmall,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = currentWeather.conditionText,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = localizedStringResource(
                            R.string.home_feels_like,
                            currentWeather.feelsLike,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = localizedStringResource(
                            R.string.home_observed_at,
                            currentWeather.formattedObservationTime(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SecondaryMetricsBlock(
    currentWeather: CurrentWeather,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 2,
    ) {
        MetricCard(
            label = localizedStringResource(R.string.home_label_humidity),
            value = "${currentWeather.humidity}%",
        )
        MetricCard(
            label = localizedStringResource(R.string.home_label_wind),
            value = listOf(
                currentWeather.windDirection,
                currentWeather.windScale,
                currentWeather.windSpeed,
            ).filter { it.isNotBlank() }.joinToString(separator = " / "),
        )
        MetricCard(
            label = localizedStringResource(R.string.home_label_precipitation),
            value = currentWeather.precipitation,
        )
        MetricCard(
            label = localizedStringResource(R.string.home_label_visibility),
            value = currentWeather.visibility,
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun cityRegionLine(city: City): String {
    return listOf(city.adm1, city.country)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString()
}

private fun CurrentWeather.formattedObservationTime(): String {
    return runCatching {
        OffsetDateTime.parse(observationTime).format(
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault()),
        )
    }.getOrDefault(observationTime)
}
