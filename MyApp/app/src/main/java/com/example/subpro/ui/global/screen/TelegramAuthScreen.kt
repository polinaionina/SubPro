import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subpro.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

@Composable
fun TelegramAuthScreen(
    serverBaseUrl: String,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }

    val startAuth: () -> Unit = {
        android.util.Log.d("TG_AUTH", "startAuth clicked")
        val deviceId = prefs.getString(
            "local_device_id",
            UUID.randomUUID().toString()
        ) ?: UUID.randomUUID().toString()

        prefs.edit().putString("local_device_id", deviceId).apply()

        val client = OkHttpClient()

        val json = """
        { "deviceId": "$deviceId" }
    """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$serverBaseUrl/api/auth/start")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        context,
                        "Ошибка сети: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: return
                if (!response.isSuccessful) {
                    println("Server error: $responseBody")
                    return
                }

                val loginUrl = try {
                    JSONObject(responseBody).getString("loginUrl")
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    context.startActivity(intent)
                }
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.strelka),
            contentDescription = "на главную",
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Start)
                .clickable { onBack() },
            tint = Color.Unspecified
        )
        Spacer(Modifier.height(30.dp))
        Text(
            "Настройка Telegram-уведомлений",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(30.dp))

        Button(
            onClick = startAuth,
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            )
        ) {
            Text(
                "Войти через Telegram",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(30.dp))
        Text(
            "Вас перекинет в браузер для входа через Telegram. После успешной аутентификации вы автоматически вернетесь в приложение с сохраненным токеном.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}