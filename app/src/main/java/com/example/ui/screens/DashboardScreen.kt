package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import com.example.ui.components.DailySummaryCard
import com.example.ui.components.FocusTopicCard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.JournalEntity
import com.example.data.TaskEntity
import com.example.ui.TimerPreset
import com.example.ui.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: WorkspaceViewModel,
    isExpanded: Boolean,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val journals by viewModel.journals.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val inferenceReport by viewModel.inferenceReport.collectAsState()
    val isInferenceRunning by viewModel.isInferenceRunning.collectAsState()

    LaunchedEffect(Unit) {
        if (inferenceReport == null) {
            viewModel.runLocalInference()
        }
    }

    var isVoiceJournalOpen by remember { mutableStateOf(false) }
    var isAiInsightsOpen by remember { mutableStateOf(false) }

    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val totalJournals = journals.size

    val greeting = getGreetingMessage()
    val todayDate = getTodayDateString()

    // Screen Layout: Supports both Expanded (Tablet side-by-side) and Custom Compact
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isExpanded) {
            // Tablet Side-by-Side Dual Column View
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Greeting, Stats, Atmosphere Visual & Bottom Shortcuts
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    WelcomeBanner(greeting = greeting, todayDate = todayDate)
                    StatsGrid(
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        totalJournals = totalJournals
                    )
                    AtmosphericArtsCard()
                    BottomShortcutsSection(
                        isExpanded = true,
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        onOpenVoiceJournal = { isVoiceJournalOpen = true },
                        onOpenAiInsights = { isAiInsightsOpen = true },
                        onNavigateToTasks = onNavigateToTasks
                    )
                }

                // Right Column: Advanced Circular Timer Panel
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    TimerWorkspace(
                        isTimerRunning = isTimerRunning,
                        timeRemaining = timeRemaining,
                        selectedPreset = selectedPreset,
                        onPresetSelect = { viewModel.selectTimerPreset(it) },
                        onStart = { viewModel.startTimer() },
                        onPause = { viewModel.pauseTimer() },
                        onReset = { viewModel.resetTimer() }
                    )
                }
            }
        } else {
            // Phone Portrait Scroll View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WelcomeBanner(greeting = greeting, todayDate = todayDate)
                StatsGrid(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    totalJournals = totalJournals
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    TimerWorkspace(
                        isTimerRunning = isTimerRunning,
                        timeRemaining = timeRemaining,
                        selectedPreset = selectedPreset,
                        onPresetSelect = { viewModel.selectTimerPreset(it) },
                        onStart = { viewModel.startTimer() },
                        onPause = { viewModel.pauseTimer() },
                        onReset = { viewModel.resetTimer() }
                    )
                }
                AtmosphericArtsCard()

                // Shortcuts at the bottom for Voice Journal, AI Insights, and Tasks
                BottomShortcutsSection(
                    isExpanded = false,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    onOpenVoiceJournal = { isVoiceJournalOpen = true },
                    onOpenAiInsights = { isAiInsightsOpen = true },
                    onNavigateToTasks = onNavigateToTasks
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (isVoiceJournalOpen) {
            VoiceJournalDialog(
                onDismiss = { isVoiceJournalOpen = false },
                onSaveVoiceEntry = { title, content, mood ->
                    viewModel.addJournalEntry(title, content, mood)
                    isVoiceJournalOpen = false
                },
                onNavigateToJournal = {
                    isVoiceJournalOpen = false
                    onNavigateToJournal()
                }
            )
        }

        if (isAiInsightsOpen) {
            AiInsightsDialog(
                tasks = tasks,
                journals = journals,
                inferenceReport = inferenceReport,
                onDismiss = { isAiInsightsOpen = false },
                onNavigateToTasks = {
                    isAiInsightsOpen = false
                    onNavigateToTasks()
                },
                onNavigateToJournal = {
                    isAiInsightsOpen = false
                    onNavigateToJournal()
                },
                onStartTopic = { topic ->
                    isAiInsightsOpen = false
                    viewModel.startFocusSessionWithTopic(topic)
                }
            )
        }
    }
}

@Composable
fun WelcomeBanner(greeting: String, todayDate: String) {
    Column {
        Text(
            text = todayDate,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("dashboard_date")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = greeting,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 38.sp,
            modifier = Modifier.testTag("dashboard_greeting")
        )
    }
}

