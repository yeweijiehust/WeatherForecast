package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetWeatherIndicesUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runTest {
        val weatherRepository = mockk<WeatherRepository>()
        coEvery {
            weatherRepository.fetchWeatherIndices(locationId = "101020100")
        } returns WeatherIndicesFetchResult.Empty
        val useCase = GetWeatherIndicesUseCase(weatherRepository)

        val result = useCase(locationId = "101020100")

        assertThat(result).isEqualTo(WeatherIndicesFetchResult.Empty)
        coVerify(exactly = 1) {
            weatherRepository.fetchWeatherIndices(locationId = "101020100")
        }
    }
}
