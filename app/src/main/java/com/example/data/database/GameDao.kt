package com.example.data.database

import androidx.room.*
import com.example.data.model.BirdInventory
import com.example.data.model.UserProgress
import com.example.data.model.WithdrawLog
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)

    @Query("SELECT * FROM bird_inventory ORDER BY birdId ASC")
    fun getBirdInventoryFlow(): Flow<List<BirdInventory>>

    @Query("SELECT * FROM bird_inventory ORDER BY birdId ASC")
    suspend fun getBirdInventoryDirect(): List<BirdInventory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBird(bird: BirdInventory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBirds(birds: List<BirdInventory>)

    @Query("SELECT * FROM withdraw_log ORDER BY timestamp DESC")
    fun getWithdrawLogsFlow(): Flow<List<WithdrawLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawLog(log: WithdrawLog)

    @Query("UPDATE withdraw_log SET timestamp = timestamp - :offsetMillis WHERE status = 'MENUNGGU'")
    suspend fun shiftPendingWithdrawLogsTime(offsetMillis: Long)

    @Query("DELETE FROM user_progress")
    suspend fun clearUserProgress()

    @Query("DELETE FROM bird_inventory")
    suspend fun clearBirdInventory()

    @Query("DELETE FROM withdraw_log")
    suspend fun clearWithdrawLogs()
}
