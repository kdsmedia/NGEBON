package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.CoolNeonCyanGradient
import com.example.ui.theme.EmeraldGradient
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GoldPremiumGradient
import com.example.ui.theme.HotPinkGradient
import com.example.ui.utils.AdHelper
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

data class FallingEgg(
    val id: Long,
    var xPercent: Float, // 0.1f to 0.9f
    var yPercent: Float = 0f,
    val type: EggType
)

enum class EggType(val emoji: String, val points: Long) {
    NORMAL("🥚", 10L),
    GOLDEN("🌟", 50L),
    BOMB("💣", -20L)
}

@Composable
fun EggCatcherDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) break
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }

    var isGameRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(20) }
    var score by remember { mutableLongStateOf(0L) }
    var basketX by remember { mutableFloatStateOf(0.5f) } // 0.0f to 1.0f

    val fallingEggs = remember { mutableStateListOf<FallingEgg>() }

    // Game Loop
    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            timeLeft = 20
            score = 0L
            fallingEggs.clear()
            var nextId = 0L

            while (timeLeft > 0 && isGameRunning) {
                delay(1000L)
                timeLeft -= 1

                // Spawn new falling egg randomly
                if (Random.nextFloat() > 0.3f) {
                    val randType = when {
                        Random.nextFloat() < 0.2f -> EggType.GOLDEN
                        Random.nextFloat() < 0.25f -> EggType.BOMB
                        else -> EggType.NORMAL
                    }
                    fallingEggs.add(
                        FallingEgg(
                            id = nextId++,
                            xPercent = 0.1f + Random.nextFloat() * 0.8f,
                            yPercent = 0f,
                            type = randType
                        )
                    )
                }
            }

            if (isGameRunning) {
                isGameRunning = false
                isGameOver = true
            }
        }
    }

    // Animation Ticker for falling speed and collision detection
    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            while (isGameRunning) {
                delay(50L) // 20 FPS motion
                val iterator = fallingEggs.iterator()
                while (iterator.hasNext()) {
                    val egg = iterator.next()
                    egg.yPercent += 0.05f

                    // Collision check with basket at bottom (y >= 0.85f)
                    if (egg.yPercent >= 0.85f && egg.yPercent <= 0.95f) {
                        if (kotlin.math.abs(egg.xPercent - basketX) <= 0.18f) {
                            score = (score + egg.type.points).coerceAtLeast(0L)
                            iterator.remove()
                            continue
                        }
                    }

                    // Remove eggs off screen
                    if (egg.yPercent > 1.0f) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tangkap Telur Emas!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_egg_catcher_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                if (!isGameRunning && !isGameOver) {
                    // Start Screen
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("🥚 🌟 💣", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cara Bermain:",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Geser keranjang ke kiri & kanan untuk menangkap telur dalam 20 detik!\n\n🥚 Telur Biasa: +10 Koin\n🌟 Telur Emas: +50 Koin\n💣 Bom: -20 Koin",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { isGameRunning = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("start_egg_catcher_game")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(EmeraldGradient)
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎮 Mulai Bermain (20 Detik)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                } else if (isGameOver) {
                    // Game Over Screen
                    Spacer(modifier = Modifier.height(30.dp))
                    Text("🎉 GAMEOVER!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Total Koin Didapatkan:",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "+$score Koin 🪙",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38EF7D)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // 2x Double Coins Ad Button
                    Button(
                        onClick = {
                            if (activity != null) {
                                AdHelper.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        viewModel.addMiniGameReward(score * 2)
                                        onDismiss()
                                    },
                                    onDismiss = {}
                                )
                            } else {
                                viewModel.addMiniGameReward(score * 2)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("double_egg_catcher_reward_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GoldPremiumGradient)
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬 Tonton Iklan -> Dobel 2x Koin (+${score * 2})", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.addMiniGameReward(score)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("claim_egg_catcher_reward")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155))))
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎁 Klaim Normal (+$score Koin)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    // Playing Screen (Game Field)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳ $timeLeft dt", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                        Text("🪙 Skor: $score", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF38EF7D))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Playfield Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF020617))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = basketX + (dragAmount.x / size.width)
                                    basketX = newX.coerceIn(0.1f, 0.9f)
                                }
                            }
                    ) {
                        // Falling items
                        fallingEggs.forEach { egg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = egg.type.emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .align(
                                            androidx.compose.ui.BiasAlignment(
                                                horizontalBias = (egg.xPercent * 2f) - 1.0f,
                                                verticalBias = (egg.yPercent * 2f) - 1.0f
                                            )
                                        )
                                )
                            }
                        }

                        // Basket at bottom
                        Box(
                            modifier = Modifier
                                .align(
                                    androidx.compose.ui.BiasAlignment(
                                        horizontalBias = (basketX * 2f) - 1.0f,
                                        verticalBias = 0.92f
                                    )
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF8B5CF6))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("🧺 Keranjang", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Geser jari ke kiri / kanan untuk menggerakkan keranjang!", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
