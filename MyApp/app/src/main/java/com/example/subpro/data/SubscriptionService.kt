package com.example.subpro.data

import android.annotation.SuppressLint
import android.content.Context
import com.example.subpro.model.Subscription
import com.example.subpro.model.SubscriptionPeriod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("StaticFieldLeak")
object SubscriptionService {

    private const val PREFS = "subscriptions_prefs"
    private const val KEY = "subscriptions"

    private lateinit var context: Context
    private val gson = Gson()

    private val idCounter = AtomicInteger(1)

    fun init(context: Context) {
        this.context = context.applicationContext
        restoreIdCounter()
    }

    fun update(subscription: Subscription) {
        val list = getAllInternal()
        val idx = list.indexOfFirst { it.id == subscription.id }
        if (idx != -1) {
            list[idx] = subscription
            save(list)
        }
    }

    fun delete(id: Int) {
        val list = getAllInternal()
        val newList = list.filterNot { it.id == id }
        save(newList)
    }

    fun getById(id: Int): Subscription? = getAllInternal().firstOrNull { it.id == id }

    // ---------- PUBLIC API ----------

    fun addFromTemplate(template: SubscriptionTemplate) {
        val list = getAllInternal()
        val newSub = Subscription(
            id = idCounter.getAndIncrement(),
            name = template.name,
            price = template.price,
            startDate = LocalDate.now().toString(),
            period = template.period,
            notificationDaysBefore = 3
        )
        list.add(newSub)
        save(list)
    }

    fun add(
        name: String,
        price: Double,
        startDate: String,
        period: SubscriptionPeriod,
        notificationDaysBefore: Int
    ) {
        val list = getAllInternal()
        val newSub = Subscription(
            id = idCounter.getAndIncrement(),
            name = name,
            price = price,
            startDate = startDate,
            period = period,
            notificationDaysBefore = notificationDaysBefore
        )
        list.add(newSub)
        save(list)
    }

    fun getAll(): List<Subscription> = getAllInternal()

    // ---------- INTERNAL ----------

    private fun getAllInternal(): MutableList<Subscription> {
        val json = prefs().getString(KEY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Subscription>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun save(list: List<Subscription>) {
        prefs().edit()
            .putString(KEY, gson.toJson(list))
            .apply()
    }

    private fun restoreIdCounter() {
        val maxId = getAllInternal().maxOfOrNull { it.id } ?: 0
        idCounter.set(maxId + 1)
    }

    private fun prefs() =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // ---------- TEMPLATE ----------

    data class SubscriptionTemplate(
        val name: String,
        val price: Double,
        val period: SubscriptionPeriod
    )
}