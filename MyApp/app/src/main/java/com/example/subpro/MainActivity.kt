package com.example.subpro

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.Subscription
import com.example.subpro.model.SubscriptionPeriod
import com.example.subpro.model.nextPayment
import com.example.subpro.ui.theme.AddSubscriptionScreen
import com.example.subpro.ui.theme.asRussianText
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Context
import android.net.Uri
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.app.Activity

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import org.json.JSONObject

import java.io.IOException
import androidx.compose.ui.platform.LocalDensity


// -------------------------------------------------------------------
// 1. НАВИГАЦИЯ И ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
// -------------------------------------------------------------------

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Calendar : Screen("calendar")
    object Add : Screen("add_choice")
    object Form : Screen("add_form")
    object TelegramAuth : Screen("telegram_auth") // <-- НОВЫЙ ЭКРАН
}

fun Month.toRussianMonthName(): String {
    return when (this) {
        Month.JANUARY -> "Январь"
        Month.FEBRUARY -> "Февраль"
        Month.MARCH -> "Март"
        Month.APRIL -> "Апрель"
        Month.MAY -> "Май"
        Month.JUNE -> "Июнь "
        Month.JULY -> "Июль"
        Month.AUGUST -> "Август"
        Month.SEPTEMBER -> "Сентябрь"
        Month.OCTOBER -> "Октябрь"
        Month.NOVEMBER -> "Ноябрь"
        Month.DECEMBER -> "Декабрь"
    }
}

// -------------------------------------------------------------------
// 2. MAIN ACTIVITY
// -------------------------------------------------------------------

class MainActivity : ComponentActivity() {

    // Состояние для Compose, которое обновляется после Deep Link
    var telegramAuthSuccess by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendTestNotification()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // --- Обработка Deep Link при запуске ---
        handleIntent(intent)
        // ----------------------------------------
        SubscriptionService.init(this)
        setContent {
            AppNavigation(
                onSendNotification = { requestNotificationPermissionAndSend() },
                isTelegramAuthSuccess = telegramAuthSuccess
            )
        }
    }

    // --- Обработка Deep Link при возврате из браузера ---
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Убеждаемся, что intent не null
        val appLinkData: Uri? = intent?.data
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        // Проверяем схему и хост (subpro://auth)
        if (appLinkData != null && appLinkData.scheme == "subpro" && appLinkData.host == "auth") {
            val token = appLinkData.getQueryParameter("token")
            val telegramId = appLinkData.getQueryParameter("telegramId")

            if (token != null && telegramId != null) {
                // Аутентификация успешна. Сохраняем токен и ID
                prefs.edit().apply {
                    putString("jwt_token", token)
                    putString("telegram_id", telegramId)
                    apply()
                }
                println("Telegram Auth Success and Token Saved: ID=$telegramId")
                telegramAuthSuccess = true // Обновляем состояние для Compose
            } else {
                println("Telegram Auth Failed: Missing token or telegramId")
                telegramAuthSuccess = false
            }
        }
    }
    // -----------------------------------------------------------------

    private fun requestNotificationPermissionAndSend() {
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
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "subscription_channel",
                "SubscriptionChannel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о подписках"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendTestNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "subscription_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Срок подписки скоро!")
            .setContentText("Не забудьте продлить подписку через 3 дня")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(this).notify(1001, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

// -------------------------------------------------------------------
// 3. COMPOSE COMPONENTS
// -------------------------------------------------------------------

@Composable
fun AppNavigation(
    onSendNotification: () -> Unit,
    isTelegramAuthSuccess: Boolean
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    val bottomBarScreens = listOf(Screen.Main.route, Screen.Calendar.route)
    val showBottomBar = currentScreen.route in bottomBarScreens

    // Получение Activity для доступа к telegramAuthSuccess
    val context = LocalContext.current
    val activity = remember(context) { context as? MainActivity }

    // Логика перехода на MainScreen после успешного Deep Link
    LaunchedEffect(isTelegramAuthSuccess) {
        if (isTelegramAuthSuccess) {
            currentScreen = Screen.Main
            // Сбрасываем флаг в Activity для предотвращения повторного вызова
            activity?.telegramAuthSuccess = false
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF4F2EF)
        ) {
            when (currentScreen) {
                is Screen.Main -> MainScreen(
                    onSendNotification = onSendNotification,
                    onGoToTelegramAuth = { currentScreen = Screen.TelegramAuth }
                )
                is Screen.Calendar -> CalendarScreen()

                is Screen.Add -> SubscriptionChoiceScreen(
                    onAddCustom = { currentScreen = Screen.Form },
                    onSuccess = { currentScreen = Screen.Main }
                )

                is Screen.Form -> AddSubscriptionScreen(
                    context = LocalContext.current,
                    onBack = { currentScreen = Screen.Main }
                )

                is Screen.TelegramAuth -> TelegramAuthScreen(
                    serverBaseUrl = "https://droopingly-troughlike-dedra.ngrok-free.dev",
                    onBack = { currentScreen = Screen.Main },
                    onAuthSuccess = { currentScreen = Screen.Main } // Этот колбэк больше не нужен благодаря LaunchedEffect, но можно оставить как заглушку
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF4F2EF)
    ) {
        val items = listOf(
            Triple(Screen.Main, "Главная", R.drawable.menu),
            Triple(Screen.Calendar, "Календарь", R.drawable.today),
            Triple(Screen.Add, "Добавить", R.drawable.dobavit)
        )

        items.forEach { (screen, label, iconRes) ->
            val iconSize = if (screen == Screen.Add) 65.dp else 50.dp
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(iconSize),
                        tint = Color.Unspecified
                    )
                },
                label = null,
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFACBACC)
                )
            )
        }
    }
}

