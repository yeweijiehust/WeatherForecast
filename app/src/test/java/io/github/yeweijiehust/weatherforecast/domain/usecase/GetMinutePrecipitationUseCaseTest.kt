package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetMinutePrecipitationUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runTest {
        val weatherRepository = mockk<WeatherRepository>()
        coEvery {
            weatherRepository.fetchMinutePrecipitation(
                latitude = "31.23",
                longitude = "121.47",
                forceRefresh = false,
            )
        } returns MinutePrecipitationFetchResult.UnsupportedRegion
        val useCase = GetMinutePrecipitationUseCase(weatherRepository)

        val result = useCase(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(MinutePrecipitationFetchResult.UnsupportedRegion)
        coVerify(exactly = 1) {
            weatherRepository.fetchMinutePrecipitation(
                latitude = "31.23",
                longitude = "121.47",
                forceRefresh = false,
            )
        }
    }
}
