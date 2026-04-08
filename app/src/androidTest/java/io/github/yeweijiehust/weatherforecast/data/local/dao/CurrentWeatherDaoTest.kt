package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.yeweijiehust.weatherforecast.data.local.db.WeatherForecastDatabase
import io.github.yeweijiehust.weatherforecast.data.local.entity.CurrentWeatherEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrentWeatherDaoTest {
    private lateinit var database: WeatherForecastDatabase
    private lateinit var currentWeatherDao: CurrentWeatherDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherForecastDatabase::class.java,
        ).build()
        currentWeatherDao = database.currentWeatherDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertAndObserveCurrentWeather_returnsMatchingLanguageAndUnitOnly() = runBlocking {
        currentWeatherDao.upsertCurrentWeather(
            CurrentWeatherEntity(
                cityId = "101020100",
                observationTime = "2026-04-08T13:45+08:00",
                temperature = "26",
                feelsLike = "28",
                conditionText = "Sunny",
                conditionIcon = "100",
                humidity = "65",
                windDirection = "East",
                windScale = "3",
                windSpeed = "15",
                precipitation = "0.0",
                pressure = "1012",
                visibility = "16",
                fetchedAtEpochMillis = 100L,
                language = "en",
                unitSystem = "metric",
            ),
        )

        val matching = currentWeatherDao.observeCurrentWeather(
            locationId = "101020100",
            language = "en",
            unitSystem = "metric",
        ).first()
        val incompatible = currentWeatherDao.getCurrentWeather(
            locationId = "101020100",
            language = "zh",
            unitSystem = "metric",
        )

        assertEquals("Sunny", matching?.conditionText)
        assertNull(incompatible)
    }
}
