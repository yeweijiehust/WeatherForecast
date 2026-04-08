package io.github.yeweijiehust.weatherforecast.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.usecase.ClearWeatherCacheUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveAppSettingsUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.UpdateLanguageUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.UpdateUnitSystemUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun observedSettings_areExposedInUiState() = runTest {
        val settingsFlow = MutableStateFlow(
            AppSettings(
                language = AppLanguage.SimplifiedChinese,
                unitSystem = UnitSystem.Imperial,
            ),
        )
        val viewModel = createViewModel(settingsFlow = settingsFlow)

        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.settings).isEqualTo(settingsFlow.value)
    }

    @Test
    fun selectLanguage_callsUseCase() = runTest {
        val updateLanguageUseCase = mockk<UpdateLanguageUseCase>(relaxed = true)
        val viewModel = createViewModel(updateLanguageUseCase = updateLanguageUseCase)

        viewModel.selectLanguage(AppLanguage.SimplifiedChinese)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { updateLanguageUseCase.invoke(AppLanguage.SimplifiedChinese) }
    }

    @Test
    fun selectUnitSystem_callsUseCase() = runTest {
        val updateUnitSystemUseCase = mockk<UpdateUnitSystemUseCase>(relaxed = true)
        val viewModel = createViewModel(updateUnitSystemUseCase = updateUnitSystemUseCase)

        viewModel.selectUnitSystem(UnitSystem.Imperial)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { updateUnitSystemUseCase.invoke(UnitSystem.Imperial) }
    }

    @Test
    fun clearCache_emitsSnackbarMessage() = runTest {
        val clearWeatherCacheUseCase = mockk<ClearWeatherCacheUseCase>(relaxed = true)
        val viewModel = createViewModel(clearWeatherCacheUseCase = clearWeatherCacheUseCase)

        viewModel.events.test {
            viewModel.clearWeatherCache()
            dispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { clearWeatherCacheUseCase.invoke() }
            assertThat(awaitItem()).isEqualTo(
                SettingsEvent.ShowMessage(UiText.StringResource(R.string.snackbar_cache_cleared)),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        settingsFlow: MutableStateFlow<AppSettings> = MutableStateFlow(AppSettings()),
        updateLanguageUseCase: UpdateLanguageUseCase = mockk(relaxed = true),
        updateUnitSystemUseCase: UpdateUnitSystemUseCase = mockk(relaxed = true),
        clearWeatherCacheUseCase: ClearWeatherCacheUseCase = mockk(relaxed = true),
    ): SettingsViewModel {
        val observeAppSettingsUseCase = mockk<ObserveAppSettingsUseCase>().also {
            every { it.invoke() } returns settingsFlow
        }
        return SettingsViewModel(
            observeAppSettingsUseCase = observeAppSettingsUseCase,
            updateLanguageUseCase = updateLanguageUseCase,
            updateUnitSystemUseCase = updateUnitSystemUseCase,
            clearWeatherCacheUseCase = clearWeatherCacheUseCase,
        )
    }
}
