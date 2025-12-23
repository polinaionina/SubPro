package com.example.subpro.repository

import com.example.subpro.model.Subscription

interface SubscriptionRepository {
    fun getAll(): List<Subscription>
    fun getById(id: Int): Subscription?
    fun add(name: String, price: Double, startDate: String, period: Any, notificationDaysBefore: Int)
    fun update(subscription: Subscription)
    fun delete(id: Int)
}
