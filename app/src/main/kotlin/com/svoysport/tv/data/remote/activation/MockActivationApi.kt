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
 * офлайн-режим, замените биндинг в AppModule с [RealActivationApi] на этот
 * класс — контракт [ActivationApi] тот же.
 */
@Singleton
class MockActivationApi @Inject constructor() : ActivationApi {
    override suspend fun devices(deviceId: String) = listOf(LinkedDevice(deviceId, "Android TV", null, true))
    override suspend fun disconnectDevice(currentDeviceId: String, targetDeviceId: String?, allOthers: Boolean) = Unit

    private val pollsBySession = mutableMapOf<String, Int>()

    override suspend fun createSession(deviceId: String, planId: String?): ActivationSession {
        delay(500)
        val sid = "mock-" + System.currentTimeMillis().toString(36)
        pollsBySession[sid] = 0
        return ActivationSession(
            sessionId = sid,
            qrUrl     = if (planId != null) "https://sport-tv.by/payment/?id=$planId"
                        else "https://sport-tv.by/activate?session=$sid"
        )
    }

    override suspend fun checkSession(sessionId: String): ActivationStatus {
        delay(400)
        val polls = (pollsBySession[sessionId] ?: 0) + 1
        pollsBySession[sessionId] = polls
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
