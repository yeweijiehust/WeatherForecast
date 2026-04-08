package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.localization.LocalWeatherForecastContext
import io.github.yeweijiehust.weatherforecast.core.localization.localizedStringResource
import io.github.yeweijiehust.weatherforecast.core.ui.resolve
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun HomeRoute(
    onManageCitiesClick: () -> Unit,
    onShowMessage: (message: String, actionLabel: String?, onAction: (() -> Unit)?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalWeatherForecastContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowMessage -> {
                    val actionLabel = event.action?.actionLabel?.resolve(context)
                    val onAction = when (event.action) {
                        HomeEventAction.RetryRefresh -> viewModel::onPullToRefresh
                        null -> null
                    }
                    onShowMessage(
                        event.message.resolve(context),
                        actionLabel,
                        onAction,
                    )
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onManageCitiesClick = onManageCitiesClick,
        onPullToRefresh = viewModel::onPullToRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onManageCitiesClick: () -> Unit,
    onPullToRefresh: () -> Unit,
) {
    val isRefreshing = uiState.state is HomeState.Refreshing

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onPullToRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val horizontalPadding = if (maxWidth >= 840.dp) 40.dp else 24.dp
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .widthIn(max = 1120.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (val state = uiState.state) {
                    HomeState.Uninitialized -> UninitializedState()
                    HomeState.EmptyNoCity -> EmptyState(onManageCitiesClick = onManageCitiesClick)
                    is HomeState.Loading -> LoadingState(city = state.city)
                    is HomeState.Content -> ContentState(
                        snapshot = state.snapshot,
                        isStaleCache = false,
                        isRefreshing = false,
                        onManageCitiesClick = onManageCitiesClick,
                    )
                    is HomeState.Refreshing -> ContentState(
                        snapshot = state.snapshot,
                        isStaleCache = false,
                        isRefreshing = true,
                        onManageCitiesClick = onManageCitiesClick,
                    )
                    is HomeState.ContentWithStaleCache -> ContentState(
                        snapshot = state.snapshot,
                        isStaleCache = true,
                        isRefreshing = false,
                        onManageCitiesClick = onManageCitiesClick,
                    )
                    is HomeState.ErrorNoCache -> ErrorState(
                        city = state.city,
                        onManageCitiesClick = onManageCitiesClick,
                        onRetryClick = onPullToRefresh,
                    )
                }
            }
        }
    }
}

@Composable
private fun UninitializedState() {
    Text(
        text = localizedStringResource(R.string.home_loading),
        style = MaterialTheme.typography.bodyLarge,
    )
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
    snapshot: HomeSnapshot,
    isStaleCache: Boolean,
    isRefreshing: Boolean,
    onManageCitiesClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val isExpandedWidth = maxWidth >= 840.dp
        if (isExpandedWidth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CurrentWeatherHeroCard(
                        city = snapshot.city,
                        currentWeather = snapshot.currentWeather,
                    )
                    SnapshotStatusBlock(
                        lastUpdatedEpochMillis = snapshot.lastUpdatedEpochMillis,
                        isRefreshing = isRefreshing,
                        isStaleCache = isStaleCache,
                    )
                    SecondaryMetricsBlock(currentWeather = snapshot.currentWeather)
                }
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HourlyForecastSection(hourlyForecast = snapshot.hourlyForecast)
                    DailyForecastSection(dailyForecast = snapshot.dailyForecast)
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CurrentWeatherHeroCard(
                    city = snapshot.city,
                    currentWeather = snapshot.currentWeather,
                )
                SnapshotStatusBlock(
                    lastUpdatedEpochMillis = snapshot.lastUpdatedEpochMillis,
                    isRefreshing = isRefreshing,
                    isStaleCache = isStaleCache,
                )
                SecondaryMetricsBlock(currentWeather = snapshot.currentWeather)
                HourlyForecastSection(hourlyForecast = snapshot.hourlyForecast)
                DailyForecastSection(dailyForecast = snapshot.dailyForecast)
            }
        }
    }
    Button(onClick = onManageCitiesClick) {
        Text(text = localizedStringResource(R.string.home_manage_saved_cities))
    }
}

