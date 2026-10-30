package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JournalEntity
import com.example.util.JournalDateFilter
import java.util.Calendar

/**
 * Filter bar with Date Picker trigger, quick day chips (All, Today, Yesterday),
 * active date filter indicator with clear button, and Timeline/Cards view switcher.
 */
@Composable
fun JournalDateFilterBar(
    selectedUtcDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    onOpenDatePicker: () -> Unit,
    isTimelineView: Boolean,
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val todayUtcMillis = remember {
        JournalDateFilter.toUtcMidnight(System.currentTimeMillis())
    }
    val yesterdayUtcMillis = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        JournalDateFilter.toUtcMidnight(cal.timeInMillis)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontal scrollable filter chips
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All" Chip
            FilterChip(
                selected = selectedUtcDateMillis == null,
                onClick = { onDateSelected(null) },
                label = { Text("All Dates") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_chip_all_dates")
            )

            // "Today" Chip
            FilterChip(
                selected = selectedUtcDateMillis == todayUtcMillis,
                onClick = {
                    if (selectedUtcDateMillis == todayUtcMillis) onDateSelected(null)
                    else onDateSelected(todayUtcMillis)
                },
                label = { Text("Today") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_chip_today")
            )

            // "Yesterday" Chip
            FilterChip(
                selected = selectedUtcDateMillis == yesterdayUtcMillis,
                onClick = {
                    if (selectedUtcDateMillis == yesterdayUtcMillis) onDateSelected(null)
                    else onDateSelected(yesterdayUtcMillis)
                },
                label = { Text("Yesterday") },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_chip_yesterday")
            )

            // "Pick Date" / Active Date Chip
            if (selectedUtcDateMillis != null &&
                selectedUtcDateMillis != todayUtcMillis &&
                selectedUtcDateMillis != yesterdayUtcMillis
            ) {
                // Custom picked date chip with clear 'X'
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("active_date_filter_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = JournalDateFilter.formatPickerDateLabel(selectedUtcDateMillis),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(
                            onClick = { onDateSelected(null) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("clear_date_filter_chip_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear date filter",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            } else {
                // Calendar Date Picker Dialog button
                OutlinedButton(
                    onClick = onOpenDatePicker,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_date_picker_dialog_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick specific date",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // View Mode Switcher: Timeline vs Cards
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isTimelineView) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .clickable { onToggleViewMode() }
                .testTag("toggle_timeline_view_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isTimelineView) "Timeline" else "Cards",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isTimelineView) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Material 3 Date Picker Dialog modal allowing users to select any specific date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDatePickerModal(
    initialDateMillis: Long?,
    onDismissRequest: () -> Unit,
    onDateConfirmed: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateConfirmed(it) }
                },
                modifier = Modifier.testTag("date_picker_confirm_btn")
            ) {
                Text("Apply Filter")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("date_picker_cancel_btn")
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Filter Reflections by Date",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}

/**
 * Chronological timeline view organizing journal entries by calendar days,
 * connected with a vertical timeline node spine and timestamp markers.
 */
@Composable
fun JournalTimelineView(
    entries: List<JournalEntity>,
    currentlySpeakingId: Int?,
    onToggleTts: (Int, String) -> Unit,
    onSelectEntry: (JournalEntity) -> Unit,
    onShareEntry: (JournalEntity) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 88.dp)
) {
    val groupedEntries = remember(entries) {
        JournalDateFilter.groupByDate(entries)
    }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("journal_timeline_view")
    ) {
        groupedEntries.forEach { (dateHeader, dayEntries) ->
            item(key = "header_$dateHeader") {
                TimelineDateSectionHeader(
                    dateHeader = dateHeader,
                    count = dayEntries.size
                )
            }

            itemsIndexed(dayEntries, key = { _, item -> item.id }) { index, entry ->
                TimelineNodeItem(
                    entry = entry,
                    isFirstInGroup = index == 0,
                    isLastInGroup = index == dayEntries.size - 1,
                    isSpeaking = currentlySpeakingId == entry.id,
                    onClick = { onSelectEntry(entry) },
                    onShare = { onShareEntry(entry) },
                    onToggleTts = {
                        onToggleTts(
                            entry.id,
                            "${entry.title}. Feeling ${entry.mood}. ${entry.content}"
                        )
                    }
                )
            }
        }
    }
}

/**
 * Date anchor banner for a chronological section in the timeline.
 */
@Composable
fun TimelineDateSectionHeader(
    dateHeader: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateHeader,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Text(
                text = if (count == 1) "1 reflection" else "$count reflections",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Single node in the chronological timeline with spine connector, timestamp badge,
 * and elevated reflection card.
 */
@Composable
fun TimelineNodeItem(
    entry: JournalEntity,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onToggleTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_node_item_${entry.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Timeline Spine and Node marker
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(44.dp)
                .padding(top = 8.dp)
        ) {
            // Node circle marker with Mood Emoji
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getMoodEmojiString(entry.mood),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
                    )
                }
            }

            // Time label under node
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = JournalDateFilter.formatTimeOnly(entry.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            // Vertical timeline line extending down
            if (!isLastInGroup) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Column: Timeline Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .testTag("timeline_card_${entry.id}"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header with mood tag and action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "${getMoodEmojiString(entry.mood)} ${entry.mood}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // TTS Audio Playback
                        IconButton(
                            onClick = onToggleTts,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("tts_play_entry_${entry.id}")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop audio playback" else "Listen to entry via TTS",
                                tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Android Share Intent
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("share_journal_entry_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share entry",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Content preview
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Full date & time footer
                Text(
                    text = JournalDateFilter.formatFullDateTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}

/**
 * State displayed when search or date filters return no matches.
 */
@Composable
fun EmptyFilteredJournalState(
    searchQuery: String,
    selectedUtcDateMillis: Long?,
    onClearFilters: () -> Unit,
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
            imageVector = if (selectedUtcDateMillis != null) Icons.Default.DateRange else Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        val titleText = when {
            selectedUtcDateMillis != null && searchQuery.isNotBlank() ->
                "No entries on ${JournalDateFilter.formatPickerDateLabel(selectedUtcDateMillis)} matching \"$searchQuery\""
            selectedUtcDateMillis != null ->
                "No reflections on ${JournalDateFilter.formatPickerDateLabel(selectedUtcDateMillis)}"
            else ->
                "No reflections match \"$searchQuery\""
        }

        Text(
            text = titleText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Try picking a different date from the calendar or clearing search terms",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onClearFilters,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("clear_all_filters_btn")
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Filters")
        }
    }
}

private fun getMoodEmojiString(mood: String): String {
    return when (mood) {
        "Ecstatic" -> "😄"
        "Happy" -> "🙂"
        "Neutral" -> "😐"
        "Tired" -> "🥱"
        "Stressed" -> "😫"
        else -> "😐"
    }
}
