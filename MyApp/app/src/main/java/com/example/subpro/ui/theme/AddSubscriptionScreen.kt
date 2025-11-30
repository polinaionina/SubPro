package com.example.subpro.ui.theme

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.SubscriptionPeriod
import com.example.subpro.util.NotificationScheduler
import java.time.LocalDate

fun getAvailableNotificationDays(period: SubscriptionPeriod): List<Int> {
    return when (period) {
        SubscriptionPeriod.WEEKLY -> listOf(1, 3)
        SubscriptionPeriod.MONTHLY -> listOf(1, 3, 7)
        SubscriptionPeriod.YEARLY -> listOf(1, 3, 7, 30)
    }
}

fun SubscriptionPeriod.asRussianText(): String {
    return when (this) {
        SubscriptionPeriod.WEEKLY -> "Еженедельно"
        SubscriptionPeriod.MONTHLY -> "Ежемесячно"
        SubscriptionPeriod.YEARLY -> "Ежегодно"
    }
}

fun getDayText(days: Int, includePrefix: Boolean = false): String {
    val prefix = if (includePrefix) "За " else ""
    return when (days) {
        0 -> "Не уведомлять"
        1 -> "${prefix}1 день"
        3 -> "${prefix}3 дня"
        7 -> "${prefix}7 дней"
        30 -> "${prefix}30 дней"
        else -> "${prefix}$days дней"
    }
}

@Composable
fun AddSubscriptionScreen(
    context: Context,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var period by remember { mutableStateOf(SubscriptionPeriod.MONTHLY) }

    var notificationDaysBefore by remember { mutableStateOf(7) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {

        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Добавить подписку", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Название подписки") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = provider,
            onValueChange = { provider = it },
            label = { Text("Сервис") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Цена") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val dialog = DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        date = LocalDate.of(y, m + 1, d)
                    },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth
                )
                dialog.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Дата начала: $date")
        }

        Spacer(Modifier.height(12.dp))

        Text("Период подписки")

        PeriodDropdownMenu(
            selected = period,
            onSelected = {
                period = it
                val availableDays = getAvailableNotificationDays(it)
                notificationDaysBefore = availableDays.first()
            }
        )

        Spacer(Modifier.height(12.dp))

        Text("Уведомление за")
        NotificationDaysDropdown(
            selectedPeriod = period,
            selectedDays = notificationDaysBefore,
            onDaysSelected = { notificationDaysBefore = it }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val priceDouble = price.toDoubleOrNull() ?: 0.0
                if (name.isBlank() || priceDouble <= 0.0) {
                    return@Button
                }

                SubscriptionService.add(
                    name = name,
                    provider = provider,
                    price = priceDouble,
                    startDate = date,
                    period = period,
                    notificationDaysBefore = notificationDaysBefore
                )

                val newSubscription = SubscriptionService.getAll().last()

                if (notificationDaysBefore > 0) {
                    NotificationScheduler.scheduleNotification(context, newSubscription)
                }

                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отмена")
        }
    }
}


@Composable
fun NotificationDaysDropdown(
    selectedPeriod: SubscriptionPeriod,
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableDays = getAvailableNotificationDays(selectedPeriod)

    LaunchedEffect(selectedPeriod) {
        if (!availableDays.contains(selectedDays)) {
            onDaysSelected(availableDays.first())
        }
    }

    // В кнопке оставляем префикс "За", чтобы не было слишком коротко
    val selectedText = getDayText(selectedDays, includePrefix = true)

    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableDays.forEach { days ->
                // В меню убираем префикс "За"
                val dayText = getDayText(days, includePrefix = false)
                DropdownMenuItem(
                    text = { Text(dayText) },
                    onClick = {
                        onDaysSelected(days)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun PeriodDropdownMenu(
    selected: SubscriptionPeriod,
    onSelected: (SubscriptionPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val periods = SubscriptionPeriod.values()

    Box {
        Button(onClick = { expanded = true }) {
            Text("Тип: ${selected.asRussianText()}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            periods.forEach {
                DropdownMenuItem(
                    text = { Text(it.asRussianText()) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}