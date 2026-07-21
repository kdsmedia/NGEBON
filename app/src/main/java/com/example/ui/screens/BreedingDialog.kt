package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.GameRepository
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.HotPinkGradient
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.example.ui.theme.GoldPremiumGradient
import com.example.ui.theme.RoyalMidnightGradient
import com.example.ui.utils.AdHelper
import com.example.ui.viewmodel.GameViewModel

@Composable
fun BreedingDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val birdInventory by viewModel.birdInventory.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()

    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) break
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }

    val ownedBirds = remember(birdInventory) {
        birdInventory.filter { it.count > 0 }.mapNotNull { inv ->
            val config = GameRepository.getConfig(inv.birdId)
            if (config != null) Pair(config, inv.count) else null
        }
    }

    var selectedParent1Id by remember(ownedBirds) { mutableIntStateOf(ownedBirds.firstOrNull()?.first?.id ?: 1) }
    var selectedParent2Id by remember(ownedBirds) { mutableIntStateOf(ownedBirds.getOrNull(1)?.first?.id ?: ownedBirds.firstOrNull()?.first?.id ?: 1) }

    val parent1Config = GameRepository.getConfig(selectedParent1Id)
    val parent2Config = GameRepository.getConfig(selectedParent2Id)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧬", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Kawin Silang Burung",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Hasilkan keturunan burung baru!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_breeding_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (ownedBirds.isEmpty()) {
                    Text(
                        text = "Anda belum memiliki burung untuk dikawinkan. Beli burung terlebih dahulu di Toko!",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    // Induk 1 Selection
                    Text(
                        text = "Pilih Induk Jantan (Parent 1):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38EF7D),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ownedBirds) { (config, count) ->
                            val isSelected = config.id == selectedParent1Id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF38EF7D).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) Color(0xFF38EF7D) else Color.White.copy(alpha = 0.1f)
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedParent1Id = config.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🐦 ", fontSize = 14.sp)
                                    Text(
                                        text = "${config.name} ($count)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF38EF7D) else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Induk 2 Selection
                    Text(
                        text = "Pilih Induk Betina (Parent 2):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ownedBirds) { (config, count) ->
                            val isSelected = config.id == selectedParent2Id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFFFFD54F).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.1f)
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedParent2Id = config.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🕊️ ", fontSize = 14.sp)
                                    Text(
                                        text = "${config.name} ($count)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFFFD54F) else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${parent1Config?.name ?: ""}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38EF7D))
                                Text("❤️", fontSize = 16.sp)
                                Text("${parent2Config?.name ?: ""}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedParent1Id == selectedParent2Id) {
                                    "✨ Perkawinan Spesies Sama:\nMenghasilkan 1 ekor ${parent1Config?.name} baru + 300 Koin Bonus!"
                                } else {
                                    "🌟 Kawin Silang Spesies Berbeda:\n70% Peluang Menetas Burung Lebih Tinggi / 30% Telur Hybrid (+800 Koin)!"
                                },
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // FREE AD BREEDING BUTTON
                    Button(
                        onClick = {
                            if (activity != null) {
                                AdHelper.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        viewModel.crossbreedBirds(selectedParent1Id, selectedParent2Id)
                                        viewModel.addCoins(200L)
                                    },
                                    onDismiss = {}
                                )
                            } else {
                                viewModel.crossbreedBirds(selectedParent1Id, selectedParent2Id)
                                viewModel.addCoins(200L)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("ad_crossbreed_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GoldPremiumGradient)
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬 ", fontSize = 16.sp)
                                Text("Kawin Silang GRATIS + 200 Koin (Ads)", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start Standard Paid Breeding Button
                    val coins = userProgress?.coins ?: 0L
                    val canAfford = coins >= 100L
                    Button(
                        onClick = {
                            viewModel.crossbreedBirds(selectedParent1Id, selectedParent2Id)
                        },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("start_crossbreed_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (canAfford) HotPinkGradient else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)))
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🧬 ", fontSize = 16.sp)
                                Text("Perkawinan Standar (100 Koin)", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
