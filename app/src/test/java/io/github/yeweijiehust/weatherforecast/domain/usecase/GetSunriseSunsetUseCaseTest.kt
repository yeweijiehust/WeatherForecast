package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetSunriseSunsetUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runTest {
        val weatherRepository = mockk<WeatherRepository>()
        coEvery {
            weatherRepository.fetchSunriseSunset(
                locationId = "101020100",
                date = "20260409",
            )
        } returns SunriseSunsetFetchResult.Failure(reason = SunriseSunsetFailureReason.Unknown)
        val useCase = GetSunriseSunsetUseCase(weatherRepository)

        val result = useCase(locationId = "101020100", date = "20260409")

        assertThat(result).isEqualTo(
            SunriseSunsetFetchResult.Failure(
                reason = SunriseSunsetFailureReason.Unknown,
            ),
        )
        coVerify(exactly = 1) {
            weatherRepository.fetchSunriseSunset(
                locationId = "101020100",
                date = "20260409",
            )
        }
    }
}
