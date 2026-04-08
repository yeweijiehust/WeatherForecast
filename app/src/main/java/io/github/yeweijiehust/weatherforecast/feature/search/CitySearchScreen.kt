package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.localization.LocalWeatherForecastContext
import io.github.yeweijiehust.weatherforecast.core.localization.localizedStringResource
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.core.ui.resolve
import io.github.yeweijiehust.weatherforecast.domain.model.City

@Composable
fun CitySearchRoute(
    onShowMessage: (String) -> Unit,
    viewModel: CitySearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalWeatherForecastContext.current

    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is CitySearchEvent.ShowMessage -> onShowMessage(event.message.resolve(context))
            }
        }
    }

    CitySearchScreen(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::search,
        onRetry = viewModel::retry,
        onSaveCity = viewModel::saveCity,
        onUseTopCitySuggestion = viewModel::onTopCitySuggestionSelected,
        onSetDefaultCity = viewModel::setDefaultCity,
        onRemoveCity = viewModel::removeCity,
    )
}

@Composable
fun CitySearchScreen(
    uiState: CitySearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onSaveCity: (City) -> Unit,
    onUseTopCitySuggestion: (String) -> Unit,
    onSetDefaultCity: (String) -> Unit,
    onRemoveCity: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = localizedStringResource(R.string.search_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = localizedStringResource(R.string.search_description),
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = {
                    Text(text = localizedStringResource(R.string.search_field_label))
                },
                placeholder = {
                    Text(text = localizedStringResource(R.string.search_field_placeholder))
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            )
            Button(
                onClick = onSearch,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(text = localizedStringResource(R.string.action_search))
            }
        }

        HorizontalDivider()

        when (val resultState = uiState.resultState) {
            CitySearchResultState.Idle -> IdleState(
                topCitySuggestions = uiState.topCitySuggestions,
                onUseTopCitySuggestion = onUseTopCitySuggestion,
            )
            CitySearchResultState.Searching -> SearchingState()
            is CitySearchResultState.Results -> ResultsState(
                cities = resultState.cities,
                savedCityIds = uiState.savedCities.map(City::id).toSet(),
                onSaveCity = onSaveCity,
            )
            is CitySearchResultState.EmptyResult -> EmptyResultState(query = resultState.query)
            is CitySearchResultState.Error -> ErrorState(
                message = resultState.message,
                onRetry = onRetry,
            )
        }

        SavedCitiesSection(
            savedCities = uiState.savedCities,
            onSetDefaultCity = onSetDefaultCity,
            onRemoveCity = onRemoveCity,
        )
    }
}

@Composable
private fun IdleState(
    topCitySuggestions: List<City>,
    onUseTopCitySuggestion: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = localizedStringResource(R.string.search_idle_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = localizedStringResource(R.string.search_idle_support),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (topCitySuggestions.isNotEmpty()) {
            Text(
                text = localizedStringResource(R.string.search_suggestions_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = localizedStringResource(R.string.search_suggestions_support),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = topCitySuggestions,
                    key = City::id,
                ) { city ->
                    FilledTonalButton(onClick = { onUseTopCitySuggestion(city.name) }) {
                        Text(text = city.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(
            text = localizedStringResource(R.string.search_loading),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.ResultsState(
    cities: List<City>,
    savedCityIds: Set<String>,
    onSaveCity: (City) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = cities,
            key = City::id,
        ) { city ->
            CityResultCard(
                city = city,
                isSaved = city.id in savedCityIds,
                onSaveCity = onSaveCity,
            )
        }
    }
}

@Composable
private fun CityResultCard(
    city: City,
    isSaved: Boolean,
    onSaveCity: (City) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = city.disambiguationLine(),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (city.timeZone.isNotBlank()) {
                Text(
                    text = localizedStringResource(R.string.search_time_zone, city.timeZone),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = { onSaveCity(city) }) {
                Text(
                    text = localizedStringResource(
                        if (isSaved) {
                            R.string.status_saved
                        } else {
                            R.string.action_save
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmptyResultState(
    query: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = localizedStringResource(R.string.search_empty_result, query),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = localizedStringResource(R.string.search_empty_result_support),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    message: UiText,
    onRetry: () -> Unit,
) {
    val context = LocalWeatherForecastContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message.resolve(context),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onRetry) {
            Text(text = localizedStringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun SavedCitiesSection(
    savedCities: List<City>,
    onSetDefaultCity: (String) -> Unit,
    onRemoveCity: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = localizedStringResource(R.string.search_saved_cities),
            style = MaterialTheme.typography.titleMedium,
        )
        if (savedCities.isEmpty()) {
            Text(
                text = localizedStringResource(R.string.search_no_saved_cities),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        savedCities.forEach { city ->
            SavedCityCard(
                city = city,
                onSetDefaultCity = onSetDefaultCity,
                onRemoveCity = onRemoveCity,
            )
        }
    }
}

@Composable
private fun SavedCityCard(
    city: City,
    onSetDefaultCity: (String) -> Unit,
    onRemoveCity: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (city.isDefault) {
                    Text(
                        text = localizedStringResource(R.string.status_default),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = city.disambiguationLine(),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!city.isDefault) {
                    FilledTonalButton(onClick = { onSetDefaultCity(city.id) }) {
                        Text(text = localizedStringResource(R.string.search_set_default))
                    }
                }
                Button(onClick = { onRemoveCity(city.id) }) {
                    Text(text = localizedStringResource(R.string.action_remove))
                }
            }
        }
    }
}

private fun City.disambiguationLine(): String {
    return listOf(adm2, adm1, country)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(separator = ", ")
}
