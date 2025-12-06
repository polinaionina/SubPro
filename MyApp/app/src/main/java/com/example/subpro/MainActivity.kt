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

import androidx.appcompat.app.AppCompatActivity
import com.example.subpro.ui.theme.TelegramWebViewActivity

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Calendar : Screen("calendar")
    object Add : Screen("add_choice")
    object Form : Screen("add_form")
}

fun Month.toRussianMonthName(): String {
    return when (this) {
        Month.JANUARY -> "Январь"
        Month.FEBRUARY -> "Февраль"
        Month.MARCH -> "Март"
        Month.APRIL -> "Апрель"
        Month.MAY -> "Май"
        Month.JUNE -> "Июнь"
        Month.JULY -> "Июль"
        Month.AUGUST -> "Август"
        Month.SEPTEMBER -> "Сентябрь"
        Month.OCTOBER -> "Октябрь"
        Month.NOVEMBER -> "Ноябрь"
        Month.DECEMBER -> "Декабрь"
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Тут активация кнопки входа
        startTelegramLogin()
    }

    private fun startTelegramLogin() {
        val intent = Intent(this, TelegramWebViewActivity::class.java)
        startActivity(intent)
    }
}
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendTestNotification()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        setContent {
            AppNavigation(
                onSendNotification = { requestNotificationPermissionAndSend() }
            )
        }
    }

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


@Composable
fun AppNavigation(onSendNotification: () -> Unit) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    val bottomBarScreens = listOf(Screen.Main.route, Screen.Calendar.route)
    val showBottomBar = currentScreen.route in bottomBarScreens
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
                is Screen.Main -> MainScreen(onSendNotification = onSendNotification)
                is Screen.Calendar -> CalendarScreen()

                is Screen.Add -> SubscriptionChoiceScreen(
                    onAddCustom = { currentScreen = Screen.Form }, // Переход к форме
                    onSuccess = { currentScreen = Screen.Main }
                )

                is Screen.Form -> AddSubscriptionScreen(
                    context = LocalContext.current,
                    onBack = { currentScreen = Screen.Main }
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
        horizontalAlignment = Alignment.Start
    ) {

        Icon(
            painter = painterResource(id = R.drawable.strelka),
            contentDescription = "на главную",
            modifier = Modifier.size(48.dp).
            clickable { onSuccess()},
            tint = Color.Unspecified
        )

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
                //Text("${template.name} | ${template.price.toInt()} ₽ | ${template.period.asRussianText()}",
                    //textAlign = TextAlign.Start)
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
fun MainScreen(onSendNotification: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("SubPro",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFE68C3A),
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(35.dp))
        Button(
            onClick = { onSendNotification() },
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp),
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
    }
}

@Composable
fun CalendarScreen() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val subscriptions = remember { SubscriptionService.getAll() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { currentMonth = currentMonth.minusMonths(1); selectedDate = null }) {
                Text("←")
            }

            Text(
                text = "${currentMonth.month.toRussianMonthName()} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )

            Button(onClick = { currentMonth = currentMonth.plusMonths(1); selectedDate = null }) {
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
                Text(it)
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
                            Text(date.dayOfMonth.toString())

                            if (isPaymentDay) {
                                Canvas(modifier = Modifier.size(6.dp)) {
                                    drawCircle(Color.Red)
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
        Text("Выберите дату в календаре", style = MaterialTheme.typography.titleMedium)
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
                "💸 Платежи на ${selectedDate.dayOfMonth}.${selectedDate.monthValue}:",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            dailySubscriptions.forEach { sub ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${sub.name} (${sub.provider})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )
                        Text(
                            text = "Цена: ${sub.price} ₽",
                            style = MaterialTheme.typography.bodyLarge
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

    return list
}