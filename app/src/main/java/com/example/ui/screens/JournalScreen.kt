package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.util.JournalExporter
import com.example.util.JournalDateFilter
import com.example.ui.components.LocalAiEnginePanel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.JournalEntity
import com.example.ui.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

enum class SpeechTarget {
    SEARCH,
    NEW_ENTRY,
    FORM_TITLE,
    FORM_CONTENT
}

@Composable
fun JournalScreen(
    viewModel: WorkspaceViewModel,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val journals by viewModel.journals.collectAsState()

    var isAddEntryOpen by remember { mutableStateOf(false) }
    var selectedEntryDetail by remember { mutableStateOf<JournalEntity?>(null) }
    var isMobileInsightsVisible by remember { mutableStateOf(false) }

    // Search and Real-time Filter State
    var searchQuery by remember { mutableStateOf("") }

    // Voice Dictation (STT) and Text-to-Speech (TTS) State
    var currentSpeechTarget by remember { mutableStateOf<SpeechTarget?>(null) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var currentlySpeakingId by remember { mutableStateOf<Int?>(null) }

    // Initialize Text-to-Speech engine safely
    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                // Initializing or unavailable
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val toggleSpeak: (Int, String) -> Unit = { entryId, textToSpeak ->
        ttsInstance?.let { tts ->
            if (currentlySpeakingId == entryId && tts.isSpeaking) {
                tts.stop()
                currentlySpeakingId = null
            } else {
                tts.stop()
                currentlySpeakingId = entryId
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "Journal_TTS_$entryId")
            }
        } ?: run {
            Toast.makeText(context, "Text-to-Speech initializing...", Toast.LENGTH_SHORT).show()
        }
    }

    // Forms Inputs
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var moodInput by remember { mutableStateOf("Happy") } // Ecstatic, Happy, Neutral, Tired, Stressed

    // Voice recognition launcher (Speech to text) - zero permissions required
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()?.trim()
            if (!spokenText.isNullOrEmpty()) {
                when (currentSpeechTarget) {
                    SpeechTarget.SEARCH -> {
                        searchQuery = spokenText
                        Toast.makeText(context, "Voice Search: \"$spokenText\"", Toast.LENGTH_SHORT).show()
                    }
                    SpeechTarget.NEW_ENTRY -> {
                        contentInput = spokenText
                        if (titleInput.isBlank()) {
                            titleInput = spokenText.take(28).replaceFirstChar { it.uppercase() }
                        }
                        isAddEntryOpen = true
                        Toast.makeText(context, "Voice transcribed into new note", Toast.LENGTH_SHORT).show()
                    }
                    SpeechTarget.FORM_TITLE -> {
                        titleInput = if (titleInput.isBlank()) spokenText else "$titleInput $spokenText"
                    }
                    SpeechTarget.FORM_CONTENT -> {
                        contentInput = if (contentInput.isBlank()) spokenText else "$contentInput $spokenText"
                    }
                    null -> {}
                }
            }
        }
        currentSpeechTarget = null
    }

    val launchVoiceRecognition: (SpeechTarget, String) -> Unit = { target, prompt ->
        currentSpeechTarget = target
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice recognition is not supported on this device", Toast.LENGTH_SHORT).show()
            currentSpeechTarget = null
        }
    }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var isTimelineView by remember { mutableStateOf(true) }

    val inferenceReport by viewModel.inferenceReport.collectAsState()
    val isInferenceRunning by viewModel.isInferenceRunning.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (inferenceReport == null) {
            viewModel.runLocalInference()
        }
    }

    // Combined keyword search, calendar date filter, and chronological sort
    val filteredJournals = remember(journals, searchQuery, selectedDateMillis) {
        JournalDateFilter.filterAndSortJournals(
            journals = journals,
            searchQuery = searchQuery,
            selectedUtcDateMillis = selectedDateMillis
        )
    }

    // Export & Share State
    var isTabletExportMenuOpen by remember { mutableStateOf(false) }
    var isMobileExportMenuOpen by remember { mutableStateOf(false) }
    var pendingExportText by remember { mutableStateOf<String?>(null) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            val textToWrite = pendingExportText ?: ""
            val success = JournalExporter.writeJournalToFile(context, it, textToWrite)
            if (success) {
                Toast.makeText(context, "Journal exported successfully to file", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to export journal file", Toast.LENGTH_SHORT).show()
            }
        }
        pendingExportText = null
    }

    val onSaveEntry = {
        if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
            viewModel.addJournalEntry(titleInput, contentInput, moodInput)
            // Reset
            titleInput = ""
            contentInput = ""
            moodInput = "Happy"
            isAddEntryOpen = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isExpanded) {
            // TABLET: Side-by-side Splitscreen
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Journal Title & Grid Entries
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Reflections Space",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Your private thoughts and cognitive patterns",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (journals.isNotEmpty()) {
                                Box {
                                    OutlinedButton(
                                        onClick = { isTabletExportMenuOpen = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("export_menu_btn_tablet")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Export and share",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export & Share", style = MaterialTheme.typography.labelLarge)
                                    }
                                    DropdownMenu(
                                        expanded = isTabletExportMenuOpen,
                                        onDismissRequest = { isTabletExportMenuOpen = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Share All via Android Intent") },
                                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                            onClick = {
                                                isTabletExportMenuOpen = false
                                                JournalExporter.shareAllJournals(context, journals)
                                            },
                                            modifier = Modifier.testTag("share_all_journal_btn")
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Save as Text File (.txt)") },
                                            leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                            onClick = {
                                                isTabletExportMenuOpen = false
                                                pendingExportText = JournalExporter.formatAllJournals(journals)
                                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                                exportFileLauncher.launch("journal_export_$timeStamp.txt")
                                            },
                                            modifier = Modifier.testTag("export_txt_file_btn")
                                        )
                                    }
                                }
                            }

                            // Voice Record Button (TTS/STT Dictation)
                            OutlinedButton(
                                onClick = { launchVoiceRecognition(SpeechTarget.NEW_ENTRY, "Dictate your reflection note...") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("voice_journal_btn_tablet")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Dictate reflection with voice",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Note", style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = { isAddEntryOpen = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("add_reflection_btn_tablet")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Mindlog", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (journals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyJournalState(onPromptAdd = { isAddEntryOpen = true })
                        }
                    } else {
                        // Real-time keyword Search Bar
                        JournalSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onMicClick = { launchVoiceRecognition(SpeechTarget.SEARCH, "Say search keyword...") },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Date filter bar and view toggle
                        JournalDateFilterBar(
                            selectedUtcDateMillis = selectedDateMillis,
                            onDateSelected = { selectedDateMillis = it },
                            onOpenDatePicker = { showDatePickerModal = true },
                            isTimelineView = isTimelineView,
                            onToggleViewMode = { isTimelineView = !isTimelineView },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (filteredJournals.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyFilteredJournalState(
                                    searchQuery = searchQuery,
                                    selectedUtcDateMillis = selectedDateMillis,
                                    onClearFilters = {
                                        searchQuery = ""
                                        selectedDateMillis = null
                                    }
                                )
                            }
                        } else if (isTimelineView) {
                            JournalTimelineView(
                                entries = filteredJournals,
                                currentlySpeakingId = currentlySpeakingId,
                                onToggleTts = { id, text -> toggleSpeak(id, text) },
                                onSelectEntry = { selectedEntryDetail = it },
                                onShareEntry = { JournalExporter.shareJournal(context, it) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(filteredJournals, key = { it.id }) { entry ->
                                    JournalGridItem(
                                        entry = entry,
                                        onClick = { selectedEntryDetail = entry },
                                        onShare = { JournalExporter.shareJournal(context, entry) },
                                        isSpeaking = currentlySpeakingId == entry.id,
                                        onToggleTts = { toggleSpeak(entry.id, "${entry.title}. Feeling ${entry.mood}. ${entry.content}") }
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column: Local AI Insights & Local Inference Pipeline
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    LocalAiEnginePanel(
                        report = inferenceReport,
                        isLoading = isInferenceRunning,
                        onRefresh = { viewModel.runLocalInference(selectedDateMillis) },
                        onStartTopic = { topic -> viewModel.startFocusSessionWithTopic(topic) }
                    )
                }
            }
        } else {
            // MOBILE / COMPACT Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Zen Journal",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Keep notes and logs offline",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (journals.isNotEmpty()) {
                            Box {
                                IconButton(
                                    onClick = { isMobileExportMenuOpen = true },
                                    modifier = Modifier.testTag("mobile_export_menu_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Export or Share Journals",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded = isMobileExportMenuOpen,
                                    onDismissRequest = { isMobileExportMenuOpen = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Share All Entries") },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                        onClick = {
                                            isMobileExportMenuOpen = false
                                            JournalExporter.shareAllJournals(context, journals)
                                        },
                                        modifier = Modifier.testTag("mobile_share_all_journal_btn")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Save as Text File (.txt)") },
                                        leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                        onClick = {
                                            isMobileExportMenuOpen = false
                                            pendingExportText = JournalExporter.formatAllJournals(journals)
                                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                            exportFileLauncher.launch("journal_export_$timeStamp.txt")
                                        },
                                        modifier = Modifier.testTag("mobile_export_txt_file_btn")
                                    )
                                }
                            }
                        }

                        // Mobile toggle for insights sheet/card helper
                        Button(
                            onClick = { isMobileInsightsVisible = !isMobileInsightsVisible },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMobileInsightsVisible) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (isMobileInsightsVisible) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Toggle Insights",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMobileInsightsVisible) "Hide Insights" else "Offline AI",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = isMobileInsightsVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .verticalScroll(rememberScrollState())
                            .heightIn(max = 420.dp)
                    ) {
                        LocalAiEnginePanel(
                            report = inferenceReport,
                            isLoading = isInferenceRunning,
                            onRefresh = { viewModel.runLocalInference(selectedDateMillis) },
                            onStartTopic = { topic -> viewModel.startFocusSessionWithTopic(topic) }
                        )
                    }
                }

                if (journals.isNotEmpty()) {
                    // Mobile Search Bar
                    JournalSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onMicClick = { launchVoiceRecognition(SpeechTarget.SEARCH, "Say search keyword...") },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Date filter bar and view toggle
                    JournalDateFilterBar(
                        selectedUtcDateMillis = selectedDateMillis,
                        onDateSelected = { selectedDateMillis = it },
                        onOpenDatePicker = { showDatePickerModal = true },
                        isTimelineView = isTimelineView,
                        onToggleViewMode = { isTimelineView = !isTimelineView },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeFilterCount = (if (searchQuery.isNotBlank()) 1 else 0) + (if (selectedDateMillis != null) 1 else 0)
                    Text(
                        text = if (activeFilterCount > 0) "Filtered Reflections (${filteredJournals.size})" else "Chronological Timeline",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    if (activeFilterCount > 0) {
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable {
                                    searchQuery = ""
                                    selectedDateMillis = null
                                }
                                .padding(4.dp)
                        )
                    }
                }

                if (journals.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyJournalState(onPromptAdd = { isAddEntryOpen = true })
                    }
                } else if (filteredJournals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyFilteredJournalState(
                            searchQuery = searchQuery,
                            selectedUtcDateMillis = selectedDateMillis,
                            onClearFilters = {
                                searchQuery = ""
                                selectedDateMillis = null
                            }
                        )
                    }
                } else if (isTimelineView) {
                    JournalTimelineView(
                        entries = filteredJournals,
                        currentlySpeakingId = currentlySpeakingId,
                        onToggleTts = { id, text -> toggleSpeak(id, text) },
                        onSelectEntry = { selectedEntryDetail = it },
                        onShareEntry = { JournalExporter.shareJournal(context, it) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredJournals, key = { it.id }) { entry ->
                            JournalGridItem(
                                entry = entry,
                                onClick = { selectedEntryDetail = entry },
                                onShare = { JournalExporter.shareJournal(context, entry) },
                                isSpeaking = currentlySpeakingId == entry.id,
                                onToggleTts = { toggleSpeak(entry.id, "${entry.title}. Feeling ${entry.mood}. ${entry.content}") }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Buttons on Mobile/Compact
        if (!isExpanded) {
            // Big Microphone FAB on bottom left
            FloatingActionButton(
                onClick = { launchVoiceRecognition(SpeechTarget.NEW_ENTRY, "Dictate your reflection note...") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .testTag("mobile_voice_journal_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Dictate reflection with voice",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Floating Action Button on bottom right (+)
            FloatingActionButton(
                onClick = { isAddEntryOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("mobile_add_journal_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Journal Entry")
            }
        }

        // Date Picker Modal Dialog
        if (showDatePickerModal) {
            JournalDatePickerModal(
                initialDateMillis = selectedDateMillis,
                onDismissRequest = { showDatePickerModal = false },
                onDateConfirmed = { pickedUtcMillis ->
                    selectedDateMillis = pickedUtcMillis
                    showDatePickerModal = false
                }
            )
        }

        // Dialog for adding entry
        if (isAddEntryOpen) {
            Dialog(onDismissRequest = { isAddEntryOpen = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("add_journal_modal"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    AddJournalForm(
                        title = titleInput,
                        content = contentInput,
                        mood = moodInput,
                        onTitleChange = { titleInput = it },
                        onContentChange = { contentInput = it },
                        onMoodChange = { moodInput = it },
                        onTitleMicClick = { launchVoiceRecognition(SpeechTarget.FORM_TITLE, "Dictate log title...") },
                        onContentMicClick = { launchVoiceRecognition(SpeechTarget.FORM_CONTENT, "Dictate reflection context...") },
                        onDismiss = { isAddEntryOpen = false },
                        onSave = onSaveEntry
                    )
                }
            }
        }

        // Dialog for viewing entry details
        if (selectedEntryDetail != null) {
            val entry = selectedEntryDetail!!
            Dialog(onDismissRequest = {
                if (currentlySpeakingId == entry.id) {
                    ttsInstance?.stop()
                    currentlySpeakingId = null
                }
                selectedEntryDetail = null
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("view_journal_modal"),
                    shape = RoundedCornerShape(24.dp),
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
                                text = "Reflections Log",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                if (currentlySpeakingId == entry.id) {
                                    ttsInstance?.stop()
                                    currentlySpeakingId = null
                                }
                                selectedEntryDetail = null
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close details")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = getMoodEmoji(entry.mood),
                                style = androidx.compose.ui.text.TextStyle(fontSize = 24.sp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Feeling ${entry.mood}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = formatDateString(entry.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        toggleSpeak(entry.id, "${entry.title}. Feeling ${entry.mood}. ${entry.content}")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("dialog_tts_journal_btn")
                                ) {
                                    Icon(
                                        imageVector = if (currentlySpeakingId == entry.id) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (currentlySpeakingId == entry.id) "Stop audio playback" else "Listen via TTS",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (currentlySpeakingId == entry.id) "Stop" else "Listen (TTS)", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        JournalExporter.shareJournal(context, entry)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("dialog_share_journal_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        pendingExportText = JournalExporter.formatSingleJournal(entry)
                                        val cleanTitle = entry.title.take(15).replace(Regex("[^a-zA-Z0-9]"), "_").ifBlank { "entry" }
                                        exportFileLauncher.launch("journal_$cleanTitle.txt")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("dialog_save_journal_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(".txt", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Button(
                                onClick = {
                                    if (currentlySpeakingId == entry.id) {
                                        ttsInstance?.stop()
                                        currentlySpeakingId = null
                                    }
                                    viewModel.deleteJournalEntry(entry.id)
                                    selectedEntryDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("dialog_delete_journal_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Evict Entry", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocalAiInsightsEngine(
    journals: List<JournalEntity>,
    modifier: Modifier = Modifier
) {
    val heuristics = remember(journals) { generateLocalInsights(journals) }

    // Phi-3 Simulated Config Settings
    var promptInput by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("Phi-3 Mini (3.8B Weights)") }
    var threadsCount by remember { mutableStateOf(4) }
    var tempSettings by remember { mutableStateOf(0.7f) }
    var quantLevel by remember { mutableStateOf("Q4_K_M (Fast Int4)") }

    var isGenerating by remember { mutableStateOf(false) }
    var loadingStepText by remember { mutableStateOf("") }
    var displayedResultText by remember { mutableStateOf("") }
    var showConfigPanel by remember { mutableStateOf(false) }

    // Rotation angle for indicator
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween<Float>(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Simulating Local Phi-3 Prompt generation
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            loadingStepText = "Allocating CPU tensor memories ($selectedModel)..."
            delay(1200)
            loadingStepText = "Initializing GGML localized model buffers (${quantLevel})..."
            delay(1000)
            loadingStepText = "Running inference with $threadsCount CPU threads (Temp $tempSettings)..."
            delay(1200)
            
            val normalizedPrompt = promptInput.lowercase().trim()
            displayedResultText = when {
                journals.isEmpty() -> {
                    "[Phi-3 Inference Complete - 3.8B Mini Q4_K_M]\n\n" +
                    "Welcome! Your offline database is currently dry (0 journal reflections logged).\n" +
                    "To generate personalized insights, please save a reflection. My local layers will analyze feelings and cognitive fatigue offline."
                }
                normalizedPrompt.contains("mood") || normalizedPrompt.contains("feel") || normalizedPrompt.contains("energy") -> {
                    "[Phi-3 Inference Complete - 3.8B Mini Q4_K_M]\n\n" +
                    "Analyzed ${journals.size} reflections in local database. Your emotional baseline tracks at ${heuristics.resiliencyScore}% Mindscale Resiliency.\n\n" +
                    "• Current Mood Spectrum: ${heuristics.energyTrend}\n" +
                    "• Deep Analysis: Logs index highly on ${heuristics.topKeywords.joinToString(", ") { "#$it" }}. " +
                    "It is recommended to schedule balanced focus blocks to sustain your current positive energy baseline."
                }
                normalizedPrompt.contains("productivity") || normalizedPrompt.contains("task") || normalizedPrompt.contains("breakthrough") || normalizedPrompt.contains("learn") -> {
                    "[Phi-3 Inference Complete - 3.8B Mini Q4_K_M]\n\n" +
                    "Productivity Analysis Matrix:\n" +
                    "Common focus keywords logged are: ${heuristics.topKeywords.joinToString(", ") { "#$it" }}.\n\n" +
                    "Based on this localized semantic parsing, your focus breakthroughs fluctuate according to your mood fatigue cycles. " +
                    "Action point: Introduce structured focus presets (e.g., 25 min Pomodoro slots) on the task tracking dashboard to maximize cognitive retention."
                }
                else -> {
                    "[Phi-3 Inference Complete - 3.8B Mini Q4_K_M]\n\n" +
                    "Context parsed (${journals.size} memories). Word frequency highlighting: ${heuristics.topKeywords.joinToString(", ") { "#$it" }}. " +
                    "Mental Baselines advise: ${heuristics.moodAdvice}\n\n" +
                    "This context was compiled 100% locally on your tablet. No server API connections were called, keeping your personal insights private."
                }
            }
            isGenerating = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("local_ai_insights_card")
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Insights",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOCAL INTELLIGENCE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFD0BCFF)
                    )
                }

                // Privacy Indicator Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF381E72))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Safe",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SECURE & OFFLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFFD0BCFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resiliency Metric Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1D192B))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular rating custom meter
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF49454F).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { heuristics.resiliencyScore.toFloat() / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFD0BCFF),
                        strokeWidth = 6.dp,
                        trackColor = Color(0xFF49454F)
                    )
                    Text(
                        text = "${heuristics.resiliencyScore}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFFE6E1E5)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mental Resiliency Index",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE6E1E5)
                    )
                    Text(
                        text = heuristics.energyTrend,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD0BCFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = heuristics.moodAdvice,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = Color(0xFF938F99),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Semantic Tags detected by heuristic
            if (heuristics.topKeywords.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trends:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE6E1E5)
                    )
                    heuristics.topKeywords.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1D192B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF938F99)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Phi-3 Playground Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phi-3 Local Companion Playground",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE6E1E5)
                )

                IconButton(
                    onClick = { showConfigPanel = !showConfigPanel }
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Setup Options",
                        tint = if (showConfigPanel) Color(0xFFD0BCFF) else Color(0xFF938F99),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Config / Phi-3 Parameters Panel
            AnimatedVisibility(visible = showConfigPanel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1D192B))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Local LLM Execution Parameters",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD0BCFF)
                    )

                    // Model Weights selector simulation
                    Column {
                        Text(
                            text = "Model Base Config:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Color(0xFF938F99)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("Phi-3 Mini 3.8B", "Gemma 2B").forEach { model ->
                                val isSel = selectedModel.startsWith(model)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF381E72) else Color(0xFF49454F).copy(alpha = 0.3f))
                                        .clickable {
                                            selectedModel = if (model.contains("Phi-3")) "Phi-3 Mini (3.8B Weights)" else "Gemma-2 IT (2.1B weights)"
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = model,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSel) Color(0xFFD0BCFF) else Color(0xFFE6E1E5)
                                    )
                                }
                            }
                        }
                    }

                    // Temperature simulation
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Temperature (Creativity):",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color(0xFF938F99)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", tempSettings),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFFD0BCFF)
                            )
                        }
                        Slider(
                            value = tempSettings,
                            onValueChange = { tempSettings = it },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD0BCFF),
                                activeTrackColor = Color(0xFFD0BCFF),
                                inactiveTrackColor = Color(0xFF49454F)
                            )
                        )
                    }

                    // Core threads simulation
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Llama.cpp Threads Allocation:",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color(0xFF938F99)
                            )
                            Text(
                                text = "$threadsCount Cores",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFFD0BCFF)
                            )
                        }
                        Slider(
                            value = threadsCount.toFloat(),
                            onValueChange = { threadsCount = it.toInt() },
                            valueRange = 2f..8f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD0BCFF),
                                activeTrackColor = Color(0xFFD0BCFF),
                                inactiveTrackColor = Color(0xFF49454F)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Prompt Playground input text
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Ask local Phi-3 companion...") },
                placeholder = { Text("e.g. 'Analyze my mood trends this week'") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phi3_prompt_input"),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3,
                trailingIcon = {
                    if (promptInput.isNotEmpty()) {
                        IconButton(onClick = { promptInput = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Prompt")
                        }
                    }
                }
            )

            // Prompt presets macros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Analyze Moods", "Sustained Productivity").forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1D192B))
                            .clickable {
                                promptInput = when (preset) {
                                    "Analyze Moods" -> "Synthesize mood log history feelings patterns."
                                    else -> "Evaluate focus productivity blocks and learning curves."
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                            color = Color(0xFFD0BCFF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Generate Trigger button
            Button(
                onClick = {
                    if (!isGenerating) {
                        isGenerating = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_insights_btn"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                enabled = !isGenerating
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGenerating) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Computing",
                            tint = Color(0xFF381E72),
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer { rotationZ = rotationAngle }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMPUTING OFFLINE...",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF381E72)
                        )
                    } else {
                        Text(
                            text = "RUN PHI-3 INFERENCE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF381E72)
                        )
                    }
                }
            }

            // Simulated generating loader steps
            if (isGenerating) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = loadingStepText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF938F99),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFD0BCFF),
                        trackColor = Color(0xFF49454F)
                    )
                }
            }

            // Playback responses output panel
            if (displayedResultText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1D192B))
                        .padding(16.dp)
                ) {
                    Text(
                        text = displayedResultText,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                        color = Color(0xFFE6E1E5)
                    )
                }
            }
        }
    }
}

