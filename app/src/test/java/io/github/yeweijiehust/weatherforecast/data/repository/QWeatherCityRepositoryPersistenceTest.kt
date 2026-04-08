package io.github.yeweijiehust.weatherforecast.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.model.SavedCityLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QWeatherCityRepositoryPersistenceTest {
    @Test
    fun saveCity_returnsDuplicateWhenLocationAlreadyExists() = runTest {
        val localDataSource = FakeSavedCityLocalDataSource(
            initialCities = listOf(
                SavedCityLocalModel(
                    locationId = "101010100",
                    name = "Beijing",
                    adm1 = "Beijing",
                    adm2 = "Beijing",
                    country = "China",
                    lat = "39.90",
                    lon = "116.40",
                    timeZone = "Asia/Shanghai",
                    sortOrder = 0,
                    createdAtEpochMillis = 1L,
                ),
            ),
        )
        val repository = createRepository(localDataSource = localDataSource)

        val result = repository.saveCity(sampleCity(id = "101010100", name = "Beijing"))

        assertThat(result).isEqualTo(SaveCityResult.Duplicate)
        assertThat(localDataSource.observeSavedCities().first()).hasSize(1)
    }

    @Test
    fun saveCity_setsFirstSavedCityAsDefault() = runTest {
        val defaultCityPreferences = FakeDefaultCityPreferencesDataSource()
        val repository = createRepository(
            defaultCityPreferencesDataSource = defaultCityPreferences,
        )

        val result = repository.saveCity(sampleCity(id = "101020100", name = "Shanghai"))

        assertThat(result).isEqualTo(SaveCityResult.Saved)
        assertThat(defaultCityPreferences.getDefaultCityId()).isEqualTo("101020100")
        assertThat(repository.observeDefaultCity().first()?.id).isEqualTo("101020100")
    }

    @Test
    fun removeCity_promotesLowestSortOrderWhenDefaultIsDeleted() = runTest {
        val localDataSource = FakeSavedCityLocalDataSource(
            initialCities = listOf(
                savedLocalCity(locationId = "101010100", sortOrder = 0, createdAt = 1L),
                savedLocalCity(locationId = "101020100", sortOrder = 1, createdAt = 2L),
                savedLocalCity(locationId = "101280101", sortOrder = 2, createdAt = 3L),
            ),
        )
        val defaultCityPreferences = FakeDefaultCityPreferencesDataSource(
            initialDefaultCityId = "101010100",
        )
        val repository = createRepository(
            localDataSource = localDataSource,
            defaultCityPreferencesDataSource = defaultCityPreferences,
        )

        repository.removeCity("101010100")

        assertThat(defaultCityPreferences.getDefaultCityId()).isEqualTo("101020100")
        assertThat(repository.observeSavedCities().first().map(City::id))
            .containsExactly("101020100", "101280101")
            .inOrder()
    }

    @Test
    fun removeCity_clearsDefaultWhenNoCitiesRemain() = runTest {
        val localDataSource = FakeSavedCityLocalDataSource(
            initialCities = listOf(
                savedLocalCity(locationId = "101010100", sortOrder = 0, createdAt = 1L),
            ),
        )
        val defaultCityPreferences = FakeDefaultCityPreferencesDataSource(
            initialDefaultCityId = "101010100",
        )
        val repository = createRepository(
            localDataSource = localDataSource,
            defaultCityPreferencesDataSource = defaultCityPreferences,
        )

        repository.removeCity("101010100")

        assertThat(defaultCityPreferences.getDefaultCityId()).isNull()
        assertThat(repository.observeDefaultCity().first()).isNull()
    }

    @Test
    fun setDefaultCity_updatesObservedSavedCities() = runTest {
        val localDataSource = FakeSavedCityLocalDataSource(
            initialCities = listOf(
                savedLocalCity(locationId = "101010100", sortOrder = 0, createdAt = 1L),
                savedLocalCity(
                    locationId = "101020100",
                    name = "Shanghai",
                    sortOrder = 1,
                    createdAt = 2L,
                ),
            ),
        )
        val defaultCityPreferences = FakeDefaultCityPreferencesDataSource(
            initialDefaultCityId = "101010100",
        )
        val repository = createRepository(
            localDataSource = localDataSource,
            defaultCityPreferencesDataSource = defaultCityPreferences,
        )

        repository.setDefaultCity("101020100")

        val savedCities = repository.observeSavedCities().first()
        assertThat(savedCities.first { it.id == "101010100" }.isDefault).isFalse()
        assertThat(savedCities.first { it.id == "101020100" }.isDefault).isTrue()
        assertThat(repository.observeDefaultCity().first()?.id).isEqualTo("101020100")
    }

    private fun createRepository(
        localDataSource: SavedCityLocalDataSource = FakeSavedCityLocalDataSource(),
        defaultCityPreferencesDataSource: DefaultCityPreferencesDataSource =
            FakeDefaultCityPreferencesDataSource(),
    ): QWeatherCityRepository {
        return QWeatherCityRepository(
            geoApiService = mockk<GeoApiService>(),
            qWeatherConfig = QWeatherConfig(
                apiKey = "test-key",
                apiHost = "example.com",
            ),
            savedCityLocalDataSource = localDataSource,
            defaultCityPreferencesDataSource = defaultCityPreferencesDataSource,
        )
    }

    private fun sampleCity(
        id: String,
        name: String,
    ): City {
        return City(
            id = id,
            name = name,
            adm1 = name,
            adm2 = name,
            country = "China",
            lat = "0",
            lon = "0",
            timeZone = "Asia/Shanghai",
        )
    }

    private fun savedLocalCity(
        locationId: String,
        name: String = "Beijing",
        sortOrder: Int,
        createdAt: Long,
    ): SavedCityLocalModel {
        return SavedCityLocalModel(
            locationId = locationId,
            name = name,
            adm1 = name,
            adm2 = name,
            country = "China",
            lat = "0",
            lon = "0",
            timeZone = "Asia/Shanghai",
            sortOrder = sortOrder,
            createdAtEpochMillis = createdAt,
        )
    }

    private class FakeSavedCityLocalDataSource(
        initialCities: List<SavedCityLocalModel> = emptyList(),
    ) : SavedCityLocalDataSource {
        private val citiesFlow = MutableStateFlow(initialCities.sortedBy(SavedCityLocalModel::sortOrder))

        override fun observeSavedCities(): Flow<List<SavedCityLocalModel>> = citiesFlow

        override suspend fun getSavedCities(): List<SavedCityLocalModel> = citiesFlow.value

        override suspend fun getSavedCity(locationId: String): SavedCityLocalModel? {
            return citiesFlow.value.firstOrNull { it.locationId == locationId }
        }

        override suspend fun insertCity(city: SavedCityLocalModel): Boolean {
            if (citiesFlow.value.any { it.locationId == city.locationId }) {
                return false
            }
            citiesFlow.value = (citiesFlow.value + city).sortedBy(SavedCityLocalModel::sortOrder)
            return true
        }

        override suspend fun deleteCity(locationId: String) {
            citiesFlow.value = citiesFlow.value.filterNot { it.locationId == locationId }
        }

        override suspend fun nextSortOrder(): Int {
            return (citiesFlow.value.maxOfOrNull(SavedCityLocalModel::sortOrder) ?: -1) + 1
        }
    }

    private class FakeDefaultCityPreferencesDataSource(
        initialDefaultCityId: String? = null,
    ) : DefaultCityPreferencesDataSource {
        private val defaultCityIdFlow = MutableStateFlow(initialDefaultCityId)

        override fun observeDefaultCityId(): Flow<String?> = defaultCityIdFlow

        override suspend fun getDefaultCityId(): String? = defaultCityIdFlow.value

        override suspend fun setDefaultCityId(cityId: String?) {
            defaultCityIdFlow.value = cityId
        }
    }
}
