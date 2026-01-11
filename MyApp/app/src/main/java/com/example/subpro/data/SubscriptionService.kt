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
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object SubscriptionService {

    private const val PREFS = "subscriptions_prefs"
    private const val KEY = "subscriptions"
    private const val SERVER_URL =
        "https://droopingly-troughlike-dedra.ngrok-free.dev/api/Subscriptions"

    private lateinit var context: Context
    private val gson = Gson()

    private val idCounter = AtomicInteger(1)

    fun init(context: Context) {
        this.context = context.applicationContext
        restoreIdCounter()
    }

    data class SubscriptionTemplate(
        val name: String,
        val price: Double,
        val period: SubscriptionPeriod
    )

    fun addFromTemplate(template: SubscriptionTemplate) {
        add(
            name = template.name,
            price = template.price,
            startDate = LocalDate.now().toString(),
            period = template.period,
            notificationDaysBefore = 3
        )
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

        sendMetric(newSub)

        sendCreateToServer(newSub)
    }

    fun update(subscription: Subscription) {
        val list = getAllInternal()
        val index = list.indexOfFirst { it.id == subscription.id }
        if (index != -1) {
            list[index] = subscription
            save(list)

            sendUpdateToServer(subscription)
        }
    }


    fun delete(id: Int) {
        val list = getAllInternal()
        save(list.filterNot { it.id == id })
        sendDeleteToServer(id)
    }

    fun getById(id: Int): Subscription? =
        getAllInternal().firstOrNull { it.id == id }

    fun getAll(): List<Subscription> = getAllInternal()

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

    private fun sendMetric(subscription: Subscription) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)

        val body = mapOf(
            "name" to subscription.name,
            "price" to subscription.price,
            "period" to subscription.period.name,
            "isAuthorized" to (token != null)
        )

        val json = gson.toJson(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())



        val request = Request.Builder()
            .url("https://droopingly-troughlike-dedra.ngrok-free.dev/api/metrics")
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

    private fun sendCreateToServer(subscription: Subscription) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null) ?: return

        val body = mapOf(
            "name" to subscription.name,
            "price" to subscription.price,
            "period" to subscription.period.name,
            "nextPaymentDate" to subscription.startDate,
            "notificationDaysBefore" to subscription.notificationDaysBefore
        )

        val json = gson.toJson(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(SERVER_URL)
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return

                try {
                    val json = JSONObject(body)
                    val serverId = json.getLong("id").toInt()

                    val list = getAllInternal()

                    val index = list.indexOfFirst { it.id == subscription.id }
                    if (index != -1) {
                        list[index] = subscription.copy(id = serverId)
                        save(list)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    response.close()
                }
            }

        })
    }

    private fun sendUpdateToServer(subscription: Subscription) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null) ?: return

        val body = mapOf(
            "name" to subscription.name,
            "price" to subscription.price,
            "period" to subscription.period.name,
            "nextPaymentDate" to subscription.startDate,
            "notificationDaysBefore" to subscription.notificationDaysBefore
        )

        val json = gson.toJson(body)
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$SERVER_URL/${subscription.id}")
            .addHeader("Authorization", "Bearer $token")
            .put(requestBody)
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


    private fun sendDeleteToServer(subscriptionId: Int) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null) ?: return

        val request = Request.Builder()
            .url("$SERVER_URL/$subscriptionId")
            .addHeader("Authorization", "Bearer $token")
            .delete()
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