private fun generateLocalInsights(entries: List<JournalEntity>): LocalHeuristics {
    if (entries.isEmpty()) {
        return LocalHeuristics(
            resiliencyScore = 100,
            moodAdvice = "Your journal is clear. Take a moment to capture your first reflection!",
            topKeywords = emptyList(),
            energyTrend = "Stable Balance"
        )
    }

    // 1. Mood counts & weighted score
    var totalWeight = 0
    entries.forEach { entry ->
        val weight = when (entry.mood) {
            "Ecstatic" -> 100
            "Happy" -> 85
            "Neutral" -> 60
            "Tired" -> 35
            "Stressed" -> 15
            else -> 50
        }
        totalWeight += weight
    }
    val avgScore = totalWeight / entries.size

    // 2. Keyword detection
    val wordMap = mutableMapOf<String, Int>()
    val fillers = setOf("the", "and", "a", "of", "to", "in", "is", "that", "it", "for", "on", "with", "as", "i", "my", "was", "we", "but", "me", "this", "you", "not", "have", "had", "will")
    entries.forEach { entry ->
        val words = entry.content.lowercase()
            .replace(Regex("[^a-zA-Z\\s]"), "")
            .split(" ")
        words.forEach { w ->
            val clean = w.trim()
            if (clean.length > 3 && clean !in fillers) {
                wordMap[clean] = (wordMap[clean] ?: 0) + 1
            }
        }
    }
    val topWords = wordMap.entries
        .sortedByDescending { it.value }
        .take(3)
        .map { it.key.uppercase() }

    // 3. Status text based on score
    val advice = when {
        avgScore >= 80 -> "Resiliency levels are exceptional. Ideal mental state for planning complex sprints or starting difficult habits."
        avgScore >= 60 -> "Balanced cognitive baseline. Focus and creativity index are steady. Continue journaling reflections regularly."
        avgScore >= 40 -> "Minor fatigue patterns observed. Dynamic insights advise scheduling focus sprints with mandatory break timers."
        else -> "Elevated fatigue or stress detected. Consider prioritizing recovery sessions, reducing task complexity, and capturing simple feelings."
    }

    val energy = when {
        avgScore >= 75 -> "Vibrant Mindscale (High Balance)"
        avgScore >= 50 -> "Steady Baseline (Mid Balance)"
        else -> "Regulating Energy (Low Recovery)"
    }

    return LocalHeuristics(
        resiliencyScore = avgScore,
        moodAdvice = advice,
        topKeywords = topWords,
        energyTrend = energy
    )
}

