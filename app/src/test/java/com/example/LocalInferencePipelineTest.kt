package com.example

import com.example.ai.LocalInferencePipeline
import com.example.data.JournalEntity
import com.example.data.TaskEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalInferencePipelineTest {

    @Test
    fun `generateDailySummary handles empty journals gracefully`() {
        val summary = LocalInferencePipeline.generateDailySummary(emptyList())
        assertNotNull(summary)
        assertEquals(0, summary.entriesCount)
        assertTrue(summary.executiveSummary.contains("No reflection notes"))
    }

    @Test
    fun `generateDailySummary synthesizes multiple journals with mood and takeaways`() {
        val now = System.currentTimeMillis()
        val journals = listOf(
            JournalEntity(
                id = 1,
                title = "Engine Architecture Refactor",
                content = "Completed major rewrite of database layers and query pipelines. It resolved concurrency bottlenecks.",
                mood = "Ecstatic",
                timestamp = now
            ),
            JournalEntity(
                id = 2,
                title = "Pomodoro Flow Session",
                content = "Built three uninterrupted cycles focusing on offline search indexing. Discovered key optimization opportunities.",
                mood = "Happy",
                timestamp = now + 1000
            )
        )

        val summary = LocalInferencePipeline.generateDailySummary(journals)
        assertEquals(2, summary.entriesCount)
        assertTrue(summary.resiliencyScore >= 80)
        assertTrue(summary.moodTrend.contains("Positive"))
        assertTrue(summary.keyTakeaways.isNotEmpty())
        assertTrue(summary.topThemes.isNotEmpty())
        assertTrue(summary.executiveSummary.contains("Across 2 reflections"))
    }

    @Test
    fun `generateFocusTopics generates urgent sprint for overdue and high priority tasks`() {
        val now = System.currentTimeMillis()
        val tasks = listOf(
            TaskEntity(
                id = 1,
                title = "Fix Critical Security CVE",
                priorityLevel = "High",
                dueDate = now - 100000L, // Overdue
                completionStatus = false,
                category = "Engineering"
            ),
            TaskEntity(
                id = 2,
                title = "Prepare Release Candidate",
                priorityLevel = "High",
                dueDate = now + 500000L,
                completionStatus = false,
                category = "Engineering"
            ),
            TaskEntity(
                id = 3,
                title = "Update Readme Docs",
                priorityLevel = "Low",
                completionStatus = false,
                category = "Docs"
            )
        )

        val topics = LocalInferencePipeline.generateFocusTopics(tasks)
        assertTrue(topics.isNotEmpty())

        val urgentTopic = topics.first()
        assertEquals("Critical Deliverables Sprint", urgentTopic.title)
        assertEquals("High", urgentTopic.priorityLevel)
        assertEquals(50, urgentTopic.suggestedMinutes)
        assertTrue(urgentTopic.nextActions.contains("Fix Critical Security CVE"))
    }

    @Test
    fun `generateFocusTopics suggests sprint review when all tasks are completed`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Task A", completionStatus = true, isCompleted = true),
            TaskEntity(id = 2, title = "Task B", completionStatus = true, isCompleted = true)
        )

        val topics = LocalInferencePipeline.generateFocusTopics(tasks)
        assertEquals(1, topics.size)
        assertEquals("Sprint Review & Milestone Planning", topics.first().title)
    }

    @Test
    fun `runInference executes asynchronously and records execution duration`() = runTest {
        val journals = listOf(
            JournalEntity(id = 1, title = "A", content = "Worked on algorithm design and tested units.", mood = "Happy")
        )
        val tasks = listOf(
            TaskEntity(id = 1, title = "Review PR", priorityLevel = "Medium", category = "Work")
        )

        val report = LocalInferencePipeline.runInference(journals, tasks)
        assertNotNull(report.dailySummary)
        assertTrue(report.suggestedTopics.isNotEmpty())
        assertTrue(report.inferenceTimeMs >= 0)
        assertTrue(report.modelName.contains("ZenSync Optimized"))
    }
}
