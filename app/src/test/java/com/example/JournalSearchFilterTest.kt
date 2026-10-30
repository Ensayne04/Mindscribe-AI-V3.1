package com.example

import com.example.data.JournalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalSearchFilterTest {

    private val entries = listOf(
        JournalEntity(
            id = 1,
            title = "Morning Deep Focus",
            content = "Completed 2 full Pomodoro cycles on the algorithm engine.",
            mood = "Ecstatic",
            timestamp = 1700000000000L
        ),
        JournalEntity(
            id = 2,
            title = "Afternoon Walk and Coffee",
            content = "Took a 20-minute break to recharge and clear mental fog.",
            mood = "Happy",
            timestamp = 1700003600000L
        ),
        JournalEntity(
            id = 3,
            title = "System Architecture Review",
            content = "Reviewed database persistence layers and concurrency bottlenecks.",
            mood = "Neutral",
            timestamp = 1700007200000L
        ),
        JournalEntity(
            id = 4,
            title = "Late Night Debugging",
            content = "Fixed state synchronization issue in the UI layer.",
            mood = "Tired",
            timestamp = 1700010800000L
        )
    )

    private fun filterJournals(journals: List<JournalEntity>, query: String): List<JournalEntity> {
        if (query.isBlank()) return journals
        val q = query.trim().lowercase()
        return journals.filter { entry ->
            entry.title.lowercase().contains(q) ||
            entry.content.lowercase().contains(q) ||
            entry.mood.lowercase().contains(q)
        }
    }

    @Test
    fun `blank search query returns all entries`() {
        val result = filterJournals(entries, "")
        assertEquals(4, result.size)

        val resultWhitespace = filterJournals(entries, "   ")
        assertEquals(4, resultWhitespace.size)
    }

    @Test
    fun `search by title keyword matches case-insensitively`() {
        val result = filterJournals(entries, "deep focus")
        assertEquals(1, result.size)
        assertEquals("Morning Deep Focus", result.first().title)

        val resultUpper = filterJournals(entries, "ARCHITECTURE")
        assertEquals(1, resultUpper.size)
        assertEquals("System Architecture Review", resultUpper.first().title)
    }

    @Test
    fun `search by content snippet matches entries`() {
        val result = filterJournals(entries, "pomodoro")
        assertEquals(1, result.size)
        assertEquals(1, result.first().id)

        val resultBreak = filterJournals(entries, "recharge")
        assertEquals(1, resultBreak.size)
        assertEquals(2, resultBreak.first().id)
    }

    @Test
    fun `search by mood filters correctly`() {
        val result = filterJournals(entries, "ecstatic")
        assertEquals(1, result.size)
        assertEquals("Ecstatic", result.first().mood)

        val resultTired = filterJournals(entries, "tired")
        assertEquals(1, resultTired.size)
        assertEquals("Tired", resultTired.first().mood)
    }

    @Test
    fun `unmatched search query returns empty list`() {
        val result = filterJournals(entries, "nonexistent keyword xyz")
        assertTrue(result.isEmpty())
    }
}
