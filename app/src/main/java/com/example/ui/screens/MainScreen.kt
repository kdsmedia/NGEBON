package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.utils.BackgroundMusicManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.GameRepository
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.UiMessage

enum class ScreenTab(val title: String) {
    KANDANG("Kandang"),
    TOKO("Toko Burung"),
    DOMPET("Dompet WD"),
    LOG("Riwayat WD")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ScreenTab.KANDANG) }
    var showDashboard by remember { mutableStateOf(false) }
    val progress by viewModel.userProgress.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

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
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "NGEBON 🐦",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Game Strategi Beternak Burung",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    ),
                    actions = {
                        val isMuted by BackgroundMusicManager.isMuted.collectAsState()
                        val context = LocalContext.current
                        IconButton(
                            onClick = { BackgroundMusicManager.toggleMute(context) },
                            modifier = Modifier.testTag("toggle_music_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.MusicOff else Icons.Filled.MusicNote,
                                contentDescription = if (isMuted) "Nyalakan Musik" else "Matikan Musik",
                                tint = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == ScreenTab.KANDANG,
                        onClick = { selectedTab = ScreenTab.KANDANG },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == ScreenTab.KANDANG) Icons.Filled.Pets else Icons.Outlined.Pets,
                                contentDescription = "Kandang"
                            )
                        },
                        label = { Text("Kandang") },
                        modifier = Modifier.testTag("tab_kandang")
                    )

                    NavigationBarItem(
                        selected = selectedTab == ScreenTab.TOKO,
                        onClick = { selectedTab = ScreenTab.TOKO },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == ScreenTab.TOKO) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Toko"
                            )
                        },
                        label = { Text("Toko") },
                        modifier = Modifier.testTag("tab_toko")
                    )

                    NavigationBarItem(
                        selected = selectedTab == ScreenTab.DOMPET,
                        onClick = { selectedTab = ScreenTab.DOMPET },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == ScreenTab.DOMPET) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                contentDescription = "Dompet"
                            )
                        },
                        label = { Text("Dompet") },
                        modifier = Modifier.testTag("tab_dompet")
                    )

                    NavigationBarItem(
                        selected = selectedTab == ScreenTab.LOG,
                        onClick = { selectedTab = ScreenTab.LOG },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == ScreenTab.LOG) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "Riwayat"
                            )
                        },
                        label = { Text("Riwayat") },
                        modifier = Modifier.testTag("tab_riwayat")
                    )
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
                    ScreenTab.LOG -> LogTab(viewModel = viewModel)
                }
            }
        }
    }
}
