package io.github.yeweijiehust.weatherforecast.data.remote.dto

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test

// JSON samples are adapted from QWeather GeoAPI docs:
// https://dev.qweather.com/docs/api/geoapi/city-lookup/
// https://dev.qweather.com/docs/api/geoapi/top-city/
class GeoApiDtoDeserializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun cityLookupResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "location": [
                {
                  "name": "北京",
                  "id": "101010100",
                  "lat": "39.90499",
                  "lon": "116.40529",
                  "adm2": "北京",
                  "adm1": "北京",
                  "country": "中国",
                  "tz": "Asia/Shanghai",
                  "utcOffset": "+08:00",
                  "isDst": "0",
                  "type": "city",
                  "rank": "10",
                  "fxLink": "https://www.qweather.com/weather/beijing-101010100.html"
                }
              ],
              "refer": {
                "sources": ["QWeather"],
                "license": ["QWeather Developers License"]
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<CityLookupResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.location).hasSize(1)
        assertThat(dto.location.first().id).isEqualTo("101010100")
        assertThat(dto.location.first().tz).isEqualTo("Asia/Shanghai")
    }

    @Test
    fun topCityResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "topCityList": [
                {
                  "name": "北京",
                  "id": "101010100",
                  "lat": "39.90499",
                  "lon": "116.40529",
                  "adm2": "北京",
                  "adm1": "北京",
                  "country": "中国",
                  "tz": "Asia/Shanghai",
                  "utcOffset": "+08:00",
                  "isDst": "0",
                  "type": "city",
                  "rank": "10",
                  "fxLink": "https://www.qweather.com/weather/beijing-101010100.html"
                },
                {
                  "name": "上海",
                  "id": "101020100",
                  "lat": "31.231706",
                  "lon": "121.472641",
                  "adm2": "上海",
                  "adm1": "上海",
                  "country": "中国",
                  "tz": "Asia/Shanghai"
                }
              ],
              "refer": {
                "sources": ["QWeather"],
                "license": ["QWeather Developers License"]
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<TopCityResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.topCityList).hasSize(2)
        assertThat(dto.topCityList[1].name).isEqualTo("上海")
        assertThat(dto.topCityList[1].id).isEqualTo("101020100")
    }
}
