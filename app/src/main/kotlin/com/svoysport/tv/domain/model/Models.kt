package com.svoysport.tv.domain.model

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val score: Int? = null
)

data class Competition(
    val id: String,
    val name: String,
    val logoUrl: String? = null
)

data class MatchItem(
    val id: String,
    val title: String,
    val description: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val competition: Competition,
    val isLive: Boolean,
    val startTimeMs: Long,
    val thumbnailUrl: String,
    val backgroundUrl: String? = null,
    val streamUrl: String? = null,
    val isSubscriptionRequired: Boolean = false,
    val isHot: Boolean = false,
    /** Длительность записи в секундах (архив); 0 — неизвестна/не запись. */
    val durationSec: Long = 0L
)

data class HomeSection(
    val id: String,
    val title: String,
    val matches: List<MatchItem>
)

data class HomeContent(
    val featuredMatch: MatchItem,
    val sections: List<HomeSection>
)
