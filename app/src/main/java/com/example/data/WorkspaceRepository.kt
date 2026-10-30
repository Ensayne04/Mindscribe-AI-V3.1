package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkspaceRepository(
    private val workspaceDao: WorkspaceDao,
    private val taskDao: TaskDao = workspaceDao
) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allJournalEntries: Flow<List<JournalEntity>> = workspaceDao.getAllJournalEntries()

    fun getTasksOrderedByDueDate(): Flow<List<TaskEntity>> = taskDao.getTasksOrderedByDueDate()

    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<TaskEntity>> =
        taskDao.getTasksByCompletionStatus(isCompleted)

    suspend fun insertTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        taskDao.deleteTaskById(id)
    }

    suspend fun insertJournal(entry: JournalEntity) {
        workspaceDao.insertJournalEntry(entry)
    }

    suspend fun deleteJournalById(id: Int) {
        workspaceDao.deleteJournalEntryById(id)
    }

    suspend fun clearAllData() {
        workspaceDao.deleteAllJournalEntries()
        taskDao.deleteAllTasks()
    }
}
