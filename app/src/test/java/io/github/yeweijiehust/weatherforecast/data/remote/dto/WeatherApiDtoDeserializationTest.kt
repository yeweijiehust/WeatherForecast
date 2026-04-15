package io.github.yeweijiehust.weatherforecast.data.remote.dto

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

// JSON samples are adapted from QWeather API docs:
// https://dev.qweather.com/docs/api/weather/weather-now/
// https://dev.qweather.com/docs/api/weather/weather-hourly-forecast/
// https://dev.qweather.com/docs/api/weather/weather-daily-forecast/
// https://dev.qweather.com/docs/api/minutecast/minute-precip/
// https://dev.qweather.com/docs/api/astronomy/sunrise-sunset/
// https://dev.qweather.com/docs/api/indices/indices-forecast/
// https://dev.qweather.com/docs/api/warning/weather-alert/
// https://dev.qweather.com/docs/api/air-quality/air-current/
class WeatherApiDtoDeserializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun currentWeatherResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "updateTime": "2020-06-30T22:00+08:00",
              "fxLink": "https://www.qweather.com/weather/beijing-101010100.html",
              "now": {
                "obsTime": "2020-06-30T21:40+08:00",
                "temp": "24",
                "feelsLike": "26",
                "icon": "101",
                "text": "多云",
                "wind360": "123",
                "windDir": "东南风",
                "windScale": "1",
                "windSpeed": "3",
                "humidity": "72",
                "precip": "0.0",
                "pressure": "1003",
                "vis": "16",
                "cloud": "10",
                "dew": "21"
              },
              "refer": {
                "sources": ["QWeather"],
                "license": ["QWeather Developers License"]
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<CurrentWeatherResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.now).isNotNull()
        assertThat(dto.now?.conditionText).isEqualTo("多云")
        assertThat(dto.now?.visibility).isEqualTo("16")
    }

    @Test
    fun hourlyForecastResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "updateTime": "2020-06-30T22:00+08:00",
              "fxLink": "https://www.qweather.com/weather/beijing-101010100.html",
              "hourly": [
                {
                  "fxTime": "2020-07-01T00:00+08:00",
                  "temp": "24",
                  "icon": "101",
                  "text": "多云",
                  "wind360": "123",
                  "windDir": "东南风",
                  "windScale": "1",
                  "windSpeed": "3",
                  "humidity": "72",
                  "pop": "5",
                  "precip": "0.0",
                  "pressure": "1003",
                  "cloud": "10",
                  "dew": "21"
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<HourlyForecastResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.hourly).hasSize(1)
        assertThat(dto.hourly?.first()?.forecastTime).isEqualTo("2020-07-01T00:00+08:00")
        assertThat(dto.hourly?.first()?.precipitationProbability).isEqualTo("5")
    }

    @Test
    fun dailyForecastResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "updateTime": "2020-06-30T22:00+08:00",
              "fxLink": "https://www.qweather.com/weather/beijing-101010100.html",
              "daily": [
                {
                  "fxDate": "2020-07-01",
                  "sunrise": "04:47",
                  "sunset": "19:47",
                  "moonrise": "16:16",
                  "moonset": "01:16",
                  "moonPhase": "盈凸月",
                  "moonPhaseIcon": "803",
                  "tempMax": "34",
                  "tempMin": "26",
                  "iconDay": "100",
                  "textDay": "晴",
                  "iconNight": "150",
                  "textNight": "晴",
                  "wind360Day": "123",
                  "windDirDay": "东南风",
                  "windScaleDay": "3-4",
                  "windSpeedDay": "15",
                  "wind360Night": "45",
                  "windDirNight": "东北风",
                  "windScaleNight": "1-2",
                  "windSpeedNight": "3",
                  "humidity": "45",
                  "precip": "0.0",
                  "pressure": "1005",
                  "vis": "25",
                  "cloud": "0",
                  "uvIndex": "11"
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<DailyForecastResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.daily).hasSize(1)
        assertThat(dto.daily?.first()?.forecastDate).isEqualTo("2020-07-01")
        assertThat(dto.daily?.first()?.conditionTextDay).isEqualTo("晴")
    }

    @Test
    fun minutePrecipitationResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "summary": "未来2小时无降水",
              "updateTime": "2026-04-09T14:00+08:00",
              "minutely": [
                {
                  "fxTime": "2026-04-09T14:05+08:00",
                  "precip": "0.0",
                  "type": "rain"
                },
                {
                  "fxTime": "2026-04-09T14:10+08:00",
                  "precip": "0.0",
                  "type": "rain"
                }
              ],
              "refer": {
                "sources": ["QWeather"],
                "license": ["QWeather Developers License"]
              }
            }
        """.trimIndent()

        val dto = json.decodeFromString<MinutePrecipitationResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.summary).contains("未来2小时")
        assertThat(dto.minutely).hasSize(2)
        assertThat(dto.minutely.first().forecastTime).isEqualTo("2026-04-09T14:05+08:00")
    }

    @Test
    fun sunriseSunsetResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "updateTime": "2021-11-15T16:35+08:00",
              "fxLink": "https://www.qweather.com",
              "sunrise": "06:50",
              "sunset": "17:03"
            }
        """.trimIndent()

        val dto = json.decodeFromString<SunriseSunsetResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.updateTime).isEqualTo("2021-11-15T16:35+08:00")
        assertThat(dto.sunrise).isEqualTo("06:50")
        assertThat(dto.sunset).isEqualTo("17:03")
    }

    @Test
    fun weatherIndicesResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "code": "200",
              "updateTime": "2021-11-16T16:35+08:00",
              "fxLink": "https://www.qweather.com",
              "daily": [
                {
                  "date": "2021-11-16",
                  "type": "1",
                  "name": "运动指数",
                  "level": "3",
                  "category": "较不宜",
                  "text": "推荐您在室内进行低强度运动。"
                },
                {
                  "date": "2021-11-16",
                  "type": "5",
                  "name": "紫外线指数",
                  "level": "3",
                  "category": "中等",
                  "text": "无须特别防护。"
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<WeatherIndicesResponseDto>(payload)

        assertThat(dto.code).isEqualTo("200")
        assertThat(dto.daily).hasSize(2)
        assertThat(dto.daily.first().name).isEqualTo("运动指数")
        assertThat(dto.daily[1].type).isEqualTo("5")
    }

    @Test
    fun weatherAlertResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "metadata": {
                "tag": "8d47f14dd1f4f928f589f8f025fdc2fd",
                "sources": ["QWeather"],
                "license": ["QWeather Developers License"]
              },
              "alerts": [
                {
                  "id": "10101010020240914150705248681617",
                  "senderName": "北京市气象台",
                  "issuedTime": "2024-09-14T15:08+08:00",
                  "messageType": {
                    "code": "alert",
                    "name": "预警"
                  },
                  "title": "北京市气象台发布暴雨黄色预警",
                  "onsetTime": "2024-09-14T15:08+08:00",
                  "expireTime": "2024-09-14T23:59+08:00",
                  "status": "active",
                  "severity": "Minor",
                  "certainty": "Unknown",
                  "urgency": "Unknown",
                  "eventType": {
                    "code": "rainstorm",
                    "name": "暴雨"
                  },
                  "color": {
                    "code": "yellow",
                    "red": 255,
                    "green": 204,
                    "blue": 0,
                    "alpha": 1.0
                  },
                  "headline": "请做好防范工作",
                  "description": "局地累计降水量可达50毫米以上。",
                  "instruction": "减少外出，远离低洼路段。"
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<WeatherAlertResponseDto>(payload)

        assertThat(dto.metadata.tag).isEqualTo("8d47f14dd1f4f928f589f8f025fdc2fd")
        assertThat(dto.alerts).hasSize(1)
        assertThat(dto.alerts.first().eventType?.code).isEqualTo("rainstorm")
        assertThat(dto.alerts.first().color?.code).isEqualTo("yellow")
    }

    @Test
    fun airQualityResponse_deserializesFromDocumentStyleJson() {
        val payload = """
            {
              "metadata": {
                "tag": "1bf9b26a6beafe48e4869f2f6ae9eca4",
                "sources": ["QWeather"],
                "license": ["QWeather Developers License"]
              },
              "indexes": [
                {
                  "code": "101",
                  "name": "AQI(CHN)",
                  "aqi": "89",
                  "aqiDisplay": "89",
                  "level": "2",
                  "category": "良",
                  "primaryPollutant": {
                    "code": "o3",
                    "name": "臭氧"
                  }
                }
              ],
              "pollutants": [
                {
                  "code": "o3",
                  "name": "臭氧",
                  "fullName": "臭氧",
                  "concentration": {
                    "value": "160.0",
                    "unit": "μg/m3"
                  }
                },
                {
                  "code": "pm2p5",
                  "name": "PM2.5",
                  "concentration": {
                    "value": "37.0",
                    "unit": "μg/m3"
                  }
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<AirQualityResponseDto>(payload)

        assertThat(dto.metadata.tag).isEqualTo("1bf9b26a6beafe48e4869f2f6ae9eca4")
        assertThat(dto.indexes).hasSize(1)
        assertThat(dto.indexes.first().aqi?.jsonPrimitive?.content).isEqualTo("89")
        assertThat(dto.pollutants).hasSize(2)
        assertThat(dto.pollutants.first().concentration?.value?.jsonPrimitive?.content).isEqualTo("160.0")
    }
}
