package com.svoysport.tv.ui.screens.profile

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val monthlyPrice: String,
    val total: String?,
    val hint: String,
    val badge: String?
)

val subscriptionPlans = listOf(
    SubscriptionPlan("month_1", "1 месяц", "9,99", null, "Попробовать сервис", null),
    SubscriptionPlan("month_3", "3 месяца", "8,49", "25,49 BYN", "Экономия 15%", "Популярный выбор"),
    SubscriptionPlan("month_12", "12 месяцев", "6,49", "77,92 BYN", "Самая выгодная цена", "Экономия 35%")
)

val defaultSubscriptionPlan = subscriptionPlans[1]
