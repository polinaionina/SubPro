package com.example.subpro.ui.theme

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.SubscriptionPeriod
import java.time.LocalDate

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

        // Дата
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

        // Периодичность
        Text("Период подписки")

        DropdownMenuDemo(
            selected = period,
            onSelected = { period = it }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                SubscriptionService.add(
                    name = name,
                    provider = provider,
                    price = price.toDoubleOrNull() ?: 0.0,
                    startDate = date,
                    period = period
                )
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
fun DropdownMenuDemo(
    selected: SubscriptionPeriod,
    onSelected: (SubscriptionPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val periods = SubscriptionPeriod.values()

    Box {
        Button(onClick = { expanded = true }) {
            Text("Тип: $selected")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            periods.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
