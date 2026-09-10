package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeHelper {

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
        return sdf.format(Date(millis))
    }

    fun formatDateOnly(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return sdf.format(Date(millis))
    }

    fun formatTimeOnly(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
        return sdf.format(Date(millis))
    }

    fun getTimeRemainingDescription(deadlineMillis: Long, nowMillis: Long): String {
        val diffMillis = deadlineMillis - nowMillis
        if (diffMillis <= 0) {
            val overdueMillis = -diffMillis
            val overdueMinutes = overdueMillis / (60 * 1000)
            return if (overdueMinutes < 60) {
                "Atrasado há ${overdueMinutes.coerceAtLeast(1)}m"
            } else {
                val hours = overdueMinutes / 60
                val mins = overdueMinutes % 60
                if (mins == 0L) "Atrasado há ${hours}h" else "Atrasado há ${hours}h ${mins}m"
            }
        }

        val remainingMinutes = diffMillis / (60 * 1000)
        return when {
            remainingMinutes < 1 -> "Expira em <1m"
            remainingMinutes < 60 -> "Expira em ${remainingMinutes}m"
            remainingMinutes < 24 * 60 -> {
                val hours = remainingMinutes / 60
                val mins = remainingMinutes % 60
                if (mins == 0L) "Expira em ${hours}h" else "Expira em ${hours}h ${mins}m"
            }
            else -> {
                val days = remainingMinutes / (24 * 60)
                "Expira em ${days}d"
            }
        }
    }

    fun getNextRecurrence(currentDeadline: Long, recurrence: String): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentDeadline
        }
        val now = System.currentTimeMillis()

        do {
            when (recurrence) {
                "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                else -> return currentDeadline
            }
        } while (cal.timeInMillis <= now)

        return cal.timeInMillis
    }

    /**
     * Returns the epoch millis for the beginning of the next recurrence period:
     * - DAILY: 00:00:00 of the following day.
     * - WEEKLY: 00:00:00 of the next week (Monday).
     * - MONTHLY: 00:00:00 of day 1 of the following month.
     */
    fun getNextPeriodStart(recurrence: String, fromTimeMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = fromTimeMillis
        }
        return when (recurrence) {
            "DAILY" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "WEEKLY" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "MONTHLY" -> {
                cal.add(Calendar.MONTH, 1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> fromTimeMillis
        }
    }

    fun getRecurrenceLabel(recurrence: String): String {
        return when (recurrence) {
            "DAILY" -> "Diário"
            "WEEKLY" -> "Semanal"
            "MONTHLY" -> "Mensal"
            else -> "Não repete"
        }
    }

    fun showDateTimePicker(
        context: Context,
        initialMillis: Long = System.currentTimeMillis() + (60 * 60 * 1000), // Default 1 hour from now
        onDateTimeSelected: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = initialMillis
        }

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Now show TimePickerDialog
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        onDateTimeSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // 24-hour format
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    // Quick presets (relative from current time)
    val quickPresets = listOf(
        Preset("Em 1h", 1 * 60 * 60 * 1000L),
        Preset("Em 3h", 3 * 60 * 60 * 1000L),
        Preset("Em 6h", 6 * 60 * 60 * 1000L),
        Preset("Amanhã", 24 * 60 * 60 * 1000L)
    )

    data class Preset(val label: String, val durationMillis: Long)
}
