package io.github.yeweijiehust.weatherforecast.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.data.local.dao.SavedCityDao
import io.github.yeweijiehust.weatherforecast.data.local.db.WeatherForecastDatabase
import io.github.yeweijiehust.weatherforecast.data.local.source.DataStoreDefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.RoomSavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageBindingsModule {
    @Binds
    abstract fun bindSavedCityLocalDataSource(
        roomSavedCityLocalDataSource: RoomSavedCityLocalDataSource,
    ): SavedCityLocalDataSource

    @Binds
    abstract fun bindDefaultCityPreferencesDataSource(
        dataStoreDefaultCityPreferencesDataSource: DataStoreDefaultCityPreferencesDataSource,
    ): DefaultCityPreferencesDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): WeatherForecastDatabase {
        return Room.databaseBuilder(
            context,
            WeatherForecastDatabase::class.java,
            "weather_forecast.db",
        ).build()
    }

    @Provides
    fun provideSavedCityDao(
        database: WeatherForecastDatabase,
    ): SavedCityDao = database.savedCityDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { File(context.filesDir, "weather_preferences.preferences_pb") },
        )
    }
}
