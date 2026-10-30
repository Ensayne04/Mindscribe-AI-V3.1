package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.JournalEntity
import com.example.data.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JournalExporter {

    private const val TAG = "JournalExporter"

    fun getMoodEmoji(mood: String): String {
        return when (mood) {
            "Ecstatic" -> "😄"
            "Happy" -> "🙂"
            "Neutral" -> "😐"
            "Tired" -> "🥱"
            "Stressed" -> "😫"
            else -> "😐"
        }
    }

    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    fun formatSingleJournal(entry: JournalEntity): String {
        return buildString {
            appendLine("Title: ${entry.title}")
            appendLine("Date: ${formatDate(entry.timestamp)}")
            appendLine("Mood: ${entry.mood} ${getMoodEmoji(entry.mood)}")
            appendLine()
            appendLine("Reflection:")
            appendLine(entry.content)
        }
    }

    fun formatAllJournals(entries: List<JournalEntity>): String {
        return buildString {
            appendLine("=========================================")
            appendLine("       MINDSCALE JOURNAL EXPORT          ")
            appendLine("=========================================")
            appendLine("Total Entries : ${entries.size}")
            appendLine("Exported On   : ${formatDate(System.currentTimeMillis())}")
            appendLine("=========================================")
            appendLine()
            entries.forEachIndexed { index, entry ->
                appendLine("--- Entry #${index + 1} ---")
                appendLine("Title: ${entry.title}")
                appendLine("Date : ${formatDate(entry.timestamp)}")
                appendLine("Mood : ${entry.mood} ${getMoodEmoji(entry.mood)}")
                appendLine()
                appendLine(entry.content)
                appendLine()
                appendLine("-----------------------------------------")
                appendLine()
            }
        }
    }

    fun formatFullWorkspaceBackup(tasks: List<TaskEntity>, entries: List<JournalEntity>): String {
        return buildString {
            appendLine("=========================================")
            appendLine("   OFFLINE WORKSPACE SECURE DATA BACKUP  ")
            appendLine("=========================================")
            appendLine("Exported On    : ${formatDate(System.currentTimeMillis())}")
            appendLine("Privacy Status : 100% Offline Device Export")
            appendLine("Total Tasks    : ${tasks.size}")
            appendLine("Total Journals : ${entries.size}")
            appendLine("=========================================")
            appendLine()
            appendLine("================== TASKS ================")
            if (tasks.isEmpty()) {
                appendLine("No tasks recorded.")
            } else {
                tasks.forEachIndexed { i, t ->
                    val status = if (t.isCompleted || t.completionStatus) "[COMPLETED]" else "[PENDING]"
                    appendLine("#${i + 1} $status ${t.title}")
                    appendLine("   Category: ${t.category} (${t.colorHex}) | Priority: ${t.priorityLevel}")
                    if (t.dueDate != null && t.dueDate > 0L) {
                        appendLine("   Due Date: ${t.formattedDueDate()}")
                    }
                    if (t.description.isNotBlank()) {
                        appendLine("   Notes: ${t.description}")
                    }
                    appendLine()
                }
            }
            appendLine()
            appendLine("============ JOURNAL REFLECTIONS ========")
            if (entries.isEmpty()) {
                appendLine("No journal entries recorded.")
            } else {
                entries.forEachIndexed { i, j ->
                    appendLine("#${i + 1} ${j.title} (${formatDate(j.timestamp)})")
                    appendLine("   Mood: ${j.mood} ${getMoodEmoji(j.mood)}")
                    appendLine("   Reflection:")
                    appendLine("   ${j.content}")
                    appendLine("-----------------------------------------")
                    appendLine()
                }
            }
        }
    }

    fun shareJournal(context: Context, entry: JournalEntity) {
        val text = formatSingleJournal(entry)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, entry.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Journal Entry").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun shareAllJournals(context: Context, entries: List<JournalEntity>) {
        if (entries.isEmpty()) return
        val text = formatAllJournals(entries)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Journal Reflections Export (${entries.size} entries)")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share All Journal Entries").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun shareFullWorkspaceBackup(context: Context, tasks: List<TaskEntity>, entries: List<JournalEntity>) {
        val text = formatFullWorkspaceBackup(tasks, entries)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Offline Workspace Data Backup")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Offline Workspace Backup").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun writeJournalToFile(context: Context, uri: Uri, text: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(text.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing journal to file uri $uri", e)
            false
        }
    }
}
