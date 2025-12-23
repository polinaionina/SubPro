package com.example.subpro.repository

import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.Subscription

class DefaultSubscriptionRepository : SubscriptionRepository {
    override fun getAll(): List<Subscription> = SubscriptionService.getAll()
    override fun getById(id: Int): Subscription? = SubscriptionService.getById(id)
    override fun add(name: String, price: Double, startDate: String, period: Any, notificationDaysBefore: Int) {
        @Suppress("UNCHECKED_CAST")
        SubscriptionService.add(name, price, startDate, period as com.example.subpro.model.SubscriptionPeriod, notificationDaysBefore)
    }
    override fun update(subscription: Subscription) = SubscriptionService.update(subscription)
    override fun delete(id: Int) = SubscriptionService.delete(id)
}
