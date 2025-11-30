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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.YearMonth
import com.example.subpro.ui.theme.AddSubscriptionScreen
import androidx.compose.ui.platform.LocalContext
import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.Subscription
import com.example.subpro.model.nextPayment 

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) sendTestNotification()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        setContent {
            TwoScreenApp(
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
fun TwoScreenApp(onSendNotification: () -> Unit) {
    var screen by remember { mutableStateOf("main") }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (screen == "main") {

                Spacer(Modifier.height(40.dp))
                Text("Первый экран", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                Button(onClick = { screen = "calendar" }) {
                    Text("Перейти на второй экран")
                }

                Spacer(Modifier.height(20.dp))

                Button(onClick = { onSendNotification() }) {
                    Text("Показать уведомление")
                }

                Spacer(Modifier.height(20.dp))

                Button(onClick = { screen = "add" }) {
                    Text("Добавить подписку")
                }

            } else if (screen == "calendar") {

                Spacer(Modifier.height(20.dp))
                Button(onClick = { screen = "main" }) { Text("Назад") }
                Spacer(Modifier.height(20.dp))
                CalendarScreen()

            } else if (screen == "add") {

                AddSubscriptionScreen(
                    context = LocalContext.current,
                    onBack = { screen = "main" }
                )
            }
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
                text = "${currentMonth.month} ${currentMonth.year}",
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