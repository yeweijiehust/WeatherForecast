package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RemoveSavedCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.SaveCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.SearchCitiesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.SetDefaultCityUseCase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val observeSavedCitiesUseCase: ObserveSavedCitiesUseCase,
    private val saveCityUseCase: SaveCityUseCase,
    private val setDefaultCityUseCase: SetDefaultCityUseCase,
    private val removeSavedCityUseCase: RemoveSavedCityUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CitySearchUiState())
    val uiState: StateFlow<CitySearchUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<CitySearchEvent>()
    val events: SharedFlow<CitySearchEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            observeSavedCitiesUseCase().collect { savedCities ->
                _uiState.update { state ->
                    state.copy(savedCities = savedCities)
                }
            }
        }
    }

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
                            message = UiText.StringResource(R.string.search_error_generic),
                        ),
                    )
                }
            }
        }
    }

    fun retry() {
        search()
    }

    fun saveCity(city: City) {
        viewModelScope.launch {
            val result = saveCityUseCase(city)
            val message = when (result) {
                SaveCityResult.Saved -> UiText.StringResource(R.string.snackbar_city_saved)
                SaveCityResult.Duplicate -> UiText.StringResource(R.string.snackbar_city_already_saved)
            }
            _events.emit(CitySearchEvent.ShowMessage(message))
        }
    }

    fun setDefaultCity(cityId: String) {
        viewModelScope.launch {
            setDefaultCityUseCase(cityId)
            _events.emit(
                CitySearchEvent.ShowMessage(
                    UiText.StringResource(R.string.snackbar_default_city_updated),
                ),
            )
        }
    }

    fun removeCity(cityId: String) {
        viewModelScope.launch {
            removeSavedCityUseCase(cityId)
            _events.emit(
                CitySearchEvent.ShowMessage(
                    UiText.StringResource(R.string.snackbar_city_removed),
                ),
            )
        }
    }
}
