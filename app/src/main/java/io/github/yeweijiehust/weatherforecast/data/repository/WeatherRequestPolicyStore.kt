package io.github.yeweijiehust.weatherforecast.data.repository

import kotlin.math.min

internal class WeatherRequestPolicyStore {
    private val policyLock = Any()
    private val refreshFailureGates = mutableMapOf<String, RefreshFailureGate>()

    fun settingsSignature(
        language: String,
        unitSystem: String,
    ): String = "$language|$unitSystem"

    fun buildPolicyKey(
        dataset: String,
        locationKey: String,
        settingsSignature: String,
    ): String = "$dataset::$locationKey::$settingsSignature"

    fun isFresh(
        fetchedAtMillis: Long?,
        ttlMillis: Long,
        now: Long,
    ): Boolean {
        if (fetchedAtMillis == null) return false
        return now - fetchedAtMillis <= ttlMillis
    }

    fun shouldSkipAutoRequest(
        policyKey: String,
        hasCachedData: Boolean,
        now: Long,
    ): Boolean {
        if (!hasCachedData) return false
        return synchronized(policyLock) {
            val gate = refreshFailureGates[policyKey] ?: return@synchronized false
            now < gate.nextAllowedAtMillis
        }
    }

    fun recordFailure(
        policyKey: String,
        now: Long,
    ) {
        synchronized(policyLock) {
            val failureCount = (refreshFailureGates[policyKey]?.failureCount ?: 0) + 1
            val exponent = (failureCount - 1).coerceAtMost(BACKOFF_MAX_EXPONENT)
            val backoffMultiplier = 1L shl exponent
            val backoffDelay = min(
                AUTO_RETRY_BASE_DELAY_MILLIS * backoffMultiplier,
                AUTO_RETRY_MAX_DELAY_MILLIS,
            )
            refreshFailureGates[policyKey] = RefreshFailureGate(
                failureCount = failureCount,
                nextAllowedAtMillis = now + backoffDelay,
            )
        }
    }

    fun clearFailureGate(policyKey: String) {
        synchronized(policyLock) {
            refreshFailureGates.remove(policyKey)
        }
    }

    private data class RefreshFailureGate(
        val failureCount: Int,
        val nextAllowedAtMillis: Long,
    )

    private companion object {
        private const val AUTO_RETRY_BASE_DELAY_MILLIS = 15 * 1000L
        private const val AUTO_RETRY_MAX_DELAY_MILLIS = 10 * 60 * 1000L
        private const val BACKOFF_MAX_EXPONENT = 6
    }
}
