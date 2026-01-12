package com.example.subpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.subpro.model.Subscription
import com.example.subpro.model.nextPayment
import java.time.LocalDate

@Composable
fun SubscriptionDetails(selectedDate: LocalDate?, subscriptions: List<Subscription>) {
    if (selectedDate == null) {
        Text(
            "Выберите дату в календаре",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF213E60)
        )
        return
    }

    val dailySubscriptions = subscriptions.filter { it.nextPayment() == selectedDate }
    var text = "На ${selectedDate.dayOfMonth}.${selectedDate.monthValue} нет платежей."
    if (selectedDate.monthValue < 10)
        text = "На ${selectedDate.dayOfMonth}.0${selectedDate.monthValue} нет платежей."
    if (dailySubscriptions.isEmpty()) {
        Text(
            text,
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
                            text = sub.name,
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