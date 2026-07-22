package com.svoysport.tv.data.mock

import com.svoysport.tv.domain.model.*
import com.svoysport.tv.domain.repository.MatchRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockMatchRepository @Inject constructor() : MatchRepository {

    private val mci = Team("mci", "Манчестер Сити", score = 2)
    private val rma = Team("rma", "Реал Мадрид", score = 1)
    private val ars = Team("ars", "Арсенал")
    private val bay = Team("bay", "Бавария")
    
    private val ucl = Competition("ucl", "Лига Чемпионов")

    private val euroleague = Competition("eur", "Баскетбол")
    private val mhl = Competition("mhl", "Хоккей")
    private val eurocup = Competition("euc", "Баскетбол")
    private val khl = Competition("khl", "Хоккей")

    private val featuredMatch = MatchItem(
        id = "f1",
        title = "Евролига. Олимпиакос – Анадолу Эфес",
        description = "Решающий матч Евролиги. Греческий Олимпиакос против турецкого Анадолу Эфес.",
        homeTeam = mci,
        awayTeam = rma,
        competition = euroleague,
        isLive = true,
        startTimeMs = System.currentTimeMillis(),
        thumbnailUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=2090",
        backgroundUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=2090",
        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        isHot = true
    )

    private val matches = listOf(
        featuredMatch,
        MatchItem(
            id = "m1",
            title = "МХЛ. Динамо-Шинник – Красная Армия",
            description = "Матч МХЛ.",
            homeTeam = ars,
            awayTeam = bay,
            competition = mhl,
            isLive = true,
            startTimeMs = System.currentTimeMillis(),
            thumbnailUrl = "https://images.unsplash.com/photo-1515703407324-5f753afd8be8?q=80&w=2100"
        ),
        MatchItem(
            id = "m2",
            title = "Еврокубок. Элицур Рамла – Унияо Спортива",
            description = "Матч Еврокубка.",
            homeTeam = mci,
            awayTeam = ars,
            competition = eurocup,
            isLive = true,
            startTimeMs = System.currentTimeMillis(),
            thumbnailUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=2090",
            isSubscriptionRequired = true
        ),
        MatchItem(
            id = "m3",
            title = "КХЛ. Динамо Минск – Шанхайские Драконы",
            description = "Матч КХЛ.",
            homeTeam = rma,
            awayTeam = bay,
            competition = khl,
            isLive = true,
            startTimeMs = System.currentTimeMillis(),
            thumbnailUrl = "https://images.unsplash.com/photo-1515703407324-5f753afd8be8?q=80&w=2100",
            isSubscriptionRequired = true
        ),
        MatchItem(
            id = "m4",
            title = "Арсенал — Бавария",
            description = "Четвертьфинал Лиги Чемпионов.",
            homeTeam = ars,
            awayTeam = bay,
            competition = ucl,
            isLive = false,
            startTimeMs = System.currentTimeMillis() + 3600000,
            thumbnailUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=2093",
            isHot = true
        )
    )

    override suspend fun getHomeContent(): Result<HomeContent> {
        delay(800) // Simulate network
        return Result.success(
            HomeContent(
                featuredMatch = featuredMatch,
                sections = listOf(
                    HomeSection("s1", "Онлайн", matches),
                    HomeSection("s2", "Предстоящие трансляции", matches.reversed()),
                    HomeSection("s3", "Хоккей", matches.filter { it.competition.id in listOf("mhl", "khl") }),
                    HomeSection("s4", "Баскетбол", matches.filter { it.competition.id in listOf("eur", "euc") })
                )
            )
        )
    }

    override suspend fun getMatchDetails(matchId: String): Result<MatchItem> {
        delay(400)
        return matches.find { it.id == matchId }?.let { Result.success(it) }
            ?: Result.failure(Exception("Match not found"))
    }

    override suspend fun getAllMatches(): Result<List<MatchItem>> = Result.success(matches)

    override suspend fun getScheduleForDay(dayStartMs: Long): Result<List<MatchItem>> =
        Result.success(matches)

    override suspend fun getArchive(): Result<List<MatchItem>> =
        Result.success(matches)
}
