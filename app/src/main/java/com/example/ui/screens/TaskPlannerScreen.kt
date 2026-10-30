package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TaskEntity
import com.example.ui.WorkspaceViewModel

@Composable
fun TaskPlannerScreen(
    viewModel: WorkspaceViewModel,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    
    // UI Local Filter states
    var selectedFilter by remember { mutableStateOf("All") } // All, Active, Completed
    var selectedCategoryFilter by remember { mutableStateOf("All") } // All or specific category
    var selectedTaskForDetail by remember { mutableStateOf<TaskEntity?>(null) }
    var isPhoneAddFormOpen by remember { mutableStateOf(false) }

    // Filter task logic combining completion status and color-coded category
    val filteredTasks = tasks.filter { task ->
        val statusMatch = when (selectedFilter) {
            "Active" -> !task.isCompleted
            "Completed" -> task.isCompleted
            else -> true
        }
        val categoryMatch = if (selectedCategoryFilter == "All") true else task.category.equals(selectedCategoryFilter, ignoreCase = true)
        statusMatch && categoryMatch
    }

    // Input States for adding tasks with color-coded categories
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Work") }
    var taskColorHex by remember { mutableStateOf(com.example.data.TaskCategoryDefaults.getColorForCategory("Work")) }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskDueDate by remember { mutableStateOf<Long?>(null) }

    val resetInputs = {
        taskTitle = ""
        taskDesc = ""
        taskCategory = "Work"
        taskColorHex = com.example.data.TaskCategoryDefaults.getColorForCategory("Work")
        taskPriority = "Medium"
        taskDueDate = null
    }

    val onSaveTask = {
        if (taskTitle.isNotBlank()) {
            viewModel.addTask(
                title = taskTitle,
                description = taskDesc,
                category = taskCategory,
                priority = taskPriority,
                dueDate = taskDueDate,
                colorHex = taskColorHex
            )
            resetInputs()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isExpanded) {
            // Tablet Interface: List on the Left, Add/Detail Form consistently on the Right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left 45%: Task List & Filter Section
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    TaskHeader(
                        selectedFilter = selectedFilter,
                        onFilterSelect = { selectedFilter = it },
                        selectedCategory = selectedCategoryFilter,
                        onCategorySelect = { selectedCategoryFilter = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (filteredTasks.isEmpty()) {
                        EmptyTasksState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            items(filteredTasks, key = { it.id }) { task ->
                                val isSelected = selectedTaskForDetail?.id == task.id
                                TaskRowCard(
                                    task = task,
                                    isSelected = isSelected,
                                    onSelect = { selectedTaskForDetail = task },
                                    onCheckChange = { viewModel.toggleTaskCompletion(task) },
                                    onDelete = { viewModel.deleteTask(task) }
                                )
                            }
                        }
                    }
                }

                // Right 1%: Separator line
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Right 0.9: Editor / Detail Area
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                ) {
                    if (selectedTaskForDetail != null) {
                        // Display full task details with an option to close / delete / change color
                        val activeDetail = selectedTaskForDetail!!
                        TaskDetailsCard(
                            task = activeDetail,
                            onClose = { selectedTaskForDetail = null },
                            onDelete = {
                                viewModel.deleteTask(activeDetail)
                                selectedTaskForDetail = null
                            },
                            onToggleComplete = {
                                viewModel.toggleTaskCompletion(activeDetail)
                                selectedTaskForDetail = activeDetail.copy(
                                    isCompleted = !activeDetail.isCompleted,
                                    completionStatus = !activeDetail.isCompleted
                                )
                            },
                            onColorChange = { newColor ->
                                viewModel.updateTaskColor(activeDetail, newColor)
                                selectedTaskForDetail = activeDetail.copy(colorHex = newColor)
                            }
                        )
                    } else {
                        // Display clean editor form on Tablet
                        TaskEditorPanel(
                            title = taskTitle,
                            desc = taskDesc,
                            category = taskCategory,
                            colorHex = taskColorHex,
                            priority = taskPriority,
                            dueDate = taskDueDate,
                            onTitleChange = { taskTitle = it },
                            onDescChange = { taskDesc = it },
                            onCategoryChange = { cat, col ->
                                taskCategory = cat
                                taskColorHex = col
                            },
                            onColorChange = { taskColorHex = it },
                            onPriorityChange = { taskPriority = it },
                            onDueDateChange = { taskDueDate = it },
                            onSave = {
                                onSaveTask()
                            }
                        )
                    }
                }
            }
        } else {
            // Compact Phone Interface
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TaskHeader(
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it },
                    selectedCategory = selectedCategoryFilter,
                    onCategorySelect = { selectedCategoryFilter = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTasks.isEmpty()) {
                    EmptyTasksState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskRowCard(
                                task = task,
                                isSelected = false,
                                onSelect = { selectedTaskForDetail = task },
                                onCheckChange = { viewModel.toggleTaskCompletion(task) },
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }

            // Quick Floating Action Button for Adding task on Mobile
            FloatingActionButton(
                onClick = { isPhoneAddFormOpen = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("mobile_add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }

            // Popup task detail slide cards for phone
            if (selectedTaskForDetail != null) {
                val activeDetail = selectedTaskForDetail!!
                Dialog(onDismissRequest = { selectedTaskForDetail = null }) {
                    TaskDetailsCard(
                        task = activeDetail,
                        onClose = { selectedTaskForDetail = null },
                        onDelete = {
                            viewModel.deleteTask(activeDetail)
                            selectedTaskForDetail = null
                        },
                        onToggleComplete = {
                            viewModel.toggleTaskCompletion(activeDetail)
                            selectedTaskForDetail = null
                        },
                        onColorChange = { newColor ->
                            viewModel.updateTaskColor(activeDetail, newColor)
                            selectedTaskForDetail = activeDetail.copy(colorHex = newColor)
                        }
                    )
                }
            }

            // Add input form modal dialog for phone
            if (isPhoneAddFormOpen) {
                Dialog(onDismissRequest = { isPhoneAddFormOpen = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Create New Task",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { isPhoneAddFormOpen = false }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            TaskEditorPanel(
                                title = taskTitle,
                                desc = taskDesc,
                                category = taskCategory,
                                colorHex = taskColorHex,
                                priority = taskPriority,
                                dueDate = taskDueDate,
                                onTitleChange = { taskTitle = it },
                                onDescChange = { taskDesc = it },
                                onCategoryChange = { cat, col ->
                                    taskCategory = cat
                                    taskColorHex = col
                                },
                                onColorChange = { taskColorHex = it },
                                onPriorityChange = { taskPriority = it },
                                onDueDateChange = { taskDueDate = it },
                                onSave = {
                                    onSaveTask()
                                    isPhoneAddFormOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFF4F46E5)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun TaskHeader(
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Column {
        Text(
            text = "Tasks & Projects",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Active", "Completed").forEach { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.4f
                            )
                        )
                        .clickable { onFilterSelect(filter) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("filter_$filter")
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.8f
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips with Color-Coded Dots
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isAllSelected = selectedCategory == "All"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isAllSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                        .clickable { onCategorySelect("All") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("cat_filter_all")
                ) {
                    Text(
                        text = "All Categories",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium),
                        color = if (isAllSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(com.example.data.TaskCategoryDefaults.PRESET_CATEGORIES) { cat ->
                val isCatSelected = selectedCategory == cat.name
                val catColor = parseHexColor(cat.colorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCatSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        )
                        .border(
                            width = if (isCatSelected) 1.5.dp else 0.5.dp,
                            color = if (isCatSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onCategorySelect(cat.name) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("cat_filter_${cat.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isCatSelected) catColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRowCard(
    task: TaskEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCheckChange: () -> Unit,
    onDelete: () -> Unit
) {
    val taskColor = parseHexColor(task.colorHex, parseHexColor(com.example.data.TaskCategoryDefaults.getColorForCategory(task.category)))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("task_item_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else taskColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Color-Coded Indicator Stripe
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(taskColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onCheckChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = taskColor,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChip(category = task.category, colorHex = task.colorHex)
                    PriorityBadge(priority = task.priorityLevel.ifEmpty { task.priority })
                    if (task.dueDate != null && task.dueDate > 0L) {
                        DueDateBadge(dueDate = task.dueDate, isOverdue = task.isOverdue)
                    }
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TaskDetailsCard(
    task: TaskEntity,
    onClose: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onColorChange: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_detail_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close detailed panel")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(category = task.category, colorHex = task.colorHex)
                PriorityBadge(priority = task.priorityLevel.ifEmpty { task.priority })
                if (task.dueDate != null && task.dueDate > 0L) {
                    DueDateBadge(dueDate = task.dueDate, isOverdue = task.isOverdue)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = task.description.ifEmpty { "No extra details or context provided for this task element." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Color-Coded Label quick modifier
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Color-Coded Label",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.example.data.TaskCategoryDefaults.PRESET_PALETTE.forEach { hex ->
                    val isCurrent = task.colorHex.equals(hex, ignoreCase = true)
                    val swatchColor = parseHexColor(hex)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                width = if (isCurrent) 2.5.dp else 1.dp,
                                color = if (isCurrent) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = onColorChange != null) { onColorChange?.invoke(hex) }
                            .testTag("detail_color_$hex"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected Color",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleComplete,
                    modifier = Modifier.weight(1.1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.Check else Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (task.isCompleted) "Reopen Task" else "Complete Task")
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task Details Panel",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorPanel(
    title: String,
    desc: String,
    category: String,
    colorHex: String,
    priority: String,
    dueDate: Long? = null,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onCategoryChange: (String, String) -> Unit,
    onColorChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onDueDateChange: (Long?) -> Unit = {},
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_editor_form")
    ) {
        Text(
            text = "Create Task Workspace",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Task Title *") },
            maxLines = 1,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_title_input"),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = desc,
            onValueChange = onDescChange,
            label = { Text("Task Details / Description") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_desc_input"),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Priority Selection Matrix
        Text(
            text = "Task Priority",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Low", "Medium", "High").forEach { item ->
                val isSelected = priority == item
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                when (item) {
                                    "High" -> Color(0xFFEF4444)
                                    "Medium" -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .clickable { onPriorityChange(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Due Date Matrix
        Text(
            text = "Due Date",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L
            val dateOptions = listOf(
                "None" to null,
                "Today" to (now + 8 * 60 * 60 * 1000L),
                "Tomorrow" to (now + dayMillis),
                "In 3 Days" to (now + 3 * dayMillis),
                "In 1 Wk" to (now + 7 * dayMillis)
            )

            dateOptions.forEach { (label, value) ->
                val isSelected = (dueDate == null && value == null) ||
                        (dueDate != null && value != null && kotlin.math.abs(dueDate - value) < 12 * 60 * 60 * 1000L)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .clickable { onDueDateChange(value) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color-Coded Category Matrix
        Text(
            text = "Category & Color-Coded Label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Predefined Category buttons with colored dots
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(com.example.data.TaskCategoryDefaults.PRESET_CATEGORIES) { item ->
                val isSelected = category == item.name
                val catColor = parseHexColor(item.colorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) catColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onCategoryChange(item.name, item.colorHex) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("cat_option_${item.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Color Palette picker row
        Text(
            text = "Custom Label Color",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.example.data.TaskCategoryDefaults.PRESET_PALETTE.forEach { hex ->
                val isSelected = colorHex.equals(hex, ignoreCase = true)
                val swatchColor = parseHexColor(hex)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(hex) }
                        .testTag("color_picker_$hex"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active Color",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("task_save_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(colorHex))
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Schedule Task", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun DueDateBadge(dueDate: Long, isOverdue: Boolean = false) {
    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    val formatted = sdf.format(java.util.Date(dueDate))

    val backColor = if (isOverdue) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val textColor = if (isOverdue) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .background(backColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = if (isOverdue) "Overdue ($formatted)" else "Due $formatted",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    colorHex: String = com.example.data.TaskCategoryDefaults.getColorForCategory(category)
) {
    val chipColor = parseHexColor(colorHex, parseHexColor(com.example.data.TaskCategoryDefaults.getColorForCategory(category)))
    Box(
        modifier = Modifier
            .background(
                chipColor.copy(alpha = 0.12f),
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                chipColor.copy(alpha = 0.35f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(chipColor)
            )
            Text(
                text = category,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = chipColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (backColor, textColor) = when (priority) {
        "High" -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
        "Medium" -> Color(0xFFECFEFF) to Color(0xFF0891B2)
        else -> Color(0xFFF0FDF4) to Color(0xFF16A34A)
    }

    Box(
        modifier = Modifier
            .background(backColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = priority,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyTasksState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.IncompleteCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            modifier = Modifier.size(68.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Clean Slate, Focus Ahead",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "No pending task items in this filter block. Add one on the right to start plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
