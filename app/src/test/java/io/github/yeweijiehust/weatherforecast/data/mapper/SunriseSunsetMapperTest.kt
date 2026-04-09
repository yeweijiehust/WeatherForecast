package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.SunriseSunsetResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import org.junit.Test

class SunriseSunsetMapperTest {
    @Test
    fun responseToDomain_mapsSunriseSunsetFields() {
        val response = SunriseSunsetResponseDto(
            code = "200",
            updateTime = "2026-04-09T11:00+08:00",
            sunrise = "2026-04-09T05:34+08:00",
            sunset = "2026-04-09T18:18+08:00",
        )

        val domain = response.toDomain()

        assertThat(domain.updateTime).isEqualTo("2026-04-09T11:00+08:00")
        assertThat(domain.sunrise).isEqualTo("2026-04-09T05:34+08:00")
        assertThat(domain.sunset).isEqualTo("2026-04-09T18:18+08:00")
    }
}
