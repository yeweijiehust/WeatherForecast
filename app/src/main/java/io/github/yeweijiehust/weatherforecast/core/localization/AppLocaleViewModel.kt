package io.github.yeweijiehust.weatherforecast.core.localization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveAppSettingsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppLocaleViewModel @Inject constructor(
    observeAppSettingsUseCase: ObserveAppSettingsUseCase,
) : ViewModel() {
    val appLanguage: StateFlow<AppLanguage> = observeAppSettingsUseCase()
        .map { it.language }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = AppLanguage.English,
        )
}
