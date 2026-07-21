package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BirdInventory
import com.example.data.model.DailyMissionEntity
import com.example.data.model.UserProgress
import com.example.data.model.WithdrawLog
import com.example.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiMessage {
    data class Success(val message: String) : UiMessage
    data class Error(val message: String) : UiMessage
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = GameRepository(db.gameDao())

    val userProgress: StateFlow<UserProgress?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val birdInventory: StateFlow<List<BirdInventory>> = repository.birdInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyMissions: StateFlow<List<DailyMissionEntity>> = repository.dailyMissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawLogs: StateFlow<List<WithdrawLog>> = repository.withdrawLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            // Real-time ticker to generate eggs smoothly every second!
            while (true) {
                try {
                    repository.tickEggGeneration()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000L)
            }
        }
    }

    fun showMessage(message: UiMessage) {
        viewModelScope.launch {
            _uiMessage.value = message
            delay(3500L)
            if (_uiMessage.value == message) {
                _uiMessage.value = null
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun harvestAll() {
        viewModelScope.launch {
            repository.harvestAllEggs()
            showMessage(UiMessage.Success("Berhasil memanen telur ke gudang!"))
        }
    }

    fun sellAll() {
        viewModelScope.launch {
            val progress = userProgress.value
            if (progress != null && progress.harvestedEggs > 0L) {
                val eggs = progress.harvestedEggs
                val coins = eggs * 100L
                repository.sellAllEggs()
                showMessage(UiMessage.Success("Berhasil menjual $eggs telur, mendapatkan $coins Koin!"))
            } else {
                showMessage(UiMessage.Error("Tidak ada telur di gudang untuk dijual."))
            }
        }
    }

    fun buyBird(birdId: Int) {
        viewModelScope.launch {
            val config = GameRepository.getConfig(birdId) ?: return@launch
            val progress = userProgress.value ?: return@launch
            val inventories = birdInventory.value
            val inv = inventories.find { it.birdId == birdId }

            if (config.cost == 0L && inv != null && inv.count >= 1) {
                showMessage(UiMessage.Error("Burung gratis (${config.name}) hanya boleh dimiliki 1 ekor dan tidak dapat ditambah!"))
                return@launch
            }

            if (progress.coins < config.cost) {
                showMessage(UiMessage.Error("Koin Anda kurang! Butuh ${config.cost} Koin untuk membeli ${config.name}."))
                return@launch
            }

            val success = repository.buyBird(birdId)
            if (success) {
                showMessage(UiMessage.Success("Sukses membeli/mengaktifkan ${config.name}!"))
            } else {
                showMessage(UiMessage.Error("Gagal membeli burung."))
            }
        }
    }

    fun upgradeBird(birdId: Int) {
        viewModelScope.launch {
            val config = GameRepository.getConfig(birdId) ?: return@launch
            val progress = userProgress.value ?: return@launch
            val inventories = birdInventory.value
            val inv = inventories.find { it.birdId == birdId } ?: return@launch

            if (config.cost == 0L) {
                showMessage(UiMessage.Error("Burung gratis (${config.name}) starter tidak dapat di-upgrade!"))
                return@launch
            }

            if (inv.count <= 0) {
                showMessage(UiMessage.Error("Anda harus memiliki setidaknya 1 ekor ${config.name} sebelum meng-upgrade!"))
                return@launch
            }

            val cost = GameRepository.getUpgradeCost(config.cost, inv.upgradeLevel)
            if (progress.coins < cost) {
                showMessage(UiMessage.Error("Koin Anda kurang! Butuh $cost Koin untuk meng-upgrade ${config.name}."))
                return@launch
            }

            val success = repository.upgradeBird(birdId)
            if (success) {
                showMessage(UiMessage.Success("Sukses meningkatkan level ${config.name} ke Level ${inv.upgradeLevel + 1}!"))
            } else {
                showMessage(UiMessage.Error("Gagal meng-upgrade burung."))
            }
        }
    }

    fun buyFeed(feedId: Int, qty: Int = 1) {
        viewModelScope.launch {
            val (success, msg) = repository.buyFeed(feedId, qty)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun buyVitamin(vitaminId: Int, qty: Int = 1) {
        viewModelScope.launch {
            val (success, msg) = repository.buyVitamin(vitaminId, qty)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun claimAdRewardFreeFeed(feedId: Int = 1) {
        viewModelScope.launch {
            val (success, msg) = repository.claimAdRewardFreeFeed(feedId)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun claimAdEggBooster() {
        viewModelScope.launch {
            val (success, msg) = repository.claimAdEggBooster()
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun claimAdFreeVitamin(vitaminId: Int = 1) {
        viewModelScope.launch {
            val (success, msg) = repository.claimAdFreeVitamin(vitaminId)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun claimAdSuperClean() {
        viewModelScope.launch {
            val (success, msg) = repository.claimAdSuperClean()
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun feedBirdWithFeed(birdId: Int, feedId: Int) {
        viewModelScope.launch {
            val (success, msg) = repository.feedBirdWithFeed(birdId, feedId)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun feedAllBirdsWithBestFeed() {
        viewModelScope.launch {
            val (success, msg) = repository.feedAllBirdsWithBestFeed()
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun giveVitaminToBird(birdId: Int, vitaminId: Int) {
        viewModelScope.launch {
            val (success, msg) = repository.giveVitaminToBird(birdId, vitaminId)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun withdraw(coinsToDeduct: Long, paymentMethod: String, accountNumber: String, ownerName: String) {
        viewModelScope.launch {
            if (ownerName.trim().isEmpty()) {
                showMessage(UiMessage.Error("Nama pemilik tidak boleh kosong!"))
                return@launch
            }

            if (accountNumber.trim().isEmpty()) {
                showMessage(UiMessage.Error("Nomor akun/e-wallet tidak boleh kosong!"))
                return@launch
            }

            if (coinsToDeduct < 100L) {
                showMessage(UiMessage.Error("Minimal penarikan adalah 100 Koin (Rp1)!"))
                return@launch
            }

            val progress = userProgress.value ?: return@launch
            if (progress.coins < coinsToDeduct) {
                showMessage(UiMessage.Error("Koin tidak mencukupi untuk penarikan!"))
                return@launch
            }

            val amountRp = coinsToDeduct / 100L
            val (success, msg) = repository.withdrawCoins(coinsToDeduct, paymentMethod, accountNumber, ownerName, amountRp)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun claim500BonusCoins() {
        viewModelScope.launch {
            val (success, msg) = repository.claim500BonusCoins()
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun warpTime(seconds: Long) {
        viewModelScope.launch {
            repository.simulateTimeElapse(seconds)
            val minutes = seconds / 60
            showMessage(UiMessage.Success("Simulasi waktu berjalan cepat: +$minutes menit!"))
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.resetGame()
            showMessage(UiMessage.Success("Data permainan berhasil direset."))
        }
    }

    fun addCoins(amount: Long) {
        viewModelScope.launch {
            val success = repository.addCoins(amount)
            if (success) {
                showMessage(UiMessage.Success("Selamat! Anda mendapatkan $amount Koin gratis!"))
            }
        }
    }

    fun feedBird(birdId: Int) {
        viewModelScope.launch {
            // Default feed with feedId 1 (or best feed)
            val (success, msg) = repository.feedBirdWithFeed(birdId, 1)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun claimMissionReward(missionId: String) {
        viewModelScope.launch {
            val missions = dailyMissions.value
            val mission = missions.find { it.id == missionId }
            val reward = mission?.rewardCoins ?: 0L
            val success = repository.claimMissionReward(missionId)
            if (success) {
                showMessage(UiMessage.Success("🎉 Selamat! Anda mendapatkan +$reward Koin dari Misi Harian!"))
            } else {
                showMessage(UiMessage.Error("Misi belum selesai atau sudah diklaim."))
            }
        }
    }

    fun cleanCage() {
        viewModelScope.launch {
            val (success, msg) = repository.cleanCage()
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun spinLuckyWheel(isAdSpin: Boolean = false) {
        viewModelScope.launch {
            val (success, msg) = repository.spinLuckyWheel(isAdSpin)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun crossbreedBirds(parent1Id: Int, parent2Id: Int) {
        viewModelScope.launch {
            val (success, msg) = repository.crossbreedBirds(parent1Id, parent2Id)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun addMiniGameReward(coinsEarned: Long) {
        viewModelScope.launch {
            val (success, msg) = repository.addMiniGameReward(coinsEarned)
            if (success) {
                showMessage(UiMessage.Success(msg))
            } else {
                showMessage(UiMessage.Error(msg))
            }
        }
    }

    fun feedAllBirds() {
        viewModelScope.launch {
            feedAllBirdsWithBestFeed()
        }
    }
}
