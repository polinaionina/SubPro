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

fun Subscription.nextPayment(): LocalDate {
    var nextDate = this.startDate
    while (nextDate.isBefore(LocalDate.now())) {
        nextDate = when (this.period) {
            SubscriptionPeriod.DAILY -> nextDate.plusDays(1)
            SubscriptionPeriod.WEEKLY -> nextDate.plusWeeks(1)
            SubscriptionPeriod.MONTHLY -> nextDate.plusMonths(1)
            SubscriptionPeriod.YEARLY -> nextDate.plusYears(1)
        }
    }
    return nextDate
}