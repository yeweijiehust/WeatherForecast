package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import org.junit.Test

class CurrentWeatherMapperTest {
    @Test
    fun dtoToLocalAndDomain_preservesCurrentWeatherFields() {
        val dto = CurrentWeatherDto(
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
        )

        val localModel = dto.toLocalModel(
            cityId = "101020100",
            fetchedAtEpochMillis = 1234L,
            language = "en",
            unitSystem = "metric",
        )
        val domainModel = localModel.toDomain()

        assertThat(localModel.cityId).isEqualTo("101020100")
        assertThat(localModel.conditionText).isEqualTo("Sunny")
        assertThat(localModel.language).isEqualTo("en")
        assertThat(localModel.unitSystem).isEqualTo("metric")
        assertThat(domainModel.cityId).isEqualTo("101020100")
        assertThat(domainModel.temperature).isEqualTo("26")
        assertThat(domainModel.feelsLike).isEqualTo("28")
        assertThat(domainModel.conditionIcon).isEqualTo("100")
        assertThat(domainModel.fetchedAtEpochMillis).isEqualTo(1234L)
    }
}
