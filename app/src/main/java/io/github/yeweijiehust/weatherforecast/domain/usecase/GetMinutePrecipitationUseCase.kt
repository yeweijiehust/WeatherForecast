package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.MinutePrecipitationRepository
import javax.inject.Inject

class GetMinutePrecipitationUseCase @Inject constructor(
    private val weatherRepository: MinutePrecipitationRepository,
) {
    suspend operator fun invoke(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): MinutePrecipitationFetchResult {
        return weatherRepository.fetchMinutePrecipitation(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = forceRefresh,
        )
    }
}
