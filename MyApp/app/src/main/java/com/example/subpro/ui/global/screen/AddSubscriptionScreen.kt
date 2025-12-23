package com.example.subpro.ui.global.screen

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.subpro.R
import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.SubscriptionPeriod
import com.example.subpro.Notification.NotificationScheduler
import java.time.LocalDate

fun getAvailableNotificationDays(period: SubscriptionPeriod): List<Int> =
    when (period) {
        SubscriptionPeriod.WEEKLY -> listOf(1, 3)
        SubscriptionPeriod.MONTHLY -> listOf(1, 3, 7)
        SubscriptionPeriod.YEARLY -> listOf(1, 3, 7, 30)
    }

fun SubscriptionPeriod.asRussianText(): String = when (this) {
    SubscriptionPeriod.WEEKLY -> "Еженедельная"
    SubscriptionPeriod.MONTHLY -> "Ежемесячная"
    SubscriptionPeriod.YEARLY -> "Ежегодная"
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

enum class Currency(val label: String) { RUB("RUB"), USD("USD") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdownMenu(
    selected: Currency,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .height(65.dp)
                .width(130.dp),
            shape = RoundedCornerShape(15.dp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            textStyle = TextStyle(
                color = Color(0xFF223F61),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color(0xFF94B7EF),
                unfocusedContainerColor = Color(0xFF94B7EF),
                disabledContainerColor = Color(0xFF94B7EF),
                errorContainerColor = Color(0xFF94B7EF),
                focusedLabelColor = Color(0xFF223F61),
                unfocusedLabelColor = Color(0xFF223F61)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Currency.values().forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.label) },
                    onClick = {
                        onSelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddSubscriptionScreen(
    context: Context,
    onBack: () -> Unit,
    subscriptionId: Int?
) {
    val existing = remember(subscriptionId) { subscriptionId?.let { SubscriptionService.getById(it) } }
    val isEdit = existing != null

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var price by remember { mutableStateOf(existing?.price?.toString() ?: "") }
    var date by remember { mutableStateOf(existing?.startDate?.let { LocalDate.parse(it) } ?: LocalDate.now()) }
    var period by remember { mutableStateOf(existing?.period ?: SubscriptionPeriod.MONTHLY) }
    var currency by remember { mutableStateOf(Currency.RUB) }
    var notificationDaysBefore by remember { mutableStateOf(existing?.notificationDaysBefore ?: 7) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.strelka),
                contentDescription = "на главную",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clickable { onBack() },
                tint = Color.Unspecified
            )
            Text(
                if (isEdit) "Редактировать подписку" else "Новая подписка",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xFF213E60)
            )
        }

        Spacer(Modifier.height(20.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(
                    "Название подписки",
                    color = Color(0xFF223F61)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            shape = RoundedCornerShape(15.dp),
            textStyle = TextStyle(
                color = Color(0xFF223F61),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color(0xFF94B7EF),
                unfocusedContainerColor = Color(0xFF94B7EF),
                disabledContainerColor = Color(0xFF94B7EF),
                errorContainerColor = Color(0xFF94B7EF),
                focusedLabelColor = Color(0xFF223F61),
                unfocusedLabelColor = Color(0xFF223F61)
            )
        )

        Spacer(Modifier.height(13.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrencyDropdownMenu(
                selected = currency,
                onSelected = { currency = it }
            )
            Spacer(Modifier.width(13.dp))
            TextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Цена") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(15.dp),
                textStyle = TextStyle(
                    color = Color(0xFF223F61),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF94B7EF),
                    unfocusedContainerColor = Color(0xFF94B7EF),
                    disabledContainerColor = Color(0xFF94B7EF),
                    errorContainerColor = Color(0xFF94B7EF),
                    focusedLabelColor = Color(0xFF223F61),
                    unfocusedLabelColor = Color(0xFF223F61)
                )
            )
        }

        Spacer(Modifier.height(46.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                modifier = Modifier
                    .height(65.dp)
                    .width(170.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF94B7EF)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Дата списания", color = Color(0xFF213E60))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "$date",
                        color = Color(0xFF213E60),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.width(13.dp))

            PeriodDropdownMenu(
                selected = period,
                onSelected = {
                    period = it
                    val availableDays = getAvailableNotificationDays(it)
                    if (!availableDays.contains(notificationDaysBefore)) {
                        notificationDaysBefore = availableDays.first()
                    }
                }
            )
        }

        Spacer(Modifier.height(13.dp))

        NotificationDaysDropdown(
            selectedPeriod = period,
            selectedDays = notificationDaysBefore,
            onDaysSelected = { notificationDaysBefore = it }
        )

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isEdit && existing != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(65.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            SubscriptionService.delete(existing.id)
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Удалить")
                    }
                    Button(
                        onClick = {
                            val priceDouble = price.toDoubleOrNull() ?: 0.0
                            if (name.isBlank() || priceDouble <= 0.0) return@Button

                            SubscriptionService.update(
                                existing.copy(
                                    name = name,
                                    price = priceDouble,
                                    startDate = date.toString(),
                                    period = period,
                                    notificationDaysBefore = notificationDaysBefore
                                )
                            )
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF94B6EF),
                            contentColor = Color(0xFF213E60)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.save),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Сохранить",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        val priceDouble = price.toDoubleOrNull() ?: 0.0
                        if (name.isBlank() || priceDouble <= 0.0) return@Button

                        SubscriptionService.add(
                            name = name,
                            price = priceDouble,
                            startDate = date.toString(),
                            period = period,
                            notificationDaysBefore = notificationDaysBefore
                        )

                        val newSubscription = SubscriptionService.getAll().lastOrNull()
                        if (notificationDaysBefore > 0 && newSubscription != null) {
                            NotificationScheduler.scheduleNotification(context, newSubscription)
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(65.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF94B6EF),
                        contentColor = Color(0xFF213E60)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.save),
                            contentDescription = null,
                            modifier = Modifier.size(35.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Сохранить",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
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

    val selectedText = getDayText(selectedDays, includePrefix = true)

    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .height(65.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF94B6EF)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Напомнить", color = Color(0xFF213E60))
                Spacer(Modifier.height(2.dp))
                Text(
                    selectedText,
                    color = Color(0xFF213E60),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableDays.forEach { days ->
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
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .height(65.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF94B6EF)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Тип подписки", color = Color(0xFF213E60))
                Spacer(Modifier.height(2.dp))
                Text(
                    selected.asRussianText(),
                    color = Color(0xFF213E60),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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