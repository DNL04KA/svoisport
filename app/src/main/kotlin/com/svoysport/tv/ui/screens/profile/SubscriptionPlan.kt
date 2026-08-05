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
    SubscriptionPlan("36733", "1 месяц", null, null, "Актуальная цена на сайте", null),
    SubscriptionPlan("36734", "6 месяцев", null, null, "Актуальная цена на сайте", "Популярный выбор"),
    SubscriptionPlan("36735", "12 месяцев", null, null, "Актуальная цена на сайте", null)
)

val defaultSubscriptionPlan = subscriptionPlans[1]
