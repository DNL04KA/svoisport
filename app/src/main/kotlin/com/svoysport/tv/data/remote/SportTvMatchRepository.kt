package com.svoysport.tv.data.remote

import com.svoysport.tv.data.mappers.toMatchItems
import com.svoysport.tv.data.remote.sporttv.SportTvApi
import com.svoysport.tv.domain.model.HomeContent
import com.svoysport.tv.domain.model.HomeSection
import com.svoysport.tv.domain.model.MatchItem
import com.svoysport.tv.domain.repository.MatchRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Боевой репозиторий: трансляции из публичного фида sport-tv.by.
 *
 * Фид отдаёт один плоский список; кэшируем его в памяти, чтобы детали матча,
 * плеер, поиск и избранное переиспользовали уже загруженные данные без
 * повторных сетевых запросов. [getHomeContent] раскладывает список по секциям.
 */
@Singleton
class SportTvMatchRepository @Inject constructor(
    private val api: SportTvApi
) : MatchRepository {

    private val cacheMutex = Mutex()
    private var cache: List<MatchItem> = emptyList()

    // Глобальный индекс id→матч из всех загруженных списков (витрина + дни),
    // чтобы детали/плеер находили матч независимо от того, откуда открыт.
    private val byId = mutableMapOf<String, MatchItem>()

    private fun index(items: List<MatchItem>) = synchronized(byId) {
        items.forEach { byId[it.id] = it }
    }

    private suspend fun load(forceRefresh: Boolean = false): Result<List<MatchItem>> {
        cacheMutex.withLock {
            if (!forceRefresh && cache.isNotEmpty()) return Result.success(cache)
            return runCatching {
                api.fetchListing()
                    .toMatchItems()
                    .sortedBy { it.startTimeMs }
                    .also { cache = it; index(it) }
            }
        }
    }

    override suspend fun getAllMatches(): Result<List<MatchItem>> = load()

    // Кэш расписания по дню (ключ — начало дня в секундах)
    private val dayMutex = Mutex()
    private val dayCache = mutableMapOf<Long, List<MatchItem>>()

    override suspend fun getScheduleForDay(dayStartMs: Long): Result<List<MatchItem>> {
        val daySec = dayStartMs / 1000
        dayMutex.withLock {
            dayCache[daySec]?.let { return Result.success(it) }
        }
        return runCatching {
            api.fetchListing(dateSec = daySec)
                .toMatchItems()
                .sortedBy { it.startTimeMs }
                .also { result -> dayMutex.withLock { dayCache[daySec] = result }; index(result) }
        }
    }

    // Кэш архива (записи меняются редко)
    private val archiveMutex = Mutex()
    private var archiveCache: List<MatchItem> = emptyList()

    override suspend fun getArchive(): Result<List<MatchItem>> {
        archiveMutex.withLock {
            if (archiveCache.isNotEmpty()) return Result.success(archiveCache)
        }
        return runCatching {
            api.fetchArchive()
                .toMatchItems()
                .sortedByDescending { it.startTimeMs }
                .also { result ->
                    archiveMutex.withLock { archiveCache = result }
                    index(result)   // детали/плеер находят запись по id
                }
        }
    }

    override suspend fun getHomeContent(): Result<HomeContent> {
        return load().mapCatching { matches ->
            if (matches.isEmpty()) throw IllegalStateException("Нет доступных трансляций")

            val live     = matches.filter { it.isLive }
            val upcoming = matches.filter { it.startTimeMs > System.currentTimeMillis() }
            val featured = live.firstOrNull() ?: matches.first()

            val sections = buildList {
                if (live.isNotEmpty())     add(HomeSection("live", "Онлайн", live))
                if (upcoming.isNotEmpty()) add(HomeSection("upcoming", "Предстоящие трансляции", upcoming))

                // Секции по виду спорта (competition.id из маппера)
                SPORT_SECTIONS.forEach { (id, title) ->
                    val items = matches.filter { it.competition.id == id }
                    if (items.isNotEmpty()) add(HomeSection(id, title, items))
                }

                // Если по какой-то причине секций нет — показываем всё
                if (isEmpty()) add(HomeSection("all", "Все трансляции", matches))
            }

            HomeContent(featuredMatch = featured, sections = sections)
        }
    }

    override suspend fun getMatchDetails(matchId: String): Result<MatchItem> {
        // Сначала смотрим в общем индексе (наполняется из главной и расписания)
        synchronized(byId) { byId[matchId] }?.let { return Result.success(it) }
        // Иначе подгружаем витрину и пробуем ещё раз
        return load().mapCatching { matches ->
            matches.find { it.id == matchId }
                ?: synchronized(byId) { byId[matchId] }
                ?: throw NoSuchElementException("Трансляция не найдена")
        }
    }

    private companion object {
        val SPORT_SECTIONS = listOf(
            "football"   to "Футбол",
            "hockey"     to "Хоккей",
            "basketball" to "Баскетбол",
            "volleyball" to "Волейбол",
            "handball"   to "Гандбол",
            "other"      to "Другие виды спорта",
        )
    }
}
