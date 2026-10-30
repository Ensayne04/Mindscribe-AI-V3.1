package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val mood: String, // Ecstatic, Happy, Neutral, Tired, Stressed
    val timestamp: Long = System.currentTimeMillis()
)
