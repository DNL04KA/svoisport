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
    fun `plans display full charge as primary price`() {
        assertEquals(
            listOf("7,00 BYN", "40,00 BYN", "75,00 BYN"),
            subscriptionPlans.map { it.fullPrice }
        )
        assertEquals(
            listOf(null, "≈ 6,67 BYN/мес.", "≈ 6,25 BYN/мес."),
            subscriptionPlans.map { it.monthlyEquivalent }
        )
    }

    @Test
    fun `payment action states the amount charged`() {
        assertEquals("Оплатить 40,00 BYN", subscriptionActionLabel(defaultSubscriptionPlan))
    }
}
