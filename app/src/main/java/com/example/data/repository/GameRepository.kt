package com.example.data.repository

import com.example.data.database.GameDao
import com.example.data.model.BirdInventory
import com.example.data.model.DailyMissionEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BirdConfig(
    val id: Int,
    val name: String,
    val cost: Long,
    val eggsPer5Min: Int,
    val description: String,
    val colorHex: String
)

data class FeedConfig(
    val id: Int,
    val name: String,
    val cost: Long,
    val healthGainPercent: Double,
    val description: String,
    val iconEmoji: String,
    val colorHex: String
)

data class VitaminConfig(
    val id: Int,
    val name: String,
    val cost: Long,
    val boostPercent: Int,
    val durationMinutes: Int,
    val description: String,
    val iconEmoji: String,
    val colorHex: String
)

class GameRepository(private val gameDao: GameDao) {

    private val mutex = Mutex()

    val userProgress: Flow<UserProgress?> = gameDao.getUserProgressFlow()
    val birdInventory: Flow<List<BirdInventory>> = gameDao.getBirdInventoryFlow()
    val dailyMissions: Flow<List<DailyMissionEntity>> = gameDao.getDailyMissionsFlow()
    val withdrawLogs: Flow<List<WithdrawLog>> = gameDao.getWithdrawLogsFlow().map { logs ->
        logs.map { log ->
            if (log.status == "MENUNGGU" && System.currentTimeMillis() - log.timestamp >= 24 * 60 * 60 * 1000L) {
                log.copy(status = "DIBAYAR")
            } else {
                log
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun updateMissionsDateIfNeededInternal() {
        val today = getTodayDateString()
        val existing = gameDao.getDailyMissionsDirect()
        val existingMap = existing.associateBy { it.id }

        val updatedMissions = DEFAULT_DAILY_MISSIONS.map { template ->
            val current = existingMap[template.id]
            if (current == null || current.lastUpdatedDate != today) {
                template.copy(
                    currentProgress = 0,
                    isClaimed = false,
                    lastUpdatedDate = today
                )
            } else {
                current
            }
        }
        gameDao.insertOrUpdateMissions(updatedMissions)
    }

    private suspend fun incrementMissionProgressInternal(missionId: String, amount: Int = 1) {
        updateMissionsDateIfNeededInternal()
        val missions = gameDao.getDailyMissionsDirect()
        val mission = missions.find { it.id == missionId } ?: return
        if (mission.isClaimed) return

        val newProgress = (mission.currentProgress + amount).coerceAtMost(mission.targetCount)
        if (newProgress != mission.currentProgress) {
            gameDao.insertOrUpdateMission(mission.copy(currentProgress = newProgress))
        }
    }

    suspend fun claimMissionReward(missionId: String): Boolean = mutex.withLock {
        updateMissionsDateIfNeededInternal()
        val missions = gameDao.getDailyMissionsDirect()
        val mission = missions.find { it.id == missionId } ?: return@withLock false

        if (!mission.isClaimed && mission.currentProgress >= mission.targetCount) {
            val progress = gameDao.getUserProgressDirect() ?: return@withLock false
            val updatedProgress = progress.copy(
                coins = progress.coins + mission.rewardCoins,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
            gameDao.insertOrUpdateProgress(updatedProgress)
            gameDao.insertOrUpdateMission(mission.copy(isClaimed = true))
            true
        } else {
            false
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

        val FEED_CONFIGS = listOf(
            FeedConfig(1, "Pakan Bijian Biasa", 10L, 25.0, "Biji-bijian standar. Memulihkan +25% Kesehatan burung.", "🌾", "#8D6E63"),
            FeedConfig(2, "Pakan Pelet Nutrisi", 35L, 50.0, "Pelet nutrisi seimbang. Memulihkan +50% Kesehatan burung.", "🧪", "#42A5F5"),
            FeedConfig(3, "Pakan Serangga Organik", 80L, 75.0, "Serangga segar tinggi protein. Memulihkan +75% Kesehatan burung.", "🦗", "#66BB6A"),
            FeedConfig(4, "Pakan Royal Jelly", 150L, 100.0, "Ekstrak royal jelly alami. Memulihkan Total 100% Kesehatan burung.", "👑", "#FFA726")
        )

        val VITAMIN_CONFIGS = listOf(
            VitaminConfig(1, "Vitamin VitaB Booster", 150L, 30, 60, "Booster ringan. Meningkatkan laju produksi telur +30% selama 60 menit.", "💊", "#26C6DA"),
            VitaminConfig(2, "Vitamin Super Stamina", 400L, 75, 60, "Booster stamina unggulan. Meningkatkan laju produksi telur +75% selama 60 menit.", "⚡", "#FF7043"),
            VitaminConfig(3, "Vitamin Golden Serum", 1000L, 150, 60, "Serum emas paling efektif! Meningkatkan laju bertelur +150% selama 60 menit.", "🔥", "#FFD54F")
        )

        val DEFAULT_DAILY_MISSIONS = listOf(
            DailyMissionEntity(
                id = "harvest_eggs",
                title = "Panen 15 Telur",
                description = "Kumpulkan minimal 15 telur dari burung di kandang",
                iconEmoji = "🥚",
                targetCount = 15,
                rewardCoins = 200L
            ),
            DailyMissionEntity(
                id = "feed_birds",
                title = "Beri Makan Burung",
                description = "Beri makan burung di kandang sebanyak 5 kali",
                iconEmoji = "🌾",
                targetCount = 5,
                rewardCoins = 150L
            ),
            DailyMissionEntity(
                id = "sell_eggs",
                title = "Jual Telur ke Pasar",
                description = "Jual telur hasil panenmu di Pasar Telur 2 kali",
                iconEmoji = "🪙",
                targetCount = 2,
                rewardCoins = 250L
            ),
            DailyMissionEntity(
                id = "buy_item",
                title = "Belanja Pakan / Vitamin",
                description = "Beli pakan atau vitamin di Toko 2 kali",
                iconEmoji = "🛒",
                targetCount = 2,
                rewardCoins = 150L
            ),
            DailyMissionEntity(
                id = "clean_cage",
                title = "Bersihkan Kandang",
                description = "Bersihkan kotoran kandang untuk memanen Pupuk Organik",
                iconEmoji = "🧹",
                targetCount = 1,
                rewardCoins = 200L
            ),
            DailyMissionEntity(
                id = "play_minigame",
                title = "Main Game Tangkap Telur",
                description = "Mainkan mini game Tangkap Telur dan kumpulkan koin",
                iconEmoji = "🎮",
                targetCount = 1,
                rewardCoins = 300L
            ),
            DailyMissionEntity(
                id = "spin_wheel",
                title = "Putar Roda Keberuntungan",
                description = "Putar Lucky Wheel untuk memenangkan hadiah acak",
                iconEmoji = "🎰",
                targetCount = 1,
                rewardCoins = 250L
            ),
            DailyMissionEntity(
                id = "crossbreed",
                title = "Kawin Silang Burung",
                description = "Lakukan kawin silang untuk menetaskan spesies baru",
                iconEmoji = "🧬",
                targetCount = 1,
                rewardCoins = 400L
            ),
            DailyMissionEntity(
                id = "upgrade_bird",
                title = "Tingkatkan Level Burung",
                description = "Upgrade level bertelur burung kesayanganmu",
                iconEmoji = "📈",
                targetCount = 1,
                rewardCoins = 250L
            ),
            DailyMissionEntity(
                id = "use_vitamin",
                title = "Beri Vitamin Burung",
                description = "Berikan vitamin booster untuk mempercepat produksi telur",
                iconEmoji = "⚡",
                targetCount = 1,
                rewardCoins = 200L
            ),
            DailyMissionEntity(
                id = "watch_ad_1",
                title = "Tonton 1 Iklan Video",
                description = "Tonton 1 iklan video untuk mendukung game & koin bonus",
                iconEmoji = "📺",
                targetCount = 1,
                rewardCoins = 350L
            ),
            DailyMissionEntity(
                id = "watch_ad_3",
                title = "Tonton 3 Iklan Video Hadiah",
                description = "Selesaikan tontonan 3 iklan video untuk bonus koin jumbo",
                iconEmoji = "🎬",
                targetCount = 3,
                rewardCoins = 750L
            ),
            DailyMissionEntity(
                id = "watch_ad_5",
                title = "Tonton 5 Iklan Video Super",
                description = "Tonton 5 iklan video untuk klaim koin mega bonus 1.500",
                iconEmoji = "💎",
                targetCount = 5,
                rewardCoins = 1500L
            ),
            DailyMissionEntity(
                id = "watch_ad_boost",
                title = "Speedup Bertelur Iklan",
                description = "Gunakan booster panen instan dari menonton iklan",
                iconEmoji = "⚡",
                targetCount = 1,
                rewardCoins = 500L
            ),
            DailyMissionEntity(
                id = "watch_ad_feed",
                title = "Klaim Item Gratis Iklan",
                description = "Klaim pakan atau vitamin gratis dari tontonan iklan 2 kali",
                iconEmoji = "🎁",
                targetCount = 2,
                rewardCoins = 600L
            )
        )

        fun getConfig(id: Int): BirdConfig? = BIRD_CONFIGS.find { it.id == id }

        fun getUpgradeCost(baseCost: Long, currentLevel: Int): Long {
            val actualBaseCost = if (baseCost <= 0L) 50L else baseCost
            return (actualBaseCost * 0.75 * currentLevel).toLong()
        }

        fun getProductionRate(baseRate: Int, currentLevel: Int): Int {
            return (baseRate * (1.0 + (currentLevel - 1) * 0.5)).toInt()
        }

        fun getFeedStock(progress: UserProgress, feedId: Int): Int {
            return when (feedId) {
                1 -> progress.feedStock1
                2 -> progress.feedStock2
                3 -> progress.feedStock3
                4 -> progress.feedStock4
                5 -> progress.feedStock5
                else -> 0
            }
        }

        fun updateFeedStock(progress: UserProgress, feedId: Int, newAmount: Int): UserProgress {
            val count = newAmount.coerceAtLeast(0)
            return when (feedId) {
                1 -> progress.copy(feedStock1 = count)
                2 -> progress.copy(feedStock2 = count)
                3 -> progress.copy(feedStock3 = count)
                4 -> progress.copy(feedStock4 = count)
                5 -> progress.copy(feedStock5 = count)
                else -> progress
            }
        }

        fun getVitaminStock(progress: UserProgress, vitaminId: Int): Int {
            return when (vitaminId) {
                1 -> progress.vitaminStock1
                2 -> progress.vitaminStock2
                3 -> progress.vitaminStock3
                else -> 0
            }
        }

        fun updateVitaminStock(progress: UserProgress, vitaminId: Int, newAmount: Int): UserProgress {
            val count = newAmount.coerceAtLeast(0)
            return when (vitaminId) {
                1 -> progress.copy(vitaminStock1 = count)
                2 -> progress.copy(vitaminStock2 = count)
                3 -> progress.copy(vitaminStock3 = count)
                else -> progress
            }
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
                lastUpdateTimestamp = System.currentTimeMillis(),
                feedStock1 = 5
            )
            gameDao.insertOrUpdateProgress(initialProgress)
        }

        val currentBirds = gameDao.getBirdInventoryDirect()
        if (currentBirds.isEmpty()) {
            val defaultBirds = BIRD_CONFIGS.map { config ->
                BirdInventory(
                    birdId = config.id,
                    count = if (config.id == 1) 1 else 0,
                    unharvestedEggsFractional = 0.0,
                    health = 100.0
                )
            }
            gameDao.insertOrUpdateBirds(defaultBirds)
        }

        updateMissionsDateIfNeededInternal()
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
                val baseRate = getProductionRate(config.eggsPer5Min, bird.upgradeLevel)
                
                // Vitamin boost rate multiplier
                val speedMultiplier = if (bird.vitaminExpiryTimestamp > now) {
                    val vConfig = VITAMIN_CONFIGS.find { it.id == bird.vitaminBoostLevel }
                    if (vConfig != null) 1.0 + (vConfig.boostPercent.toDouble() / 100.0) else 1.0
                } else {
                    1.0
                }

                val activeRate = baseRate * speedMultiplier
                val eggsPerSecond = activeRate / 300.0
                
                // Health decreases by 12% per hour -> (12.0 / 3600.0) % per second
                val healthDecrease = elapsedSeconds * (12.0 / 3600.0)
                val newHealth = (bird.health - healthDecrease).coerceAtLeast(0.0)
                
                val efficiency = newHealth / 100.0
                val generatedEggs = bird.count * eggsPerSecond * elapsedSeconds * efficiency
                bird.copy(
                    health = newHealth,
                    unharvestedEggsFractional = bird.unharvestedEggsFractional + generatedEggs
                )
            } else {
                bird
            }
        }

        gameDao.insertOrUpdateBirds(updatedBirds)
        gameDao.insertOrUpdateProgress(progress.copy(lastUpdateTimestamp = now))
    }

    suspend fun simulateTimeElapse(seconds: Long) = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock
        val now = System.currentTimeMillis()
        val birds = gameDao.getBirdInventoryDirect()

        val updatedBirds = birds.map { bird ->
            val config = getConfig(bird.birdId)
            if (config != null && bird.count > 0) {
                val baseRate = getProductionRate(config.eggsPer5Min, bird.upgradeLevel)
                val speedMultiplier = if (bird.vitaminExpiryTimestamp > now) {
                    val vConfig = VITAMIN_CONFIGS.find { it.id == bird.vitaminBoostLevel }
                    if (vConfig != null) 1.0 + (vConfig.boostPercent.toDouble() / 100.0) else 1.0
                } else {
                    1.0
                }
                val activeRate = baseRate * speedMultiplier
                val eggsPerSecond = activeRate / 300.0
                
                val healthDecrease = seconds * (12.0 / 3600.0)
                val newHealth = (bird.health - healthDecrease).coerceAtLeast(0.0)
                
                val efficiency = newHealth / 100.0
                val generatedEggs = bird.count * eggsPerSecond * seconds * efficiency
                bird.copy(
                    health = newHealth,
                    unharvestedEggsFractional = bird.unharvestedEggsFractional + generatedEggs
                )
            } else {
                bird
            }
        }

        gameDao.insertOrUpdateBirds(updatedBirds)
        gameDao.shiftPendingWithdrawLogsTime(seconds * 1000L)
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
            val newDirt = (progress.dirtLevel + (totalHarvested.toInt() / 2).coerceAtLeast(1)).coerceAtMost(100)
            gameDao.insertOrUpdateBirds(updatedBirds)
            gameDao.insertOrUpdateProgress(
                progress.copy(
                    harvestedEggs = progress.harvestedEggs + totalHarvested,
                    dirtLevel = newDirt,
                    lastUpdateTimestamp = System.currentTimeMillis()
                )
            )
            incrementMissionProgressInternal("harvest_eggs", totalHarvested.toInt())
        }
    }

