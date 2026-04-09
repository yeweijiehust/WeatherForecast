package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.remote.dto.DailyForecastDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import org.junit.Test

class DailyForecastMapperTest {
    @Test
    fun dtoToLocalAndDomain_preservesDailyForecastFieldsAndUsesFallbackForPop() {
        val dto = DailyForecastDto(
            forecastDate = "2026-04-09",
            tempMax = "30",
            tempMin = "22",
            conditionTextDay = "Sunny",
            conditionIconDay = "100",
            precipitation = "0.0",
            windDirectionDay = "South",
            windScaleDay = "3",
            windSpeedDay = "16",
        )

        val localModel = dto.toLocalModel(
            cityId = "101020100",
            fetchedAtEpochMillis = 1234L,
            language = "en",
            unitSystem = "metric",
        )
        val domainModel = localModel.toDomain()

        assertThat(localModel.cityId).isEqualTo("101020100")
        assertThat(localModel.conditionTextDay).isEqualTo("Sunny")
        assertThat(localModel.precipitationProbability).isEqualTo("--")
        assertThat(localModel.language).isEqualTo("en")
        assertThat(localModel.unitSystem).isEqualTo("metric")
        assertThat(domainModel.cityId).isEqualTo("101020100")
        assertThat(domainModel.forecastDate).isEqualTo("2026-04-09")
        assertThat(domainModel.tempMax).isEqualTo("30")
        assertThat(domainModel.tempMin).isEqualTo("22")
        assertThat(domainModel.conditionIconDay).isEqualTo("100")
        assertThat(domainModel.fetchedAtEpochMillis).isEqualTo(1234L)
    }

}
