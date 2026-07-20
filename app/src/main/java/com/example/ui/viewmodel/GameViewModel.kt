package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BirdInventory
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
            delay(3000L)
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

    fun withdraw(coinsToDeduct: Long, paymentMethod: String, accountNumber: String) {
        viewModelScope.launch {
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

            // Conversion: 100 Coins = Rp1
            val amountRp = coinsToDeduct / 100L
            val success = repository.withdrawCoins(coinsToDeduct, paymentMethod, accountNumber, amountRp)
            if (success) {
                showMessage(UiMessage.Success("Penarikan Rp$amountRp ke $paymentMethod berhasil diproses!"))
            } else {
                showMessage(UiMessage.Error("Gagal memproses penarikan."))
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
}
