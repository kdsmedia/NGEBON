package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyMissionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMissionsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val missions by viewModel.dailyMissions.collectAsState()
    val completedCount = remember(missions) { missions.count { it.isClaimed } }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(BorderStroke(2.dp, GoldPremiumGradient), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1B2C1E), // Rich dark gaming modal background
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 MISI HARIAN (12 Misi)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selesaikan tugas harian ($completedCount/${missions.size} Selesai) untuk koin ekstra!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(missions, key = { it.id }) { mission ->
                    DailyMissionCard(
                        mission = mission,
                        onClaim = { viewModel.claimMissionReward(mission.id) },
                        onWatchAd = {
                            if (activity != null) {
                                com.example.ui.utils.AdHelper.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        viewModel.addCoins(300L)
                                    },
                                    onDismiss = {}
                                )
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38EF7D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("close_daily_missions_dialog")
            ) {
                Text(
                    text = "Tutup",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun DailyMissionCard(
    mission: DailyMissionEntity,
    onClaim: () -> Unit,
    onWatchAd: () -> Unit = {}
) {
    val isCompleted = mission.currentProgress >= mission.targetCount
    val progressFraction = (mission.currentProgress.toFloat() / mission.targetCount.toFloat()).coerceIn(0f, 1f)
    val isAdMission = mission.id.startsWith("watch_ad") || mission.id == "claim_ad"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    if (mission.isClaimed) Color(0xFF38EF7D).copy(alpha = 0.5f)
                    else if (isCompleted) Color(0xFFFFD54F)
                    else Color.White.copy(alpha = 0.15f)
                ),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF101D13),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (mission.isClaimed) Color(0xFF38EF7D).copy(alpha = 0.2f)
                            else if (isCompleted) Color(0xFFFFD54F).copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mission.iconEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mission Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mission.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD54F).copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, Color(0xFFFFD54F))
                        ) {
                            Text(
                                text = "🪙 +${mission.rewardCoins}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = mission.description,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar & Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progres: ${mission.currentProgress}/${mission.targetCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF38EF7D) else Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF38EF7D) else Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (mission.isClaimed) Color(0xFF38EF7D) else if (isCompleted) Color(0xFFFFD54F) else Color(0xFF00F2FE),
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Action Button
                if (mission.isClaimed) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF38EF7D).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF38EF7D))
                    ) {
                        Text(
                            text = "✓ Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38EF7D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else if (isCompleted) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("claim_mission_${mission.id}")
                    ) {
                        Text(
                            text = "KLAIM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                } else if (isAdMission) {
                    Button(
                        onClick = onWatchAd,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF1744)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("watch_ad_mission_${mission.id}")
                    ) {
                        Text(
                            text = "🎬 Nonton",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "Belum",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
