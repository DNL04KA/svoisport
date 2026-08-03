package com.svoysport.tv.data.remote.activation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ActivationCreateRequest(
    @com.google.gson.annotations.SerializedName("device_id") val deviceId: String,
    @com.google.gson.annotations.SerializedName("plan_id") val planId: String?
)

internal object ActivationEndpoint {
    fun createRequest(deviceId: String, planId: String?): ActivationCreateRequest =
        ActivationCreateRequest(deviceId, planId)

    fun parseStatus(value: String?): ActivationStatus = when (value?.lowercase()) {
        "activated" -> ActivationStatus.ACTIVATED
        "expired" -> ActivationStatus.EXPIRED
        "device_limit" -> ActivationStatus.DEVICE_LIMIT
        else -> ActivationStatus.WAITING
    }

    fun statusUrl(baseUrl: String, sessionId: String): String =
        "${baseUrl.trimEnd('/')}/check-activation-session.php?sessionId=${encode(sessionId)}"

    fun subscriptionUrl(baseUrl: String, deviceId: String): String =
        "${baseUrl.trimEnd('/')}/check-subscription.php?device_id=${encode(deviceId)}"

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
