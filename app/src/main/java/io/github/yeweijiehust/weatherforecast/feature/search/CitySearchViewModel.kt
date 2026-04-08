package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.domain.usecase.SearchCitiesUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val searchCitiesUseCase: SearchCitiesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CitySearchUiState())
    val uiState: StateFlow<CitySearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                query = query,
                resultState = CitySearchResultState.Idle,
            )
        }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.update { it.copy(resultState = CitySearchResultState.Idle) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(resultState = CitySearchResultState.Searching)
            }

            try {
                val cities = searchCitiesUseCase(query)
                _uiState.update {
                    it.copy(
                        resultState = if (cities.isEmpty()) {
                            CitySearchResultState.EmptyResult(query = query)
                        } else {
                            CitySearchResultState.Results(cities = cities)
                        },
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        resultState = CitySearchResultState.Error(
                            query = query,
                            message = SEARCH_ERROR_MESSAGE,
                        ),
                    )
                }
            }
        }
    }

    fun retry() {
        search()
    }

    private companion object {
        private const val SEARCH_ERROR_MESSAGE =
            "We couldn't search right now. Check the connection and try again."
    }
}
