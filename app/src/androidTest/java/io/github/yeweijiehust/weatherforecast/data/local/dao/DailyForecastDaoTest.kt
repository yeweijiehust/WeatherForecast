package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.yeweijiehust.weatherforecast.data.local.db.WeatherForecastDatabase
import io.github.yeweijiehust.weatherforecast.data.local.entity.DailyForecastEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyForecastDaoTest {
    private lateinit var database: WeatherForecastDatabase
    private lateinit var dailyForecastDao: DailyForecastDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherForecastDatabase::class.java,
        ).build()
        dailyForecastDao = database.dailyForecastDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceAndObserveDailyForecast_returnsSortedMatchingLanguageAndUnitOnly() = runBlocking {
        dailyForecastDao.replaceDailyForecast(
            locationId = "101020100",
            language = "en",
            unitSystem = "metric",
            dailyForecast = listOf(
                createDailyForecastEntity(
                    forecastDate = "2026-04-10",
                    language = "en",
                    unitSystem = "metric",
                ),
                createDailyForecastEntity(
                    forecastDate = "2026-04-09",
                    language = "en",
                    unitSystem = "metric",
                ),
            ),
        )
        dailyForecastDao.replaceDailyForecast(
            locationId = "101020100",
            language = "zh",
            unitSystem = "metric",
            dailyForecast = listOf(
                createDailyForecastEntity(
                    forecastDate = "2026-04-09",
                    language = "zh",
                    unitSystem = "metric",
                ),
            ),
        )

        val matching = dailyForecastDao.observeDailyForecast(
            locationId = "101020100",
            language = "en",
            unitSystem = "metric",
        ).first()
        val incompatible = dailyForecastDao.getDailyForecast(
            locationId = "101020100",
            language = "en",
            unitSystem = "imperial",
        )

        assertEquals(2, matching.size)
        assertEquals("2026-04-09", matching.first().forecastDate)
        assertEquals("2026-04-10", matching.last().forecastDate)
        assertTrue(incompatible.isEmpty())
    }

    private fun createDailyForecastEntity(
        forecastDate: String,
        language: String,
        unitSystem: String,
    ): DailyForecastEntity {
        return DailyForecastEntity(
            cityId = "101020100",
            forecastDate = forecastDate,
            tempMax = "30",
            tempMin = "22",
            conditionTextDay = "Sunny",
            conditionIconDay = "100",
            precipitationProbability = "10",
            precipitation = "0.0",
            windDirectionDay = "South",
            windScaleDay = "3",
            windSpeedDay = "16",
            fetchedAtEpochMillis = 100L,
            language = language,
            unitSystem = unitSystem,
        )
    }
}
