package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.remote.dto.HourlyForecastDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import org.junit.Test

class HourlyForecastMapperTest {
    @Test
    fun dtoToLocalAndDomain_preservesHourlyForecastFields() {
        val dto = HourlyForecastDto(
            forecastTime = "2026-04-08T16:00+08:00",
            temperature = "24",
            conditionText = "Cloudy",
            conditionIcon = "101",
            precipitationProbability = "20",
            precipitation = "0.0",
            windDirection = "South",
            windScale = "2",
            windSpeed = "13",
        )

        val localModel = dto.toLocalModel(
            cityId = "101020100",
            fetchedAtEpochMillis = 1234L,
            language = "en",
            unitSystem = "metric",
        )
        val domainModel = localModel.toDomain()

        assertThat(localModel.cityId).isEqualTo("101020100")
        assertThat(localModel.conditionText).isEqualTo("Cloudy")
        assertThat(localModel.precipitationProbability).isEqualTo("20")
        assertThat(localModel.language).isEqualTo("en")
        assertThat(localModel.unitSystem).isEqualTo("metric")
        assertThat(domainModel.cityId).isEqualTo("101020100")
        assertThat(domainModel.forecastTime).isEqualTo("2026-04-08T16:00+08:00")
        assertThat(domainModel.temperature).isEqualTo("24")
        assertThat(domainModel.conditionIcon).isEqualTo("101")
        assertThat(domainModel.fetchedAtEpochMillis).isEqualTo(1234L)
    }
}
