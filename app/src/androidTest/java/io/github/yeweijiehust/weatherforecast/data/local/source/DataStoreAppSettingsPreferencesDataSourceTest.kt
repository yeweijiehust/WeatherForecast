package io.github.yeweijiehust.weatherforecast.data.local.source

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreAppSettingsPreferencesDataSourceTest {
    @Test
    fun updateSettings_persistsLanguageAndUnitSystem() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.cacheDir, "datastore-test").apply { mkdirs() }
        val file = File(
            directory,
            "settings-${System.nanoTime()}.preferences_pb",
        )
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
        val dataSource = DataStoreAppSettingsPreferencesDataSource(dataStore)

        try {
            assertEquals(AppSettings(), dataSource.observeAppSettings().first())

            dataSource.updateLanguage(AppLanguage.SimplifiedChinese)
            dataSource.updateUnitSystem(UnitSystem.Imperial)

            assertEquals(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Imperial,
                ),
                dataSource.getCurrentSettings(),
            )
        } finally {
            scope.cancel()
            file.delete()
        }
    }
}
