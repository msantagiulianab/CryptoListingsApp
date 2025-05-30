package com.example.cryptolistings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.data.HistoricalPrice
import com.example.cryptolistings.data.PricePoint
import com.example.cryptolistings.network.CryptoApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(val pricePoints: List<PricePoint>) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class CryptoDetailViewModel(
    private val apiService: CryptoApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadCryptoDetails(crypto: CryptoModel) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // Construct the symbol for Binance API (e.g., "BTCUSDT")
                val symbol = "${crypto.symbol}USDT"
                
                // Get historical prices with explicit parameters
                val historicalData = apiService.getHistoricalPrices(
                    symbol = symbol,
                    interval = "1h",
                    limit = 24
                )
                val pricePoints = historicalData.mapIndexed { index, kline ->
                    val timestamp = kline[0].toLong()
                    val closePrice = kline[4].toDoubleOrNull() ?: 0.0
                    val dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault()
                    )
                    PricePoint(dateTime, closePrice)
                }
                _uiState.value = DetailUiState.Success(pricePoints)
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Failed to load price history: ${e.message}")
            }
        }
    }

    fun formatDate(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
    }

    class Factory(private val apiService: CryptoApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CryptoDetailViewModel::class.java)) {
                return CryptoDetailViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 