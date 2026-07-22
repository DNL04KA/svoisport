package com.svoysport.tv.data.repository

import com.svoysport.tv.data.remote.activation.ActivationApi
import com.svoysport.tv.data.remote.activation.ActivationSession
import com.svoysport.tv.data.remote.activation.ActivationStatus
import com.svoysport.tv.data.remote.activation.SubscriptionInfo
import javax.inject.Inject
import javax.inject.Singleton

/** Тонкая обёртка над [ActivationApi], оборачивает ошибки в [Result]. */
@Singleton
class ActivationRepository @Inject constructor(
    private val api: ActivationApi
) {
    suspend fun startSession(deviceId: String): Result<ActivationSession> =
        runCatching { api.createSession(deviceId) }

    suspend fun pollStatus(sessionId: String): Result<ActivationStatus> =
        runCatching { api.checkSession(sessionId) }

    suspend fun subscription(deviceId: String): Result<SubscriptionInfo> =
        runCatching { api.checkSubscription(deviceId) }
}
