package com.svoysport.tv.data.remote

import com.svoysport.tv.data.mappers.toMatchItems
import com.svoysport.tv.data.remote.sporttv.SportTvListingDto
import com.svoysport.tv.domain.model.HomeContent
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.repository.MatchRepository
import javax.inject.Inject

/**
 * Future implementation for remote data.
 * Will eventually use Retrofit/Ktor to call the backend.
 */
class ApiMatchRepository @Inject constructor(
    // private val apiService: SvoySportApiService
) : MatchRepository {

    override suspend fun getHomeContent(): Result<HomeContent> {
        // In the future:
        // val response = apiService.getHomeContent()
        // return Result.success(response.toDomain())
        return Result.failure(Exception("ApiMatchRepository not implemented yet. Use MockMatchRepository."))
    }

    override suspend fun getMatchDetails(matchId: String): Result<MatchItem> {
        // In the future:
        // val response = apiService.getMatchDetails(matchId)
        // return Result.success(response.toDomain())
        return Result.failure(Exception("ApiMatchRepository not implemented yet."))
    }

    override suspend fun getAllMatches(): Result<List<MatchItem>> =
        Result.failure(Exception("ApiMatchRepository not implemented yet."))

    override suspend fun getScheduleForDay(dayStartMs: Long): Result<List<MatchItem>> =
        Result.failure(Exception("ApiMatchRepository not implemented yet."))
}
