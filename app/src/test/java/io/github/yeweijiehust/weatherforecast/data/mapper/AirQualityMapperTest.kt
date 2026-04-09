package io.github.yeweijiehust.weatherforecast.data.mapper

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityConcentrationDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityIndexDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityMetadataDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityPollutantDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityPrimaryPollutantDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomainOrNull
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class AirQualityMapperTest {
    @Test
    fun responseToDomainOrNull_mapsNewSchemaIndexesAndPollutants() {
        val response = AirQualityResponseDto(
            metadata = AirQualityMetadataDto(tag = "test-tag"),
            indexes = listOf(
                AirQualityIndexDto(
                    aqi = JsonPrimitive(53),
                    aqiDisplay = "53",
                    category = "Good",
                    primaryPollutant = AirQualityPrimaryPollutantDto(
                        code = "pm2p5",
                        name = "PM 2.5",
                    ),
                ),
            ),
            pollutants = listOf(
                AirQualityPollutantDto(
                    code = "pm2p5",
                    concentration = AirQualityConcentrationDto(value = JsonPrimitive(37.0)),
                ),
                AirQualityPollutantDto(
                    code = "pm10",
                    concentration = AirQualityConcentrationDto(value = JsonPrimitive(47.57)),
                ),
                AirQualityPollutantDto(
                    code = "no2",
                    concentration = AirQualityConcentrationDto(value = JsonPrimitive(18.0)),
                ),
            ),
        )

        val domain = response.toDomainOrNull()

        assertThat(domain).isNotNull()
        assertThat(domain?.aqi).isEqualTo("53")
        assertThat(domain?.category).isEqualTo("Good")
        assertThat(domain?.primary).isEqualTo("PM 2.5")
        assertThat(domain?.pm2p5).isEqualTo("37.0")
        assertThat(domain?.pm10).isEqualTo("47.57")
        assertThat(domain?.no2).isEqualTo("18.0")
        assertThat(domain?.so2).isEqualTo("--")
        assertThat(domain?.co).isEqualTo("--")
        assertThat(domain?.o3).isEqualTo("--")
    }
}
