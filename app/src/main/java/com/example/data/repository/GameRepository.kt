package com.example.data.repository

import com.example.data.database.GameDao
import com.example.data.model.BirdInventory
import com.example.data.model.UserProgress
import com.example.data.model.WithdrawLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BirdConfig(
    val id: Int,
    val name: String,
    val cost: Long,
    val eggsPer5Min: Int,
    val description: String,
    val colorHex: String
)

class GameRepository(private val gameDao: GameDao) {

    private val mutex = Mutex()

    val userProgress: Flow<UserProgress?> = gameDao.getUserProgressFlow()
    val birdInventory: Flow<List<BirdInventory>> = gameDao.getBirdInventoryFlow()
    val withdrawLogs: Flow<List<WithdrawLog>> = gameDao.getWithdrawLogsFlow().map { logs ->
        logs.map { log ->
            if (log.status == "MENUNGGU" && System.currentTimeMillis() - log.timestamp >= 24 * 60 * 60 * 1000L) {
                log.copy(status = "DIBAYAR")
            } else {
                log
            }
        }
    }

    companion object {
        val BIRD_CONFIGS = listOf(
            BirdConfig(1, "Burung Pipit", 0L, 1, "Burung kecil pemakan biji-bijian, sangat lincah.", "#FF8F00"),
            BirdConfig(2, "Burung Gereja", 100L, 3, "Sering berkumpul di atap, menghasilkan telur dengan cepat.", "#A1887F"),
            BirdConfig(3, "Burung Kutilang", 300L, 5, "Suaranya merdu, sangat aktif berkicau di taman.", "#4DB6AC"),
            BirdConfig(4, "Burung Merpati", 700L, 7, "Burung lambang kesetiaan, terbang dengan tenang.", "#90A4AE"),
            BirdConfig(5, "Burung Parkit", 1500L, 10, "Bulu hijau-kuning cerah, ramah dan suka berkelompok.", "#81C784"),
            BirdConfig(6, "Burung Kenari", 3000L, 13, "Terkenal dengan suaranya yang nyaring dan warna kuning indahnya.", "#FDD835"),
            BirdConfig(7, "Burung Lovebird", 5000L, 16, "Burung cinta dengan kombinasi warna bulu eksotis.", "#E57373"),
            BirdConfig(8, "Burung Jalak Suren", 8000L, 20, "Pandai meniru suara, bulu hitam putih gagah.", "#BA68C8"),
            BirdConfig(9, "Burung Murai Batu", 12000L, 24, "Ekor panjang menawan, juara kicau kelas premium.", "#E0F2F1"),
            BirdConfig(10, "Burung Kakaktua", 18000L, 28, "Cerdas, memiliki jambul indah, bisa meniru kata-kata.", "#ECEFF1"),
            BirdConfig(11, "Burung Beo", 25000L, 33, "Bicara dengan fasih, bulu hitam berkilau keunguan.", "#303F9F"),
            BirdConfig(12, "Burung Kepodang", 35000L, 38, "Burung pesolek Jawa dengan warna kuning keemasan mewah.", "#FFB300"),
            BirdConfig(13, "Burung Enggang", 50000L, 44, "Paruh besar bermahkota, lambang kesucian suku Dayak.", "#E64A19"),
            BirdConfig(14, "Burung Cendrawasih", 70000L, 50, "Burung surga dari Papua, bulu ekornya luar biasa indah.", "#D81B60"),
            BirdConfig(15, "Burung Elang Bondol", 95000L, 57, "Maskot Jakarta, tangguh terbang tinggi di pesisir pantai.", "#795548"),
            BirdConfig(16, "Burung Merak", 125000L, 65, "Kipas ekor megah berwarna-warni, sangat anggun.", "#00838F"),
            BirdConfig(17, "Burung Kasuari", 160000L, 74, "Burung purba berukuran besar, kaki sangat kuat dan kokoh.", "#004D40"),
            BirdConfig(18, "Burung Rajawali", 200000L, 84, "Raja angkasa dengan cakar tajam dan pandangan super tajam.", "#37474F"),
            BirdConfig(19, "Burung Unta", 250000L, 95, "Burung pelari tercepat di dunia, tubuh sangat besar.", "#263238"),
            BirdConfig(20, "Burung Garuda", 300000L, 107, "Burung legendaris lambang kejayaan nusantara.", "#FFD700")
        )

        fun getConfig(id: Int): BirdConfig? = BIRD_CONFIGS.find { it.id == id }

        fun getUpgradeCost(baseCost: Long, currentLevel: Int): Long {
            val actualBaseCost = if (baseCost <= 0L) 50L else baseCost
            return (actualBaseCost * 0.75 * currentLevel).toLong()
        }

        fun getProductionRate(baseRate: Int, currentLevel: Int): Int {
            return (baseRate * (1.0 + (currentLevel - 1) * 0.5)).toInt()
        }
    }

