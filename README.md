# WeatherForecast

Android weather app built with Kotlin + Jetpack Compose, using QWeather APIs for city search and weather data.

## What This Project Delivers
- Multi-screen weather experience: `Home`, `Search Cities`, `Settings`, `Weather Detail`.
- Adaptive navigation with `NavigationSuiteScaffold` for phone, foldable, and tablet layouts.
- Full in-app localization switching (English / Simplified Chinese) with localized context.
- Snackbar-based feedback for user actions and network/cache states.
- Cached forecast pipeline (Room + DataStore) for stronger resilience under unstable network.

For Chinese documentation, see [README.zh-CN.md](README.zh-CN.md).

## Screenshots
### Home
![Home Screen](docs/screenshots/home-current-weather.png)

### Search Cities
![Search Screen](docs/screenshots/search-results.png)

### Settings
![Settings Screen](docs/screenshots/settings-default.png)

### Weather Detail
![Detail Screen](docs/screenshots/detail-content.png)

## Feature Highlights
- **Home**
  - Current weather snapshot (temperature, condition, feels-like, humidity, wind, precipitation, visibility).
  - 24-hour forecast + 7-day forecast.
  - Quick insights (alert summary + air-quality summary).
  - Entry to detailed weather view for the selected city.
- **Search Cities**
  - QWeather city lookup + top city suggestions.
  - Save city, set default city, remove city.
- **Settings**
  - Unit system switch (Metric / Imperial).
  - Language switch (English / Simplified Chinese) with immediate app-wide effect.
  - Clear weather cache.
- **Weather Detail**
  - Hourly timeline, daily forecast.
  - Minute precipitation timeline.
  - Sunrise/sunset.
  - Weather indices.
  - Weather alerts.
  - Air quality and pollutant breakdown.

## Backend Integration (QWeather)
- **Authentication**: OkHttp interceptor injects QWeather auth header `X-QW-Api-Key`.
- **Base URL handling**: resolved from local runtime configuration and normalized automatically.
- **Serialization**: `kotlinx.serialization` (`ignoreUnknownKeys = true`).
- **Main API groups**
  - Geo:
    - `geo/v2/city/lookup`
    - `geo/v2/city/top`
  - Weather:
    - `v7/weather/now`
    - `v7/weather/24h`
    - `v7/weather/7d`
    - `v7/minutely/5m`
    - `v7/astronomy/sun`
    - `v7/indices/1d`
    - `weatheralert/v1/current/{lat}/{lon}`
    - `airquality/v1/current/{lat}/{lon}`

## Data/Domain Architecture
- Clean architecture layering: `feature -> domain -> data`.
- Repository abstraction in domain (`WeatherRepository`, `CityRepository`, `SettingsRepository`, etc.).
- Data implementation split:
  - `QWeatherCityRepository` for city search/save/default.
  - `QWeatherForecastRepository` for current/hourly/daily + Room caching.
  - `QWeatherSecondaryRepository` for alerts/AQI/minutely/sunrise-sunset/indices.
  - `QWeatherWeatherRepository` as composition root for weather repositories.
- Request policy includes TTL checks and failure backoff gating (`WeatherRequestPolicyStore`) to reduce repeated failing auto-refreshes.

## Quick Start
1. Prepare your private local QWeather runtime configuration (keep secrets out of version control).
2. Build and run:
   ```bash
   ./gradlew :app:assembleDebug
   ./gradlew :app:installDebug
   ```
3. Run tests:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ./gradlew :app:connectedDebugAndroidTest
   ```

## Tech Stack
- Kotlin, Coroutines, Flow
- Jetpack Compose + Material 3 + Material 3 Adaptive
- Navigation Compose
- Hilt (DI)
- Retrofit + OkHttp + kotlinx.serialization
- Room + DataStore
- JUnit, MockK, Truth, Turbine, Compose UI Test, Robolectric, Roborazzi
