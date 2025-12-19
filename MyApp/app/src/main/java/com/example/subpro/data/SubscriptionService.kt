package com.example.subpro.data

import android.annotation.SuppressLint
import android.content.Context
import com.example.subpro.model.Subscription
import com.example.subpro.model.SubscriptionPeriod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

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
            sendSubscriptionEvent("UPDATE", subscription)
        }
    }

    fun delete(id: Int) {
        val list = getAllInternal()
        val deleted = list.firstOrNull { it.id == id }
        val newList = list.filterNot { it.id == id }
        save(newList)

        if (deleted != null) {
            sendSubscriptionEvent("DELETE", deleted)
        }
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
        sendSubscriptionEvent("CREATE", newSub)
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

    private fun sendSubscriptionEvent(
        action: String,
        subscription: Subscription?
    ) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        val token = prefs.getString("jwt_token", null) ?: return
        val deviceId = prefs.getString("local_device_id", null) ?: return

        val body = mapOf(
            "action" to action,
            "deviceId" to deviceId,
            "subscription" to subscription
        )

        val json = gson.toJson(body)

        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ТВОЙ_СЕРВЕР/api/subscriptions/event")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.close()
            }
        })
    }
}