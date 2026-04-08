package io.github.yeweijiehust.weatherforecast.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.model.SavedCityLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CityDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CityLookupResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.TopCityResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QWeatherCityRepositoryTest {
    @Test
    fun fetchTopCities_passesExpectedParametersAndMapsCities() = runTest {
        val service = mockk<GeoApiService>()
        coEvery {
            service.topCities(
                language = "en",
                number = 8,
            )
        } returns TopCityResponseDto(
            code = "200",
            topCityList = listOf(
                CityDto(
                    id = "101020100",
                    name = "Shanghai",
                    adm1 = "Shanghai",
                    adm2 = "Shanghai",
                    country = "China",
                    lat = "31.23",
                    lon = "121.47",
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
            savedCityLocalDataSource = EmptySavedCityLocalDataSource,
            defaultCityPreferencesDataSource = EmptyDefaultCityPreferencesDataSource,
        )

        val cities = repository.fetchTopCities(
            language = "en",
            number = 8,
        )

        coVerify(exactly = 1) {
            service.topCities(
                language = "en",
                number = 8,
            )
        }
        assertThat(cities).hasSize(1)
        assertThat(cities.single().id).isEqualTo("101020100")
        assertThat(cities.single().name).isEqualTo("Shanghai")
    }

    @Test
    fun fetchTopCities_throwsWhenApiRespondsWithFailureCode() = runTest {
        val service = mockk<GeoApiService>()
        coEvery {
            service.topCities(
                language = "en",
                number = 10,
            )
        } returns TopCityResponseDto(code = "401", topCityList = emptyList())
        val repository = QWeatherCityRepository(
            geoApiService = service,
            qWeatherConfig = QWeatherConfig(
                apiKey = "test-key",
                apiHost = "example.com",
            ),
            savedCityLocalDataSource = EmptySavedCityLocalDataSource,
            defaultCityPreferencesDataSource = EmptyDefaultCityPreferencesDataSource,
        )

        val error = runCatching {
            repository.fetchTopCities(
                language = "en",
                number = 10,
            )
        }.exceptionOrNull()

        assertThat(error).isNotNull()
        assertThat(error).hasMessageThat().contains("401")
        coVerify(exactly = 1) {
            service.topCities(
                language = "en",
                number = 10,
            )
        }
    }

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
            savedCityLocalDataSource = EmptySavedCityLocalDataSource,
            defaultCityPreferencesDataSource = EmptyDefaultCityPreferencesDataSource,
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
            savedCityLocalDataSource = EmptySavedCityLocalDataSource,
            defaultCityPreferencesDataSource = EmptyDefaultCityPreferencesDataSource,
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
            savedCityLocalDataSource = EmptySavedCityLocalDataSource,
            defaultCityPreferencesDataSource = EmptyDefaultCityPreferencesDataSource,
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

    private object EmptySavedCityLocalDataSource : SavedCityLocalDataSource {
        override fun observeSavedCities(): Flow<List<SavedCityLocalModel>> = emptyFlow()

        override suspend fun getSavedCities(): List<SavedCityLocalModel> = emptyList()

        override suspend fun getSavedCity(locationId: String): SavedCityLocalModel? = null

        override suspend fun insertCity(city: SavedCityLocalModel): Boolean = true

        override suspend fun deleteCity(locationId: String) = Unit

        override suspend fun nextSortOrder(): Int = 0
    }

    private object EmptyDefaultCityPreferencesDataSource : DefaultCityPreferencesDataSource {
        override fun observeDefaultCityId(): Flow<String?> = emptyFlow()

        override suspend fun getDefaultCityId(): String? = null

        override suspend fun setDefaultCityId(cityId: String?) = Unit
    }
}
