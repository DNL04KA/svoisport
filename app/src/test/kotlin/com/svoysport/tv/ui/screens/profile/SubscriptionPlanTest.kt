package com.svoysport.tv.ui.screens.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionPlanTest {
    @Test
    fun `plans use production payment product ids`() {
        assertEquals(
            listOf(
                "36733" to "1 месяц",
                "36734" to "6 месяцев",
                "36735" to "12 месяцев"
            ),
            subscriptionPlans.map { it.id to it.title }
        )
    }

    @Test
    fun `six month plan is selected by default`() {
        assertEquals("36734", defaultSubscriptionPlan.id)
    }

    @Test
    fun `payment link appends product id as query parameter`() {
        assertEquals(
            "https://sport-tv.by/payment/?id=36735",
            subscriptionPlans.last().paymentUrl
        )
    }

    @Test
    fun `plans display supplied prices`() {
        assertEquals(listOf("7,00", "6,67", "6,25"), subscriptionPlans.map { it.monthlyPrice })
        assertEquals(listOf(null, "40,00 BYN", "75,00 BYN"), subscriptionPlans.map { it.total })
    }
}
