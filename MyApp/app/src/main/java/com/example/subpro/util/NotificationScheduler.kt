package com.example.subpro.util

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.example.subpro.model.Subscription
import com.example.subpro.model.nextPayment
import java.time.Instant
import java.time.ZoneId

object NotificationScheduler {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    const val CHANNEL_ID = "subscription_channel"

    fun scheduleNotification(context: Context, subscription: Subscription) {
        if (subscription.notificationDaysBefore <= 0) return

        val nextPaymentDate = subscription.nextPayment()

        // 1. Вычисляем время уведомления (в 9:00 утра)
        val notificationDateTime = nextPaymentDate
            .minusDays(subscription.notificationDaysBefore.toLong())
            .atTime(9, 0)
            .atZone(ZoneId.systemDefault())

        if (notificationDateTime.toInstant().isBefore(java.time.Instant.now())) return

        // Конвертируем в миллисекунды для AlarmManager
        val notificationTimeMillis = notificationDateTime.toEpochSecond() * 1000

        // 2. Создаем Intent и PendingIntent
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("SUB_NAME", subscription.name)
            putExtra("SUB_PRICE", subscription.price)
            putExtra("SUB_DAYS_BEFORE", subscription.notificationDaysBefore)
            putExtra("SUB_ID", subscription.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            subscription.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Устанавливаем будильник с проверкой разрешения
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 (API 31) и выше требуют явной проверки
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
            } else {
                // Если разрешение не предоставлено, используем set, который не требует разрешения,
                // но может быть менее точным.
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
                // ВАЖНО: Мы должны показать пользователю, что он может включить это разрешение.
            }
        } else {
            // Для старых версий (до API 31)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotification(context: Context, subscriptionId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            subscriptionId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }
}