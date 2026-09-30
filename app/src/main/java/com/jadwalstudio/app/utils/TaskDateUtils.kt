package com.jadwalstudio.app.utils

import com.jadwalstudio.app.ui.screens.SubjectTaskItem
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TaskDateUtils {
    private fun parse(value: String): Date? {
        val formats = listOf("yyyy-MM-dd", "d MMM yyyy", "d MMMM yyyy", "dd/MM/yyyy")
        for (locale in listOf(Locale("id", "ID"), Locale.ENGLISH)) {
            for (pattern in formats) {
                val format = SimpleDateFormat(pattern, locale).apply { isLenient = false }
                val position = ParsePosition(0)
                val date = format.parse(value, position)
                if (date != null && position.index == value.length) return date
            }
        }
        return null
    }
    fun normalizeDeadline(value: String): String {
        val text = value.trim().lowercase(Locale.ROOT)
        if (text.isEmpty()) return ""
        val days = when (text) {
            "hari ini", "today", "sekarang" -> 0
            "besok", "tomorrow" -> 1
            "lusa" -> 2
            else -> Regex("^(\\d+)\\s*(hari|minggu)( lagi)?$").matchEntire(text)?.let {
                val count = it.groupValues[1].toIntOrNull() ?: throw IllegalArgumentException("Deadline tidak valid.")
                require(count <= 36500) { "Deadline terlalu jauh." }
                count * if (it.groupValues[2] == "minggu") 7 else 1
            }
        }
        val date = if (days != null) Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }.time
            else parse(value.trim()) ?: throw IllegalArgumentException("Pilih tanggal deadline yang valid dan sertakan tahun.")
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(date)
    }
    fun getDaysRemaining(dueDateStr: String, isUrgent: Boolean = false): Long {
        if (dueDateStr.isBlank()) return Long.MAX_VALUE
        val date = parse(dueDateStr.trim()) ?: return Long.MAX_VALUE
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        // Round rather than truncate so DST changes cannot move a due date by a day.
        return kotlin.math.round((date.time - today.timeInMillis) / 86400000.0).toLong()
    }
    fun getDueStatusLabel(dueDateStr: String, isUrgent: Boolean = false, completed: Boolean = false): String {
        if (completed) return "Selesai"
        if (dueDateStr.isBlank()) return "Tanpa deadline"
        val days = getDaysRemaining(dueDateStr)
        return when {
            days == Long.MAX_VALUE -> "Tanggal tidak valid"
            days < 0 -> "Terlambat"
            days == 0L -> "Hari ini"
            days == 1L -> "Besok"
            days <= 7 -> "$days hari lagi"
            else -> dueDateStr
        }
    }
    fun sortTasksByPriorityAndDueDate(tasks: List<SubjectTaskItem>): List<SubjectTaskItem> =
        tasks.sortedWith(compareBy<SubjectTaskItem> { it.completed }
            .thenBy { getDaysRemaining(it.due) }.thenByDescending { it.isUrgent }.thenBy { it.title })
}
