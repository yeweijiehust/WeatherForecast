package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveCurrentWeatherUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDefaultCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshCurrentWeatherUseCase
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeDefaultCityUseCase: ObserveDefaultCityUseCase,
    private val observeCurrentWeatherUseCase: ObserveCurrentWeatherUseCase,
    private val refreshCurrentWeatherUseCase: RefreshCurrentWeatherUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentWeatherObservationJob: Job? = null

    init {
        viewModelScope.launch {
            observeDefaultCityUseCase().collectLatest { city ->
                currentWeatherObservationJob?.cancel()
                when (city) {
                    null -> _uiState.value = HomeUiState(state = HomeState.EmptyNoCity)
                    else -> observeCityWeather(city)
                }
            }
        }
    }

    private fun observeCityWeather(city: City) {
        _uiState.value = HomeUiState(state = HomeState.Loading(city))
        currentWeatherObservationJob = viewModelScope.launch {
            observeCurrentWeatherUseCase(city.id).collect { currentWeather ->
                if (currentWeather != null) {
                    _uiState.value = HomeUiState(
                        state = HomeState.Content(
                            city = city,
                            currentWeather = currentWeather,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            runCatching {
                refreshCurrentWeatherUseCase(city.id)
            }.onFailure {
                if (!hasContentFor(city.id)) {
                    _uiState.value = HomeUiState(state = HomeState.ErrorNoCache(city))
                }
            }
        }
    }

    private fun hasContentFor(cityId: String): Boolean {
        val state = _uiState.value.state
        return state is HomeState.Content && state.city.id == cityId
    }
}
