package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "dueDate")
    val dueDate: Long? = null,
    @ColumnInfo(name = "priorityLevel")
    val priorityLevel: String = "Medium",
    @ColumnInfo(name = "completionStatus")
    val completionStatus: Boolean = false,
    @ColumnInfo(name = "priority")
    val priority: String = priorityLevel,
    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = completionStatus,
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "category")
    val category: String = "Work",
    @ColumnInfo(name = "colorHex", defaultValue = "#4F46E5")
    val colorHex: String = TaskCategoryDefaults.getColorForCategory(category),
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    @Ignore
    constructor(
        title: String,
        dueDate: Long? = null,
        priorityLevel: String = "Medium",
        completionStatus: Boolean = false,
        description: String = "",
        category: String = "Work",
        colorHex: String = TaskCategoryDefaults.getColorForCategory(category),
        id: Int = 0,
        timestamp: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        title = title,
        dueDate = dueDate,
        priorityLevel = priorityLevel,
        completionStatus = completionStatus,
        priority = priorityLevel,
        isCompleted = completionStatus,
        description = description,
        category = category,
        colorHex = colorHex,
        timestamp = timestamp
    )

    fun formattedDueDate(): String {
        return if (dueDate != null && dueDate > 0L) {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(dueDate))
        } else {
            "No due date"
        }
    }

    val isOverdue: Boolean
        get() = !isCompleted && !completionStatus && dueDate != null && dueDate < System.currentTimeMillis()
}

typealias Task = TaskEntity

data class TaskCategory(
    val name: String,
    val colorHex: String
)

object TaskCategoryDefaults {
    val PRESET_CATEGORIES = listOf(
        TaskCategory("Work", "#4F46E5"),       // Indigo
        TaskCategory("Personal", "#10B981"),   // Emerald
        TaskCategory("Creative", "#9333EA"),   // Purple
        TaskCategory("Health", "#F43F5E"),     // Rose
        TaskCategory("Finance", "#F59E0B"),    // Amber
        TaskCategory("Urgent", "#EF4444"),     // Red
        TaskCategory("Learning", "#0EA5E9"),   // Sky Blue
        TaskCategory("Home", "#14B8A6")        // Teal
    )

    val PRESET_PALETTE = listOf(
        "#4F46E5", // Indigo
        "#10B981", // Emerald
        "#9333EA", // Purple
        "#F43F5E", // Rose
        "#F59E0B", // Amber
        "#0EA5E9", // Sky Blue
        "#EF4444", // Crimson
        "#14B8A6", // Teal
        "#F97316", // Orange
        "#6B7280"  // Slate
    )

    fun getColorForCategory(categoryName: String): String {
        val match = PRESET_CATEGORIES.find { it.name.equals(categoryName, ignoreCase = true) }
        return match?.colorHex ?: "#4F46E5"
    }
}
