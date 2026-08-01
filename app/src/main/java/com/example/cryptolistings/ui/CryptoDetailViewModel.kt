package com.example.cryptolistings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.data.ExchangeRateManager
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

enum class TimeFrame(
    val label: String,
    val interval: String,
    val limit: Int,
    val dateFormat: String,
    val customFormatter: ((LocalDateTime) -> String)? = null
) {
    LAST_24H("Last 24h", "1h", 24, "HH:mm", { date -> 
        // Show time for Last 24h
        date.format(DateTimeFormatter.ofPattern("HH:mm"))
    }),
    LAST_WEEK("Last Week", "4h", 42, "EEE", { date -> 
        // Show only day name
        date.dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }
    }),
    LAST_MONTH("Last Month", "1d", 30, "dd MMM", { date -> 
        // Show only date
        date.format(DateTimeFormatter.ofPattern("dd MMM"))
    }),
    LAST_3_MONTHS("Last 3 Months", "1d", 90, "dd MMM", { date -> 
        // Show month at the start of each month and at regular intervals
        when (date.dayOfMonth) {
            1, 15, 30 -> date.format(DateTimeFormatter.ofPattern("MMM"))
            else -> ""
        }
    }),
    LAST_6_MONTHS("Last 6 Months", "1d", 180, "dd MMM", { date -> 
        // Show only month
        date.format(DateTimeFormatter.ofPattern("MMM"))
    }),
    LAST_YEAR("Last Year", "1d", 365, "MMM yyyy", { date -> 
        // Show only month
        date.format(DateTimeFormatter.ofPattern("MMM"))
    })
}

class CryptoDetailViewModel(
    private val apiService: CryptoApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    private val _currentTimeFrame = MutableStateFlow(TimeFrame.LAST_24H)
    val currentTimeFrame: StateFlow<TimeFrame> = _currentTimeFrame.asStateFlow()
    private var currentCrypto: CryptoModel? = null

    fun formatPrice(price: Double): String {
        return ExchangeRateManager.formatPrice(price)
    }

    fun setTimeFrame(timeFrame: TimeFrame) {
        _currentTimeFrame.value = timeFrame
        currentCrypto?.let { loadCryptoDetails(it) }
    }

    fun loadCryptoDetails(crypto: CryptoModel) {
        currentCrypto = crypto
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // Get historical prices
                val symbol = "${crypto.symbol}USDT"
                val klines = apiService.getHistoricalPrices(
                    symbol = symbol,
                    interval = currentTimeFrame.value.interval,
                    limit = currentTimeFrame.value.limit
                )
                
                val pricePoints = klines.map { kline ->
                    // kline format: [timestamp, open, high, low, close, ...]
                    val timestamp = kline[0].toLong()
                    val closePrice = kline[4].toDoubleOrNull() ?: 0.0
                    PricePoint(
                        timestamp = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(timestamp),
                            ZoneId.systemDefault()
                        ),
                        price = closePrice
                    )
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