package com.example.subpro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.subpro.model.Subscription
import com.example.subpro.model.nextPayment
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarMonthView(
    month: YearMonth,
    subscriptions: List<Subscription>,
    onDateSelected: (LocalDate?) -> Unit
) {
    val days = remember(month) { generateDaysForMonth(month) }
    val today = remember { LocalDate.now() }
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
                Text(it, color = Color(0xFF213E60))
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
                        val isToday = date == today

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isToday) Color(0xFFE68C3A) else Color(0xFF213E60)
                            )

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

fun generateDaysForMonth(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val shift = (firstDay.dayOfWeek.value - 1) % 7
    val days = mutableListOf<LocalDate?>()
    repeat(shift) { days.add(null) }
    repeat(month.lengthOfMonth()) { days.add(month.atDay(it + 1)) }
    while (days.size % 7 != 0) days.add(null)
    return days
}
