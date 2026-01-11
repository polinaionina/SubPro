package com.example.subpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.subpro.viewmodel.SubscriptionViewModel
import com.example.subpro.ui.components.SubscriptionCard
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.subpro.data.SubscriptionService
import com.example.subpro.mutil.NotificationHelper

@Composable
fun MainScreen(
    notificationHelper: NotificationHelper,
    onSendNotification: () -> Unit,
    onGoToTelegramAuth: () -> Unit,
    onEditSubscription: (Int) -> Unit
) {
    var subscriptions by remember { mutableStateOf(SubscriptionService.getAll()) }

    LaunchedEffect(Unit) {
        subscriptions = SubscriptionService.getAll()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SubPro",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFFE68C3A),
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(35.dp))

        Button(
            onClick = onGoToTelegramAuth,
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE68C3A),
                contentColor = Color.White
            )
        ) {
            Text(
                "Настроить Telegram-уведомления",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        Spacer(Modifier.height(15.dp))

        if (subscriptions.isEmpty()) {
            Text(
                "У вас пока нет подписок",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
        } else {
            val totalPrice = subscriptions.sumOf { it.price }
            Text(
                "Всего подписок: ${subscriptions.size}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Общая сумма: ${totalPrice.toInt()} ₽/мес",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subscriptions) { sub ->
                    SubscriptionCard(
                        subscription = sub,
                        onClick = { onEditSubscription(sub.id) }
                    )
                }
            }
        }
    }
}

