package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WorkspaceViewModel
import com.example.util.JournalExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: WorkspaceViewModel,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val physicalKeyboardEnterEnables by viewModel.physicalKeyboardEnterEnables.collectAsState()
    val autoCapitalizeJournal by viewModel.autoCapitalizeJournal.collectAsState()

    val tasks by viewModel.tasks.collectAsState()
    val journals by viewModel.journals.collectAsState()

    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isUsingDefaultPin by viewModel.isUsingDefaultPin.collectAsState()

    var speechSpeed by remember { mutableStateOf(1.0f) }
    var testPhrasePlayed by remember { mutableStateOf(false) }

    var pendingBackupText by remember { mutableStateOf<String?>(null) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            val text = pendingBackupText ?: ""
            val success = JournalExporter.writeJournalToFile(context, it, text)
            if (success) {
                Toast.makeText(context, "Full workspace backup saved securely to file", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save backup file", Toast.LENGTH_SHORT).show()
            }
        }
        pendingBackupText = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isExpanded) 24.dp else 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Screen Header
            Text(
                text = "Workspace Options",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Control local parameters, behaviors, and diagnostic states offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isExpanded) {
                // Splitscreen Grid for Tablet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        ThemeAndCustomizationCard(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { viewModel.setDarkTheme(it) },
                            isSoundEnabled = isSoundEnabled,
                            onSoundToggle = { viewModel.setSoundEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AccessibilityAndTtsCard(
                            isTtsEnabled = isTtsEnabled,
                            onTtsToggle = { viewModel.setTtsEnabled(it) },
                            speechSpeed = speechSpeed,
                            onSpeedChange = { speechSpeed = it },
                            testPhrasePlayed = testPhrasePlayed,
                            onPlayTestPhrase = { testPhrasePlayed = !testPhrasePlayed }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        KeyboardAndInputsCard(
                            physicalKeyboardEnterEnables = physicalKeyboardEnterEnables,
                            onKeyboardEnterToggle = { viewModel.setPhysicalKeyboardEnterEnables(it) },
                            autoCapitalizeJournal = autoCapitalizeJournal,
                            onAutoCapitalizeToggle = { viewModel.setAutoCapitalizeJournal(it) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AppLockSecurityCard(
                            isAppLockEnabled = isAppLockEnabled,
                            onAppLockToggle = { viewModel.setAppLockEnabled(it) },
                            isBiometricEnabled = isBiometricEnabled,
                            onBiometricToggle = { viewModel.setBiometricEnabled(it) },
                            isUsingDefaultPin = isUsingDefaultPin,
                            onChangePinClick = { showChangePinDialog = true },
                            onLockAppNowClick = {
                                viewModel.lockApp()
                                Toast.makeText(context, "Workspace locked", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        DataPrivacyAndSecurityCard(
                            totalTasks = tasks.size,
                            totalJournals = journals.size,
                            onExportFile = {
                                pendingBackupText = JournalExporter.formatFullWorkspaceBackup(tasks, journals)
                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                backupFileLauncher.launch("workspace_backup_$timeStamp.txt")
                            },
                            onShareBackup = {
                                JournalExporter.shareFullWorkspaceBackup(context, tasks, journals)
                            },
                            onWipeData = {
                                showWipeConfirmDialog = true
                            }
                        )
                    }
                }
            } else {
                // Compact Column Layout for Mobile
                ThemeAndCustomizationCard(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { viewModel.setDarkTheme(it) },
                    isSoundEnabled = isSoundEnabled,
                    onSoundToggle = { viewModel.setSoundEnabled(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AccessibilityAndTtsCard(
                    isTtsEnabled = isTtsEnabled,
                    onTtsToggle = { viewModel.setTtsEnabled(it) },
                    speechSpeed = speechSpeed,
                    onSpeedChange = { speechSpeed = it },
                    testPhrasePlayed = testPhrasePlayed,
                    onPlayTestPhrase = { testPhrasePlayed = !testPhrasePlayed }
                )

                Spacer(modifier = Modifier.height(16.dp))

                KeyboardAndInputsCard(
                    physicalKeyboardEnterEnables = physicalKeyboardEnterEnables,
                    onKeyboardEnterToggle = { viewModel.setPhysicalKeyboardEnterEnables(it) },
                    autoCapitalizeJournal = autoCapitalizeJournal,
                    onAutoCapitalizeToggle = { viewModel.setAutoCapitalizeJournal(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppLockSecurityCard(
                    isAppLockEnabled = isAppLockEnabled,
                    onAppLockToggle = { viewModel.setAppLockEnabled(it) },
                    isBiometricEnabled = isBiometricEnabled,
                    onBiometricToggle = { viewModel.setBiometricEnabled(it) },
                    isUsingDefaultPin = isUsingDefaultPin,
                    onChangePinClick = { showChangePinDialog = true },
                    onLockAppNowClick = {
                        viewModel.lockApp()
                        Toast.makeText(context, "Workspace locked", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DataPrivacyAndSecurityCard(
                    totalTasks = tasks.size,
                    totalJournals = journals.size,
                    onExportFile = {
                        pendingBackupText = JournalExporter.formatFullWorkspaceBackup(tasks, journals)
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        backupFileLauncher.launch("workspace_backup_$timeStamp.txt")
                    },
                    onShareBackup = {
                        JournalExporter.shareFullWorkspaceBackup(context, tasks, journals)
                    },
                    onWipeData = {
                        showWipeConfirmDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showWipeConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showWipeConfirmDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Erase All Offline Data?",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text(
                        text = "This will permanently purge all tasks, checklists, and mindlog reflections from your device's local database. No data exists in the cloud, so this action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllData()
                            showWipeConfirmDialog = false
                            Toast.makeText(context, "All local workspace data permanently erased", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.testTag("confirm_wipe_data_btn")
                    ) {
                        Text("Erase Everything")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showWipeConfirmDialog = false },
                        modifier = Modifier.testTag("cancel_wipe_data_btn")
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showChangePinDialog) {
            var oldPin by remember { mutableStateOf("") }
            var newPin by remember { mutableStateOf("") }
            var confirmPin by remember { mutableStateOf("") }
            var changePinError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showChangePinDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Change 4-Digit Security PIN") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = oldPin,
                            onValueChange = { if (it.length <= 4) oldPin = it },
                            label = { Text("Current PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("current_pin_input")
                        )
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4) newPin = it },
                            label = { Text("New 4-Digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("new_pin_input")
                        )
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { if (it.length <= 4) confirmPin = it },
                            label = { Text("Confirm New PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("confirm_pin_input")
                        )
                        if (changePinError != null) {
                            Text(
                                text = changePinError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
                                changePinError = "New PIN must be exactly 4 digits"
                                return@Button
                            }
                            if (newPin != confirmPin) {
                                changePinError = "New PIN and confirmation do not match"
                                return@Button
                            }
                            val success = viewModel.changePin(oldPin, newPin)
                            if (success) {
                                showChangePinDialog = false
                                Toast.makeText(context, "PIN updated successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                changePinError = "Current PIN is incorrect"
                            }
                        },
                        modifier = Modifier.testTag("save_new_pin_btn")
                    ) {
                        Text("Save PIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeAndCustomizationCard(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_customization_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AESTHETICS & FEEDBACK",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dark Mode switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AMOLED Charcoal Dark Mode",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Saves power offline on OLED tablets & displays",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("dark_mode_toggle")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Sound feedback switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aural Notification alerts",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Play soft tones upon completing Pomodoro and Tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = onSoundToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("sound_toggle")
                )
            }
        }
    }
}

@Composable
fun AccessibilityAndTtsCard(
    isTtsEnabled: Boolean,
    onTtsToggle: (Boolean) -> Unit,
    speechSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    testPhrasePlayed: Boolean,
    onPlayTestPhrase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("accessibility_tts_card")
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEXT TO SPEECH (TTS) ENG",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Local TTS Reader",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Narrate journals and reflections aloud offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = isTtsEnabled,
                    onCheckedChange = onTtsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tts_toggle")
                )
            }

            AnimatedVisibility(visible = isTtsEnabled) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Synthesizer Speed / Tempo:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fx", speechSpeed),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = speechSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onPlayTestPhrase,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (testPhrasePlayed) "⏹️ Stop playback test" else "▶️ Stream test synthesis",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (testPhrasePlayed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "\"Hello! Your offline mindspace is fully calibrated. Local speech synthesis is active.\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardAndInputsCard(
    physicalKeyboardEnterEnables: Boolean,
    onKeyboardEnterToggle: (Boolean) -> Unit,
    autoCapitalizeJournal: Boolean,
    onAutoCapitalizeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyboard_inputs_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PHYSICAL KEYBOARD & INPUT",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enter to save switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enter Key Quick-Submit",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tapping Enter saves reflections and tasks directly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = physicalKeyboardEnterEnables,
                    onCheckedChange = onKeyboardEnterToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("enter_submit_toggle")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Auto capitalize journals switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cognitive Auto-Capitalize",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Automatically enforce word-starts casing in journals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = autoCapitalizeJournal,
                    onCheckedChange = onAutoCapitalizeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("autocap_toggle")
                )
            }
        }
    }
}

@Composable
fun DataPrivacyAndSecurityCard(
    totalTasks: Int,
    totalJournals: Int,
    onExportFile: () -> Unit,
    onShareBackup: () -> Unit,
    onWipeData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("data_privacy_security_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Shield",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DATA PRIVACY & LOCAL SECURITY",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 100% Offline Isolation Badge Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2E7D32).copy(alpha = 0.12f))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Cloud Off",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Offline Device Isolation",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All personal journals and task records reside exclusively in your device's private SQLite sandbox. Android OS internet access is omitted from the manifest, and automatic cloud backups are disabled. Your data will never leave your phone unless you explicitly export it below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diagnostic status rows
            SecurityAuditRow(
                label = "Local Storage Sandbox:",
                value = "Room SQLite (Isolated)",
                valueColor = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SecurityAuditRow(
                label = "Network Transmission:",
                value = "Blocked (Zero INTERNET Permission)",
                valueColor = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SecurityAuditRow(
                label = "Cloud Backup Extraction:",
                value = "Disabled (allowBackup=false)",
                valueColor = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SecurityAuditRow(
                label = "Active Tasks Stored:",
                value = "$totalTasks tasks",
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SecurityAuditRow(
                label = "Mindlogs Stored:",
                value = "$totalJournals entries",
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Controlled Data Export & Management",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Explicitly export your local database records as plaintext files or share them with authorized offline apps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Button(
                onClick = onExportFile,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_workspace_file_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Backup to File (.txt)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onShareBackup,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_workspace_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Workspace Backup")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onWipeData,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wipe_workspace_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Purge Local Database",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SecurityAuditRow(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
fun AppLockSecurityCard(
    isAppLockEnabled: Boolean,
    onAppLockToggle: (Boolean) -> Unit,
    isBiometricEnabled: Boolean,
    onBiometricToggle: (Boolean) -> Unit,
    isUsingDefaultPin: Boolean,
    onChangePinClick: () -> Unit,
    onLockAppNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_lock_security_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "APP LOCK & BIOMETRIC AUTH",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Lock Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Require PIN on Startup",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Secures journal reflections and task data behind authentication",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = isAppLockEnabled,
                    onCheckedChange = onAppLockToggle,
                    modifier = Modifier.testTag("toggle_app_lock")
                )
            }

            if (isAppLockEnabled) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Biometric Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Biometric Fast Unlock",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enable device fingerprint or biometric verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onBiometricToggle,
                        modifier = Modifier.testTag("toggle_biometric")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Current PIN Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PIN Security State:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isUsingDefaultPin) "Default (1234)" else "Custom PIN (SHA-256)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isUsingDefaultPin) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onChangePinClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("change_pin_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change PIN")
                    }

                    Button(
                        onClick = onLockAppNowClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lock_app_now_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock Now")
                    }
                }
            }
        }
    }
}
