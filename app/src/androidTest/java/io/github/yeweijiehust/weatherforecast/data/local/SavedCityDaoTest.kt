package io.github.yeweijiehust.weatherforecast.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.yeweijiehust.weatherforecast.data.local.db.WeatherForecastDatabase
import io.github.yeweijiehust.weatherforecast.data.local.entity.SavedCityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedCityDaoTest {
    private lateinit var database: WeatherForecastDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            WeatherForecastDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndObserveSavedCities_returnsCitiesInSortOrder() = runBlocking {
        val dao = database.savedCityDao()
        dao.insertCity(savedCityEntity(locationId = "101020100", sortOrder = 1, createdAt = 2L))
        dao.insertCity(savedCityEntity(locationId = "101010100", sortOrder = 0, createdAt = 1L))

        val savedCities = dao.observeSavedCities().first()

        assertEquals(listOf("101010100", "101020100"), savedCities.map(SavedCityEntity::locationId))
    }

    @Test
    fun insertCity_ignoresDuplicateLocationId() = runBlocking {
        val dao = database.savedCityDao()
        val firstInsert = dao.insertCity(savedCityEntity(locationId = "101010100", sortOrder = 0, createdAt = 1L))
        val duplicateInsert = dao.insertCity(savedCityEntity(locationId = "101010100", sortOrder = 1, createdAt = 2L))

        assertTrue(firstInsert > 0L)
        assertEquals(-1L, duplicateInsert)
        assertEquals(1, dao.observeSavedCities().first().size)
    }

    private fun savedCityEntity(
        locationId: String,
        sortOrder: Int,
        createdAt: Long,
    ): SavedCityEntity {
        return SavedCityEntity(
            locationId = locationId,
            name = "City",
            adm1 = "Region",
            adm2 = "Subregion",
            country = "Country",
            lat = "0",
            lon = "0",
            timeZone = "Asia/Shanghai",
            sortOrder = sortOrder,
            createdAtEpochMillis = createdAt,
        )
    }
}
