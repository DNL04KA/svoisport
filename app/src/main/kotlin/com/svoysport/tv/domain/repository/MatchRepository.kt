package com.svoysport.tv.domain.repository

import com.svoysport.tv.domain.model.HomeContent
import com.svoysport.tv.domain.model.MatchItem

interface MatchRepository {
    suspend fun getHomeContent(): Result<HomeContent>
    suspend fun getMatchDetails(matchId: String): Result<MatchItem>

    /** Плоский список всех известных трансляций — для поиска и избранного. */
    suspend fun getAllMatches(): Result<List<MatchItem>>

    /** Расписание на конкретный день (по началу дня в мс). */
    suspend fun getScheduleForDay(dayStartMs: Long): Result<List<MatchItem>>
}
