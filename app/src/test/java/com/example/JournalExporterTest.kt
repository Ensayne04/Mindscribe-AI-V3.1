package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.JournalEntity
import com.example.util.JournalExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

import androidx.core.content.IntentCompat

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JournalExporterTest {

    private val sampleEntry = JournalEntity(
        id = 1,
        title = "Morning Deep Focus",
        content = "Achieved 2 solid pomodoro blocks on the compiler module today.",
        mood = "Ecstatic",
        timestamp = 1700000000000L
    )

    private val sampleEntry2 = JournalEntity(
        id = 2,
        title = "Evening Reflection",
        content = "Took a walk outside, feeling relaxed and mindful.",
        mood = "Happy",
        timestamp = 1700003600000L
    )

    @Test
    fun `formatSingleJournal contains title, mood, emoji and content`() {
        val formatted = JournalExporter.formatSingleJournal(sampleEntry)

        assertTrue("Should contain title", formatted.contains("Title: Morning Deep Focus"))
        assertTrue("Should contain mood and emoji", formatted.contains("Mood: Ecstatic 😄"))
        assertTrue("Should contain reflection content", formatted.contains("Achieved 2 solid pomodoro blocks"))
        assertTrue("Should contain reflection label", formatted.contains("Reflection:"))
    }

    @Test
    fun `formatAllJournals formats header and multiple entries with dividers`() {
        val formatted = JournalExporter.formatAllJournals(listOf(sampleEntry, sampleEntry2))

        assertTrue("Should contain header banner", formatted.contains("MINDSCALE JOURNAL EXPORT"))
        assertTrue("Should indicate total entries", formatted.contains("Total Entries : 2"))
        assertTrue("Should have Entry #1", formatted.contains("--- Entry #1 ---"))
        assertTrue("Should have Entry #2", formatted.contains("--- Entry #2 ---"))
        assertTrue("Should contain entry 1 title", formatted.contains("Morning Deep Focus"))
        assertTrue("Should contain entry 2 title", formatted.contains("Evening Reflection"))
    }

    @Test
    fun `shareJournal starts Chooser Intent with ACTION_SEND and plain text`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowApp = shadowOf(context as android.app.Application)

        JournalExporter.shareJournal(context, sampleEntry)

        val nextIntent = shadowApp.nextStartedActivity
        assertNotNull("Intent should be started", nextIntent)
        assertEquals("Should be Chooser Intent", Intent.ACTION_CHOOSER, nextIntent.action)

        val targetIntent = IntentCompat.getParcelableExtra(nextIntent, Intent.EXTRA_INTENT, Intent::class.java)
        assertNotNull("Target intent should be wrapped in chooser", targetIntent)
        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals("text/plain", targetIntent?.type)
        assertEquals("Morning Deep Focus", targetIntent?.getStringExtra(Intent.EXTRA_SUBJECT))
        assertTrue(targetIntent?.getStringExtra(Intent.EXTRA_TEXT)?.contains("Achieved 2 solid pomodoro") == true)
    }

    @Test
    fun `shareAllJournals starts Chooser Intent for all entries`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowApp = shadowOf(context as android.app.Application)

        JournalExporter.shareAllJournals(context, listOf(sampleEntry, sampleEntry2))

        val nextIntent = shadowApp.nextStartedActivity
        assertNotNull("Intent should be started", nextIntent)
        assertEquals(Intent.ACTION_CHOOSER, nextIntent.action)

        val targetIntent = IntentCompat.getParcelableExtra(nextIntent, Intent.EXTRA_INTENT, Intent::class.java)
        assertNotNull("Target intent should be present", targetIntent)
        assertEquals(Intent.ACTION_SEND, targetIntent?.action)
        assertEquals("text/plain", targetIntent?.type)
        assertTrue(targetIntent?.getStringExtra(Intent.EXTRA_SUBJECT)?.contains("2 entries") == true)
        assertTrue(targetIntent?.getStringExtra(Intent.EXTRA_TEXT)?.contains("Total Entries : 2") == true)
    }

    @Test
    fun `writeJournalToFile writes content to file uri successfully`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempFile = File(context.cacheDir, "test_export.txt")
        val uri = Uri.fromFile(tempFile)

        val content = JournalExporter.formatSingleJournal(sampleEntry)
        val success = JournalExporter.writeJournalToFile(context, uri, content)

        assertTrue("writeJournalToFile should return true", success)
        assertTrue("File should exist", tempFile.exists())
        assertEquals("Written text should match", content, tempFile.readText())
    }
}
