package com.example.subpro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    // Регистрация запроса разрешения на уведомления
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                sendTestNotification()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        setContent {
            TwoScreenApp()
        }

        // Тестовое уведомление через 5 секунд с проверкой разрешения
        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sendTestNotification()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                sendTestNotification()
            }
        }, 5000)
    }

    // Создание канала уведомлений для Android 8+
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SubscriptionChannel"
            val descriptionText = "Уведомления о подписках"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("subscription_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Отправка тестового уведомления с проверкой разрешения и обработкой SecurityException
    private fun sendTestNotification() {
        // Проверяем разрешение перед отправкой
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Разрешение не получено — уведомление не отправляем
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "subscription_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Срок подписки скоро!")
            .setContentText("Не забудьте продлить подписку через 3 дня")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(1001, builder.build())
            }
        } catch (e: SecurityException) {
            e.printStackTrace() // Разрешение не предоставлено — уведомление не отправлено
        }
    }
}

@Composable
fun TwoScreenApp() {
    var isFirstScreen by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFirstScreen) {
                Text("Первый экран", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { isFirstScreen = false }) {
                    Text("Перейти на второй экран")
                }
            } else {
                Text("Второй экран", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { isFirstScreen = true }) {
                    Text("Назад")
                }
            }
        }
    }
}
