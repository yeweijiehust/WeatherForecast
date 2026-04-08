package io.github.yeweijiehust.weatherforecast.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CityDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CityLookupResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QWeatherCityRepositoryTest {
    @Test
    fun searchCities_passesExpectedParametersAndMapsCities() = runTest {
        val service = mockk<GeoApiService>()
        coEvery {
            service.lookupCities(
                location = "Beijing",
                language = "en",
                number = 10,
            )
        } returns CityLookupResponseDto(
            code = "200",
            location = listOf(
                CityDto(
                    id = "101010100",
                    name = "Beijing",
                    adm1 = "Beijing",
                    adm2 = "Beijing",
                    country = "China",
                    lat = "39.90499",
                    lon = "116.40529",
                    tz = "Asia/Shanghai",
                ),
            ),
        )
        val repository = QWeatherCityRepository(
            geoApiService = service,
            qWeatherConfig = QWeatherConfig(
                apiKey = "test-key",
                apiHost = "example.com",
            ),
        )

        val cities = repository.searchCities(
            query = "Beijing",
            language = "en",
        )

        coVerify(exactly = 1) {
            service.lookupCities(
                location = "Beijing",
                language = "en",
                number = 10,
            )
        }
        assertThat(cities).hasSize(1)
        assertThat(cities.single().name).isEqualTo("Beijing")
        assertThat(cities.single().timeZone).isEqualTo("Asia/Shanghai")
        assertThat(cities.single().isDefault).isFalse()
    }

    @Test
    fun searchCities_throwsWhenApiConfigIsMissing() = runTest {
        val service = mockk<GeoApiService>()
        val repository = QWeatherCityRepository(
            geoApiService = service,
            qWeatherConfig = QWeatherConfig(apiKey = "", apiHost = ""),
        )

        val error = runCatching {
            repository.searchCities(
                query = "Beijing",
                language = "en",
            )
        }.exceptionOrNull()

        assertThat(error).isNotNull()
        assertThat(error).hasMessageThat().contains("Weather API is not configured")
        coVerify(exactly = 0) { service.lookupCities(any(), any(), any()) }
    }

    @Test
    fun searchCities_throwsWhenApiRespondsWithFailureCode() = runTest {
        val service = mockk<GeoApiService>()
        coEvery {
            service.lookupCities(
                location = "Beijing",
                language = "en",
                number = 10,
            )
        } returns CityLookupResponseDto(code = "401", location = emptyList())
        val repository = QWeatherCityRepository(
            geoApiService = service,
            qWeatherConfig = QWeatherConfig(
                apiKey = "test-key",
                apiHost = "example.com",
            ),
        )

        val error = runCatching {
            repository.searchCities(
                query = "Beijing",
                language = "en",
            )
        }.exceptionOrNull()

        assertThat(error).isNotNull()
        assertThat(error).hasMessageThat().contains("401")
        coVerify(exactly = 1) {
            service.lookupCities(
                location = "Beijing",
                language = "en",
                number = 10,
            )
        }
    }
}
