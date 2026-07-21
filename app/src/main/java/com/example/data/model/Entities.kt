package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val coins: Long = 0,
    val harvestedEggs: Long = 0,
    val totalWithdrawnRp: Long = 0,
    val lastUpdateTimestamp: Long = System.currentTimeMillis(),
    val feedStock1: Int = 5,
    val feedStock2: Int = 0,
    val feedStock3: Int = 0,
    val feedStock4: Int = 0,
    val feedStock5: Int = 0,
    val vitaminStock1: Int = 0,
    val vitaminStock2: Int = 0,
    val vitaminStock3: Int = 0,
    val adsRewardClaimCountToday: Int = 0,
    val lastAdsClaimDate: String = "",
    val lastSpinDate: String = "",
    val dirtLevel: Int = 25,
    val bonusClaimCountToday: Int = 0,
    val lastBonusClaimDate: String = "",
    val withdrawCountToday: Int = 0,
    val lastWithdrawDate: String = "",
    val danaAccountName: String = "",
    val danaAccountNumber: String = "",
    val ovoAccountName: String = "",
    val ovoAccountNumber: String = "",
    val gopayAccountName: String = "",
    val gopayAccountNumber: String = ""
)

@Entity(tableName = "bird_inventory")
data class BirdInventory(
    @PrimaryKey val birdId: Int,
    val count: Int = 0,
    val unharvestedEggsFractional: Double = 0.0,
    val upgradeLevel: Int = 1,
    val health: Double = 100.0,
    val vitaminBoostLevel: Int = 0,
    val vitaminExpiryTimestamp: Long = 0L
)

@Entity(tableName = "withdraw_log")
data class WithdrawLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountRp: Long,
    val coinsDeducted: Long,
    val paymentMethod: String,
    val accountNumber: String,
    val status: String = "SUKSES",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_mission")
data class DailyMissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val targetCount: Int,
    val currentProgress: Int = 0,
    val rewardCoins: Long,
    val isClaimed: Boolean = false,
    val lastUpdatedDate: String = ""
)
