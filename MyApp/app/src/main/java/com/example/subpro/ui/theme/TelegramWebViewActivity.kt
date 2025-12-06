package com.example.subpro.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.example.subpro.data.ApiClient
import kotlinx.coroutines.*

class TelegramWebViewActivity : ComponentActivity() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // если используешь callback-страницу, можешь поймать url и закрыть Activity
                if (url != null && url.contains("/auth/verify")) {
                    // можно прочитать параметры из url при необходимости
                    finish()
                    return true
                }
                return false
            }
        }

        // Запросим /auth/start и загрузим в WebView полученный URL
        scope.launch {
            try {
                val response = ApiClient.api.startAuth()
                // response.url — должен быть полный URL вида https://yourserver/.../telegram-login.html?nonce=...
                webView.loadUrl(response.url)
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
