package com.svoysport.tv.data.remote.sporttv

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Лёгкий клиент к публичному фиду sport-tv.by.
 * Эндпоинт отдаёт JSON-массив трансляций (см. [SportTvListingDto]).
 *
 * Используем HttpURLConnection + Gson, чтобы не тащить отдельный HTTP-стек:
 * запрос редкий (раз на загрузку главной) и тело небольшое (~5 КБ).
 */
@Singleton
class SportTvApi @Inject constructor() {

    private val gson = Gson()

    /**
     * @param dateSec unix-таймстамп (секунды) для расписания конкретного дня —
     *                уходит в list.php (только он понимает ?date=);
     *                null — витрина из list2.php (реальные превью, isPaid, id).
     */
    suspend fun fetchListing(dateSec: Long? = null): List<SportTvListingDto> =
        fetch(if (dateSec != null) "$DAY_URL?date=$dateSec" else LISTING_URL)

    /** Архив записей: DVR-HLS source, duration (сек), реальные превью. */
    suspend fun fetchArchive(): List<SportTvListingDto> = fetch(ARCHIVE_URL)

    private suspend fun fetch(url: String): List<SportTvListingDto> = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", USER_AGENT)
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw SportTvException("sport-tv.by вернул HTTP $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<SportTvListingDto>>() {}.type
            gson.fromJson<List<SportTvListingDto>>(body, type) ?: emptyList()
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val LISTING_URL = "https://sport-tv.by/list2.php"
        private const val ARCHIVE_URL = "https://sport-tv.by/list2.php?archive=1"
        private const val DAY_URL     = "https://sport-tv.by/list.php"
        private const val TIMEOUT_MS = 15_000
        private const val USER_AGENT = "SvoySportTV/1.0 (Android TV)"
    }
}

class SportTvException(message: String) : Exception(message)
