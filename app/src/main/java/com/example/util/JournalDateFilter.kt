package com.example.util

import com.example.data.JournalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility for filtering and organizing journal entries chronologically and by calendar dates.
 */
object JournalDateFilter {

    /**
     * Checks if a local epoch timestamp falls on the same calendar day as a UTC date picker selection.
     * Material 3 DatePicker outputs milliseconds since epoch representing midnight in UTC.
     */
    fun isSameCalendarDay(timestamp: Long, utcDateMillis: Long): Boolean {
        val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcDateMillis
        }
        val calLocal = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        return calUtc.get(Calendar.YEAR) == calLocal.get(Calendar.YEAR) &&
               calUtc.get(Calendar.MONTH) == calLocal.get(Calendar.MONTH) &&
               calUtc.get(Calendar.DAY_OF_MONTH) == calLocal.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Converts a local timestamp into a UTC midnight timestamp matching what DatePicker produces
     * for the same calendar date.
     */
    fun toUtcMidnight(localTimestamp: Long): Long {
        val calLocal = Calendar.getInstance().apply {
            timeInMillis = localTimestamp
        }
        val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                calLocal.get(Calendar.YEAR),
                calLocal.get(Calendar.MONTH),
                calLocal.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
        }
        return calUtc.timeInMillis
    }

    /**
     * Filters entries by an optional UTC date selection and an optional search query,
     * returning entries sorted in chronological descending order (newest first).
     */
    fun filterAndSortJournals(
        journals: List<JournalEntity>,
        searchQuery: String = "",
        selectedUtcDateMillis: Long? = null
    ): List<JournalEntity> {
        val dateFiltered = if (selectedUtcDateMillis == null) {
            journals
        } else {
            journals.filter { isSameCalendarDay(it.timestamp, selectedUtcDateMillis) }
        }

        val textFiltered = if (searchQuery.isBlank()) {
            dateFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            dateFiltered.filter { entry ->
                entry.title.lowercase().contains(q) ||
                entry.content.lowercase().contains(q) ||
                entry.mood.lowercase().contains(q)
            }
        }

        return textFiltered.sortedByDescending { it.timestamp }
    }

    /**
     * Formats the selected UTC date picker timestamp into a user-friendly label (e.g. "Oct 29, 2026").
     */
    fun formatPickerDateLabel(utcDateMillis: Long): String {
        val calUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcDateMillis
        }
        val cal = Calendar.getInstance().apply {
            set(
                calUtc.get(Calendar.YEAR),
                calUtc.get(Calendar.MONTH),
                calUtc.get(Calendar.DAY_OF_MONTH)
            )
        }
        return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)
    }

    /**
     * Converts a local timestamp into a friendly timeline date group header:
     * - "Today • Oct 29, 2026"
     * - "Yesterday • Oct 28, 2026"
     * - "Oct 24, 2026"
     */
    fun formatTimelineDateHeader(timestamp: Long): String {
        val entryCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance()

        val isSameYear = entryCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        val dayOfYearDiff = nowCal.get(Calendar.DAY_OF_YEAR) - entryCal.get(Calendar.DAY_OF_YEAR)

        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

        return when {
            isSameYear && dayOfYearDiff == 0 -> "Today • $dateStr"
            isSameYear && dayOfYearDiff == 1 -> "Yesterday • $dateStr"
            else -> dateStr
        }
    }

    /**
     * Formats timestamp to time of day string (e.g., "10:30 AM").
     */
    fun formatTimeOnly(timestamp: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * Formats full date and time for entry details.
     */
    fun formatFullDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    /**
     * Groups chronological entries by calendar day (preserving descending order).
     */
    fun groupByDate(journals: List<JournalEntity>): List<Pair<String, List<JournalEntity>>> {
        val grouped = linkedMapOf<String, MutableList<JournalEntity>>()
        for (entry in journals.sortedByDescending { it.timestamp }) {
            val key = formatTimelineDateHeader(entry.timestamp)
            grouped.getOrPut(key) { mutableListOf() }.add(entry)
        }
        return grouped.map { Pair(it.key, it.value) }
    }
}
