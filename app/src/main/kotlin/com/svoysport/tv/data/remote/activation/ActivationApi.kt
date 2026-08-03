package com.svoysport.tv.data.remote.activation

/** Статус сессии активации (совпадает со значениями бэкенда sport-tv.by). */
enum class ActivationStatus { WAITING, ACTIVATED, EXPIRED, DEVICE_LIMIT }

/** Ответ create-activation-session: id сессии + ссылка для QR. */
data class ActivationSession(val sessionId: String, val qrUrl: String)

/** Состояние подписки устройства. */
data class SubscriptionInfo(val active: Boolean, val until: String?)

/**
 * Контракт активации устройства по QR.
 *
 * Бэкенд (PHP + БД на sport-tv.by):
 *  - createSession   → POST create-activation-session.php  {device_id} → {sessionId, qrUrl}
 *  - checkSession    → GET  check-activation-session.php?sessionId=…    → {status}
 *  - checkSubscription→ GET подписки по device_id                        → {active, until}
 *
 * Клиент опрашивает checkSession каждые ~3 сек, пока не придёт ACTIVATED.
 * Реализации: [RealActivationApi] (по умолчанию) и [MockActivationApi] для офлайн-разработки.
 */
interface ActivationApi {
    suspend fun createSession(deviceId: String, planId: String? = null): ActivationSession
    suspend fun checkSession(sessionId: String): ActivationStatus
    suspend fun checkSubscription(deviceId: String): SubscriptionInfo
}
