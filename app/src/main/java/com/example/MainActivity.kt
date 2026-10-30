package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.WorkspaceViewModel
import com.example.ui.screens.AuthLockScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TaskPlannerScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WorkspaceViewModel = viewModel(
                factory = WorkspaceViewModel.Factory(application)
            )
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isAppLocked by viewModel.isAppLocked.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
            val isUsingDefaultPin by viewModel.isUsingDefaultPin.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (isAppLocked) {
                    AuthLockScreen(
                        isUsingDefaultPin = isUsingDefaultPin,
                        isBiometricEnabled = isBiometricEnabled,
                        onPinEntered = { enteredPin ->
                            viewModel.verifyPin(enteredPin)
                        },
                        onBiometricSuccess = {
                            viewModel.unlockApp()
                        },
                        onResetDefaultPin = {
                            viewModel.resetPinToDefault()
                        }
                    )
                } else {
                    WorkspaceRootLayout(viewModel)
                }
            }
        }
    }
}

@Composable
fun WorkspaceRootLayout(viewModel: WorkspaceViewModel) {
    val navItems = remember {
        listOf(
            NavigationItem("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
            NavigationItem("Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, "nav_tasks"),
            NavigationItem("Journal", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "nav_journal"),
            NavigationItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
        )
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { navItems.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Smooth back navigation to Dashboard first
    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isExpanded = maxWidth >= 600.dp

        if (isExpanded) {
            // Responsive Side-Rail for Wide Tablet Displays
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(96.dp)
                        .testTag("tablet_nav_rail"),
                    header = {
                        Text(
                            text = "ZS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = pagerState.currentPage == index
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.testTag(item.tag)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                // Touch-sensitive swipeable pager for Main Content beside the rail
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .testTag("tablet_horizontal_pager")
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            isExpanded = true,
                            onNavigateToTasks = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToJournal = {
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            }
                        )
                        1 -> TaskPlannerScreen(viewModel = viewModel, isExpanded = true)
                        2 -> JournalScreen(viewModel = viewModel, isExpanded = true)
                        3 -> SettingsScreen(viewModel = viewModel, isExpanded = true)
                    }
                }
            }
        } else {
            // Standard Phone Bottom Navigation Layout with Touch-Sensitive Swipeable Screens
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .testTag("phone_nav_bar")
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = pagerState.currentPage == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        modifier = Modifier.testTag(item.tag)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .testTag("phone_horizontal_pager")
                ) { page ->
                    when (page) {
                        0 -> DashboardScreen(
                            viewModel = viewModel,
                            isExpanded = false,
                            onNavigateToTasks = {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToJournal = {
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            }
                        )
                        1 -> TaskPlannerScreen(viewModel = viewModel, isExpanded = false)
                        2 -> JournalScreen(viewModel = viewModel, isExpanded = false)
                        3 -> SettingsScreen(viewModel = viewModel, isExpanded = false)
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
