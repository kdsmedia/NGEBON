package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BirdConfig
import com.example.data.repository.GameRepository
import com.example.data.model.BirdInventory
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.theme.*

@Composable
fun BirdDashboard(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsState()
    val inventories by viewModel.birdInventory.collectAsState()

    val currentCoins = progress?.coins ?: 0L

    // Calculate metrics
    val totalSpecies = GameRepository.BIRD_CONFIGS.size
    val unlockedSpeciesCount = remember(inventories) {
        inventories.count { it.count > 0 }
    }
    val totalProductionRate = remember(inventories) {
        inventories.sumOf { inv ->
            val config = GameRepository.getConfig(inv.birdId)
            if (config != null) {
                GameRepository.getProductionRate(config.eggsPer5Min, inv.upgradeLevel) * inv.count
            } else {
                0
            }
        }
    }

    // Selected Bird detail state for dialog
    var selectedBirdConfig by remember { mutableStateOf<BirdConfig?>(null) }

    // Filters: 0 -> Semua, 1 -> Dimiliki, 2 -> Terkunci
    var filterState by remember { mutableStateOf(0) }

    val filteredConfigs = remember(filterState, inventories) {
        when (filterState) {
            1 -> GameRepository.BIRD_CONFIGS.filter { config ->
                val inv = inventories.find { it.birdId == config.id }
                (inv?.count ?: 0) > 0
            }
            2 -> GameRepository.BIRD_CONFIGS.filter { config ->
                val inv = inventories.find { it.birdId == config.id }
                (inv?.count ?: 0) == 0
            }
            else -> GameRepository.BIRD_CONFIGS
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Sleek dark canvas background
    ) {
        // TOP BACK APP BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalMidnightGradient)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dashboard Burung (Grid 4)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Koleksi & statistik 20 spesies burung",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // STATS OVERVIEW CARDS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Unlock progress Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(CoolNeonCyanGradient)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔑 Terbuka",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$unlockedSpeciesCount / $totalSpecies Spesies",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val progressRatio = unlockedSpeciesCount.toFloat() / totalSpecies
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }

                    // Total speed Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(SunsetGradient)
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚡ Total Kecepatan",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+$totalProductionRate telur / 5m",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // FILTER CHIPS ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple(0, "Semua (20)", "filter_all"),
                Triple(1, "Dimiliki ($unlockedSpeciesCount)", "filter_owned"),
                Triple(2, "Terkunci (${totalSpecies - unlockedSpeciesCount})", "filter_locked")
            ).forEach { (stateId, label, tag) ->
                val isSelected = filterState == stateId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) EmeraldGradient
                            else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
                        )
                        .clickable { filterState = stateId }
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isSelected) GlassBorderGradient else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)))
                            ),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag(tag),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // BIRD 4-COLUMN GRID LIST
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (filteredConfigs.isEmpty()) {
                item(span = { GridItemSpan(4) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tidak Ada Burung",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ubah filter di atas untuk melihat spesies lainnya.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredConfigs, key = { it.id }) { config ->
                    val inventory = inventories.find { it.birdId == config.id }
                    BirdDashboardGridCard(
                        config = config,
                        inventory = inventory,
                        currentCoins = currentCoins,
                        onBuy = { viewModel.buyBird(config.id) },
                        onUpgrade = { viewModel.upgradeBird(config.id) },
                        onCardClick = { selectedBirdConfig = config }
                    )
                }
            }
        }
    }

    // DETAIL DIALOG WHEN CLICKED
    selectedBirdConfig?.let { config ->
        val inventory = inventories.find { it.birdId == config.id }
        BirdDetailDialog(
            config = config,
            inventory = inventory,
            currentCoins = currentCoins,
            totalProductionRate = totalProductionRate.toLong(),
            onBuy = { viewModel.buyBird(config.id) },
            onUpgrade = { viewModel.upgradeBird(config.id) },
            onDismiss = { selectedBirdConfig = null }
        )
    }
}

