package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GameRepository
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP BACK APP BAR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dashboard Koleksi Burung",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Statistik & optimasi 20 spesies penghasil telur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STATS OVERVIEW CARDS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Unlock progress Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔑 Terbuka",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$unlockedSpeciesCount / $totalSpecies Spesies",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val progressRatio = unlockedSpeciesCount.toFloat() / totalSpecies
                            LinearProgressIndicator(
                                progress = progressRatio,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                    }

                    // Total speed Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚡ Total Kecepatan",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+$totalProductionRate telur",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "/ 5 menit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = filterState == 0,
                onClick = { filterState = 0 },
                label = { Text("Semua (20)") },
                modifier = Modifier.testTag("filter_all")
            )
            FilterChip(
                selected = filterState == 1,
                onClick = { filterState = 1 },
                label = { Text("Dimiliki ($unlockedSpeciesCount)") },
                modifier = Modifier.testTag("filter_owned")
            )
            FilterChip(
                selected = filterState == 2,
                onClick = { filterState = 2 },
                label = { Text("Terkunci (${totalSpecies - unlockedSpeciesCount})") },
                modifier = Modifier.testTag("filter_locked")
            )
        }

        // BIRD GRID LIST
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (filteredConfigs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Hasil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coba ubah kriteria filter pencarian Anda di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredConfigs, key = { it.id }) { config ->
                    val inventory = inventories.find { it.birdId == config.id }
                    BirdDashboardCard(
                        config = config,
                        inventory = inventory,
                        currentCoins = currentCoins,
                        totalProductionRate = totalProductionRate.toLong(),
                        onBuy = { viewModel.buyBird(config.id) },
                        onUpgrade = { viewModel.upgradeBird(config.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BirdDashboardCard(
    config: com.example.data.repository.BirdConfig,
    inventory: com.example.data.model.BirdInventory?,
    currentCoins: Long,
    totalProductionRate: Long,
    onBuy: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countOwned = inventory?.count ?: 0
    val isOwned = countOwned > 0
    val canAfford = currentCoins >= config.cost

    val themeColor = remember(config.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(config.colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    // Animation tracking states
    val currentLevel = inventory?.upgradeLevel ?: 1
    val currentCount = inventory?.count ?: 0
    var previousLevel by remember { mutableStateOf<Int?>(null) }
    var previousCount by remember { mutableStateOf<Int?>(null) }
    var showUpgradeEffect by remember { mutableStateOf(false) }
    var effectType by remember { mutableStateOf("") } // "upgrade" or "buy"

    LaunchedEffect(currentLevel) {
        if (previousLevel != null && currentLevel > previousLevel!!) {
            showUpgradeEffect = true
            effectType = "upgrade"
            delay(1500)
            showUpgradeEffect = false
        }
        previousLevel = currentLevel
    }

    LaunchedEffect(currentCount) {
        if (previousCount != null && currentCount > previousCount!!) {
            showUpgradeEffect = true
            effectType = "buy"
            delay(1500)
            showUpgradeEffect = false
        }
        previousCount = currentCount
    }

    // Bounce Scale Animation
    val scale by animateFloatAsState(
        targetValue = if (showUpgradeEffect) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    // Border glowing/color animation spec
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val customBorderColor = if (showUpgradeEffect) {
        if (effectType == "upgrade") {
            MaterialTheme.colorScheme.tertiary.copy(alpha = pulseAlpha)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
        }
    } else {
        if (isOwned) themeColor.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f)
    }

    val customBorderWidth = if (showUpgradeEffect) 2.5.dp else (if (isOwned) 1.5.dp else 1.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_bird_card_${config.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (showUpgradeEffect) {
                    if (effectType == "upgrade") {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    }
                } else if (isOwned) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            ),
            border = BorderStroke(
                width = customBorderWidth,
                color = customBorderColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (showUpgradeEffect) 6.dp else if (isOwned) 2.dp else 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Floating Success Banner/Badge if upgraded/purchased
                AnimatedVisibility(
                    visible = showUpgradeEffect,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (effectType == "upgrade") {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            .padding(vertical = 6.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (effectType == "upgrade") "✨ UPGRADE SUKSES! Laju Produksi +50% ✨" else "🎉 BURUNG BERHASIL DIBELI! 🎉",
                            color = if (effectType == "upgrade") MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Circle containing Icon with Theme Color Background
                    val rotateAngle by animateFloatAsState(
                        targetValue = if (showUpgradeEffect) 360f else 0f,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "iconRotate"
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(themeColor.copy(alpha = if (isOwned) 0.15f else 0.05f))
                            .border(
                                1.5.dp,
                                if (isOwned) themeColor else Color.Gray.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .graphicsLayer(rotationZ = rotateAngle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isOwned) {
                                if (showUpgradeEffect) "✨" else "🐦"
                            } else {
                                "🔒"
                            },
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Middle Info Section
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = config.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isOwned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(
                                    containerColor = if (isOwned) themeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isOwned) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = "Tier ${config.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (isOwned) {
                                    val badgeBgColor by animateColorAsState(
                                        targetValue = if (showUpgradeEffect && effectType == "upgrade") {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        animationSpec = tween(500),
                                        label = "badgeBgColor"
                                    )
                                    Badge(
                                        containerColor = badgeBgColor,
                                        contentColor = if (showUpgradeEffect && effectType == "upgrade") {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = "Lv. ${inventory?.upgradeLevel ?: 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (isOwned) "Milik Anda: $countOwned ekor" else "Belum Terbuka",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isOwned) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isOwned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Production Rate Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Produksi",
                                tint = if (isOwned) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val activeRate = GameRepository.getProductionRate(config.eggsPer5Min, inventory?.upgradeLevel ?: 1)
                            Text(
                                text = "Laju: +$activeRate telur / 5 m per ekor",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isOwned && totalProductionRate > 0) {
                            val activeRate = GameRepository.getProductionRate(config.eggsPer5Min, inventory?.upgradeLevel ?: 1)
                            val contributionPercent = ((activeRate * countOwned).toFloat() / totalProductionRate * 100).toInt()
                            Text(
                                text = "Kontribusi: $contributionPercent% dari total peternakan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cost and Purchase Button Section
                        if (isOwned) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            // Row for Beli Lagi (Buy More)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Tambah Populasi (+1 ekor):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = "Koin",
                                            tint = if (canAfford) Color(0xFFFBC02D) else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${config.cost} Koin",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canAfford) MaterialTheme.colorScheme.onSurface else Color.Gray
                                        )
                                    }
                                }

                                Button(
                                    onClick = onBuy,
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("dashboard_buy_button_${config.id}")
                                ) {
                                    Text(
                                        text = "Beli Lagi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Row for Upgrade (Tingkatkan Laju)
                            val upgradeCost = GameRepository.getUpgradeCost(config.cost, inventory?.upgradeLevel ?: 1)
                            val canAffordUpgrade = currentCoins >= upgradeCost
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Upgrade Laju Produksi (+50%):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = "Koin",
                                            tint = if (canAffordUpgrade) Color(0xFFFBC02D) else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$upgradeCost Koin",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canAffordUpgrade) MaterialTheme.colorScheme.onSurface else Color.Gray
                                        )
                                    }
                                }

                                Button(
                                    onClick = onUpgrade,
                                    enabled = canAffordUpgrade,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("dashboard_upgrade_button_${config.id}")
                                ) {
                                    Text(
                                        text = "Upgrade Laju",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            // Not owned yet, standard unlock flow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Biaya Buka:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = "Koin",
                                            tint = if (canAfford) Color(0xFFFBC02D) else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${config.cost} Koin",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (canAfford) MaterialTheme.colorScheme.onSurface else Color.Gray
                                        )
                                    }
                                }

                                Button(
                                    onClick = onBuy,
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeColor,
                                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("dashboard_buy_button_${config.id}")
                                ) {
                                    Text(
                                        text = "Buka",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
