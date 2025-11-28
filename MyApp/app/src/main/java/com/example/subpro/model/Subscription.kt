package com.example.subpro.model

import java.time.LocalDate

enum class SubscriptionPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

data class Subscription(
    val id: Int,
    val name: String,
    val provider: String,
    val price: Double,
    val startDate: LocalDate,
    val period: SubscriptionPeriod
)
