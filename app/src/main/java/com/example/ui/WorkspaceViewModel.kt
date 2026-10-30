package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.JournalEntity
import com.example.data.TaskEntity
import com.example.data.WorkspaceDatabase
import com.example.data.WorkspaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimerPreset(val displayName: String, val minutes: Int) {
    FOCUS("Focus Session", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkspaceRepository
    private val prefs = application.getSharedPreferences("offline_workspace_settings", android.content.Context.MODE_PRIVATE)
    private val securityManager = com.example.security.AppSecurityManager(application)

    // Database streams
    val tasks: StateFlow<List<TaskEntity>>
    val journals: StateFlow<List<JournalEntity>>

    // App Lock & Biometric Security States
    private val _isAppLocked = MutableStateFlow(securityManager.isAppLockEnabled())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(securityManager.isAppLockEnabled())
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isUsingDefaultPin = MutableStateFlow(securityManager.isUsingDefaultPin())
    val isUsingDefaultPin: StateFlow<Boolean> = _isUsingDefaultPin.asStateFlow()

    // Interactive Pomodoro Timer States
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(25 * 60)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _selectedPreset = MutableStateFlow(TimerPreset.FOCUS)
    val selectedPreset: StateFlow<TimerPreset> = _selectedPreset.asStateFlow()

    // Offline Workspace Settings
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(prefs.getBoolean("is_tts_enabled", false))
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean("is_sound_enabled", true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _physicalKeyboardEnterEnables = MutableStateFlow(prefs.getBoolean("keyboard_enter_saves", false))
    val physicalKeyboardEnterEnables: StateFlow<Boolean> = _physicalKeyboardEnterEnables.asStateFlow()

    private val _autoCapitalizeJournal = MutableStateFlow(prefs.getBoolean("auto_capitalize_journal", true))
    val autoCapitalizeJournal: StateFlow<Boolean> = _autoCapitalizeJournal.asStateFlow()

    private var timerJob: Job? = null

    init {
        val database = WorkspaceDatabase.getDatabase(application)
        repository = WorkspaceRepository(database.workspaceDao())
        
        tasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        journals = repository.allJournalEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Automatically run local inference pipeline when tasks or journals update
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(tasks, journals) { t, j -> Pair(t, j) }
                .collect { (t, j) ->
                    if (t.isNotEmpty() || j.isNotEmpty()) {
                        _inferenceReport.value = com.example.ai.LocalInferencePipeline.runInference(j, t)
                    }
                }
        }
    }

    // Local Inference Pipeline States
    private val _inferenceReport = MutableStateFlow<com.example.ai.LocalInferenceReport?>(null)
    val inferenceReport: StateFlow<com.example.ai.LocalInferenceReport?> = _inferenceReport.asStateFlow()

    private val _isInferenceRunning = MutableStateFlow(false)
    val isInferenceRunning: StateFlow<Boolean> = _isInferenceRunning.asStateFlow()

    fun runLocalInference(targetUtcDateMillis: Long? = null) {
        viewModelScope.launch {
            _isInferenceRunning.value = true
            try {
                val report = com.example.ai.LocalInferencePipeline.runInference(
                    journals = journals.value,
                    tasks = tasks.value,
                    targetUtcDateMillis = targetUtcDateMillis
                )
                _inferenceReport.value = report
            } finally {
                _isInferenceRunning.value = false
            }
        }
    }

    fun startFocusSessionWithTopic(topic: com.example.ai.FocusTopicSuggestion) {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _selectedPreset.value = TimerPreset.FOCUS
        _timeRemainingSeconds.value = topic.suggestedMinutes * 60
        startTimer()
    }

    // --- Task Operations ---
    fun addTask(
        title: String,
        description: String = "",
        category: String = "Work",
        priority: String = "Medium",
        dueDate: Long? = null,
        colorHex: String = com.example.data.TaskCategoryDefaults.getColorForCategory(category)
    ) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                repository.insertTask(
                    TaskEntity(
                        title = title,
                        dueDate = dueDate,
                        priorityLevel = priority,
                        completionStatus = false,
                        priority = priority,
                        isCompleted = false,
                        description = description,
                        category = category,
                        colorHex = colorHex
                    )
                )
            }
        }
    }

    fun updateTaskColor(task: TaskEntity, newColorHex: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(colorHex = newColorHex))
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            repository.updateTask(
                task.copy(
                    isCompleted = newCompleted,
                    completionStatus = newCompleted
                )
            )
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteTaskById(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
        }
    }

    // --- Journal Operations ---
    fun addJournalEntry(title: String, content: String, mood: String) {
        viewModelScope.launch {
            if (title.isNotBlank() && content.isNotBlank()) {
                repository.insertJournal(
                    JournalEntity(
                        title = title,
                        content = content,
                        mood = mood
                    )
                )
            }
        }
    }

    fun deleteJournalEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteJournalById(id)
        }
    }

    // --- Pomodoro Timer Logic ---
    fun selectTimerPreset(preset: TimerPreset) {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _selectedPreset.value = preset
        _timeRemainingSeconds.value = preset.minutes * 60
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0) {
                delay(1000)
                _timeRemainingSeconds.value -= 1
            }
            _isTimerRunning.value = false
            // Play a gentle notification or reset
            selectTimerPreset(_selectedPreset.value)
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timeRemainingSeconds.value = _selectedPreset.value.minutes * 60
    }

    // --- Settings Setters ---
    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    }

    fun setTtsEnabled(enabled: Boolean) {
        _isTtsEnabled.value = enabled
        prefs.edit().putBoolean("is_tts_enabled", enabled).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        prefs.edit().putBoolean("is_sound_enabled", enabled).apply()
    }

    fun setPhysicalKeyboardEnterEnables(enabled: Boolean) {
        _physicalKeyboardEnterEnables.value = enabled
        prefs.edit().putBoolean("keyboard_enter_saves", enabled).apply()
    }

    fun setAutoCapitalizeJournal(enabled: Boolean) {
        _autoCapitalizeJournal.value = enabled
        prefs.edit().putBoolean("auto_capitalize_journal", enabled).apply()
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _inferenceReport.value = null
        }
    }

    // --- Local PIN & Biometric Security Actions ---
    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_isAppLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun verifyPin(pin: String): Boolean {
        val isValid = securityManager.verifyPin(pin)
        if (isValid) {
            _isAppLocked.value = false
        }
        return isValid
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val changed = securityManager.changePin(oldPin, newPin)
        if (changed) {
            _isUsingDefaultPin.value = securityManager.isUsingDefaultPin()
        }
        return changed
    }

    fun resetPinToDefault() {
        securityManager.resetPinToDefault()
        _isUsingDefaultPin.value = true
    }

    fun setAppLockEnabled(enabled: Boolean) {
        securityManager.setAppLockEnabled(enabled)
        _isAppLockEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // --- Factory ---
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkspaceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkspaceViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
