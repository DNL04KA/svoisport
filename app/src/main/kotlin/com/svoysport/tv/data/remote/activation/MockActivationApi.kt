package com.svoysport.tv.data.remote.activation

import com.svoysport.tv.session.SubscriptionManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Мок-бэкенд активации для разработки без сервера.
 *
 * Имитирует сценарий: пользователь сканирует QR и подтверждает подписку на
 * телефоне — на 4-м опросе сессия становится ACTIVATED. Чтобы переключиться на
 * реальные эндпоинты sport-tv.by, замените биндинг в AppModule на
 * [RealActivationApi] — контракт [ActivationApi] тот же.
 */
@Singleton
class MockActivationApi @Inject constructor() : ActivationApi {

    private var polls = 0

    override suspend fun createSession(deviceId: String): ActivationSession {
        delay(500)
        polls = 0
        val sid = "mock-" + System.currentTimeMillis().toString(36)
        return ActivationSession(
            sessionId = sid,
            qrUrl     = "https://sport-tv.by/activate?session=$sid"
        )
    }

    override suspend fun checkSession(sessionId: String): ActivationStatus {
        delay(400)
        polls++
        return if (polls >= 4) ActivationStatus.ACTIVATED else ActivationStatus.WAITING
    }

    override suspend fun checkSubscription(deviceId: String): SubscriptionInfo {
        delay(200)
        return SubscriptionInfo(
            active = SubscriptionManager.isSubscribed.value,
            until  = SubscriptionManager.subscribedUntil.value
        )
    }

    companion object {
        /** Дата окончания подписки для мока — сегодня + 1 год, по-русски. */
        fun mockUntilDate(): String {
            val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
            return SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(cal.time)
        }
    }
}
