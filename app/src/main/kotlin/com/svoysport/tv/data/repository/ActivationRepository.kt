package com.svoysport.tv.data.repository

import com.svoysport.tv.data.remote.activation.ActivationApi
import com.svoysport.tv.data.remote.activation.ActivationSession
import com.svoysport.tv.data.remote.activation.ActivationStatus
import com.svoysport.tv.data.remote.activation.SubscriptionInfo
import com.svoysport.tv.data.remote.activation.LinkedDevice
import javax.inject.Inject
import javax.inject.Singleton

/** Тонкая обёртка над [ActivationApi], оборачивает ошибки в [Result]. */
@Singleton
class ActivationRepository @Inject constructor(
    private val api: ActivationApi
) {
    suspend fun startSession(deviceId: String, planId: String? = null): Result<ActivationSession> =
        runCatching { api.createSession(deviceId, planId) }

    suspend fun pollStatus(sessionId: String): Result<ActivationStatus> =
        runCatching { api.checkSession(sessionId) }

    suspend fun subscription(deviceId: String): Result<SubscriptionInfo> =
        runCatching { api.checkSubscription(deviceId) }
    suspend fun devices(deviceId: String): Result<List<LinkedDevice>> = runCatching { api.devices(deviceId) }
    suspend fun disconnect(current: String, target: String? = null, allOthers: Boolean = false): Result<Unit> =
        runCatching { api.disconnectDevice(current, target, allOthers) }
}
