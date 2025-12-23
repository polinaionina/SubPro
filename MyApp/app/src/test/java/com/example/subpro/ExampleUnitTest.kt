package com.example.subpro

import org.junit.Test
import java.time.YearMonth
import org.junit.Assert.*


class MainActivityTest {
    @Test
    fun `february 2024 has 29 days`() {
        val days = generateDaysForMonth(YearMonth.of(2024, 2))
            .filterNotNull()

        assertEquals(29, days.size)
    }

    @Test
    fun `calendar always multiple of 7`() {
        val days = generateDaysForMonth(YearMonth.of(2025, 1))

        assertEquals(0, days.size % 7)
    }
}

class SubscriptionServiceTest {

}