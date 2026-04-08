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
