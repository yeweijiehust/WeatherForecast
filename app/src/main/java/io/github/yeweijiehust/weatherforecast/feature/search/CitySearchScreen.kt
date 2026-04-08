package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.yeweijiehust.weatherforecast.domain.model.City

@Composable
fun CitySearchRoute(
    viewModel: CitySearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CitySearchScreen(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::search,
        onRetry = viewModel::retry,
    )
}

@Composable
fun CitySearchScreen(
    uiState: CitySearchUiState,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Find a city",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Search by city name to see clear, disambiguated results.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Saved city management arrives in the next step.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(text = "City name")
                },
                placeholder = {
                    Text(text = "Search for a city")
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            )
            Button(
                onClick = onSearch,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(text = "Search")
            }
        }

        HorizontalDivider()

        when (val resultState = uiState.resultState) {
            CitySearchResultState.Idle -> IdleState()
            CitySearchResultState.Searching -> SearchingState()
            is CitySearchResultState.Results -> ResultsState(cities = resultState.cities)
            is CitySearchResultState.EmptyResult -> EmptyResultState(query = resultState.query)
            is CitySearchResultState.Error -> ErrorState(
                message = resultState.message,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun IdleState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Type a city name to start searching.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Results will show city, region, country, and time zone when available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            text = "Searching cities...",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.ResultsState(
    cities: List<City>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = cities,
            key = City::id,
        ) { city ->
            CityResultCard(city = city)
        }
    }
}

@Composable
private fun CityResultCard(
    city: City,
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
                    text = "Time zone: ${city.timeZone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            text = "No cities found for \"$query\".",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Try a broader spelling or include a larger nearby city.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

private fun City.disambiguationLine(): String {
    return listOf(adm2, adm1, country)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(separator = ", ")
}
