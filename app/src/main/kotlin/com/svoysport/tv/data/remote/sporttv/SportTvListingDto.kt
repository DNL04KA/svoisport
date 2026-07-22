package com.svoysport.tv.data.remote.sporttv

/**
 * DTO фида sport-tv.by.
 *
 * Источники:
 *  - https://sport-tv.by/list2.php            — витрина (богатый формат)
 *  - https://sport-tv.by/list2.php?archive=1  — архив записей (DVR HLS + duration)
 *  - https://sport-tv.by/list.php?date=<unix> — расписание конкретного дня
 *
 * list.php не отдаёт поля list2 (id, thumbnail, isPaid, …) — они nullable.
 */
data class SportTvListingDto(
    val date: String,
    val date_end: String,
    val title: String,
    val annotation: String,
    val sport_id: String,
    val source: String,
    val category_id: String,
    // ── только list2.php ──────────────────────────────────────────────
    val id: String? = null,
    val thumbnail: String? = null,
    val poster: String? = null,
    val isPaid: Boolean? = null,
    val is_archive: Boolean? = null,
    val duration: Long? = null
)