@Composable
fun BirdDashboardGridCard(
    config: BirdConfig,
    inventory: BirdInventory?,
    currentCoins: Long,
    onBuy: () -> Unit,
    onUpgrade: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countOwned = inventory?.count ?: 0
    val isOwned = countOwned > 0
    val canAffordBuy = currentCoins >= config.cost
    val upgradeCost = if (config.cost > 0) GameRepository.getUpgradeCost(config.cost, inventory?.upgradeLevel ?: 1) else 0L
    val canAffordUpgrade = currentCoins >= upgradeCost && upgradeCost > 0

    val themeColor = remember(config.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(config.colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    val activeRate = GameRepository.getProductionRate(config.eggsPer5Min, inventory?.upgradeLevel ?: 1)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                BorderStroke(
                    1.5.dp,
                    if (isOwned) themeColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable { onCardClick() }
            .testTag("dashboard_bird_card_${config.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isOwned) {
                        Brush.verticalGradient(
                            listOf(themeColor.copy(alpha = 0.3f), Color(0xFF142218).copy(alpha = 0.9f))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B).copy(alpha = 0.4f), Color(0xFF0F172A).copy(alpha = 0.95f))
                        )
                    }
                )
        ) {
            // Badges
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(
                        if (isOwned) themeColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "T${config.id}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isOwned) themeColor else Color.White.copy(alpha = 0.5f)
                )
            }

            if (isOwned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(GoldPremiumGradient, RoundedCornerShape(bottomStart = 8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Lv.${inventory?.upgradeLevel ?: 1}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = if (isOwned) 0.35f else 0.1f))
                        .border(1.dp, if (isOwned) themeColor else Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOwned) "🐦" else "🔒",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Name
                Text(
                    text = config.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOwned) Color.White else Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Rate & Owned
                if (isOwned) {
                    Text(
                        text = "x$countOwned Ekor",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38EF7D)
                    )
                    Text(
                        text = "+$activeRate/5m",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00F2FE)
                    )
                } else {
                    Text(
                        text = "Terkunci",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "🪙 ${config.cost}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                if (isOwned) {
                    if (config.cost == 0L) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Gratis",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Buy +1
                            Button(
                                onClick = onBuy,
                                enabled = canAffordBuy,
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .testTag("dashboard_buy_button_${config.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (canAffordBuy) EmeraldGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+1",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (canAffordBuy) Color.White else Color.White.copy(alpha = 0.3f)
                                    )
                                }
                            }

                            // Upgrade
                            Button(
                                onClick = onUpgrade,
                                enabled = canAffordUpgrade,
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .testTag("dashboard_upgrade_button_${config.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (canAffordUpgrade) GoldPremiumGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Lv+",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (canAffordUpgrade) Color.White else Color.White.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Buy / Unlock button
                    Button(
                        onClick = onBuy,
                        enabled = canAffordBuy,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("dashboard_buy_button_${config.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (canAffordBuy) EmeraldGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Buka",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canAffordBuy) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirdDetailDialog(
    config: BirdConfig,
    inventory: BirdInventory?,
    currentCoins: Long,
    totalProductionRate: Long,
    onBuy: () -> Unit,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val countOwned = inventory?.count ?: 0
    val isOwned = countOwned > 0
    val canAffordBuy = currentCoins >= config.cost
    val upgradeCost = if (config.cost > 0) GameRepository.getUpgradeCost(config.cost, inventory?.upgradeLevel ?: 1) else 0L
    val canAffordUpgrade = currentCoins >= upgradeCost && upgradeCost > 0
    val activeRate = GameRepository.getProductionRate(config.eggsPer5Min, inventory?.upgradeLevel ?: 1)

    val themeColor = remember(config.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(config.colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(BorderStroke(2.dp, themeColor), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF131D2A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.3f))
                        .border(1.dp, themeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isOwned) "🐦" else "🔒", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = config.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Spesies Tier ${config.id} • ${if (isOwned) "Tersedia $countOwned ekor" else "Belum Terbuka"}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Info Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.06f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Laju Produksi Dasar:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("+${config.eggsPer5Min} telur / 5m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        if (isOwned) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Laju Efektif (Lv.${inventory?.upgradeLevel ?: 1}):", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                Text("+$activeRate telur / 5m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F2FE))
                            }
                            if (totalProductionRate > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val contributionPercent = ((activeRate * countOwned).toFloat() / totalProductionRate * 100).toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kontribusi Peternakan:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("$contributionPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                }
                            }
                        }
                    }
                }

                // Actions Section
                if (isOwned) {
                    if (config.cost > 0) {
                        // Buy extra
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tambah Populasi (+1):", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("🪙 ${config.cost} Koin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                            Button(
                                onClick = onBuy,
                                enabled = canAffordBuy,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38EF7D))
                            ) {
                                Text("Beli Lagi", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Upgrade
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Upgrade Laju (+50%):", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("🪙 $upgradeCost Koin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                            Button(
                                onClick = onUpgrade,
                                enabled = canAffordUpgrade,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                            ) {
                                Text("Upgrade", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Burung starter gratis tidak bisa ditambah atau di-upgrade.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biaya Pembukaan:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("🪙 ${config.cost} Koin", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        }
                        Button(
                            onClick = onBuy,
                            enabled = canAffordBuy,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38EF7D))
                        ) {
                            Text("Buka Burung", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.White)
            }
        }
    )
}
