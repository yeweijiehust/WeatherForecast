package io.github.yeweijiehust.weatherforecast.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppSettingsTest {
    @Test
    fun language_exposesApiLanguageCode() {
        assertThat(AppLanguage.English.apiCode).isEqualTo("en")
        assertThat(AppLanguage.SimplifiedChinese.apiCode).isEqualTo("zh")
    }

    @Test
    fun unitSystem_exposesApiUnitCode() {
        assertThat(UnitSystem.Metric.apiCode).isEqualTo("m")
        assertThat(UnitSystem.Imperial.apiCode).isEqualTo("i")
    }
}
