package com.example.subpro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.subpro.model.Subscription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.subpro.model.nextPayment
import java.time.format.DateTimeFormatter

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit
) {
    val nextPayment = subscription.nextPayment()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF213E60)
                )
                Text(
                    text = "Следующий платёж: ${nextPayment.dayOfMonth}. ${nextPayment.monthValue}. ${nextPayment.year}",
                    fontSize = 12.sp,
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
