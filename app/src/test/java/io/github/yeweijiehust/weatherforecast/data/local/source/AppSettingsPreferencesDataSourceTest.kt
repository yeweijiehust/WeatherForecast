package io.github.yeweijiehust.weatherforecast.data.local.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppSettingsPreferencesDataSourceTest {
    @Test
    fun observeAppSettings_returnsDefaultsWhenNothingStored() = runTest {
        val dataSource = DataStoreAppSettingsPreferencesDataSource(TestPreferencesDataStore())

        assertThat(dataSource.observeAppSettings().first()).isEqualTo(AppSettings())
        assertThat(dataSource.getCurrentSettings()).isEqualTo(AppSettings())
    }

    @Test
    fun updateSettings_persistsLanguageAndUnitSystem() = runTest {
        val dataSource = DataStoreAppSettingsPreferencesDataSource(TestPreferencesDataStore())

        dataSource.updateLanguage(AppLanguage.SimplifiedChinese)
        dataSource.updateUnitSystem(UnitSystem.Imperial)

        assertThat(dataSource.observeAppSettings().first()).isEqualTo(
            AppSettings(
                language = AppLanguage.SimplifiedChinese,
                unitSystem = UnitSystem.Imperial,
            ),
        )
        assertThat(dataSource.getCurrentSettings()).isEqualTo(
            AppSettings(
                language = AppLanguage.SimplifiedChinese,
                unitSystem = UnitSystem.Imperial,
            ),
        )
    }

    private class TestPreferencesDataStore : DataStore<Preferences> {
        private val preferencesFlow = MutableStateFlow<Preferences>(emptyPreferences())

        override val data: Flow<Preferences> = preferencesFlow

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updatedPreferences = transform(preferencesFlow.value)
            preferencesFlow.value = updatedPreferences
            return updatedPreferences
        }
    }
}