@Composable
fun SubscriptionChoiceScreen(
    onAddCustom: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }

    val template = remember {
        SubscriptionService.SubscriptionTemplate(
            name = "ПЛЮС",
            provider = "Яндекс",
            price = 400.0,
            period = SubscriptionPeriod.MONTHLY,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {

        Box(modifier = Modifier.fillMaxWidth().padding(0.dp),

            contentAlignment = Alignment.Center)
        {
            Icon(
                painter = painterResource(id = R.drawable.strelka),
                contentDescription = "на главную",
                modifier = Modifier.align(Alignment.CenterStart).size(48.dp).
                clickable { onSuccess()},
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(25.dp))
            Text("Шаблоны подписок",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xFF213E60))
        }

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                SubscriptionService.addFromTemplate(template)
                message = "Успешно добавлено: ${template.name} (${template.price.toInt()} ₽/${template.period.asRussianText()})"
                onSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF94B6EF),
                contentColor = Color(0xFF213E60)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Icon(
                    painter = painterResource(id = R.drawable.yandex_icon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(13.dp))
                Text("Яндекс ПЛЮС",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Button(
            onClick = onAddCustom,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .height(65.dp)
                .width(200.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF94B6EF),
                contentColor = Color(0xFF213E60)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ){
                Icon(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(2.dp))
                Text("Добавить свое",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start)
            }
        }
    }
}


@Composable
fun MainScreen(
    onSendNotification: () -> Unit,
    onGoToTelegramAuth: () -> Unit
) {
    var subscriptions by remember { mutableStateOf(SubscriptionService.getAll()) }

    LaunchedEffect(Unit) {
        subscriptions = SubscriptionService.getAll()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment. CenterHorizontally
    ) {
        Text(
            "SubPro",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFE68C3A),
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(35.dp))

        // --- КНОПКА ТЕСТОВОГО УВЕДОМЛЕНИЯ ---
        Button(
            onClick = { onSendNotification() },
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF94B6EF),
                contentColor = Color(0xFF213E60)
            )
        ) {
            Text("Показать тестовое уведомление",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start)
        }

        // --- НОВАЯ КНОПКА TELEGRAM ---
        Button(
            onClick = onGoToTelegramAuth, // <-- Вызываем переход на экран аутентификации
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE68C3A), // Контрастный цвет
                contentColor = Color.White
            )
        ) {
            Text("Настроить Telegram-уведомления",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start)
        }

        Spacer(Modifier.height(15.dp)) // Отделяем кнопки от списка подписок

        // --- Существующая логика списка подписок ---
        if (subscriptions.isEmpty()) {
            Text(
                "У вас пока нет подписок",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
        } else {
            val totalPrice = subscriptions.sumOf { it. price }
            Text(
                "Всего подписок: ${subscriptions.size}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Общая сумма: ${totalPrice. toInt()} ₽/мес",
                style = MaterialTheme.typography.titleMedium,
                color = Color. Gray
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement. spacedBy(8.dp)
            ) {
                items(subscriptions) { sub ->
                    SubscriptionCard(subscription = sub)
                }
            }
        }
    }
}

@Composable
fun SubscriptionCard(subscription: Subscription) {
    val nextPayment = subscription.nextPayment()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF94B6EF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subscription.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight. SemiBold,
                    color = Color(0xFF213E60)
                )
                Text(
                    text = subscription. provider,
                    fontSize = 14.sp,
                    color = Color(0xFF213E60). copy(alpha = 0.7f)
                )
                Text(
                    text = "Следующий платёж: ${nextPayment.dayOfMonth}. ${nextPayment.monthValue}. ${nextPayment.year}",
                    fontSize = 12. sp,
                    color = Color(0xFF213E60).copy(alpha = 0.6f)
                )
            }

            Text(
                text = "${subscription.price.toInt()} ₽",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF213E60)
            )
        }
    }
}

