package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherIndexDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherIndicesResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import org.junit.Test

class WeatherIndicesMapperTest {
    @Test
    fun responseToDomain_mapsAndPrioritizesIndicesOrder() {
        val response = WeatherIndicesResponseDto(
            code = "200",
            updateTime = "2026-04-09T13:57+08:00",
            daily = listOf(
                WeatherIndexDto(type = "10", name = "Air Pollution", level = "2", category = "Good", text = "Air quality is stable."),
                WeatherIndexDto(type = "1", name = "Sport", level = "3", category = "Poor", text = "Indoor exercise advised."),
                WeatherIndexDto(type = "5", name = "UV Index", level = "2", category = "Low", text = "Use basic sunscreen."),
                WeatherIndexDto(type = "8", name = "Comfort", level = "2", category = "Good", text = "Feels comfortable."),
            ),
        )

        val domain = response.toDomain()

        assertThat(domain.updateTime).isEqualTo("2026-04-09T13:57+08:00")
        assertThat(domain.items).hasSize(4)
        assertThat(domain.items.map { it.type }).containsExactly("5", "8", "1", "10").inOrder()
        assertThat(domain.items.first().name).isEqualTo("UV Index")
    }
}
