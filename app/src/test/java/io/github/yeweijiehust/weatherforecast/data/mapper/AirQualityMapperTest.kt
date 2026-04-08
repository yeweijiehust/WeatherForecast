package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityCurrentDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import org.junit.Test

class AirQualityMapperTest {
    @Test
    fun dtoToDomain_handlesPartialPayloadWithFallbacks() {
        val dto = AirQualityCurrentDto(
            publishTime = "2026-04-08T14:00+08:00",
            aqi = "86",
            category = null,
            primary = "pm2p5",
            pm2p5 = "65",
            pm10 = null,
            no2 = "18",
            so2 = null,
            co = "0.7",
            o3 = null,
        )

        val domain = dto.toDomain()

        assertThat(domain.publishTime).isEqualTo("2026-04-08T14:00+08:00")
        assertThat(domain.aqi).isEqualTo("86")
        assertThat(domain.category).isEqualTo("--")
        assertThat(domain.primary).isEqualTo("pm2p5")
        assertThat(domain.pm2p5).isEqualTo("65")
        assertThat(domain.pm10).isEqualTo("--")
        assertThat(domain.no2).isEqualTo("18")
        assertThat(domain.so2).isEqualTo("--")
        assertThat(domain.co).isEqualTo("0.7")
        assertThat(domain.o3).isEqualTo("--")
    }
}
