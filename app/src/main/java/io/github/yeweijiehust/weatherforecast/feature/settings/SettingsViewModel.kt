package io.github.yeweijiehust.weatherforecast.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
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
            runCatching {
                updateLanguageUseCase(language)
            }.onFailure {
                _events.emit(
                    SettingsEvent.ShowMessage(
                        UiText.StringResource(R.string.snackbar_operation_failed_try_again),
                    ),
                )
            }
        }
    }

    fun selectUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            runCatching {
                updateUnitSystemUseCase(unitSystem)
            }.onFailure {
                _events.emit(
                    SettingsEvent.ShowMessage(
                        UiText.StringResource(R.string.snackbar_operation_failed_try_again),
                    ),
                )
            }
        }
    }

    fun clearWeatherCache() {
        viewModelScope.launch {
            runCatching {
                clearWeatherCacheUseCase()
            }.onSuccess {
                _events.emit(
                    SettingsEvent.ShowMessage(
                        UiText.StringResource(R.string.snackbar_cache_cleared),
                    ),
                )
            }.onFailure {
                _events.emit(
                    SettingsEvent.ShowMessage(
                        UiText.StringResource(R.string.snackbar_operation_failed_try_again),
                    ),
                )
            }
        }
    }
}