@Composable
fun StatsGrid(totalTasks: Int, completedTasks: Int, totalJournals: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Completed",
            value = "$completedTasks/$totalTasks",
            subtitle = "Tasks done",
            themeColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.CheckCircle,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Reflections",
            value = totalJournals.toString(),
            subtitle = "Logged logs",
            themeColor = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.Book,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    themeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun AtmosphericArtsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Aesthetic Zen Companion",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Design acts as a clear silent order. Enjoy your focus, balance details, and persist calmly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun TimerWorkspace(
    isTimerRunning: Boolean,
    timeRemaining: Int,
    selectedPreset: TimerPreset,
    onPresetSelect: (TimerPreset) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val totalSeconds = selectedPreset.minutes * 60
    val progressFraction = if (totalSeconds > 0) {
        (timeRemaining.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    // Smooth visual progress animation:
    // When running, interpolates over the 1000ms tick period with LinearEasing for liquid-smooth continuous motion.
    // When resetting or switching presets, smoothly transitions in 400ms.
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = if (isTimerRunning) {
            tween(durationMillis = 1000, easing = LinearEasing)
        } else {
            tween(durationMillis = 400, easing = FastOutSlowInEasing)
        },
        label = "pomodoro_progress_ring"
    )

    // Themed palettes per preset mode
    val (presetGradient, presetTrackColor, presetAccent) = when (selectedPreset) {
        TimerPreset.FOCUS -> Triple(
            listOf(Color(0xFF6366F1), Color(0xFF38BDF8), Color(0xFF818CF8)),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            Color(0xFF6366F1)
        )
        TimerPreset.SHORT_BREAK -> Triple(
            listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF059669)),
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color(0xFF10B981)
        )
        TimerPreset.LONG_BREAK -> Triple(
            listOf(Color(0xFFF59E0B), Color(0xFFFB923C), Color(0xFFF43F5E)),
            Color(0xFFF59E0B).copy(alpha = 0.15f),
            Color(0xFFF59E0B)
        )
    }

    // Breathing pulse for live active focus state
    val infiniteTransition = rememberInfiniteTransition(label = "TimerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TimerPulseAlpha"
    )
    val pulseGlowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TimerPulseScale"
    )

    val neutralOnSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pomodoro Timer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isTimerRunning) "Session in progress" else "Ready to focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Compact Mode Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(presetAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = selectedPreset.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = presetAccent
                )
            }
        }

        // Preset selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(24.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimerPreset.values().forEach { preset ->
                val isSelected = preset == selectedPreset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) presetAccent else Color.Transparent
                        )
                        .clickable { onPresetSelect(preset) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.displayName.split(" ")[0],
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Visual Progress Ring Display
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radiusSize = (size.minDimension - strokeWidth * 1.5f).coerceAtLeast(0f)
                val canvasCenter = Offset(size.width / 2f, size.height / 2f)

                if (radiusSize > 0f) {
                    val trackRadius = radiusSize / 2f

                    // 1. Ambient breathing glow behind the ring when timer is active
                    if (isTimerRunning) {
                        drawCircle(
                            color = presetAccent.copy(alpha = 0.08f * pulseAlpha),
                            radius = trackRadius * pulseGlowScale,
                            center = canvasCenter
                        )
                    }

                    // 2. Subtle Precision Dial Ticks (12 pips for 5-minute intervals)
                    for (i in 0 until 12) {
                        val angleDeg = (i * 30.0) - 90.0
                        val angleRad = Math.toRadians(angleDeg)
                        val isMajor = (i % 3 == 0) // Quarters (12, 3, 6, 9)
                        val innerR = trackRadius - (if (isMajor) 18.dp.toPx() else 14.dp.toPx())
                        val outerR = trackRadius - 8.dp.toPx()

                        val startX = canvasCenter.x + innerR * cos(angleRad).toFloat()
                        val startY = canvasCenter.y + innerR * sin(angleRad).toFloat()
                        val endX = canvasCenter.x + outerR * cos(angleRad).toFloat()
                        val endY = canvasCenter.y + outerR * sin(angleRad).toFloat()

                        drawLine(
                            color = presetTrackColor.copy(alpha = if (isMajor) 0.5f else 0.25f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // 3. Background Track Ring
                    drawCircle(
                        color = presetTrackColor,
                        radius = trackRadius,
                        center = canvasCenter,
                        style = Stroke(width = strokeWidth)
                    )

                    // 4. Smooth Animated Progress Arc
                    val sweepAngle = 360f * animatedProgress
                    val arcTopLeft = Offset(
                        canvasCenter.x - trackRadius,
                        canvasCenter.y - trackRadius
                    )

                    if (sweepAngle > 0.5f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = presetGradient,
                                center = canvasCenter
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            ),
                            size = Size(radiusSize, radiusSize),
                            topLeft = arcTopLeft
                        )

                        // 5. Luminous Orbital Tip Bead (smoothly travels with the arc head)
                        val tipAngleRad = Math.toRadians((-90.0 + sweepAngle).toDouble())
                        val tipX = canvasCenter.x + trackRadius * cos(tipAngleRad).toFloat()
                        val tipY = canvasCenter.y + trackRadius * sin(tipAngleRad).toFloat()

                        // Outer soft glowing halo
                        drawCircle(
                            color = presetAccent.copy(alpha = if (isTimerRunning) pulseAlpha else 0.5f),
                            radius = strokeWidth * 0.9f,
                            center = Offset(tipX, tipY)
                        )
                        // Solid inner white core
                        drawCircle(
                            color = Color.White,
                            radius = strokeWidth * 0.35f,
                            center = Offset(tipX, tipY)
                        )
                        // Accent colored center pip
                        drawCircle(
                            color = presetAccent,
                            radius = strokeWidth * 0.18f,
                            center = Offset(tipX, tipY)
                        )
                    }
                }
            }

            // Central Information: Monospace countdown, status badge, and progress fraction
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Status pill badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(presetAccent.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTimerRunning) presetAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isTimerRunning) {
                            if (selectedPreset == TimerPreset.FOCUS) "FOCUS" else "REST"
                        } else if (progressFraction in 0.01f..0.999f) {
                            "PAUSED"
                        } else {
                            "READY"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = if (isTimerRunning) presetAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Countdown Digits
                Text(
                    text = formatTimerString(timeRemaining),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = neutralOnSurface,
                    modifier = Modifier.testTag("timer_countdown_clock")
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Percentage and minutes remaining
                val percentRemaining = (animatedProgress * 100).toInt()
                Text(
                    text = "$percentRemaining% remaining",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }

        // Timer action controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        CircleShape
                    )
                    .testTag("timer_reset_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Play / Pause Action Button
            Button(
                onClick = { if (isTimerRunning) onPause() else onStart() },
                modifier = Modifier
                    .height(54.dp)
                    .testTag("timer_play_pause_btn"),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTimerRunning) MaterialTheme.colorScheme.error else presetAccent
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimerRunning) "Pause Session" else "Start ${selectedPreset.displayName.split(" ")[0]}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Helpers
private fun formatTimerString(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

private fun getGreetingMessage(): String {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Welcome, Focus Mornings"
        in 12..16 -> "Good Afternoon, Creator"
        in 17..21 -> "Good Evening, Reflection"
        else -> "Good Night, Serene Rest"
    }
}

private fun getTodayDateString(): String {
    val date = Calendar.getInstance().time
    val format = SimpleDateFormat("EEE, d MMMM yyyy", Locale.getDefault())
    return format.format(date).uppercase(Locale.getDefault())
}

@Composable
fun BottomShortcutsSection(
    isExpanded: Boolean,
    totalTasks: Int,
    completedTasks: Int,
    onOpenVoiceJournal: () -> Unit,
    onOpenAiInsights: () -> Unit,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_shortcuts_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Shortcuts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Direct Actions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }

        if (isExpanded) {
            // Tablet: 3 columns in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShortcutCard(
                    title = "Voice Journal",
                    subtitle = "Capture audio reflection",
                    badge = "Record",
                    badgeEmoji = "🎙️",
                    icon = Icons.Default.Mic,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    testTag = "shortcut_voice_journal",
                    onClick = onOpenVoiceJournal,
                    modifier = Modifier.weight(1f)
                )
                ShortcutCard(
                    title = "AI Insights",
                    subtitle = "Focus & habit trends",
                    badge = "Analyze",
                    badgeEmoji = "✨",
                    icon = Icons.Default.AutoAwesome,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    testTag = "shortcut_ai_insights",
                    onClick = onOpenAiInsights,
                    modifier = Modifier.weight(1f)
                )
                ShortcutCard(
                    title = "Tasks",
                    subtitle = "$completedTasks/$totalTasks done",
                    badge = "Planner",
                    badgeEmoji = "📋",
                    icon = Icons.Default.TaskAlt,
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    testTag = "shortcut_tasks",
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Mobile: 3 rich vertical cards with >= 48dp touch targets
            ShortcutCard(
                title = "Voice Journal",
                subtitle = "Dictate thoughts & voice reflections",
                badge = "Record Voice",
                badgeEmoji = "🎙️",
                icon = Icons.Default.Mic,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                testTag = "shortcut_voice_journal",
                onClick = onOpenVoiceJournal
            )
            ShortcutCard(
                title = "AI Insights",
                subtitle = "Cognitive heuristics & smart suggestions",
                badge = "View Insights",
                badgeEmoji = "✨",
                icon = Icons.Default.AutoAwesome,
                iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                testTag = "shortcut_ai_insights",
                onClick = onOpenAiInsights
            )
            ShortcutCard(
                title = "Tasks",
                subtitle = "$completedTasks of $totalTasks completed • Open Planner",
                badge = "Open Tasks",
                badgeEmoji = "📋",
                icon = Icons.Default.TaskAlt,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                testTag = "shortcut_tasks",
                onClick = onNavigateToTasks
            )
        }
    }
}

