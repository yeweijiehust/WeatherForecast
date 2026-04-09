package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.AirQualityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetAirQualityUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runTest {
        val weatherRepository = mockk<AirQualityRepository>()
        coEvery {
            weatherRepository.fetchAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                forceRefresh = false,
            )
        } returns AirQualityFetchResult.UnsupportedRegion
        val useCase = GetAirQualityUseCase(weatherRepository)

        val result = useCase(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(AirQualityFetchResult.UnsupportedRegion)
        coVerify(exactly = 1) {
            weatherRepository.fetchAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                forceRefresh = false,
            )
        }
    }
}
