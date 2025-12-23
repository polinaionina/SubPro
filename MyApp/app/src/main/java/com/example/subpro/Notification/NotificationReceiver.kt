
package com.example.subpro.Notification

import android.Manifest
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

const val CHANNEL_ID = "subscription_channel"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subName = intent.getStringExtra("SUB_NAME") ?: "Подписька"
        val subPrice = intent.getDoubleExtra("SUB_PRICE", 0.0)
        val daysBefore = intent.getIntExtra("SUB_DAYS_BEFORE", 0)
        val subId = intent.getIntExtra("SUB_ID", 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val formattedPrice = String.format("%.2f", subPrice)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Скоро платеж: $subName")
            .setContentText("Напоминаем: $subName (${formattedPrice} ₽) будет списана через $daysBefore дня(ей).")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(subId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}