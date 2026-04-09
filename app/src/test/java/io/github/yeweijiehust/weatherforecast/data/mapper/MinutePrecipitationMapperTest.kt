package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.MinutePrecipitationPointDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.MinutePrecipitationResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomainOrNull
import org.junit.Test

class MinutePrecipitationMapperTest {
    @Test
    fun responseToDomainOrNull_mapsSummaryAndPoints() {
        val response = MinutePrecipitationResponseDto(
            code = "200",
            summary = "No rainfall in the next 2 hours.",
            updateTime = "2026-04-09T14:00+08:00",
            minutely = listOf(
                MinutePrecipitationPointDto(
                    forecastTime = "2026-04-09T14:05+08:00",
                    precipitation = "0.0",
                    type = "rain",
                ),
                MinutePrecipitationPointDto(
                    forecastTime = "2026-04-09T14:10+08:00",
                    precipitation = "0.2",
                    type = "rain",
                ),
            ),
        )

        val domain = response.toDomainOrNull()

        assertThat(domain).isNotNull()
        assertThat(domain?.summary).isEqualTo("No rainfall in the next 2 hours.")
        assertThat(domain?.updateTime).isEqualTo("2026-04-09T14:00+08:00")
        assertThat(domain?.points).hasSize(2)
        assertThat(domain?.points?.first()?.forecastTime).isEqualTo("2026-04-09T14:05+08:00")
        assertThat(domain?.points?.first()?.precipitation).isEqualTo("0.0")
        assertThat(domain?.points?.first()?.type).isEqualTo("rain")
    }

    @Test
    fun responseToDomainOrNull_returnsNullWhenNoPoints() {
        val response = MinutePrecipitationResponseDto(
            code = "200",
            summary = "No valid minutely data.",
            updateTime = "2026-04-09T14:00+08:00",
            minutely = emptyList(),
        )

        val domain = response.toDomainOrNull()

        assertThat(domain).isNull()
    }
}
