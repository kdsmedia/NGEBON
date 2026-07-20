package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val coins: Long = 0,
    val harvestedEggs: Long = 0,
    val totalWithdrawnRp: Long = 0,
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bird_inventory")
data class BirdInventory(
    @PrimaryKey val birdId: Int,
    val count: Int = 0,
    val unharvestedEggsFractional: Double = 0.0,
    val upgradeLevel: Int = 1
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