    suspend fun ensureInitialized() = mutex.withLock {
        val currentProgress = gameDao.getUserProgressDirect()
        if (currentProgress == null) {
            val initialProgress = UserProgress(
                id = 1,
                coins = 0L,
                harvestedEggs = 0L,
                totalWithdrawnRp = 0L,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
            gameDao.insertOrUpdateProgress(initialProgress)
        }

        val currentBirds = gameDao.getBirdInventoryDirect()
        if (currentBirds.isEmpty()) {
            val defaultBirds = BIRD_CONFIGS.map { config ->
                BirdInventory(
                    birdId = config.id,
                    count = if (config.id == 1) 1 else 0,
                    unharvestedEggsFractional = 0.0
                )
            }
            gameDao.insertOrUpdateBirds(defaultBirds)
        }
    }

    suspend fun tickEggGeneration() = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - progress.lastUpdateTimestamp) / 1000L
        if (elapsedSeconds <= 0) return@withLock

        val birds = gameDao.getBirdInventoryDirect()
        val updatedBirds = birds.map { bird ->
            val config = getConfig(bird.birdId)
            if (config != null && bird.count > 0) {
                val activeRate = getProductionRate(config.eggsPer5Min, bird.upgradeLevel)
                val eggsPerSecond = activeRate.toDouble() / 300.0
                val generatedEggs = bird.count * eggsPerSecond * elapsedSeconds
                bird.copy(unharvestedEggsFractional = bird.unharvestedEggsFractional + generatedEggs)
            } else {
                bird
            }
        }

