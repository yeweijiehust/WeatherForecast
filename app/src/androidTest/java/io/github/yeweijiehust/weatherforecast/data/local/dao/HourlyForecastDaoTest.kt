package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.yeweijiehust.weatherforecast.data.local.db.WeatherForecastDatabase
import io.github.yeweijiehust.weatherforecast.data.local.entity.HourlyForecastEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HourlyForecastDaoTest {
    private lateinit var database: WeatherForecastDatabase
    private lateinit var hourlyForecastDao: HourlyForecastDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherForecastDatabase::class.java,
        ).build()
        hourlyForecastDao = database.hourlyForecastDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceAndObserveHourlyForecast_returnsSortedMatchingLanguageAndUnitOnly() = runBlocking {
        hourlyForecastDao.replaceHourlyForecast(
            locationId = "101020100",
            language = "en",
            unitSystem = "metric",
            hourlyForecast = listOf(
                createHourlyForecastEntity(
                    forecastTime = "2026-04-08T17:00+08:00",
                    language = "en",
                    unitSystem = "metric",
                ),
                createHourlyForecastEntity(
                    forecastTime = "2026-04-08T16:00+08:00",
                    language = "en",
                    unitSystem = "metric",
                ),
            ),
        )
        hourlyForecastDao.replaceHourlyForecast(
            locationId = "101020100",
            language = "zh",
            unitSystem = "metric",
            hourlyForecast = listOf(
                createHourlyForecastEntity(
                    forecastTime = "2026-04-08T16:00+08:00",
                    language = "zh",
                    unitSystem = "metric",
                ),
            ),
        )

        val matching = hourlyForecastDao.observeHourlyForecast(
            locationId = "101020100",
            language = "en",
            unitSystem = "metric",
        ).first()
        val incompatible = hourlyForecastDao.getHourlyForecast(
            locationId = "101020100",
            language = "en",
            unitSystem = "imperial",
        )

        assertEquals(2, matching.size)
        assertEquals("2026-04-08T16:00+08:00", matching.first().forecastTime)
        assertEquals("2026-04-08T17:00+08:00", matching.last().forecastTime)
        assertTrue(incompatible.isEmpty())
    }

    private fun createHourlyForecastEntity(
        forecastTime: String,
        language: String,
        unitSystem: String,
    ): HourlyForecastEntity {
        return HourlyForecastEntity(
            cityId = "101020100",
            forecastTime = forecastTime,
            temperature = "24",
            conditionText = "Cloudy",
            conditionIcon = "101",
            precipitationProbability = "20",
            precipitation = "0.0",
            windDirection = "South",
            windScale = "2",
            windSpeed = "13",
            fetchedAtEpochMillis = 100L,
            language = language,
            unitSystem = unitSystem,
        )
    }
}