data class LocalHeuristics(
    val resiliencyScore: Int,
    val moodAdvice: String,
    val topKeywords: List<String>,
    val energyTrend: String
)

@Composable
fun JournalGridItem(
    entry: JournalEntity,
    onClick: () -> Unit,
    onShare: () -> Unit,
    isSpeaking: Boolean = false,
    onToggleTts: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("journal_item_card_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = getMoodEmoji(entry.mood),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 24.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDateString(entry.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleTts,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("tts_play_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop audio playback" else "Listen to entry via TTS",
                            tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_journal_entry_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share entry",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.61f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJournalForm(
    title: String,
    content: String,
    mood: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onMoodChange: (String) -> Unit,
    onTitleMicClick: () -> Unit,
    onContentMicClick: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .testTag("add_journal_form")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Capture Reflection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Form")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Mood scales selectors
        Text(
            text = "Your Mindscale energy",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Ecstatic", "Happy", "Neutral", "Tired", "Stressed").forEach { m ->
                val isSelected = mood == m
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                        .clickable { onMoodChange(m) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getMoodEmoji(m),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 24.sp)
                    )
                    Text(
                        text = m,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Log Title *") },
            maxLines = 1,
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = onTitleMicClick,
                    modifier = Modifier.testTag("form_title_mic_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dictate title",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("journal_title_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reflection context *",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            TextButton(
                onClick = onContentMicClick,
                modifier = Modifier.testTag("form_content_mic_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dictate with Voice", style = MaterialTheme.typography.labelSmall)
            }
        }

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            placeholder = { Text("Reflection context, ideas or learnings...") },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("journal_content_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("journal_save_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Commit Log", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun JournalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search reflections, keywords or mood...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.testTag("journal_search_clear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.testTag("journal_search_mic_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dictate search query",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("journal_search_input")
    )
}

@Composable
fun EmptySearchResultsState(
    query: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No reflections match \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Try searching for a different keyword, feeling, or phrase",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onClear,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("clear_search_btn")
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Search")
        }
    }
}

@Composable
fun EmptyJournalState(onPromptAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            modifier = Modifier.size(68.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Mindscale Canvas is Empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Track your days, capture focus breakthroughs or write ideas. Click add to write.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onPromptAdd,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
        ) {
            Text("Create First Entry")
        }
    }
}

// Helpers
private fun getMoodEmoji(mood: String): String {
    return when (mood) {
        "Ecstatic" -> "😄"
        "Happy" -> "🙂"
        "Neutral" -> "😐"
        "Tired" -> "🥱"
        "Stressed" -> "😫"
        else -> "😐"
    }
}

private fun formatDateString(ticks: Long): String {
    val date = Date(ticks)
    val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return formatter.format(date)
}
