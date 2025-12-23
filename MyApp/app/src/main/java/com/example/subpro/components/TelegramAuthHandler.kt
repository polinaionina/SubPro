package com.example.subpro.mutil

import android.content.Intent
import android.content.SharedPreferences

object TelegramAuthHandler {

    fun handle(intent: Intent?, prefs: SharedPreferences): Boolean {
        val data = intent?.data
        if (data != null && data.scheme == "subpro" && data.host == "auth") {
            val token = data.getQueryParameter("token")
            val telegramId = data.getQueryParameter("telegramId")
            if (token != null && telegramId != null) {
                prefs.edit().apply {
                    putString("jwt_token", token)
                    putString("telegram_id", telegramId)
                    apply()
                }
                return true
            }
        }
        return false
    }
}
