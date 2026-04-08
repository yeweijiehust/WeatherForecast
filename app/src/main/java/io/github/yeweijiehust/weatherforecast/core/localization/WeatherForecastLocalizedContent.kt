package io.github.yeweijiehust.weatherforecast.core.localization

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import java.util.Locale

val LocalWeatherForecastContext = staticCompositionLocalOf<Context> {
    error("Localized weather forecast context was not provided.")
}

@Composable
fun WeatherForecastLocalizedContent(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current
    val locale = remember(language) { Locale.forLanguageTag(language.storageValue) }
    val localizedConfiguration = remember(baseConfiguration, locale) {
        Configuration(baseConfiguration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
    }
    val localizedContext = remember(baseContext, localizedConfiguration) {
        baseContext.createConfigurationContext(localizedConfiguration)
    }

    CompositionLocalProvider(
        LocalWeatherForecastContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        content()
    }
}

@Composable
fun localizedStringResource(
    @StringRes resId: Int,
    vararg formatArgs: Any,
): String {
    val localizedContext = LocalWeatherForecastContext.current
    LocalConfiguration.current
    return localizedContext.getString(resId, *formatArgs)
}
