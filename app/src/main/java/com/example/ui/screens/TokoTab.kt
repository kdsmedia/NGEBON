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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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

@Composable
fun TokoTab(
    viewModel: GameViewModel,
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

    val currentCoins = progress?.coins ?: 0L
    val todayClaimCount = progress?.adsRewardClaimCountToday ?: 0
    val remainingAdClaims = (5 - todayClaimCount).coerceAtLeast(0)

    // Category Selector State (0 = Pasar Burung, 1 = Toko Pakan, 2 = Toko Vitamin)
    var selectedCategory by remember { mutableIntStateOf(0) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(12),
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // INFO HEADER CARD - Spans all 12 columns
        item(span = { GridItemSpan(12) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(PurpleGlossGradient)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏪",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Toko Pasar Burung & Perlengkapan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Koin: $currentCoins 🪙 | Beli burung, 5 pakan bernutrisi, & 3 booster vitamin!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // AD REWARD FREE FEED BANNER - Spans all 12 columns
        item(span = { GridItemSpan(12) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp))
                    .testTag("ad_reward_feed_card"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(GoldPremiumGradient)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "🎁", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pakan Burung Gratis! (Ads)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Tonton iklan -> Gratis 1 Pakan! Sisa klaim: $remainingAdClaims/5x hari ini.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (remainingAdClaims <= 0) {
                                viewModel.showMessage(UiMessage.Error("Batas klaim pakan gratis harian (5x) sudah habis!"))
                            } else {
                                if (activity != null) {
                                    AdHelper.showRewardedAd(
                                        activity = activity,
                                        onRewardEarned = {
                                            viewModel.claimAdRewardFreeFeed(1)
                                        },
                                        onDismiss = {
                                            viewModel.claimAdRewardFreeFeed(1)
                                        }
                                    )
                                } else {
                                    viewModel.claimAdRewardFreeFeed(1)
                                }
                            }
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("watch_ad_free_feed_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = remainingAdClaims > 0,
                        shape = RoundedCornerShape(19.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    if (remainingAdClaims > 0) CoolNeonCyanGradient
                                    else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (remainingAdClaims > 0) "🎬 Klaim Free" else "🔒 Habis",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // FREE VITAMIN AD CARD - Spans all 12 columns
        item(span = { GridItemSpan(12) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, SolidColor(Color(0xFFFFD54F))), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(PurpleGlossGradient)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "💊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vitamin Serum Gratis! (Ads)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Tonton iklan -> Dapatkan Vitamin Booster + Produksi Telur Melonjak!",
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
                                    onRewardEarned = { viewModel.claimAdFreeVitamin(1) },
                                    onDismiss = {}
                                )
                            } else {
                                viewModel.claimAdFreeVitamin(1)
                            }
                        },
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("watch_ad_free_vitamin_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(19.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(GoldPremiumGradient)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎬 Vitamin Gratis",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY TAB SWITCHER - Spans all 12 columns
        item(span = { GridItemSpan(12) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, GlassBorderGradient, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair(0, "🐦 Burung (20)"),
                    Pair(1, "🌾 Pakan (5)"),
                    Pair(2, "💊 Vitamin (3)")
                ).forEach { (index, title) ->
                    val isSelected = selectedCategory == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) EmeraldGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedCategory = index }
                            .testTag("shop_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- CONTENT SECTION 0: BIRDS ---
        if (selectedCategory == 0) {
            itemsIndexed(
                items = GameRepository.BIRD_CONFIGS,
                key = { _, config -> config.id },
                span = { _, _ -> GridItemSpan(3) }
            ) { _, config ->
                val inventory = inventories.find { it.birdId == config.id }
                val countOwned = inventory?.count ?: 0

                val isFreeMaxed = config.cost == 0L && countOwned >= 1
                val canAfford = currentCoins >= config.cost && !isFreeMaxed
                val birdColor = Color(android.graphics.Color.parseColor(config.colorHex))

                val shopItemBgGradient = remember(birdColor, canAfford) {
                    if (canAfford) {
                        Brush.verticalGradient(
                            listOf(birdColor.copy(alpha = 0.25f), Color(0xFF142218).copy(alpha = 0.85f))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF2C3E50).copy(alpha = 0.3f), Color(0xFF1C2833).copy(alpha = 0.9f))
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.46f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.buyBird(config.id) }
                        .border(
                            BorderStroke(
                                width = 1.5.dp,
                                color = if (canAfford) birdColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.15f)
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("shop_item_card_${config.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (canAfford) 4.dp else 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(brush = GoldPremiumGradient, shape = RoundedCornerShape(bottomEnd = 12.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(text = "Lv.${config.id}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }

                        if (countOwned > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(brush = EmeraldGradient, shape = RoundedCornerShape(bottomStart = 12.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(text = "x$countOwned", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(shopItemBgGradient)
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(birdColor.copy(alpha = 0.3f))
                                    .border(2.dp, birdColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🐦", fontSize = 18.sp)
                            }

                            Text(
                                text = config.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🥚 +${config.eggsPer5Min}/5m",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (canAfford) EmeraldGradient else Brush.linearGradient(listOf(Color(0xFF34495E), Color(0xFF2C3E50)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (isFreeMaxed) "🔒 " else if (!canAfford) "🔒 " else "🪙 ", fontSize = 8.sp)
                                    Text(
                                        text = if (isFreeMaxed) "Max (1)" else if (config.cost == 0L) "FREE" else "${config.cost}",
                                        color = if (canAfford) Color.White else Color.White.copy(alpha = 0.4f),
                                        fontSize = 9.sp,
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

        // --- CONTENT SECTION 1: FEED STORE (4 FEEDS - PRECISE 2x2 GRID) ---
        if (selectedCategory == 1) {
            itemsIndexed(
                items = GameRepository.FEED_CONFIGS,
                key = { _, config -> config.id },
                span = { _, _ -> GridItemSpan(6) } // 6 out of 12 columns = 2 items per row (2x2 grid for 4 items)
            ) { _, feed ->
                val stock = when (feed.id) {
                    1 -> progress?.feedStock1 ?: 0
                    2 -> progress?.feedStock2 ?: 0
                    3 -> progress?.feedStock3 ?: 0
                    4 -> progress?.feedStock4 ?: 0
                    else -> 0
                }

                val canAfford = currentCoins >= feed.cost
                val feedColor = Color(android.graphics.Color.parseColor(feed.colorHex))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.15f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.buyFeed(feed.id, 1) }
                        .border(
                            BorderStroke(1.5.dp, if (canAfford) feedColor else Color.White.copy(alpha = 0.15f)),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("buy_feed_card_${feed.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Stock Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    brush = if (stock > 0) EmeraldGradient else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray)),
                                    shape = RoundedCornerShape(bottomStart = 12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Stok: $stock", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(feedColor.copy(alpha = 0.35f), Color(0xFF1E293B))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(feedColor.copy(alpha = 0.35f))
                                    .border(2.dp, feedColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = feed.iconEmoji, fontSize = 22.sp)
                            }

                            Text(
                                text = feed.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "❤️ +${feed.healthGainPercent.toInt()}% Health",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                    color = Color(0xFF38EF7D),
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = { viewModel.buyFeed(feed.id, 1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (canAfford) GoldPremiumGradient else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🪙 ${feed.cost}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- CONTENT SECTION 2: VITAMIN STORE (3 VITAMINS - PRECISE 3-COLUMN GRID) ---
        if (selectedCategory == 2) {
            itemsIndexed(
                items = GameRepository.VITAMIN_CONFIGS,
                key = { _, config -> config.id },
                span = { _, _ -> GridItemSpan(4) } // 4 out of 12 columns = Exactly 1/3 width (3 equal precise grid items)
            ) { _, vit ->
                val stock = when (vit.id) {
                    1 -> progress?.vitaminStock1 ?: 0
                    2 -> progress?.vitaminStock2 ?: 0
                    3 -> progress?.vitaminStock3 ?: 0
                    else -> 0
                }

                val canAfford = currentCoins >= vit.cost
                val vitColor = Color(android.graphics.Color.parseColor(vit.colorHex))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.68f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.buyVitamin(vit.id, 1) }
                        .border(
                            BorderStroke(1.5.dp, if (canAfford) vitColor else Color.White.copy(alpha = 0.15f)),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("buy_vitamin_card_${vit.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Stock Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    brush = if (stock > 0) SunsetGradient else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray)),
                                    shape = RoundedCornerShape(bottomStart = 12.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(text = "Stok: $stock", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(vitColor.copy(alpha = 0.35f), Color(0xFF0F172A))
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(vitColor.copy(alpha = 0.35f))
                                    .border(2.dp, vitColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = vit.iconEmoji, fontSize = 20.sp)
                            }

                            Text(
                                text = vit.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚡ +${vit.boostPercent}% (${vit.durationMinutes}m)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = { viewModel.buyVitamin(vit.id, 1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (canAfford) CoolNeonCyanGradient else Brush.linearGradient(listOf(Color.DarkGray, Color.Gray))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🪙 ${vit.cost}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Native Ads - Spans all 12 columns at the bottom
        item(span = { GridItemSpan(12) }) {
            AdNative()
        }
    }
}
