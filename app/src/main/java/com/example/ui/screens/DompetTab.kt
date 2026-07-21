package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import android.app.Activity
import com.example.ui.utils.AdHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.model.WithdrawLog
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DompetTab(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsState()
    val withdrawLogs by viewModel.withdrawLogs.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) break
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }
    val scrollState = rememberScrollState()

    val currentCoins = progress?.coins ?: 0L
    val currentRp = currentCoins / 100.0
    val totalWithdrawn = progress?.totalWithdrawnRp ?: 0L

    val todayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }

    val todayBonusClaims = remember(progress, todayStr) {
        if (progress?.lastBonusClaimDate == todayStr) progress?.bonusClaimCountToday ?: 0 else 0
    }
    val remainingBonusClaims = (2 - todayBonusClaims).coerceAtLeast(0)

    val todayWithdraws = remember(progress, todayStr) {
        if (progress?.lastWithdrawDate == todayStr) progress?.withdrawCountToday ?: 0 else 0
    }
    val canWithdrawToday = todayWithdraws < 1

    // FORM STATES
    val paymentMethods = listOf("DANA", "OVO", "GoPay")
    var selectedMethod by remember { mutableStateOf("DANA") }
    var ownerName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var selectedNominalType by remember { mutableStateOf("500") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WALLET CARDS HEADER (Deep Royal/Space Gradient look)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(RoyalMidnightGradient)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💎 VIP Dompet NGEBON 💎",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🪙 Saldo Koin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currentCoins",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = String.format("≈ Rp%.2f", currentRp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .height(52.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                            .align(Alignment.CenterVertically)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "💸 Total Ditarik",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rp$totalWithdrawn",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38EF7D)
                        )
                        Text(
                            text = "Status: VIP Sukses",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F2FE)
                        )
                    }
                }
            }
        }

        // WITHDRAWAL FORM
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💳 ", fontSize = 20.sp)
                    Text(
                        text = "Form Penarikan Rupiah (WD)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Select Method Chips
                Column {
                    Text(
                        text = "Pilih Metode Pembayaran",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethods.forEach { method ->
                            val isSelected = selectedMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) EmeraldGradient
                                        else Brush.linearGradient(listOf(Color(0xFFE5E8E8), Color(0xFFD5D8DC)))
                                    )
                                    .clickable { selectedMethod = method }
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) GlassBorderGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else Color(0xFF2C3E50)
                                )
                            }
                        }
                    }
                }

                // Nama Pemilik Input
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Nama Pemilik") },
                    placeholder = { Text("Contoh: Budi Santoso") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_owner_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Nomor E-Wallet Input
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Nomor E-Wallet") },
                    placeholder = { Text("Contoh: 08123456789") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_account_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 4 Preset Nominal Selection Buttons
                Column {
                    Text(
                        text = "Pilih Nominal Penarikan",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Row 1: Rp.500, Rp.1000
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("500", "1000").forEach { opt ->
                            val isSelected = selectedNominalType == opt
                            Button(
                                onClick = { selectedNominalType = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("nominal_button_$opt")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isSelected) EmeraldGradient
                                            else Brush.linearGradient(listOf(Color(0xFFEAEDED), Color(0xFFEAEDED)))
                                        )
                                        .border(
                                            BorderStroke(
                                                1.5.dp,
                                                if (isSelected) GlassBorderGradient else Brush.linearGradient(listOf(Color.LightGray, Color.LightGray))
                                            ),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Rp.$opt",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color(0xFF2C3E50)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Row 2: Rp.5000, Rp.10000
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("5000", "10000").forEach { opt ->
                            val isSelected = selectedNominalType == opt
                            Button(
                                onClick = { selectedNominalType = opt },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("nominal_button_$opt")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isSelected) EmeraldGradient
                                            else Brush.linearGradient(listOf(Color(0xFFEAEDED), Color(0xFFEAEDED)))
                                        )
                                        .border(
                                            BorderStroke(
                                                1.5.dp,
                                                if (isSelected) GlassBorderGradient else Brush.linearGradient(listOf(Color.LightGray, Color.LightGray))
                                            ),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Rp.$opt",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color(0xFF2C3E50)
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic conversion display below choice buttons
                    val coinsToDeduct = when (selectedNominalType) {
                        "500" -> 50000L
                        "1000" -> 100000L
                        "5000" -> 500000L
                        "10000" -> 1000000L
                        else -> 50000L
                    }
                    val rpVal = coinsToDeduct / 100L
                    Text(
                        text = "Penarikan sebesar: Rp $rpVal (${java.text.NumberFormat.getInstance(java.util.Locale.GERMAN).format(coinsToDeduct)} Koin)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (!canWithdrawToday) {
                            viewModel.showMessage(com.example.ui.viewmodel.UiMessage.Error("Penarikan dibatasi maksimal 1x per hari! Silakan coba lagi besok."))
                            return@Button
                        }
                        if (ownerName.trim().isEmpty()) {
                            viewModel.showMessage(com.example.ui.viewmodel.UiMessage.Error("Nama pemilik tidak boleh kosong!"))
                            return@Button
                        }
                        if (accountNumber.trim().isEmpty()) {
                            viewModel.showMessage(com.example.ui.viewmodel.UiMessage.Error("Nomor ewallet tidak boleh kosong!"))
                            return@Button
                        }

                        val coinsToDeduct = when (selectedNominalType) {
                            "500" -> 50000L
                            "1000" -> 100000L
                            "5000" -> 500000L
                            "10000" -> 1000000L
                            else -> 50000L
                        }

                        if (activity != null) {
                            AdHelper.showInterstitial(activity) {
                                viewModel.withdraw(
                                    coinsToDeduct = coinsToDeduct,
                                    paymentMethod = selectedMethod,
                                    accountNumber = accountNumber,
                                    ownerName = ownerName
                                )
                            }
                        } else {
                            viewModel.withdraw(
                                coinsToDeduct = coinsToDeduct,
                                paymentMethod = selectedMethod,
                                accountNumber = accountNumber,
                                ownerName = ownerName
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("withdraw_submit_button"),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (canWithdrawToday) GoldPremiumGradient else Brush.linearGradient(listOf(Color(0xFF616161), Color(0xFF424242)))
                            )
                            .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (canWithdrawToday) "💸 " else "🛑 ", fontSize = 20.sp)
                            Text(
                                text = if (canWithdrawToday) "Proses Penarikan Rp (Maks 1x/Hari)" else "Sudah Ditarik Hari Ini (Maks 1x/Hari)",
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }


        // BONUS FREE COINS BUTTON ONLY (NO CARD BLOCK)
        Button(
            onClick = {
                if (remainingBonusClaims <= 0) {
                    viewModel.showMessage(com.example.ui.viewmodel.UiMessage.Error("Klaim bonus 500 koin sudah habis hari ini (Maksimal 2x/hari)!"))
                    return@Button
                }
                if (activity != null) {
                    AdHelper.showRewardedAd(
                        activity = activity,
                        onRewardEarned = {
                            viewModel.claim500BonusCoins()
                        },
                        onDismiss = {}
                    )
                } else {
                    viewModel.claim500BonusCoins()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reward_ad_button")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (remainingBonusClaims > 0) HotPinkGradient else Brush.linearGradient(listOf(Color(0xFF616161), Color(0xFF424242)))
                    )
                    .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁 ", fontSize = 18.sp)
                    Text(
                        text = if (remainingBonusClaims > 0) "Klaim Bonus (+500 Koin) - Sisa $remainingBonusClaims/2" else "Klaim Bonus 500 Koin Habis Hari Ini (0/2)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // RIWAYAT PENARIKAN (WITHDRAWAL HISTORY BLOCK AT BOTTOM)
        WithdrawHistoryBlock(logs = withdrawLogs)
    }
}

@Composable
fun WithdrawHistoryBlock(
    logs: List<WithdrawLog>
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, GlassBorderGradient), RoundedCornerShape(20.dp))
            .testTag("withdraw_history_block"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF122316)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00F2FE).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF00F2FE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📜", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Riwayat Penarikan Rupiah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Catatan transaksi pencairan koin telur",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "${logs.size} WD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00F2FE),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 38.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Belum Ada Riwayat Penarikan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Gunakan form penarikan di atas untuk menukar koin menjadi Rupiah!",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    logs.forEach { log ->
                        val dateString = remember(log.timestamp) {
                            sdf.format(Date(log.timestamp))
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    RoundedCornerShape(14.dp)
                                )
                                .testTag("withdraw_log_card_${log.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B2C1E)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38EF7D).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFF38EF7D), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💸", fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "WD ke ${log.paymentMethod}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(EmeraldGradient)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = log.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Akun: ${log.accountNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )

                                    Text(
                                        text = dateString,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFFEAEA))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "-${log.coinsDeducted} Koin",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFF4757)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE8FDF0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Terima: Rp${log.amountRp}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF2ED573)
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
    }
}
