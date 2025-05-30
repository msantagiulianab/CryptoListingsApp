package com.example.cryptolistings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptolistings.data.BinanceTickerResponse
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.network.CryptoApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

sealed class CryptoUiState {
    data object Loading : CryptoUiState()
    data class Success(val cryptos: List<CryptoModel>) : CryptoUiState()
    data class Error(val message: String) : CryptoUiState()
}

class CryptoViewModel(
    private val apiService: CryptoApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow<CryptoUiState>(CryptoUiState.Loading)
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    private var usdToGbpRate: Double = 0.79 // Default rate, will be updated
    private val gbpFormat = NumberFormat.getCurrencyInstance(Locale.UK).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    // List of top cryptocurrencies we want to display
    private val topCryptoSymbols = setOf(
        "BTC", "ETH", "BNB", "SOL", "XRP", "ADA", "AVAX", "DOGE", "DOT", "MATIC",
        "LINK", "UNI", "LTC", "ATOM", "ETC", "XLM", "ALGO", "ICP", "FIL", "VET",
        "MANA", "SAND", "AXS", "THETA", "EOS", "AAVE", "CAKE", "KLAY", "NEAR", "GRT"
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = CryptoUiState.Loading
            try {
                // First get the exchange rate
                val gbpPrice = apiService.getExchangeRate(symbol = "GBPUSDT")
                val gbpPriceDouble = gbpPrice.price.toDoubleOrNull() ?: 0.79
                usdToGbpRate = if (gbpPriceDouble > 0) 1.0 / gbpPriceDouble else 0.79
                
                // Then get the crypto data and filter for top cryptocurrencies
                val binanceResponse = apiService.getTopCryptos()
                val cryptos = binanceResponse
                    .filter { response -> 
                        // Only include USDT pairs and our top cryptocurrencies
                        response.symbol.endsWith("USDT") && 
                        topCryptoSymbols.contains(response.symbol.replace("USDT", ""))
                    }
                    .map { response -> CryptoModel.fromBinanceResponse(response) }
                    .sortedBy { it.symbol } // Sort alphabetically by symbol
                
                _uiState.value = CryptoUiState.Success(cryptos)
            } catch (e: Exception) {
                _uiState.value = CryptoUiState.Error("Failed to load data: ${e.message}")
            }
        }
    }

    fun getCryptoById(id: String): CryptoModel? {
        return (uiState.value as? CryptoUiState.Success)?.cryptos?.find { it.id == id }
    }

    fun formatPriceInGBP(usdPrice: Double): String {
        val gbpPrice = usdPrice * usdToGbpRate
        return gbpFormat.format(gbpPrice)
    }

    class Factory(private val apiService: CryptoApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
                return CryptoViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 