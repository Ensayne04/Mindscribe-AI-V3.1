package com.example

import com.example.data.JournalEntity
import com.example.util.JournalDateFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class JournalDateFilterTest {

    private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun createUtcMidnight(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis
    }

    @Test
    fun `isSameCalendarDay matches timestamp and utc midnight correctly`() {
        val entryTimestamp = createTimestamp(2026, Calendar.OCTOBER, 29, 14, 30)
        val utcSelected = createUtcMidnight(2026, Calendar.OCTOBER, 29)
        val otherDay = createUtcMidnight(2026, Calendar.OCTOBER, 28)

        assertTrue(JournalDateFilter.isSameCalendarDay(entryTimestamp, utcSelected))
        assertFalse(JournalDateFilter.isSameCalendarDay(entryTimestamp, otherDay))
    }

    @Test
    fun `filterAndSortJournals filters by specific date and sorts chronologically descending`() {
        val timeDay1Morning = createTimestamp(2026, Calendar.OCTOBER, 28, 9, 0)
        val timeDay1Evening = createTimestamp(2026, Calendar.OCTOBER, 28, 20, 0)
        val timeDay2 = createTimestamp(2026, Calendar.OCTOBER, 29, 11, 0)

        val entries = listOf(
            JournalEntity(id = 1, title = "Morning Day 1", content = "Walk", mood = "Happy", timestamp = timeDay1Morning),
            JournalEntity(id = 2, title = "Evening Day 1", content = "Reading", mood = "Neutral", timestamp = timeDay1Evening),
            JournalEntity(id = 3, title = "Day 2 Review", content = "Sprint", mood = "Ecstatic", timestamp = timeDay2)
        )

        // Filter for Day 1
        val utcDay1 = createUtcMidnight(2026, Calendar.OCTOBER, 28)
        val filteredDay1 = JournalDateFilter.filterAndSortJournals(
            journals = entries,
            selectedUtcDateMillis = utcDay1
        )

        assertEquals(2, filteredDay1.size)
        // Should be descending: evening before morning
        assertEquals("Evening Day 1", filteredDay1[0].title)
        assertEquals("Morning Day 1", filteredDay1[1].title)

        // No date filter: all 3 sorted descending
        val allSorted = JournalDateFilter.filterAndSortJournals(entries)
        assertEquals(3, allSorted.size)
        assertEquals("Day 2 Review", allSorted[0].title)
    }

    @Test
    fun `groupByDate organizes entries into date sections`() {
        val time1 = createTimestamp(2026, Calendar.OCTOBER, 29, 10, 0)
        val time2 = createTimestamp(2026, Calendar.OCTOBER, 29, 16, 0)
        val time3 = createTimestamp(2026, Calendar.OCTOBER, 28, 12, 0)

        val entries = listOf(
            JournalEntity(id = 1, title = "First", content = "A", mood = "Happy", timestamp = time1),
            JournalEntity(id = 2, title = "Second", content = "B", mood = "Happy", timestamp = time2),
            JournalEntity(id = 3, title = "Yesterday Note", content = "C", mood = "Neutral", timestamp = time3)
        )

        val grouped = JournalDateFilter.groupByDate(entries)
        assertEquals(2, grouped.size)
        // Group 1 has 2 entries (both from Oct 29)
        assertEquals(2, grouped[0].second.size)
        // Group 2 has 1 entry (from Oct 28)
        assertEquals(1, grouped[1].second.size)
    }
}