        gameDao.insertOrUpdateBirds(updatedBirds)
        gameDao.insertOrUpdateProgress(progress.copy(lastUpdateTimestamp = now))
    }

    suspend fun simulateTimeElapse(seconds: Long) = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock
        val birds = gameDao.getBirdInventoryDirect()

        val updatedBirds = birds.map { bird ->
            val config = getConfig(bird.birdId)
            if (config != null && bird.count > 0) {
                val activeRate = getProductionRate(config.eggsPer5Min, bird.upgradeLevel)
                val eggsPerSecond = activeRate.toDouble() / 300.0
                val generatedEggs = bird.count * eggsPerSecond * seconds
                bird.copy(unharvestedEggsFractional = bird.unharvestedEggsFractional + generatedEggs)
            } else {
                bird
            }
        }

        gameDao.insertOrUpdateBirds(updatedBirds)
        // Shift pending withdraw logs time back to simulate passage of time
        gameDao.shiftPendingWithdrawLogsTime(seconds * 1000L)
        // Move current tick anchor slightly back to simulate passage of time
        gameDao.insertOrUpdateProgress(progress.copy(lastUpdateTimestamp = progress.lastUpdateTimestamp))
    }

    suspend fun harvestAllEggs() = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock
        val birds = gameDao.getBirdInventoryDirect()

        var totalHarvested = 0L
        val updatedBirds = birds.map { bird ->
            val harvestedFromThis = bird.unharvestedEggsFractional.toLong()
            totalHarvested += harvestedFromThis
            bird.copy(unharvestedEggsFractional = bird.unharvestedEggsFractional - harvestedFromThis)
        }

        if (totalHarvested > 0) {
            gameDao.insertOrUpdateBirds(updatedBirds)
            gameDao.insertOrUpdateProgress(
                progress.copy(
                    harvestedEggs = progress.harvestedEggs + totalHarvested,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sellAllEggs() = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock
        val eggsToSell = progress.harvestedEggs
        if (eggsToSell <= 0) return@withLock

        // 1 Egg = 100 Coins
        val coinsEarned = eggsToSell * 100L
        gameDao.insertOrUpdateProgress(
            progress.copy(
                coins = progress.coins + coinsEarned,
                harvestedEggs = 0L,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun buyBird(birdId: Int): Boolean = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock false
        val config = getConfig(birdId) ?: return@withLock false
        val birds = gameDao.getBirdInventoryDirect()
        val currentBird = birds.find { it.birdId == birdId } ?: return@withLock false

        if (progress.coins >= config.cost) {
            val updatedProgress = progress.copy(
                coins = progress.coins - config.cost,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
            val updatedBird = currentBird.copy(
                count = currentBird.count + 1
            )
            gameDao.insertOrUpdateProgress(updatedProgress)
            gameDao.insertOrUpdateBird(updatedBird)
            true
        } else {
            false
        }
    }

    suspend fun upgradeBird(birdId: Int): Boolean = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock false
        val config = getConfig(birdId) ?: return@withLock false
        val birds = gameDao.getBirdInventoryDirect()
        val currentBird = birds.find { it.birdId == birdId } ?: return@withLock false

        if (currentBird.count <= 0) return@withLock false // Must own at least one to upgrade

        val cost = getUpgradeCost(config.cost, currentBird.upgradeLevel)
        if (progress.coins >= cost) {
            val updatedProgress = progress.copy(
                coins = progress.coins - cost,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
            val updatedBird = currentBird.copy(
                upgradeLevel = currentBird.upgradeLevel + 1
            )
            gameDao.insertOrUpdateProgress(updatedProgress)
            gameDao.insertOrUpdateBird(updatedBird)
            true
        } else {
            false
        }
    }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    suspend fun withdrawCoins(coinsToDeduct: Long, paymentMethod: String, accountNumber: String, amountRp: Long): Boolean = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock false
        if (progress.coins >= coinsToDeduct && coinsToDeduct > 0) {
            val updatedProgress = progress.copy(
                coins = progress.coins - coinsToDeduct,
                totalWithdrawnRp = progress.totalWithdrawnRp + amountRp,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
            val log = WithdrawLog(
                amountRp = amountRp,
                coinsDeducted = coinsToDeduct,
                paymentMethod = paymentMethod,
                accountNumber = accountNumber,
                status = "MENUNGGU",
                timestamp = System.currentTimeMillis()
            )
            gameDao.insertOrUpdateProgress(updatedProgress)
            gameDao.insertWithdrawLog(log)

            // Send Telegram message asynchronously so we don't block the database lock!
            GlobalScope.launch {
                sendTelegramNotification(amountRp, paymentMethod)
            }

            true
        } else {
            false
        }
    }

    private suspend fun sendTelegramNotification(amountRp: Long, paymentMethod: String) {
        withContext(Dispatchers.IO) {
            try {
                val token = "6495136419:AAGQTwqr-8O6aKO0yR34h_IdgZlWI-iQ548"
                val chatId = "6468643791"
                val text = """
                    TARIK SALDO NGEBON
                    Nominal: Rp $amountRp
                    Ewallet: $paymentMethod
                """.trimIndent()

                val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                val urlString = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&text=$encodedText"

                val url = java.net.URL(urlString)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                android.util.Log.d("TelegramNotification", "Telegram response: $responseCode")
                connection.disconnect()
            } catch (e: Exception) {
                android.util.Log.e("TelegramNotification", "Error sending Telegram notification: ${e.message}", e)
            }
        }
    }

    suspend fun resetGame() = mutex.withLock {
        gameDao.clearUserProgress()
        gameDao.clearBirdInventory()
        gameDao.clearWithdrawLogs()

        val initialProgress = UserProgress(
            id = 1,
            coins = 0L,
            harvestedEggs = 0L,
            totalWithdrawnRp = 0L,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(initialProgress)

        val defaultBirds = BIRD_CONFIGS.map { config ->
            BirdInventory(
                birdId = config.id,
                count = if (config.id == 1) 1 else 0,
                unharvestedEggsFractional = 0.0
            )
        }
        gameDao.insertOrUpdateBirds(defaultBirds)
    }
}
