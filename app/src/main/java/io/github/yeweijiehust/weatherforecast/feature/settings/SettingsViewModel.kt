package io.github.yeweijiehust.weatherforecast.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.usecase.ClearWeatherCacheUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveAppSettingsUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.UpdateLanguageUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.UpdateUnitSystemUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAppSettingsUseCase: ObserveAppSettingsUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateUnitSystemUseCase: UpdateUnitSystemUseCase,
    private val clearWeatherCacheUseCase: ClearWeatherCacheUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeAppSettingsUseCase().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            updateLanguageUseCase(language)
        }
    }

    fun selectUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            updateUnitSystemUseCase(unitSystem)
        }
    }

    fun clearWeatherCache() {
        viewModelScope.launch {
            clearWeatherCacheUseCase()
            _events.emit(SettingsEvent.ShowMessage("Cached weather cleared."))
        }
    }
}
