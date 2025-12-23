package com.example.subpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.subpro.data.SubscriptionService
import com.example.subpro.model.Subscription
import com.example.subpro.model.nextPayment
import com.example.subpro.ui.components.CalendarMonthView
import com.example.subpro.ui.components.SubscriptionDetails
import com.example.subpro.viewmodel.SubscriptionViewModel
import toRussianMonthName
import java.time.LocalDate
import java.time.YearMonth

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
                )
            ) { Text("←") }

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
                )
            ) { Text("→") }
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
