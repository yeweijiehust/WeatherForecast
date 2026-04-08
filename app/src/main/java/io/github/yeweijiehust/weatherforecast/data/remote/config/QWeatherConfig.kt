package io.github.yeweijiehust.weatherforecast.data.remote.config

data class QWeatherConfig(
    val apiKey: String,
    val apiHost: String,
) {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiHost.isNotBlank()

    val baseUrl: String
        get() {
            if (apiHost.isBlank()) {
                return FALLBACK_BASE_URL
            }

            val host = apiHost.removeSuffix("/")
            return when {
                host.startsWith("https://") || host.startsWith("http://") -> "$host/"
                else -> "https://$host/"
            }
        }

    private companion object {
        private const val FALLBACK_BASE_URL = "https://example.com/"
    }
}
