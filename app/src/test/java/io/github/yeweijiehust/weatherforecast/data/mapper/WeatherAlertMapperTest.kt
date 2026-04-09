package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertColorDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertEventTypeDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertMessageTypeDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import org.junit.Test

class WeatherAlertMapperTest {
    @Test
    fun dtoToDomain_preservesAlertFields() {
        val dto = WeatherAlertDto(
            id = "52f63dbf40f5f089f5f69f2d7f929f4f",
            senderName = "Shanghai Meteorological Center",
            issuedTime = "2026-04-08T12:00+08:00",
            headline = "Rainstorm Blue Warning",
            onsetTime = "2026-04-08T12:00+08:00",
            expireTime = "2026-04-08T23:00+08:00",
            status = "active",
            messageType = WeatherAlertMessageTypeDto(code = "alert"),
            severity = "Blue",
            color = WeatherAlertColorDto(code = "blue"),
            eventType = WeatherAlertEventTypeDto(
                code = "rainstorm",
                name = "Rainstorm",
            ),
            description = "Expect heavy rain in the next 6 hours.",
        )

        val domain = dto.toDomain()

        assertThat(domain.id).isEqualTo("52f63dbf40f5f089f5f69f2d7f929f4f")
        assertThat(domain.sender).isEqualTo("Shanghai Meteorological Center")
        assertThat(domain.publishTime).isEqualTo("2026-04-08T12:00+08:00")
        assertThat(domain.title).isEqualTo("Rainstorm Blue Warning")
        assertThat(domain.startTime).isEqualTo("2026-04-08T12:00+08:00")
        assertThat(domain.endTime).isEqualTo("2026-04-08T23:00+08:00")
        assertThat(domain.status).isEqualTo("active")
        assertThat(domain.severity).isEqualTo("Blue")
        assertThat(domain.severityColor).isEqualTo("blue")
        assertThat(domain.type).isEqualTo("rainstorm")
        assertThat(domain.typeName).isEqualTo("Rainstorm")
        assertThat(domain.text).isEqualTo("Expect heavy rain in the next 6 hours.")
    }
}
