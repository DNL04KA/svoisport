package com.svoysport.tv.ui.screens.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionPlanTest {
    @Test
    fun `plans match the latest Figma subscription screen`() {
        assertEquals(
            listOf(
                SubscriptionPlan("month_1", "1 месяц", "9,99", null, "Попробовать сервис", null),
                SubscriptionPlan("month_3", "3 месяца", "8,49", "25,49 BYN", "Экономия 15%", "Популярный выбор"),
                SubscriptionPlan("month_12", "12 месяцев", "6,49", "77,92 BYN", "Самая выгодная цена", "Экономия 35%")
            ),
            subscriptionPlans
        )
    }

    @Test
    fun `popular three month plan is selected by default`() {
        assertEquals("month_3", defaultSubscriptionPlan.id)
    }
}
