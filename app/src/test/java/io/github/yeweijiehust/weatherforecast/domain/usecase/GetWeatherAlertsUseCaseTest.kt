package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetWeatherAlertsUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runTest {
        val repository = mockk<WeatherRepository>()
        coEvery {
            repository.fetchWeatherAlerts(
                latitude = "31.23",
                longitude = "121.47",
                forceRefresh = false,
            )
        } returns WeatherAlertFetchResult.Empty
        val useCase = GetWeatherAlertsUseCase(repository)

        val result = useCase(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(WeatherAlertFetchResult.Empty)
        coVerify(exactly = 1) {
            repository.fetchWeatherAlerts(
                latitude = "31.23",
                longitude = "121.47",
                forceRefresh = false,
            )
        }
    }
}
