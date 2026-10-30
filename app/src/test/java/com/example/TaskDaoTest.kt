package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.Task
import com.example.data.TaskDao
import com.example.data.TaskEntity
import com.example.data.WorkspaceDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TaskDaoTest {

    private lateinit var database: WorkspaceDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WorkspaceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test inserting and querying task with title, dueDate, priorityLevel, completionStatus`() = runBlocking {
        val dueTimestamp = System.currentTimeMillis() + 86400000L
        val task = TaskEntity(
            title = "Prepare Q4 Presentation",
            dueDate = dueTimestamp,
            priorityLevel = "High",
            completionStatus = false,
            description = "Include metrics and roadmap slides"
        )

        val insertedId = taskDao.insertTask(task)
        assertTrue(insertedId > 0)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(1, allTasks.size)

        val retrieved = allTasks.first()
        assertEquals("Prepare Q4 Presentation", retrieved.title)
        assertEquals(dueTimestamp, retrieved.dueDate)
        assertEquals("High", retrieved.priorityLevel)
        assertEquals("High", retrieved.priority)
        assertFalse(retrieved.completionStatus)
        assertFalse(retrieved.isCompleted)
    }

    @Test
    fun `test updating completion status`() = runBlocking {
        val task = Task(
            title = "Submit expense report",
            dueDate = System.currentTimeMillis(),
            priorityLevel = "Medium",
            completionStatus = false
        )
        val id = taskDao.insert(task).toInt()

        taskDao.updateCompletionStatus(id, true)

        val updated = taskDao.getTaskById(id)
        assertNotNull(updated)
        assertTrue(updated!!.completionStatus)
        assertTrue(updated.isCompleted)
    }

    @Test
    fun `test query tasks by completion status`() = runBlocking {
        taskDao.insertTask(
            TaskEntity(
                title = "Task 1",
                dueDate = 1000L,
                priorityLevel = "Low",
                completionStatus = false
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "Task 2",
                dueDate = 2000L,
                priorityLevel = "High",
                completionStatus = true
            )
        )

        val pendingTasks = taskDao.getTasksByCompletionStatus(false).first()
        assertEquals(1, pendingTasks.size)
        assertEquals("Task 1", pendingTasks.first().title)

        val completedTasks = taskDao.getTasksByCompletionStatus(true).first()
        assertEquals(1, completedTasks.size)
        assertEquals("Task 2", completedTasks.first().title)
    }

    @Test
    fun `test delete task by id`() = runBlocking {
        val task = TaskEntity(
            title = "Temporary task",
            dueDate = null,
            priorityLevel = "Low",
            completionStatus = false
        )
        val id = taskDao.insertTask(task).toInt()
        assertEquals(1, taskDao.getAllTasks().first().size)

        taskDao.deleteTaskById(id)
        assertEquals(0, taskDao.getAllTasks().first().size)
    }

    @Test
    fun `test task with custom category and colorHex persists properly in Room`() = runBlocking {
        val task = TaskEntity(
            title = "Design Brand Colors",
            category = "Creative",
            colorHex = "#9333EA",
            priorityLevel = "High"
        )
        val id = taskDao.insertTask(task).toInt()
        val retrieved = taskDao.getTaskById(id)
        assertNotNull(retrieved)
        assertEquals("Creative", retrieved?.category)
        assertEquals("#9333EA", retrieved?.colorHex)
    }
}
