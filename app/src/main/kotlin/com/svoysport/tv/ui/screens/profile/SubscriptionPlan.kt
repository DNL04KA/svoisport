package com.svoysport.tv.ui.screens.profile

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val fullPrice: String,
    val monthlyEquivalent: String?,
    val hint: String,
    val badge: String?
) {
    val paymentUrl: String
        get() = "https://sport-tv.by/payment/?id=$id"
}

val subscriptionPlans = listOf(
    SubscriptionPlan("36733", "1 месяц", "7,00 BYN", null, "Попробовать сервис", null),
    SubscriptionPlan("36734", "6 месяцев", "40,00 BYN", "≈ 6,67 BYN/мес.", "Экономия 2,00 BYN", "Популярный выбор"),
    SubscriptionPlan("36735", "12 месяцев", "75,00 BYN", "≈ 6,25 BYN/мес.", "Самая выгодная цена", "Экономия 9,00 BYN")
)

val defaultSubscriptionPlan = subscriptionPlans[1]

internal fun subscriptionActionLabel(plan: SubscriptionPlan): String =
    "Оплатить ${plan.fullPrice}"