    suspend fun sellAllEggs() = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock
        val eggsToSell = progress.harvestedEggs
        if (eggsToSell <= 0) return@withLock

        val coinsEarned = eggsToSell * 100L
        gameDao.insertOrUpdateProgress(
            progress.copy(
                coins = progress.coins + coinsEarned,
                harvestedEggs = 0L,
                lastUpdateTimestamp = System.currentTimeMillis()
            )
        )
        incrementMissionProgressInternal("sell_eggs", 1)
    }

    suspend fun buyBird(birdId: Int): Boolean = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock false
        val config = getConfig(birdId) ?: return@withLock false
        val birds = gameDao.getBirdInventoryDirect()
        val currentBird = birds.find { it.birdId == birdId } ?: return@withLock false

        // Free bird restriction: max 1 owned, cannot re-buy
        if (config.cost == 0L && currentBird.count >= 1) {
            return@withLock false
        }

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

        if (currentBird.count <= 0) return@withLock false

        // Free bird restriction: cannot upgrade
        if (config.cost == 0L) {
            return@withLock false
        }

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
            incrementMissionProgressInternal("upgrade_bird", 1)
            true
        } else {
            false
        }
    }

    suspend fun buyFeed(feedId: Int, qty: Int = 1): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val config = FEED_CONFIGS.find { it.id == feedId } ?: return@withLock Pair(false, "Jenis pakan tidak ditemukan")
        val totalCost = config.cost * qty

        if (progress.coins >= totalCost) {
            val currentStock = getFeedStock(progress, feedId)
            var updated = progress.copy(coins = progress.coins - totalCost)
            updated = updateFeedStock(updated, feedId, currentStock + qty)
            gameDao.insertOrUpdateProgress(updated)
            incrementMissionProgressInternal("buy_item", qty)
            Pair(true, "Berhasil membeli $qty ${config.name}! Stok sekarang: ${currentStock + qty}")
        } else {
            Pair(false, "Koin tidak cukup! Butuh $totalCost Koin untuk membeli $qty ${config.name}.")
        }
    }

    suspend fun buyVitamin(vitaminId: Int, qty: Int = 1): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val config = VITAMIN_CONFIGS.find { it.id == vitaminId } ?: return@withLock Pair(false, "Jenis vitamin tidak ditemukan")
        val totalCost = config.cost * qty

        if (progress.coins >= totalCost) {
            val currentStock = getVitaminStock(progress, vitaminId)
            var updated = progress.copy(coins = progress.coins - totalCost)
            updated = updateVitaminStock(updated, vitaminId, currentStock + qty)
            gameDao.insertOrUpdateProgress(updated)
            incrementMissionProgressInternal("buy_item", qty)
            Pair(true, "Berhasil membeli $qty ${config.name}! Stok sekarang: ${currentStock + qty}")
        } else {
            Pair(false, "Koin tidak cukup! Butuh $totalCost Koin untuk membeli $qty ${config.name}.")
        }
    }

    suspend fun claimAdRewardFreeFeed(feedId: Int = 1): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val currentClaimCount = if (progress.lastAdsClaimDate == todayStr) progress.adsRewardClaimCountToday else 0
        if (currentClaimCount >= 5) {
            return@withLock Pair(false, "Batas pakan gratis harian (5x) sudah tercapai hari ini!")
        }

        val feedConfig = FEED_CONFIGS.find { it.id == feedId } ?: FEED_CONFIGS[0]
        val currentStock = getFeedStock(progress, feedConfig.id)
        val newClaimCount = currentClaimCount + 1

        var updated = progress.copy(
            adsRewardClaimCountToday = newClaimCount,
            lastAdsClaimDate = todayStr
        )
        updated = updateFeedStock(updated, feedConfig.id, currentStock + 1)
        gameDao.insertOrUpdateProgress(updated)
        incrementMissionProgressInternal("claim_ad", 1)
        incrementMissionProgressInternal("watch_ad_1", 1)
        incrementMissionProgressInternal("watch_ad_3", 1)

        val sisaCount = 5 - newClaimCount
        Pair(true, "Selamat! Anda mendapatkan 1x ${feedConfig.name} gratis dari Ads! (Sisa klaim hari ini: $sisaCount/5)")
    }

    suspend fun feedBirdWithFeed(birdId: Int, feedId: Int): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data tidak ditemukan")
        val feedConfig = FEED_CONFIGS.find { it.id == feedId } ?: return@withLock Pair(false, "Jenis pakan tidak ditemukan")
        val currentStock = getFeedStock(progress, feedId)

        if (currentStock <= 0) {
            return@withLock Pair(false, "Stok ${feedConfig.name} Anda habis! Beli pakan di Toko atau klaim via Ads.")
        }

        val birds = gameDao.getBirdInventoryDirect()
        val bird = birds.find { it.birdId == birdId } ?: return@withLock Pair(false, "Burung tidak ditemukan")
        if (bird.count <= 0) return@withLock Pair(false, "Anda belum memiliki burung ini")

        val newHealth = (bird.health + feedConfig.healthGainPercent).coerceAtMost(100.0)
        val updatedBird = bird.copy(health = newHealth)
        val updatedProgress = updateFeedStock(progress, feedId, currentStock - 1)

        gameDao.insertOrUpdateBird(updatedBird)
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("feed_birds", 1)

        val birdConfig = getConfig(birdId)
        Pair(true, "Berhasil memberi ${feedConfig.name} ke ${birdConfig?.name}! Kesehatan +${feedConfig.healthGainPercent.toInt()}% (Stok sisa: ${currentStock - 1})")
    }

    suspend fun feedAllBirdsWithBestFeed(): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Progress tidak ditemukan")
        val birds = gameDao.getBirdInventoryDirect()
        val ownedBirds = birds.filter { it.count > 0 && it.health < 100.0 }

        if (ownedBirds.isEmpty()) {
            return@withLock Pair(false, "Semua burung Anda sudah dalam kondisi sehat 100%!")
        }

        var selectedFeedId = 0
        for (fId in 1..5) {
            if (getFeedStock(progress, fId) > 0) {
                selectedFeedId = fId
                break
            }
        }

        if (selectedFeedId == 0) {
            return@withLock Pair(false, "Stok pakan Anda habis! Silakan beli pakan di Toko atau klaim pakan gratis Ads.")
        }

        val feedConfig = FEED_CONFIGS.find { it.id == selectedFeedId }!!
        var availableStock = getFeedStock(progress, selectedFeedId)
        var fedCount = 0

        val updatedBirds = birds.map { bird ->
            if (bird.count > 0 && bird.health < 100.0 && availableStock > 0) {
                availableStock--
                fedCount++
                bird.copy(health = (bird.health + feedConfig.healthGainPercent).coerceAtMost(100.0))
            } else {
                bird
            }
        }

        val updatedProgress = updateFeedStock(progress, selectedFeedId, availableStock)
        gameDao.insertOrUpdateBirds(updatedBirds)
        gameDao.insertOrUpdateProgress(updatedProgress)
        if (fedCount > 0) {
            incrementMissionProgressInternal("feed_birds", fedCount)
        }

        Pair(true, "Berhasil memberi makan $fedCount burung menggunakan ${feedConfig.name}! (Sisa stok: $availableStock)")
    }

    suspend fun giveVitaminToBird(birdId: Int, vitaminId: Int): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Progress tidak ditemukan")
        val vitConfig = VITAMIN_CONFIGS.find { it.id == vitaminId } ?: return@withLock Pair(false, "Jenis vitamin tidak ditemukan")
        val currentStock = getVitaminStock(progress, vitaminId)

        if (currentStock <= 0) {
            return@withLock Pair(false, "Stok ${vitConfig.name} Anda habis! Beli vitamin di Toko.")
        }

        val birds = gameDao.getBirdInventoryDirect()
        val bird = birds.find { it.birdId == birdId } ?: return@withLock Pair(false, "Burung tidak ditemukan")
        if (bird.count <= 0) return@withLock Pair(false, "Anda belum memiliki burung ini")

        val expiryTime = System.currentTimeMillis() + (vitConfig.durationMinutes * 60 * 1000L)
        val updatedBird = bird.copy(
            vitaminBoostLevel = vitaminId,
            vitaminExpiryTimestamp = expiryTime
        )
        val updatedProgress = updateVitaminStock(progress, vitaminId, currentStock - 1)

        gameDao.insertOrUpdateBird(updatedBird)
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("use_vitamin", 1)

        val birdConfig = getConfig(birdId)
        Pair(true, "Berhasil memberikan ${vitConfig.name} ke ${birdConfig?.name}! Laju bertelur +${vitConfig.boostPercent}% selama ${vitConfig.durationMinutes}m!")
    }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    suspend fun withdrawCoins(coinsToDeduct: Long, paymentMethod: String, accountNumber: String, ownerName: String, amountRp: Long): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val todayStr = getTodayDateString()
        val currentWithdrawCount = if (progress.lastWithdrawDate == todayStr) progress.withdrawCountToday else 0

        if (currentWithdrawCount >= 1) {
            return@withLock Pair(false, "Batas Penarikan Tercapai: Penarikan dibatasi maksimal 1x per hari. Silakan coba lagi besok!")
        }

        if (progress.coins < coinsToDeduct || coinsToDeduct <= 0) {
            return@withLock Pair(false, "Koin Anda tidak cukup untuk melakukan penarikan ini!")
        }

        val updatedProgress = progress.copy(
            coins = progress.coins - coinsToDeduct,
            totalWithdrawnRp = progress.totalWithdrawnRp + amountRp,
            withdrawCountToday = currentWithdrawCount + 1,
            lastWithdrawDate = todayStr,
            lastUpdateTimestamp = System.currentTimeMillis(),
            danaAccountName = if (paymentMethod.equals("DANA", ignoreCase = true)) ownerName.trim() else progress.danaAccountName,
            danaAccountNumber = if (paymentMethod.equals("DANA", ignoreCase = true)) accountNumber.trim() else progress.danaAccountNumber,
            ovoAccountName = if (paymentMethod.equals("OVO", ignoreCase = true)) ownerName.trim() else progress.ovoAccountName,
            ovoAccountNumber = if (paymentMethod.equals("OVO", ignoreCase = true)) accountNumber.trim() else progress.ovoAccountNumber,
            gopayAccountName = if (paymentMethod.equals("GOPAY", ignoreCase = true)) ownerName.trim() else progress.gopayAccountName,
            gopayAccountNumber = if (paymentMethod.equals("GOPAY", ignoreCase = true)) accountNumber.trim() else progress.gopayAccountNumber
        )
        val log = WithdrawLog(
            amountRp = amountRp,
            coinsDeducted = coinsToDeduct,
            paymentMethod = "$paymentMethod ($ownerName)",
            accountNumber = accountNumber,
            status = "MENUNGGU",
            timestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        gameDao.insertWithdrawLog(log)

        GlobalScope.launch {
            sendTelegramNotification(amountRp, paymentMethod, accountNumber, ownerName)
        }

        Pair(true, "Permintaan penarikan Rp $amountRp berhasil dikirim! (Batas harian: 1x/hari)")
    }

    suspend fun saveEWalletData(method: String, ownerName: String, accountNumber: String): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val methodUpper = method.trim().uppercase()
        val updatedProgress = when (methodUpper) {
            "DANA" -> progress.copy(danaAccountName = ownerName.trim(), danaAccountNumber = accountNumber.trim())
            "OVO" -> progress.copy(ovoAccountName = ownerName.trim(), ovoAccountNumber = accountNumber.trim())
            "GOPAY" -> progress.copy(gopayAccountName = ownerName.trim(), gopayAccountNumber = accountNumber.trim())
            else -> progress
        }
        gameDao.insertOrUpdateProgress(updatedProgress)
        Pair(true, "Data E-Wallet $method berhasil disimpan & dikunci!")
    }

    suspend fun claim500BonusCoins(): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val todayStr = getTodayDateString()
        val currentClaimCount = if (progress.lastBonusClaimDate == todayStr) progress.bonusClaimCountToday else 0

        if (currentClaimCount >= 2) {
            return@withLock Pair(false, "Batas Klaim Tercapai: Klaim bonus 500 koin dibatasi maksimal 2x per hari!")
        }

        val newClaimCount = currentClaimCount + 1
        val updatedProgress = progress.copy(
            coins = progress.coins + 500L,
            bonusClaimCountToday = newClaimCount,
            lastBonusClaimDate = todayStr,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("claim_ad", 1)
        incrementMissionProgressInternal("watch_ad_1", 1)
        incrementMissionProgressInternal("watch_ad_3", 1)

        val sisaCount = 2 - newClaimCount
        Pair(true, "Selamat! Anda mendapatkan +500 Koin Bonus! 🎁 (Sisa klaim bonus hari ini: $sisaCount/2)")
    }

    suspend fun addCoins(amount: Long): Boolean = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock false
        val updatedProgress = progress.copy(
            coins = progress.coins + amount,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("claim_ad", 1)
        incrementMissionProgressInternal("watch_ad_1", 1)
        incrementMissionProgressInternal("watch_ad_3", 1)
        true
    }

    private suspend fun sendTelegramNotification(amountRp: Long, paymentMethod: String, accountNumber: String, ownerName: String) {
        withContext(Dispatchers.IO) {
            try {
                val token = "6495136419:AAGQTwqr-8O6aKO0yR34h_IdgZlWI-iQ548"
                val chatId = "6468643791"
                val text = """
                    ====================
                     TARIK SALDO NGEBON
                    ====================
                    Nominal: Rp $amountRp
                    Ewallet: $paymentMethod
                    No. E-Wallet: $accountNumber
                    Nama Pemilik: $ownerName
                    ====================
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

    suspend fun cleanCage(): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        if (progress.dirtLevel <= 0) {
            return@withLock Pair(false, "Kandang sudah sangat bersih!")
        }
        val currentDirt = progress.dirtLevel
        val fertilizerCoins = currentDirt * 15L
        val updatedProgress = progress.copy(
            dirtLevel = 0,
            coins = progress.coins + fertilizerCoins,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("clean_cage", 1)
        Pair(true, "Kandang bersih sempurna! Menjual Pupuk Organik dan mendapatkan +$fertilizerCoins Koin! 🧹✨")
    }

    suspend fun spinLuckyWheel(isAdSpin: Boolean = false): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val today = getTodayDateString()
        if (!isAdSpin && progress.lastSpinDate == today) {
            return@withLock Pair(false, "Anda sudah memutar Roda Keberuntungan gratis hari ini. Tonton video untuk spin lagi!")
        }

        val randomPrizeIndex = (0..5).random()
        var prizeMsg = ""
        var newCoins = progress.coins
        var newRoyalJelly = progress.feedStock4
        var newVitSuper = progress.vitaminStock3

        when (randomPrizeIndex) {
            0 -> {
                newCoins += 300L
                prizeMsg = "Selamat! Anda memenangkan +300 Koin! 🪙"
            }
            1 -> {
                newCoins += 1000L
                prizeMsg = "JACKPOT! Anda memenangkan +1000 Koin! 👑✨"
            }
            2 -> {
                newRoyalJelly += 3
                prizeMsg = "Selamat! Anda mendapatkan 3x Pakan Royal Jelly! 👑"
            }
            3 -> {
                newVitSuper += 1
                prizeMsg = "Selamat! Anda mendapatkan 1x Vitamin Super Speed! 🧪"
            }
            4 -> {
                newCoins += 1000L
                prizeMsg = "Selamat! Panen Telur Emas senilai 1000 Koin! 🥚✨"
            }
            5 -> {
                newCoins += 500L
                prizeMsg = "Selamat! Anda memenangkan +500 Koin Bonus! 🎁"
            }
        }

        val updatedProgress = progress.copy(
            coins = newCoins,
            feedStock4 = newRoyalJelly,
            vitaminStock3 = newVitSuper,
            lastSpinDate = today,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("spin_wheel", 1)
        if (isAdSpin) {
            incrementMissionProgressInternal("claim_ad", 1)
            incrementMissionProgressInternal("watch_ad_1", 1)
            incrementMissionProgressInternal("watch_ad_3", 1)
        }
        Pair(true, prizeMsg)
    }

    suspend fun crossbreedBirds(parent1Id: Int, parent2Id: Int): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val breedCost = 100L
        if (progress.coins < breedCost) {
            return@withLock Pair(false, "Koin tidak cukup untuk biaya kawin silang ($breedCost Koin)")
        }

        val birds = gameDao.getBirdInventoryDirect()
        val parent1 = birds.find { it.birdId == parent1Id }
        val parent2 = birds.find { it.birdId == parent2Id }

        if (parent1 == null || parent1.count <= 0 || parent2 == null || parent2.count <= 0) {
            return@withLock Pair(false, "Anda harus memiliki minimal 1 ekor untuk kedua jenis burung induk!")
        }

        if (parent1Id == parent2Id && parent1.count < 2) {
            return@withLock Pair(false, "Untuk mengawinkan spesies yang sama, Anda membutuhkan minimal 2 ekor burung!")
        }

        val p1Config = getConfig(parent1Id)
        val p2Config = getConfig(parent2Id)

        var updatedProgress = progress.copy(coins = progress.coins - breedCost)

        var resultMessage = ""
        if (parent1Id == parent2Id) {
            val newCount = parent1.count + 1
            gameDao.insertOrUpdateBird(parent1.copy(count = newCount))
            updatedProgress = updatedProgress.copy(coins = updatedProgress.coins + 300L)
            resultMessage = "Berhasil! Perkawinan ${p1Config?.name} menghasilkan 1 ekor ${p1Config?.name} baru + 300 Koin Bonus! 🐣✨"
        } else {
            val targetTier = (maxOf(parent1Id, parent2Id) + 1).coerceAtMost(BIRD_CONFIGS.size)
            val roll = (1..100).random()
            if (roll <= 70) {
                val targetBird = birds.find { it.birdId == targetTier } ?: BirdInventory(birdId = targetTier, count = 0)
                gameDao.insertOrUpdateBird(targetBird.copy(count = targetBird.count + 1))
                val targetConfig = getConfig(targetTier)
                resultMessage = "Luar Biasa! Kawin silang ${p1Config?.name} & ${p2Config?.name} menetas menjadi 1 ekor ${targetConfig?.name}! 🧬🌟"
            } else {
                updatedProgress = updatedProgress.copy(coins = updatedProgress.coins + 800L)
                resultMessage = "Kawin silang ${p1Config?.name} & ${p2Config?.name} berhasil menjual Telur Hybrid seharga +800 Koin! 🧬🪙"
            }
        }

        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("crossbreed", 1)
        Pair(true, resultMessage)
    }

    suspend fun addMiniGameReward(coinsEarned: Long): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val updatedProgress = progress.copy(
            coins = progress.coins + coinsEarned,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("play_minigame", 1)
        Pair(true, "Hebat! Kamu mendapatkan +$coinsEarned Koin dari Mini Game Tangkap Telur! 🎮🥚")
    }

    suspend fun claimAdEggBooster(): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val updatedProgress = progress.copy(
            harvestedEggs = progress.harvestedEggs + 15L,
            coins = progress.coins + 250L,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("claim_ad", 1)
        incrementMissionProgressInternal("watch_ad_1", 1)
        incrementMissionProgressInternal("watch_ad_3", 1)
        incrementMissionProgressInternal("watch_ad_5", 1)
        incrementMissionProgressInternal("watch_ad_boost", 1)
        incrementMissionProgressInternal("harvest_eggs", 15)
        Pair(true, "⚡ Booster Iklan Aktif! +15 Telur langsung masuk ke Gudang + 250 Koin Bonus! 🥚🎁")
    }

    suspend fun claimAdFreeVitamin(vitaminId: Int): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val vitConfig = VITAMIN_CONFIGS.find { it.id == vitaminId } ?: VITAMIN_CONFIGS.first()
        val updatedProgress = when (vitConfig.id) {
            1 -> progress.copy(vitaminStock1 = progress.vitaminStock1 + 1)
            2 -> progress.copy(vitaminStock2 = progress.vitaminStock2 + 1)
            else -> progress.copy(vitaminStock3 = progress.vitaminStock3 + 1)
        }
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("claim_ad", 1)
        incrementMissionProgressInternal("watch_ad_1", 1)
        incrementMissionProgressInternal("watch_ad_3", 1)
        incrementMissionProgressInternal("watch_ad_5", 1)
        incrementMissionProgressInternal("watch_ad_feed", 1)
        Pair(true, "🎁 Selamat! Anda mendapatkan 1x ${vitConfig.name} gratis dari Iklan! 💊")
    }

    suspend fun claimAdSuperClean(): Pair<Boolean, String> = mutex.withLock {
        val progress = gameDao.getUserProgressDirect() ?: return@withLock Pair(false, "Data pemain tidak ditemukan")
        val updatedProgress = progress.copy(
            dirtLevel = 0,
            coins = progress.coins + 300L,
            lastUpdateTimestamp = System.currentTimeMillis()
        )
        gameDao.insertOrUpdateProgress(updatedProgress)
        incrementMissionProgressInternal("claim_ad", 1)
        incrementMissionProgressInternal("watch_ad_1", 1)
        incrementMissionProgressInternal("watch_ad_3", 1)
        incrementMissionProgressInternal("watch_ad_5", 1)
        incrementMissionProgressInternal("clean_cage", 1)
        Pair(true, "🧹 Sapu Super Iklan! Kandang kinclong 0% kotor + 300 Koin Bonus! ✨")
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
            lastUpdateTimestamp = System.currentTimeMillis(),
            feedStock1 = 5
        )
        gameDao.insertOrUpdateProgress(initialProgress)

        val defaultBirds = BIRD_CONFIGS.map { config ->
            BirdInventory(
                birdId = config.id,
                count = if (config.id == 1) 1 else 0,
                unharvestedEggsFractional = 0.0,
                health = 100.0
            )
        }
        gameDao.insertOrUpdateBirds(defaultBirds)
    }
}
