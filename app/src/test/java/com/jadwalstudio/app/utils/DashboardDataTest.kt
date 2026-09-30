package com.jadwalstudio.app.utils

import com.jadwalstudio.app.ui.screens.ScheduleItem
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DashboardDataTest {
    private fun monday(hour: Int) = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
        clear(); set(2026, Calendar.SEPTEMBER, 28, hour, 0)
    }
    private fun schedule(id: String, day: String, time: String) =
        ScheduleItem(id, id, day, time, "17:00", "", "")

    @Test fun choosesTodayInsteadOfEarliestClockOnAnotherDay() {
        val monday = schedule("today", "Senin", "10:00")
        assertEquals(monday, nextSchedule(listOf(schedule("tomorrow", "Selasa", "07:00"), monday), monday(9)))
    }
    @Test fun skipsPastStarts() {
        val tomorrow = schedule("next", "Selasa", "07:00")
        assertEquals(tomorrow, nextSchedule(listOf(schedule("past", "Senin", "08:00"), tomorrow), monday(9)))
    }
    @Test fun wrapsToNextWeek() {
        val nextMonday = schedule("next", "Senin", "08:00")
        assertEquals(nextMonday, nextSchedule(listOf(nextMonday, schedule("later", "Selasa", "08:00")), monday(9).apply { add(Calendar.DAY_OF_YEAR, 6) }))
    }
    @Test fun exactStartAndEmptySchedules() {
        val current = schedule("now", "Senin", "09:00")
        assertEquals(current, nextSchedule(listOf(current), monday(9)))
        assertNull(nextSchedule(emptyList(), monday(9)))
        assertNull(nextSchedule(listOf(schedule("bad", "Senin", "25:00")), monday(9)))
    }
    @Test fun groupsWithoutTasksRemainVisibleAndClickable() {
        val projects = dashboardProjects(listOf("a" to "Kelompok A", "b" to "Kelompok B"), emptyList())
        assertEquals(listOf("a", "b"), projects.map { it.id })
        assertEquals(listOf("Kelompok A", "Kelompok B"), projects.map { it.name })
        assertTrue(projects.all { it.tasks.isEmpty() })
        assertTrue(dashboardProjects(emptyList(), emptyList()).isEmpty())
    }
}