@Composable
fun ShortcutCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeEmoji: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainerColor: Color,
    iconContentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconContentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$badgeEmoji $badge",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceJournalDialog(
    onDismiss: () -> Unit,
    onSaveVoiceEntry: (title: String, content: String, mood: String) -> Unit,
    onNavigateToJournal: () -> Unit
) {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var moodInput by remember { mutableStateOf("Happy") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            isRecording = true
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                contentInput = if (contentInput.isBlank()) spokenText else "$contentInput $spokenText"
            }
        }
        isRecording = false
    }

    // Timer when recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Animated waveform bars
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val waveAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val waveAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val waveAnim3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("voice_journal_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Voice Journal",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Record or dictate your reflection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_voice_journal_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Voice Recording Area
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRecording) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Central Record Button with Touch Target >= 56dp
                        Button(
                            onClick = {
                                if (isRecording) {
                                    isRecording = false
                                    if (contentInput.isBlank()) {
                                        val nowStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                                        contentInput = "Voice reflection recorded at $nowStr. Captured focus thoughts and mind state."
                                    }
                                } else {
                                    if (hasAudioPermission) {
                                        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your journal reflection...")
                                        }
                                        try {
                                            speechRecognizerLauncher.launch(speechIntent)
                                            isRecording = true
                                        } catch (e: Exception) {
                                            // Fallback to internal audio recording simulation
                                            isRecording = true
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("toggle_voice_record_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                tint = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // State & Timer indicator
                        Text(
                            text = if (isRecording) {
                                "Recording... ${String.format(Locale.US, "%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)}"
                            } else {
                                "Tap to Record Voice"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        )

                        // Audio Waveform Visualizer
                        if (isRecording) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(36.dp)
                            ) {
                                val heights = listOf(waveAnim1, waveAnim2, waveAnim3, waveAnim1 * 0.7f, waveAnim3 * 0.8f, waveAnim2, waveAnim1)
                                heights.forEach { fraction ->
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height((32 * fraction).coerceAtLeast(6f).dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Or type your reflection directly below",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Mood selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Reflection Mood",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "Ecstatic" to "😄",
                            "Happy" to "🙂",
                            "Neutral" to "😐",
                            "Tired" to "🥱",
                            "Stressed" to "🤯"
                        ).forEach { (moodName, emoji) ->
                            val isSelected = moodInput == moodName
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                    .clickable { moodInput = moodName }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                                Text(
                                    text = moodName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title (Optional)") },
                    placeholder = { Text("e.g. Focus Sprint Note") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_journal_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Content / Transcript field
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Transcription & Reflection") },
                    placeholder = { Text("Your voice or typed reflection...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_journal_content_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val finalTitle = titleInput.ifBlank {
                                "Voice Note - ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())}"
                            }
                            val finalContent = contentInput.ifBlank {
                                "Voice reflection recorded. Maintained focus and serene workflow."
                            }
                            onSaveVoiceEntry(finalTitle, finalContent, moodInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_voice_journal_btn"),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Save Reflection to Journal",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToJournal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_journal_from_voice_btn"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("View All Entries in Journal")
                    }
                }
            }
        }
    }
}

@Composable
fun AiInsightsDialog(
    tasks: List<TaskEntity>,
    journals: List<JournalEntity>,
    inferenceReport: com.example.ai.LocalInferenceReport? = null,
    onDismiss: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onStartTopic: (com.example.ai.FocusTopicSuggestion) -> Unit = {}
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val taskEfficiency = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 100

    val resiliencyScore = calculateResiliencyScore(journals)
    val recommendation = generateSmartRecommendation(totalTasks, completedTasks, pendingTasks, resiliencyScore)
    val themes = extractThemes(journals)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("ai_insights_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI Insights",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cognitive & productivity heuristics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_ai_insights_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Task Completion",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$taskEfficiency%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$completedTasks of $totalTasks done",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Resiliency Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$resiliencyScore/100",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = if (resiliencyScore >= 70) "Optimal State" else "Regulating",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Local On-Device AI Daily Summary & Focus Topics
                if (inferenceReport != null) {
                    DailySummaryCard(
                        summary = inferenceReport.dailySummary,
                        onRefresh = {}
                    )

                    if (inferenceReport.suggestedTopics.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "SUGGESTED FOCUS TOPICS (FROM TASK HISTORY)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            inferenceReport.suggestedTopics.take(2).forEach { topic ->
                                FocusTopicCard(
                                    topic = topic,
                                    onStartTimer = onStartTopic
                                )
                            }
                        }
                    }
                } else {
                    // AI Recommendation Card Fallback
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "AI WORKSPACE RECOMMENDATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Top Themes from Journals
                if (themes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Recent Reflection Themes",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            themes.forEach { theme ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#$theme",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToTasks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("insights_go_tasks_btn"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Tasks Planner", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToJournal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("insights_go_journal_btn"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Full Journal Analysis")
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("insights_dismiss_btn")
                    ) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

private fun calculateResiliencyScore(entries: List<JournalEntity>): Int {
    if (entries.isEmpty()) return 85
    var total = 0
    entries.forEach { entry ->
        val weight = when (entry.mood) {
            "Ecstatic" -> 100
            "Happy" -> 85
            "Neutral" -> 60
            "Tired" -> 35
            "Stressed" -> 15
            else -> 50
        }
        total += weight
    }
    return (total / entries.size).coerceIn(10, 100)
}

private fun generateSmartRecommendation(
    totalTasks: Int,
    completedTasks: Int,
    pendingTasks: Int,
    resiliencyScore: Int
): String {
    return when {
        totalTasks == 0 ->
            "Start your day by organizing 2-3 key tasks in the Tasks Planner, then use a 25-minute Pomodoro focus block to build momentum."
        pendingTasks > 0 && resiliencyScore < 50 ->
            "Cognitive fatigue pattern detected with $pendingTasks pending tasks. Prioritize the single most important task and schedule a 15-minute restorative break."
        pendingTasks > 3 ->
            "You have $pendingTasks active tasks waiting. Launch a 25m Focus Session on your top priority task to prevent context switching."
        pendingTasks == 0 && totalTasks > 0 ->
            "Fantastic work! All $totalTasks tasks are completed. Capture your victory in the Voice Journal and take time to recharge."
        else ->
            "Productivity flow is well-balanced. Keep up the steady cadence by using short breaks and capturing regular reflections."
    }
}

private fun extractThemes(entries: List<JournalEntity>): List<String> {
    if (entries.isEmpty()) return listOf("FOCUS", "MINDFULNESS", "CLARITY")
    val words = mutableMapOf<String, Int>()
    val stopWords = setOf("the", "and", "a", "to", "in", "is", "of", "it", "with", "for", "on", "my", "was", "this", "that", "i", "at", "as", "by")
    entries.forEach { entry ->
        entry.content.lowercase().split(Regex("[^a-zA-Z]")).forEach { word ->
            val clean = word.trim()
            if (clean.length > 3 && clean !in stopWords) {
                words[clean] = (words[clean] ?: 0) + 1
            }
        }
    }
    val top = words.entries.sortedByDescending { it.value }.take(3).map { it.key.uppercase() }
    return if (top.isNotEmpty()) top else listOf("FOCUS", "PRODUCTIVITY", "SERENITY")
}
