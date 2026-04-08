package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import org.junit.Test

class WeatherAlertMapperTest {
    @Test
    fun dtoToDomain_preservesAlertFields() {
        val dto = WeatherAlertDto(
            id = "10102010020260408120000",
            sender = "Shanghai Meteorological Center",
            publishTime = "2026-04-08T12:00+08:00",
            title = "Rainstorm Blue Warning",
            startTime = "2026-04-08T12:00+08:00",
            endTime = "2026-04-08T23:00+08:00",
            status = "active",
            severity = "Blue",
            severityColor = "Blue",
            type = "rainstorm",
            typeName = "Rainstorm",
            text = "Expect heavy rain in the next 6 hours.",
        )

        val domain = dto.toDomain()

        assertThat(domain.id).isEqualTo("10102010020260408120000")
        assertThat(domain.sender).isEqualTo("Shanghai Meteorological Center")
        assertThat(domain.publishTime).isEqualTo("2026-04-08T12:00+08:00")
        assertThat(domain.title).isEqualTo("Rainstorm Blue Warning")
        assertThat(domain.startTime).isEqualTo("2026-04-08T12:00+08:00")
        assertThat(domain.endTime).isEqualTo("2026-04-08T23:00+08:00")
        assertThat(domain.status).isEqualTo("active")
        assertThat(domain.severity).isEqualTo("Blue")
        assertThat(domain.severityColor).isEqualTo("Blue")
        assertThat(domain.type).isEqualTo("rainstorm")
        assertThat(domain.typeName).isEqualTo("Rainstorm")
        assertThat(domain.text).isEqualTo("Expect heavy rain in the next 6 hours.")
    }
}
