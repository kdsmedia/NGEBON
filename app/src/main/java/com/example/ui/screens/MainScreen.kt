package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.utils.BackgroundMusicManager
import com.example.ui.utils.AdBanner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.GameRepository
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.UiMessage
import com.example.ui.theme.*

import com.example.ui.screens.DailyMissionsDialog

enum class ScreenTab(val title: String) {
    KANDANG("Kandang"),
    TOKO("Toko Burung"),
    DOMPET("Dompet & Riwayat")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ScreenTab.KANDANG) }
    var showDashboard by remember { mutableStateOf(false) }
    var showDailyMissions by remember { mutableStateOf(false) }
    val progress by viewModel.userProgress.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val claimableMissionCount = remember(dailyMissions) {
        dailyMissions.count { !it.isClaimed && it.currentProgress >= it.targetCount }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            when (it) {
                is UiMessage.Success -> {
                    snackbarHostState.showSnackbar(
                        message = it.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiMessage.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "⚠️ ${it.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
            viewModel.clearMessage()
        }
    }

    if (showDashboard) {
        BirdDashboard(
            viewModel = viewModel,
            onBack = { showDashboard = false },
            modifier = modifier.fillMaxSize()
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                // Modern 3D/HTML5 Gradient Top App Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldGradient)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "NGEBON 🐦",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Game Strategi Beternak Burung",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Music Toggle & Daily Missions Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Daily Missions Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (claimableMissionCount > 0) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.2f)
                                    )
                                    .clickable { showDailyMissions = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("topbar_daily_missions_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🎯",
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (claimableMissionCount > 0) "Misi ($claimableMissionCount)" else "Misi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (claimableMissionCount > 0) Color.Black else Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Music Toggle Button with modern background pill
                            val isMuted by BackgroundMusicManager.isMuted.collectAsState()
                            val context = LocalContext.current
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { BackgroundMusicManager.toggleMute(context) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("toggle_music_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isMuted) "🔇" else "🎵",
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isMuted) "Mute" else "Musik",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Column {
                    AdBanner()
                    // Sleek floating glassmorphic dock
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF142218), // Rich dark green gaming container
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tab 1: KANDANG
                                NavigationBarItem3D(
                                    selected = selectedTab == ScreenTab.KANDANG,
                                    onClick = { selectedTab = ScreenTab.KANDANG },
                                    activeEmoji = "🐣",
                                    inactiveEmoji = "🐥",
                                    labelText = "Kandang",
                                    testTag = "tab_kandang"
                                )

                                // Tab 2: TOKO
                                NavigationBarItem3D(
                                    selected = selectedTab == ScreenTab.TOKO,
                                    onClick = { selectedTab = ScreenTab.TOKO },
                                    activeEmoji = "🏪",
                                    inactiveEmoji = "🛒",
                                    labelText = "Toko",
                                    testTag = "tab_toko"
                                )

                                // Tab 3: DOMPET & RIWAYAT
                                NavigationBarItem3D(
                                    selected = selectedTab == ScreenTab.DOMPET,
                                    onClick = { selectedTab = ScreenTab.DOMPET },
                                    activeEmoji = "💰",
                                    inactiveEmoji = "💵",
                                    labelText = "Dompet",
                                    testTag = "tab_dompet"
                                )
                            }
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selectedTab) {
                    ScreenTab.KANDANG -> KandangTab(
                        viewModel = viewModel,
                        onOpenDashboard = { showDashboard = true }
                    )
                    ScreenTab.TOKO -> TokoTab(viewModel = viewModel)
                    ScreenTab.DOMPET -> DompetTab(viewModel = viewModel)
                }
            }

            if (showDailyMissions) {
                DailyMissionsDialog(
                    viewModel = viewModel,
                    onDismiss = { showDailyMissions = false }
                )
            }
        }
    }
}

@Composable
fun NavigationBarItem3D(
    selected: Boolean,
    onClick: () -> Unit,
    activeEmoji: String,
    inactiveEmoji: String,
    labelText: String,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        // Glowing active background under emoji
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        Brush.radialGradient(
                            listOf(Color(0xFF38EF7D).copy(alpha = 0.3f), Color.Transparent)
                        )
                    } else {
                        Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selected) activeEmoji else inactiveEmoji,
                fontSize = if (selected) 26.sp else 20.sp,
                modifier = Modifier.scale(if (selected) 1.15f else 1.0f)
            )
        }
        Text(
            text = labelText,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF38EF7D) else Color.White.copy(alpha = 0.6f)
        )
    }
}

