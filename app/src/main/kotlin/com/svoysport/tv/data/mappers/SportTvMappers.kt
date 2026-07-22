package com.svoysport.tv.data.mappers

import com.svoysport.tv.data.remote.sporttv.SportTvListingDto
import com.svoysport.tv.domain.model.*

/**
 * Маппинг внешних DTO sport-tv.by во внутренние доменные модели.
 *
 * Фид не содержит изображений и не всегда содержит команды (бывают турнирные
 * трансляции вида «Чемпионат … Корт 1»), поэтому:
 *  - превью подбираем по виду спорта;
 *  - команды парсим best-effort из сегмента «A - B», иначе оставляем пустыми.
 */

// key — для группировки в секции/сайдбаре (одна из 6 категорий),
// label — подпись на карточке, thumb — мок-изображение (уходит в размытый фон).
private data class SportMeta(val key: String, val label: String, val thumb: String)

// fm=jpg — принудительно JPEG: AVIF/WebP не декодируются на эмуляторе и части TV
private const val W = "?q=80&w=1280&fm=jpg&fit=crop"

private val FOOTBALL   = SportMeta("football",   "Футбол",        "https://images.unsplash.com/photo-1522778119026-d647f0596c20$W")
private val HOCKEY     = SportMeta("hockey",     "Хоккей",        "https://images.unsplash.com/photo-1515703407324-5f753afd8be8$W")
private val BASKETBALL = SportMeta("basketball", "Баскетбол",     "https://images.unsplash.com/photo-1546519638-68e109498ffc$W")
private val VOLLEYBALL = SportMeta("volleyball", "Волейбол",      "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1$W")
private val HANDBALL   = SportMeta("handball",   "Гандбол",       "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1$W")
private val COMBAT     = SportMeta("other",      "Единоборства",  "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed$W")
private val BOXING     = SportMeta("other",      "Бокс",          "https://images.unsplash.com/photo-1517438476312-10d79c077509$W")
private val BASEBALL   = SportMeta("other",      "Бейсбол",       "https://images.unsplash.com/photo-1508344928928-7165b67de128$W")
private val MOTORSPORT = SportMeta("other",      "Автоспорт",     "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7$W")
private val TENNIS     = SportMeta("other",      "Теннис",        "https://images.unsplash.com/photo-1554068865-24cecd4e34b8$W")
private val OTHER      = SportMeta("other",      "Спорт",         "https://images.unsplash.com/photo-1461896836934-ffe607ba8211$W")

private fun detectSport(title: String): SportMeta {
    val t = title.lowercase()
    return when {
        "футбол"      in t || "football"   in t -> FOOTBALL
        "хоккей"      in t || "hockey"     in t -> HOCKEY
        "баскетбол"   in t || "basketball" in t -> BASKETBALL
        "волейбол"    in t || "volleyball" in t -> VOLLEYBALL
        "гандбол"     in t || "handball"   in t -> HANDBALL
        "единоборств" in t || "mma"        in t || "ufc" in t || "ksw" in t -> COMBAT
        "бокс"        in t || "boxing"     in t -> BOXING
        "бейсбол"     in t || "baseball"   in t -> BASEBALL
        "автоспорт"   in t || "ралли"      in t || "rally" in t || "wrc" in t || "формула" in t -> MOTORSPORT
        "теннис"      in t || "tennis"     in t -> TENNIS
        else                                    -> OTHER
    }
}

fun SportTvListingDto.toMatchItem(): MatchItem {
    val sport = detectSport("$title $annotation")

    // Команды: ищем сегмент с разделителем " - " (или " — ")
    val teamSegment = title.split(". ").firstOrNull { it.contains(" - ") || it.contains(" — ") }
    val teams = teamSegment?.split(" - ", " — ")
    val homeName = teams?.getOrNull(0)?.trim().orEmpty()
    val awayName = teams?.getOrNull(1)?.trim().orEmpty()

    val startMs = (date.toLongOrNull() ?: 0L) * 1000
    val endMs   = (date_end.toLongOrNull() ?: 0L) * 1000
    val now     = System.currentTimeMillis()
    val live    = now in startMs..endMs

    val stream = source.takeIf { it.isNotBlank() && it != "no" }

    // list2.php даёт стабильный id и реальное превью; list.php — нет, тогда
    // id собираем из sport_id+date+хэша (сами по себе они не уникальны).
    val discriminator = kotlin.math.abs((title + "|" + source).hashCode())
    val stableId = id?.takeIf { it.isNotBlank() }?.let { "stv_$it" }
        ?: "${sport_id}_${date}_$discriminator"
    val thumb = thumbnail?.takeIf { it.isNotBlank() } ?: sport.thumb

    return MatchItem(
        id            = stableId,
        title         = title.trim(),
        description   = annotation.trim(),
        homeTeam      = Team(id = "home", name = homeName),
        awayTeam      = Team(id = "away", name = awayName),
        competition   = Competition(id = sport.key, name = sport.label),
        isLive        = live,
        startTimeMs   = startMs,
        thumbnailUrl  = thumb,
        backgroundUrl = thumb,
        streamUrl     = stream,
        isSubscriptionRequired = isPaid == true,
        isHot         = live,
        durationSec   = duration ?: ((endMs - startMs) / 1000).coerceAtLeast(0)
    )
}

fun List<SportTvListingDto>.toMatchItems(): List<MatchItem> =
    map { it.toMatchItem() }.distinctBy { it.id }   // защита от полностью одинаковых записей
