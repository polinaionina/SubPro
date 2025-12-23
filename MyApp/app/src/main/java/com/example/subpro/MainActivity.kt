package com.example.subpro

import AppNavigation
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.subpro.data.SubscriptionService
import com.example.subpro.mutil.NotificationHelper
import com.example.subpro.mutil.TelegramAuthHandler

class MainActivity : ComponentActivity() {

    private lateinit var notificationHelper: NotificationHelper
    var telegramAuthSuccess by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHelper = NotificationHelper(this)
        notificationHelper.createChannel()

        SubscriptionService.init(this)

        handleIntent(intent)

        setContent {
            AppNavigation(
                notificationHelper = notificationHelper,
                onSendNotification = {
                    notificationHelper.requestPermissionAndSend(
                        title = "Обычное уведомление",
                        text = ""
                    )
                },
                isTelegramAuthSuccess = telegramAuthSuccess
            )
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        telegramAuthSuccess = TelegramAuthHandler.handle(intent, prefs)
    }
}
