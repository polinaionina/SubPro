package com.example.subpro.data

import com.example.subpro.model.Subscription
import com.example.subpro.model.SubscriptionPeriod
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

object SubscriptionService {

    private val idCounter = AtomicInteger(1)
    private val subscriptions = mutableListOf<Subscription>()

    data class SubscriptionTemplate(
        val name: String,
        val provider: String,
        val price: Double,
        val period: SubscriptionPeriod
    )

    fun addFromTemplate(template: SubscriptionService.SubscriptionTemplate) {
        val newSubscription = Subscription(
            id = idCounter.getAndIncrement(),
            name = template.name,
            provider = template.provider,
            price = template.price,

            startDate = LocalDate.now(),

            period = template.period,

            notificationDaysBefore = 3
        )
        subscriptions.add(newSubscription)
    }

    fun add(
        name: String,
        provider: String,
        price: Double,
        startDate: LocalDate,
        period: SubscriptionPeriod,
        notificationDaysBefore: Int
    ) {
        val newSub = Subscription(
            id = idCounter.getAndIncrement(),
            name = name,
            provider = provider,
            price = price,
            startDate = startDate,
            period = period,
            notificationDaysBefore = notificationDaysBefore
        )
        subscriptions.add(newSub)
    }

    fun getAll(): List<Subscription> = subscriptions.toList()
}