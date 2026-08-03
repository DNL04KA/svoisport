package com.svoysport.tv.data.remote.activation

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.svoysport.tv.BuildConfig
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
 * Используется по умолчанию и обращается к опубликованным Vercel-эндпоинтам.
 */
@Singleton
class RealActivationApi @Inject constructor() : ActivationApi {

    private val gson = Gson()

    private data class CreateRes(
        @SerializedName("sessionId") val sessionId: String?,
        @SerializedName("qrUrl")     val qrUrl: String?
    )
    private data class StatusRes(@SerializedName("status") val status: String?)
    private data class SubRes(
        @SerializedName("active") val active: Boolean = false,
        @SerializedName("until")  val until: String? = null
    )
    private data class DevicesRes(val devices: List<DeviceRes> = emptyList())
    private data class DeviceRes(val id: String, val name: String, @SerializedName("last_seen") val lastSeen: String?, @SerializedName("is_current") val isCurrent: Boolean)

    override suspend fun createSession(deviceId: String, planId: String?): ActivationSession = withContext(Dispatchers.IO) {
        val request = ActivationEndpoint.createRequest(deviceId, planId).copy(
            deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
        )
        val body = gson.toJson(request)
        val json = post("$baseUrl/create-activation-session.php", body)
        val res  = gson.fromJson(json, CreateRes::class.java)
        val sid  = res.sessionId ?: error("Пустой sessionId от сервера")
        val qr   = res.qrUrl ?: error("Пустой qrUrl от сервера")
        ActivationSession(sid, qr)
    }

    override suspend fun checkSession(sessionId: String): ActivationStatus = withContext(Dispatchers.IO) {
        val json = get(ActivationEndpoint.statusUrl(baseUrl, sessionId))
        ActivationEndpoint.parseStatus(gson.fromJson(json, StatusRes::class.java).status)
    }

    override suspend fun checkSubscription(deviceId: String): SubscriptionInfo = withContext(Dispatchers.IO) {
        val json = get(ActivationEndpoint.subscriptionUrl(baseUrl, deviceId))
        val res  = gson.fromJson(json, SubRes::class.java)
        SubscriptionInfo(res.active, res.until)
    }

    override suspend fun devices(deviceId: String): List<LinkedDevice> = withContext(Dispatchers.IO) {
        val json = get("$baseUrl/devices.php?current_device_id=${java.net.URLEncoder.encode(deviceId, "UTF-8")}")
        gson.fromJson(json, DevicesRes::class.java).devices.map { LinkedDevice(it.id, it.name, it.lastSeen, it.isCurrent) }
    }

    override suspend fun disconnectDevice(currentDeviceId: String, targetDeviceId: String?, allOthers: Boolean) = withContext(Dispatchers.IO) {
        post("$baseUrl/disconnect-device.php", gson.toJson(mapOf("current_device_id" to currentDeviceId, "target_device_id" to targetDeviceId, "all_others" to allOthers)))
        Unit
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

    private val baseUrl = BuildConfig.ACTIVATION_API_BASE_URL.trimEnd('/')
}
