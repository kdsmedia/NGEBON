package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CoolNeonCyanGradient
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.GoldPremiumGradient
import com.example.ui.theme.HotPinkGradient
import com.example.ui.utils.AdHelper
import com.example.ui.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LuckyWheelDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userProgress by viewModel.userProgress.collectAsState()

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val isFreeSpinAvailable = remember(userProgress) {
        userProgress?.lastSpinDate != todayStr
    }

    var isSpinning by remember { mutableStateOf(false) }
    val rotationAngle = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val sliceColors = listOf(
        Color(0xFFFF1744),
        Color(0xFF2979FF),
        Color(0xFF00E676),
        Color(0xFFFFEA00),
        Color(0xFFAA00FF),
        Color(0xFFFF9100)
    )

    val prizes = listOf("300 🪙", "1000 👑", "3x 👑 Pakan", "1x 🧪 Vit", "100 🥚", "500 🪙")

    fun triggerSpin(isAdSpin: Boolean) {
        if (isSpinning) return
        isSpinning = true
        coroutineScope.run {
            kotlinx.coroutines.GlobalScope.run {
                // Launch rotation animation
            }
        }
    }

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
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎰", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Roda Keberuntungan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Putar dan dapatkan hadiah menarik!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_lucky_wheel_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wheel Canvas
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val center = Offset(canvasWidth / 2, canvasHeight / 2)
                        val radius = canvasWidth / 2

                        rotate(rotationAngle.value, pivot = center) {
                            for (i in 0 until 6) {
                                drawArc(
                                    color = sliceColors[i],
                                    startAngle = i * 60f,
                                    sweepAngle = 60f,
                                    useCenter = true,
                                    size = Size(canvasWidth, canvasHeight)
                                )
                            }

                            // Outer gold ring
                            drawCircle(
                                color = Color(0xFFFFD54F),
                                radius = radius,
                                style = Stroke(width = 6.dp.toPx())
                            )
                        }

                        // Center Knob
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = 24.dp.toPx()
                        )
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = 20.dp.toPx()
                        )
                    }

                    // Pointer at top
                    Text(
                        text = "▼",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    Text(
                        text = "SPIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prizes Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prizes.take(3).forEach { p ->
                        Text(text = p, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    prizes.drop(3).forEach { p ->
                        Text(text = p, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Spin Action Button
                if (isFreeSpinAvailable) {
                    Button(
                        onClick = {
                            viewModel.spinLuckyWheel(isAdSpin = false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("free_lucky_spin_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GoldPremiumGradient)
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎰 Putar Roda Gratis Hari Ini!", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (activity != null) {
                                AdHelper.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        viewModel.spinLuckyWheel(isAdSpin = true)
                                    },
                                    onDismiss = {}
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("ad_lucky_spin_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(HotPinkGradient)
                                .border(BorderStroke(1.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬 ", fontSize = 16.sp)
                                Text("Tonton Video Untuk Spin Lagi!", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