@Composable
fun TelegramAuthScreen(
    serverBaseUrl: String,
    onBack: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }

    val startAuth: () -> Unit = {
        val deviceId = prefs.getString(
            "local_device_id",
            UUID.randomUUID().toString()
        ) ?: UUID.randomUUID().toString()

        prefs.edit().putString("local_device_id", deviceId).apply()

        val client = okhttp3.OkHttpClient()

        val json = """
        { "deviceId": "$deviceId" }
    """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())

        val request = okhttp3.Request.Builder()
            .url("$serverBaseUrl/api/auth/start")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: return
                val loginUrl = JSONObject(responseBody).getString("loginUrl")

                // 🔥 ВОТ ЭТО КЛЮЧЕВО

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
            }
        })
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка назад
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
            onClick = startAuth, // <-- Запуск процесса
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3), // Цвет Telegram
                contentColor = Color.White
            )
        ) {
            Text("Войти через Telegram",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium)
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

// -------------------------------------------------------------------
// 4. CALENDAR COMPONENTS (Возвращены для разрешения ссылок)
// -------------------------------------------------------------------

@Composable
fun CalendarScreen() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    var subscriptions by remember { mutableStateOf(SubscriptionService.getAll()) }

    LaunchedEffect(Unit) {
        subscriptions = SubscriptionService.getAll()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { currentMonth = currentMonth.minusMonths(1); selectedDate = null },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF213E60),
                    contentColor = Color(0xFFF4F2EF)
                )) {
                Text("←")
            }

            Text(
                text = "${currentMonth.month.toRussianMonthName()} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF213E60)
            )

            Button(
                onClick = { currentMonth = currentMonth.plusMonths(1); selectedDate = null },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF213E60),
                    contentColor = Color(0xFFF4F2EF)
                )) {
                Text("→")
            }
        }

        Spacer(Modifier.height(16.dp))

        CalendarMonthView(
            month = currentMonth,
            subscriptions = subscriptions,

            onDateSelected = { date -> selectedDate = date }
        )

        Spacer(Modifier.height(24.dp))

        SubscriptionDetails(selectedDate = selectedDate, subscriptions = subscriptions)
    }
}


@Composable
fun CalendarMonthView(
    month: YearMonth,
    subscriptions: List<Subscription>,
    onDateSelected: (LocalDate?) -> Unit
) {
    val days = remember(month) { generateDaysForMonth(month) }

    val paymentDates: List<LocalDate> = remember(subscriptions) {
        subscriptions.map { it.nextPayment() }
    }


    Row(Modifier.fillMaxWidth()) {
        listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(it,
                    color = Color(0xFF213E60))
            }
        }
    }

    Spacer(Modifier.height(8.dp))


    days.chunked(7).forEach { week ->
        Row(Modifier.fillMaxWidth()) {
            week.forEach { date ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .height(50.dp)

                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    if (date != null) {
                        val isPaymentDay = paymentDates.contains(date)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(date.dayOfMonth.toString(),
                                color = Color(0xFF213E60))

                            if (isPaymentDay) {
                                Canvas(modifier = Modifier.size(6.dp)) {
                                    drawCircle(Color(0xFFE68C3A))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SubscriptionDetails(selectedDate: LocalDate?, subscriptions: List<Subscription>) {
    if (selectedDate == null) {
        Text("Выберите дату в календаре", style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF213E60))
        return
    }

    val dailySubscriptions = subscriptions.filter { it.nextPayment() == selectedDate }

    if (dailySubscriptions.isEmpty()) {
        Text(
            "На ${selectedDate.dayOfMonth}.${selectedDate.monthValue} нет платежей.",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Платежи на ${selectedDate.dayOfMonth}.${selectedDate.monthValue}:",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF213E60)
            )
            Spacer(Modifier.height(8.dp))

            dailySubscriptions.forEach { sub ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFBDCBE4))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${sub.name} (${sub.provider})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                            color = Color(0xFF374658)
                        )
                        Text(
                            text = "Цена: ${sub.price} ₽",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF374658)
                        )
                    }
                }
            }
        }
    }
}


fun generateDaysForMonth(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    val shift = (firstDay.dayOfWeek.value - 1) % 7

    val list = mutableListOf<LocalDate?>()

    repeat(shift) { list.add(null) }

    for (i in 1..daysInMonth) {
        list.add(month.atDay(i))
    }
    val remainder = list.size % 7
    if (remainder != 0) {
        repeat(7 - remainder) { list.add(null) }
    }
    return list
}