package com.svoysport.tv.session

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

/**
 * Состояние подписки и стабильный device_id устройства.
 *
 * device_id генерируется один раз и хранится в SharedPreferences — именно он
 * привязывается к подписке на сайте при активации по QR. Флаг [isSubscribed] и
 * дата [subscribedUntil] обновляются после успешной активации.
 */
object SubscriptionManager {

    private const val PREFS       = "subscription"
    private const val KEY_DEVICE  = "device_id"
    private const val KEY_ACTIVE  = "active"
    private const val KEY_UNTIL   = "until"

    private lateinit var prefs: SharedPreferences

    val deviceId        = mutableStateOf("")
    val isSubscribed    = mutableStateOf(false)
    val subscribedUntil = mutableStateOf<String?>(null)

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val existing = prefs.getString(KEY_DEVICE, null)
        val id = existing ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE, it).apply()
        }
        deviceId.value        = id
        isSubscribed.value    = prefs.getBoolean(KEY_ACTIVE, false)
        subscribedUntil.value = prefs.getString(KEY_UNTIL, null)
    }

    fun activate(until: String) {
        isSubscribed.value    = true
        subscribedUntil.value = until
        prefs.edit().putBoolean(KEY_ACTIVE, true).putString(KEY_UNTIL, until).apply()
    }

    fun clear() {
        isSubscribed.value    = false
        subscribedUntil.value = null
        prefs.edit().putBoolean(KEY_ACTIVE, false).remove(KEY_UNTIL).apply()
    }
}