@Composable
private fun SnapshotStatusBlock(
    lastUpdatedEpochMillis: Long,
    isRefreshing: Boolean,
    isStaleCache: Boolean,
) {
    Text(
        text = localizedStringResource(
            R.string.home_last_updated,
            lastUpdatedEpochMillis.formattedLastUpdatedTime(),
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (isRefreshing) {
        Text(
            text = localizedStringResource(R.string.home_refreshing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    if (isStaleCache) {
        Text(
            text = localizedStringResource(R.string.home_stale_cache),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ErrorState(
    city: City,
    onManageCitiesClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Text(
        text = city.name,
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        text = localizedStringResource(R.string.home_error_no_cache),
        style = MaterialTheme.typography.bodyLarge,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onRetryClick) {
            Text(text = localizedStringResource(R.string.action_retry))
        }
        Button(onClick = onManageCitiesClick) {
            Text(text = localizedStringResource(R.string.home_manage_saved_cities))
        }
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

@Composable
private fun SecondaryMetricsBlock(
    currentWeather: CurrentWeather,
) {
    val windValue = listOf(
        currentWeather.windDirection,
        currentWeather.windScale,
        currentWeather.windSpeed,
    ).filter { it.isNotBlank() }.joinToString(separator = " / ").ifBlank { "-" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = localizedStringResource(R.string.home_label_humidity),
                value = "${currentWeather.humidity}%",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = localizedStringResource(R.string.home_label_wind),
                value = windValue,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = localizedStringResource(R.string.home_label_precipitation),
                value = currentWeather.precipitation,
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = localizedStringResource(R.string.home_label_visibility),
                value = currentWeather.visibility,
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HourlyForecastSection(
    hourlyForecast: List<HourlyForecast>,
) {
    Text(
        text = localizedStringResource(R.string.home_hourly_title),
        style = MaterialTheme.typography.titleLarge,
    )
    if (hourlyForecast.isEmpty()) {
        Text(
            text = localizedStringResource(R.string.home_hourly_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 8.dp),
    ) {
        items(
            items = hourlyForecast.take(24),
            key = { item -> item.forecastTime },
        ) { hour ->
            HourlyForecastCard(hourlyForecast = hour)
        }
    }
}

@Composable
private fun HourlyForecastCard(
    hourlyForecast: HourlyForecast,
) {
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = hourlyForecast.formattedForecastTime(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = localizedStringResource(
                    R.string.home_hero_temperature,
                    hourlyForecast.temperature,
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = hourlyForecast.conditionText,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = localizedStringResource(
                    R.string.home_hourly_pop,
                    hourlyForecast.precipitationProbability,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DailyForecastSection(
    dailyForecast: List<DailyForecast>,
) {
    Text(
        text = localizedStringResource(R.string.home_daily_title),
        style = MaterialTheme.typography.titleLarge,
    )
    if (dailyForecast.isEmpty()) {
        Text(
            text = localizedStringResource(R.string.home_daily_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dailyForecast.take(7).forEach { forecast ->
            DailyForecastCard(dailyForecast = forecast)
        }
    }
}

@Composable
private fun DailyForecastCard(
    dailyForecast: DailyForecast,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = dailyForecast.formattedForecastDate(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = dailyForecast.conditionTextDay,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = localizedStringResource(
                    R.string.home_daily_temp_range,
                    dailyForecast.tempMax,
                    dailyForecast.tempMin,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = localizedStringResource(
                    R.string.home_daily_pop,
                    dailyForecast.precipitationProbability,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private fun HourlyForecast.formattedForecastTime(): String {
    return runCatching {
        OffsetDateTime.parse(forecastTime).format(
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault()),
        )
    }.getOrDefault(forecastTime)
}

private fun DailyForecast.formattedForecastDate(): String {
    return runCatching {
        LocalDate.parse(forecastDate).format(
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()),
        )
    }.getOrDefault(forecastDate)
}

private fun Long.formattedLastUpdatedTime(): String {
    return runCatching {
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime().format(
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault()),
        )
    }.getOrDefault(this.toString())
}
