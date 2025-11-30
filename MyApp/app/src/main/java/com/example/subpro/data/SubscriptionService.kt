package com.example.subpro.data

import com.example.subpro.model.Subscription
import com.example.subpro.model.SubscriptionPeriod
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

object SubscriptionService {

    private val idCounter = AtomicInteger(1)
    private val subscriptions = mutableListOf<Subscription>()

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