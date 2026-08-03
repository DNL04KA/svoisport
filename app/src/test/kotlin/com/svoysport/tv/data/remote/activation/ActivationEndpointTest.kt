package com.svoysport.tv.data.remote.activation

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationEndpointTest {

    @Test
    fun `maps known backend statuses and treats unknown as waiting`() {
        assertEquals(ActivationStatus.ACTIVATED, ActivationEndpoint.parseStatus("activated"))
        assertEquals(ActivationStatus.EXPIRED, ActivationEndpoint.parseStatus("EXPIRED"))
        assertEquals(ActivationStatus.WAITING, ActivationEndpoint.parseStatus("waiting"))
        assertEquals(ActivationStatus.WAITING, ActivationEndpoint.parseStatus("payment_pending"))
        assertEquals(ActivationStatus.WAITING, ActivationEndpoint.parseStatus(null))
    }

    @Test
    fun `maps account TV limit status`() {
        assertEquals(ActivationStatus.DEVICE_LIMIT, ActivationEndpoint.parseStatus("device_limit"))
    }

    @Test
    fun `encodes query parameter values`() {
        assertEquals(
            "https://example.test/api/check-activation-session.php?sessionId=id+with%2Fsymbols%3F",
            ActivationEndpoint.statusUrl("https://example.test/api/", "id with/symbols?")
        )
        assertEquals(
            "https://example.test/api/check-subscription.php?device_id=tv+room%2F1",
            ActivationEndpoint.subscriptionUrl("https://example.test/api", "tv room/1")
        )
    }

    @Test
    fun `create request includes selected plan when buying and omits it when activating existing subscription`() {
        assertEquals(
            ActivationCreateRequest(deviceId = "tv-1", planId = "month_3"),
            ActivationEndpoint.createRequest("tv-1", "month_3")
        )
        assertEquals(
            ActivationCreateRequest(deviceId = "tv-1", planId = null),
            ActivationEndpoint.createRequest("tv-1", null)
        )
    }
}
