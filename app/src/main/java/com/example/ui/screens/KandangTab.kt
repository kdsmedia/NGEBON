package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GameRepository
import com.example.ui.theme.*
import com.example.ui.utils.AdHelper
import com.example.ui.utils.AdNative
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.UiMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KandangTab(
    viewModel: GameViewModel,
    onOpenDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsState()
    val inventories by viewModel.birdInventory.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) break
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }

    val ownedBirds = remember(inventories) {
        inventories.filter { it.count > 0 }
    }

    val totalFeedStock = (progress?.feedStock1 ?: 0) +
            (progress?.feedStock2 ?: 0) +
            (progress?.feedStock3 ?: 0) +
            (progress?.feedStock4 ?: 0) +
            (progress?.feedStock5 ?: 0)

    val totalVitaminStock = (progress?.vitaminStock1 ?: 0) +
            (progress?.vitaminStock2 ?: 0) +
            (progress?.vitaminStock3 ?: 0)

    // Selection Dialog State for Feed or Vitamin
    var activeFeedDialogBirdId by remember { mutableStateOf<Int?>(null) }
    var activeVitaminDialogBirdId by remember { mutableStateOf<Int?>(null) }
    var showDailyMissionsDialog by remember { mutableStateOf(false) }
    var showBreedingDialog by remember { mutableStateOf(false) }
    var showLuckyWheelDialog by remember { mutableStateOf(false) }
    var showEggCatcherDialog by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // WALLET & BALANCE CARD - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(GoldPremiumGradient)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💎 Keuangan Peternakan 💎",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Coins Card
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🪙", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${progress?.coins ?: 0}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Koin",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Eggs in warehouse Card
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🪺", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${progress?.harvestedEggs ?: 0}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Gudang Telur",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Rp conversion
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "💳", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val coins = progress?.coins ?: 0L
                            val rpValue = coins / 100.0
                            Text(
                                text = String.format("Rp%.2f", rpValue),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Nilai WD",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // DAILY MISSIONS & DASHBOARD BUTTONS ROW - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            val missions by viewModel.dailyMissions.collectAsState()
            val claimableCount = remember(missions) {
                missions.count { !it.isClaimed && it.currentProgress >= it.targetCount }
            }
            val completedCount = remember(missions) { missions.count { it.isClaimed } }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // DAILY MISSIONS COMPACT CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDailyMissionsDialog = true }
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (claimableCount > 0) SolidColor(Color(0xFFFFD54F)) else GlassBorderGradient
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("kandang_daily_missions_card"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                if (claimableCount > 0) Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                else PurpleGlossGradient
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎯", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Misi Harian",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                if (claimableCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD54F))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$claimableCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (claimableCount > 0) "Klaim Bonus!" else "$completedCount/${missions.size} Selesai",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (claimableCount > 0) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // DASHBOARD COMPACT CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenDashboard() }
                        .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp))
                        .testTag("open_dashboard_card"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(CoolNeonCyanGradient)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📈", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dashboard",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "20 Spesies",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // ACTIONS: HARVEST & SELL - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.harvestAll() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("harvest_all_button"),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(EmeraldGradient)
                            .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌾", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Panen Semua", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }

                Button(
                    onClick = {
                        if (activity != null) {
                            AdHelper.showInterstitial(activity) {
                                viewModel.sellAll()
                            }
                        } else {
                            viewModel.sellAll()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("sell_all_button"),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SunsetGradient)
                            .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💰", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jual Telur", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }
        }

        // INSTANT AD BOOSTER CARD - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, SolidColor(Color(0xFFFFD54F))), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFFFF007F)))
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "⚡", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Booster Bertelur Instan (Ads)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Dapatkan +15 Telur Instan + 250 Koin Bonus!",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                AdHelper.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = { viewModel.claimAdEggBooster() },
                                    onDismiss = {}
                                )
                            } else {
                                viewModel.claimAdEggBooster()
                            }
                        },
                        modifier = Modifier.height(38.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(19.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(GoldPremiumGradient)
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(19.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬 Booster (Ads)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }

        // FEED & VITAMIN STOCK SUMMARY BAR - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(RoyalMidnightGradient)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGradient)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🌾 Pakan: $totalFeedStock", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CoolNeonCyanGradient)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("💊 Vitamin: $totalVitaminStock", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Button(
                        onClick = { viewModel.feedAllBirdsWithBestFeed() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(GoldPremiumGradient)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌾 Beri Makan Semua", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }

        // FEATURE QUICK ACTIONS BAR - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Feature 1: Kawin Silang
                OutlinedButton(
                    onClick = { showBreedingDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_breeding_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE040FB)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧬 Kawin Silang", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE040FB))
                    }
                }

                // Feature 2: Roda Keberuntungan
                OutlinedButton(
                    onClick = { showLuckyWheelDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_lucky_wheel_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎰 Roda Harian", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F))
                    }
                }

                // Feature 3: Mini Game
                OutlinedButton(
                    onClick = { showEggCatcherDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_egg_catcher_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF00E676)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎮 Mini Game", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E676))
                    }
                }
            }
        }

        // CAGE SANITATION & ORGANIC FERTILIZER BAR - Spans all 4 columns
        val dirtLevel = progress?.dirtLevel ?: 0
        item(span = { GridItemSpan(4) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(RoyalMidnightGradient)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧹", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Kebersihan Kandang: $dirtLevel% Kotor",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dirtLevel > 50) Color(0xFFFF5252) else Color.White
                            )
                            Text(
                                text = "Bersihkan untuk memanen Pupuk Organik!",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.cleanCage() },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(EmeraldGradient)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧹 Sapu", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = {
                                if (activity != null) {
                                    AdHelper.showRewardedAd(
                                        activity = activity,
                                        onRewardEarned = { viewModel.claimAdSuperClean() },
                                        onDismiss = {}
                                    )
                                } else {
                                    viewModel.claimAdSuperClean()
                                }
                            },
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(GoldPremiumGradient)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎬 Super +300 (Ads)", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // MY BIRDS LABEL - Spans all 4 columns
        item(span = { GridItemSpan(4) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪶", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kandang Burung Aktif (${ownedBirds.sumOf { it.count }} Ekor)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // BIRD ENCLOSURE LIST OR EMPTY STATE
        if (ownedBirds.isEmpty()) {
            item(span = { GridItemSpan(4) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🐦", fontSize = 48.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = "Kandang Masih Kosong!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Beli burung pertamamu secara GRATIS di tab 'Toko Burung' untuk mulai memproduksi telur!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Displays in a 4-column grid of mini-cards!
            items(ownedBirds, key = { it.birdId }) { bird ->
                val config = GameRepository.getConfig(bird.birdId)
                if (config != null) {
                    val birdColor = Color(android.graphics.Color.parseColor(config.colorHex))
                    val birdBgGradient = remember(birdColor) {
                        Brush.verticalGradient(
                            listOf(birdColor.copy(alpha = 0.25f), Color(0xFF142218).copy(alpha = 0.85f))
                        )
                    }

                    val now = System.currentTimeMillis()
                    val isVitaminActive = bird.vitaminExpiryTimestamp > now
                    val activeVitConfig = if (isVitaminActive) {
                        GameRepository.VITAMIN_CONFIGS.find { it.id == bird.vitaminBoostLevel }
                    } else null

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.38f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.5.dp, if (isVitaminActive) Color(0xFFFFD54F) else birdColor.copy(alpha = 0.7f)), RoundedCornerShape(16.dp))
                            .testTag("enclosure_card_${bird.birdId}"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(birdBgGradient)
                        ) {
                            // Level Badge at top-left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(brush = GoldPremiumGradient, shape = RoundedCornerShape(bottomEnd = 12.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "Lv.${config.id}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }

                            // Owned Count at top-right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(brush = EmeraldGradient, shape = RoundedCornerShape(bottomStart = 12.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "x${bird.count}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Spacer(modifier = Modifier.height(14.dp))

                                // Bird 3D Icon Container
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(birdColor.copy(alpha = 0.3f))
                                        .border(2.dp, birdColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🐦", fontSize = 18.sp)
                                }

                                // Bird Name
                                Text(
                                    text = config.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Active Vitamin Badge or Upgrade Level
                                if (activeVitConfig != null) {
                                    val remainingMins = ((bird.vitaminExpiryTimestamp - now) / 60000L).coerceAtLeast(1)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SunsetGradient)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "⚡ +${activeVitConfig.boostPercent}% (${remainingMins}m)",
                                            color = Color.White,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Up. Lvl ${bird.upgradeLevel}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                        color = Color(0xFF00F2FE),
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Health Indicator & Egg Production Progress
                                val healthPercent = bird.health.coerceIn(0.0, 100.0).toFloat()
                                val healthColor = when {
                                    healthPercent > 60f -> Color(0xFF38EF7D)
                                    healthPercent > 20f -> Color(0xFFFF9F43)
                                    else -> Color(0xFFFF4757)
                                }

                                val eggProgress = (bird.unharvestedEggsFractional % 1.0).toFloat().coerceIn(0f, 1f)
                                val eggBarColor = if (isVitaminActive) Color(0xFFFFD54F) else Color(0xFF00F2FE)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Health Indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("❤️", fontSize = 7.sp)
                                        Text(
                                            text = "${healthPercent.toInt()}%",
                                            color = healthColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(1.dp))
                                    LinearProgressIndicator(
                                        progress = { healthPercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = healthColor,
                                        trackColor = healthColor.copy(alpha = 0.2f)
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Next Egg Generation Progress Bar (Influenced by Vitamin)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isVitaminActive) "⚡🥚" else "🥚",
                                            fontSize = 7.sp
                                        )
                                        Text(
                                            text = "${(eggProgress * 100).toInt()}%",
                                            color = eggBarColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(1.dp))
                                    LinearProgressIndicator(
                                        progress = { eggProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = eggBarColor,
                                        trackColor = eggBarColor.copy(alpha = 0.2f)
                                    )
                                }

                                // "Beri Makan" Button
                                Button(
                                    onClick = { activeFeedDialogBirdId = bird.birdId },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .testTag("feed_button_${bird.birdId}"),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(EmeraldGradient),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🌾 Makan", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }

                                // "Beri Vitamin" Button
                                Button(
                                    onClick = { activeVitaminDialogBirdId = bird.birdId },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .testTag("vitamin_button_${bird.birdId}"),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(CoolNeonCyanGradient),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("💊 Vitamin", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }

                                // Unharvested Eggs Fraction
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🥚 " + String.format("%.2f", bird.unharvestedEggsFractional),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FEED SELECTION DIALOG ---
    if (activeFeedDialogBirdId != null) {
        val birdId = activeFeedDialogBirdId!!
        val birdConfig = GameRepository.getConfig(birdId)

        AlertDialog(
            onDismissRequest = { activeFeedDialogBirdId = null },
            title = {
                Text(
                    text = "🌾 Pilih Pakan untuk ${birdConfig?.name ?: "Burung"}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameRepository.FEED_CONFIGS.forEach { feed ->
                        val stock = when (feed.id) {
                            1 -> progress?.feedStock1 ?: 0
                            2 -> progress?.feedStock2 ?: 0
                            3 -> progress?.feedStock3 ?: 0
                            4 -> progress?.feedStock4 ?: 0
                            5 -> progress?.feedStock5 ?: 0
                            else -> 0
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = stock > 0) {
                                    viewModel.feedBirdWithFeed(birdId, feed.id)
                                    activeFeedDialogBirdId = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (stock > 0) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(feed.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(feed.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("❤️ +${feed.healthGainPercent.toInt()}% Sembuh | Stok: $stock pcs", color = Color(0xFF38EF7D), fontSize = 11.sp)
                                    }
                                }
                                Text(
                                    text = if (stock > 0) "Gunakan" else "Kosong",
                                    color = if (stock > 0) Color(0xFF00F2FE) else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeFeedDialogBirdId = null }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- VITAMIN SELECTION DIALOG ---
    if (activeVitaminDialogBirdId != null) {
        val birdId = activeVitaminDialogBirdId!!
        val birdConfig = GameRepository.getConfig(birdId)

        AlertDialog(
            onDismissRequest = { activeVitaminDialogBirdId = null },
            title = {
                Text(
                    text = "💊 Beri Vitamin Booster ke ${birdConfig?.name ?: "Burung"}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameRepository.VITAMIN_CONFIGS.forEach { vit ->
                        val stock = when (vit.id) {
                            1 -> progress?.vitaminStock1 ?: 0
                            2 -> progress?.vitaminStock2 ?: 0
                            3 -> progress?.vitaminStock3 ?: 0
                            else -> 0
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = stock > 0) {
                                    viewModel.giveVitaminToBird(birdId, vit.id)
                                    activeVitaminDialogBirdId = null
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (stock > 0) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(vit.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(vit.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("⚡ +${vit.boostPercent}% Laju Bertelur (${vit.durationMinutes}m) | Stok: $stock", color = Color(0xFFFFD54F), fontSize = 11.sp)
                                    }
                                }
                                Text(
                                    text = if (stock > 0) "Gunakan" else "Kosong",
                                    color = if (stock > 0) Color(0xFF00F2FE) else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeVitaminDialogBirdId = null }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDailyMissionsDialog) {
        DailyMissionsDialog(
            viewModel = viewModel,
            onDismiss = { showDailyMissionsDialog = false }
        )
    }

    if (showBreedingDialog) {
        BreedingDialog(
            viewModel = viewModel,
            onDismiss = { showBreedingDialog = false }
        )
    }

    if (showLuckyWheelDialog) {
        LuckyWheelDialog(
            viewModel = viewModel,
            onDismiss = { showLuckyWheelDialog = false }
        )
    }

    if (showEggCatcherDialog) {
        EggCatcherDialog(
            viewModel = viewModel,
            onDismiss = { showEggCatcherDialog = false }
        )
    }
}
