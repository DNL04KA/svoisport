package com.svoysport.tv.ui.screens.profile

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val monthlyPrice: String?,
    val total: String?,
    val hint: String,
    val badge: String?
) {
    val paymentUrl: String
        get() = "https://sport-tv.by/payment/?id=$id"
}

val subscriptionPlans = listOf(
    SubscriptionPlan("36733", "1 месяц", "7,00", null, "Попробовать сервис", null),
    SubscriptionPlan("36734", "6 месяцев", "6,67", "40,00 BYN", "Экономия 2,00 BYN", "Популярный выбор"),
    SubscriptionPlan("36735", "12 месяцев", "6,25", "75,00 BYN", "Самая выгодная цена", "Экономия 9,00 BYN")
)

val defaultSubscriptionPlan = subscriptionPlans[1]
