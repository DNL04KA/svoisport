package com.svoysport.tv.data.remote.activation

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Боевая реализация активации против PHP-эндпоинтов sport-tv.by.
 *
 * НЕ забиндена по умолчанию — чтобы включить, поменяйте биндинг в AppModule
 * с [MockActivationApi] на [RealActivationApi]. Эндпоинты должны существовать
 * на сервере (см. контракт в [ActivationApi]).
 */
@Singleton
class RealActivationApi @Inject constructor() : ActivationApi {

    private val gson = Gson()

    private data class CreateReq(@SerializedName("device_id") val deviceId: String)
    private data class CreateRes(
        @SerializedName("sessionId") val sessionId: String?,
        @SerializedName("qrUrl")     val qrUrl: String?
    )
    private data class StatusRes(@SerializedName("status") val status: String?)
    private data class SubRes(
        @SerializedName("active") val active: Boolean = false,
        @SerializedName("until")  val until: String? = null
    )

    override suspend fun createSession(deviceId: String): ActivationSession = withContext(Dispatchers.IO) {
        val body = gson.toJson(CreateReq(deviceId))
        val json = post("$BASE/create-activation-session.php", body)
        val res  = gson.fromJson(json, CreateRes::class.java)
        val sid  = res.sessionId ?: error("Пустой sessionId от сервера")
        val qr   = res.qrUrl ?: "$SITE/activate?session=$sid"
        ActivationSession(sid, qr)
    }

    override suspend fun checkSession(sessionId: String): ActivationStatus = withContext(Dispatchers.IO) {
        val json = get("$BASE/check-activation-session.php?sessionId=$sessionId")
        when (gson.fromJson(json, StatusRes::class.java).status?.lowercase()) {
            "activated" -> ActivationStatus.ACTIVATED
            "expired"   -> ActivationStatus.EXPIRED
            else        -> ActivationStatus.WAITING
        }
    }

    override suspend fun checkSubscription(deviceId: String): SubscriptionInfo = withContext(Dispatchers.IO) {
        val json = get("$BASE/check-subscription.php?device_id=$deviceId")
        val res  = gson.fromJson(json, SubRes::class.java)
        SubscriptionInfo(res.active, res.until)
    }

    private fun get(url: String): String = openConn(url, "GET").run {
        try { inputStream.bufferedReader().use { it.readText() } } finally { disconnect() }
    }

    private fun post(url: String, body: String): String = openConn(url, "POST").run {
        try {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            OutputStreamWriter(outputStream).use { it.write(body) }
            inputStream.bufferedReader().use { it.readText() }
        } finally { disconnect() }
    }

    private fun openConn(url: String, method: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("User-Agent", "SvoySportTV/1.0 (Android TV)")
        }

    private companion object {
        const val SITE = "https://sport-tv.by"
        const val BASE = "$SITE/api"
    }
}
