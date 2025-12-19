package com.example.subpro.model

import java.time.LocalDate

enum class SubscriptionPeriod {
    WEEKLY, MONTHLY, YEARLY
}

data class Subscription(
    val id: Int,
    val name: String,
    val provider: String,
    val price: Double,
    val startDate: String,
    val period: SubscriptionPeriod,
    val notificationDaysBefore: Int
)

fun Subscription.nextPayment(): LocalDate {
    var nextDate = LocalDate.parse(startDate)

    while (!nextDate.isAfter(LocalDate.now())) {
        nextDate = when (this.period) {
            SubscriptionPeriod.WEEKLY -> nextDate.plusWeeks(1)
            SubscriptionPeriod.MONTHLY -> nextDate.plusMonths(1)
            SubscriptionPeriod.YEARLY -> nextDate.plusYears(1)
        }
    }
    return nextDate
}