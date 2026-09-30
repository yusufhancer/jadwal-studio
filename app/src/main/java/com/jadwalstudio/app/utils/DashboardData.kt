package com.jadwalstudio.app.utils

import com.jadwalstudio.app.ui.screens.ProjectCardData
import com.jadwalstudio.app.ui.screens.ScheduleItem
import com.jadwalstudio.app.ui.screens.SubjectTaskItem
import java.util.Calendar

fun dashboardProjects(groups: List<Pair<String, String>>, tasks: List<SubjectTaskItem>): List<ProjectCardData> =
    groups.map { (id, name) -> ProjectCardData(name, tasks.filter { it.groupId == id }, id) }

/** Weekly schedule: choose the next start, including next week's occurrence. */
fun nextSchedule(schedules: List<ScheduleItem>, now: Calendar = Calendar.getInstance()): ScheduleItem? {
    val days = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    return schedules.mapNotNull { item ->
        val day = days.indexOfFirst { it.equals(item.day, ignoreCase = true) }
        val time = item.startTime.split(':').map { it.toIntOrNull() }
        val hour = time.getOrNull(0)
        val minute = time.getOrNull(1)
        if (day < 0 || time.size != 2 || hour == null || minute == null || hour !in 0..23 || minute !in 0..59) return@mapNotNull null
        val start = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, (day + 1 - get(Calendar.DAY_OF_WEEK) + 7) % 7)
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis < now.timeInMillis) add(Calendar.DAY_OF_YEAR, 7)
        }
        item to start.timeInMillis
    }.minByOrNull { it.second }?.first
}
