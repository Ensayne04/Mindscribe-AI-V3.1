package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao : TaskDao {

    // Journal
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntity): Long

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntryById(id: Int)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllJournalEntries()
}
