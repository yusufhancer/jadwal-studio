package com.jadwalstudio.app.utils

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskDateUtilsTest {
    @Test fun relativeDatesBecomeFixedDates() {
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        assertEquals(SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(tomorrow.time), TaskDateUtils.normalizeDeadline("Besok"))
        assertEquals(1L, TaskDateUtils.getDaysRemaining(TaskDateUtils.normalizeDeadline("Besok")))
        assertEquals(7L, TaskDateUtils.getDaysRemaining(TaskDateUtils.normalizeDeadline("1 Minggu")))
    }
    @Test fun invalidDateIsRejectedInsteadOfGuessed() {
        listOf("2030-02-30", "2026-13-01", "sometime", "2026-01-01garbage", "999999999999 hari").forEach { value ->
            assertThrows(IllegalArgumentException::class.java) { TaskDateUtils.normalizeDeadline(value) }
        }
    }
    @Test fun indonesianAndIsoDatesReferToSameDay() {
        assertEquals("2030-08-28", TaskDateUtils.normalizeDeadline("28 Agustus 2030"))
        assertEquals("2030-08-28", TaskDateUtils.normalizeDeadline("28/08/2030"))
    }
    @Test fun doneIsNeverOverdue() {
        assertEquals("Selesai", TaskDateUtils.getDueStatusLabel("2000-01-01", completed = true))
        assertEquals("Terlambat", TaskDateUtils.getDueStatusLabel("2000-01-01", completed = false))
    }
    @Test fun noDeadlineDoesNotInventADate() {
        assertEquals("", TaskDateUtils.normalizeDeadline(""))
        assertEquals(Long.MAX_VALUE, TaskDateUtils.getDaysRemaining(""))
        assertEquals("Tanpa deadline", TaskDateUtils.getDueStatusLabel(""))
    }
}
