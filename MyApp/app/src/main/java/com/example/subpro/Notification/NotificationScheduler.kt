package com.example.subpro.Notification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.example.subpro.model.Subscription
import com.example.subpro.model.nextPayment
import java.time.ZoneId

object NotificationScheduler {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    const val CHANNEL_ID = "subscription_channel"

    fun scheduleNotification(context: Context, subscription: Subscription) {
        if (subscription.notificationDaysBefore <= 0) return

        val nextPaymentDate = subscription.nextPayment()

        val notificationDateTime = nextPaymentDate
            .minusDays(subscription.notificationDaysBefore.toLong())
            .atTime(9, 0)
            .atZone(ZoneId.systemDefault())

        if (notificationDateTime.toInstant().isBefore(java.time.Instant.now())) return

        val notificationTimeMillis = notificationDateTime.toEpochSecond() * 1000

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

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    notificationTimeMillis,
                    pendingIntent
                )
            }
        } else {
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