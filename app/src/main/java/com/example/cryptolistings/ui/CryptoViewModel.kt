package com.example.cryptolistings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.data.ExchangeRateManager
import com.example.cryptolistings.network.CryptoApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                // Get the crypto data and filter for top cryptocurrencies
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

    fun formatPrice(price: Double): String {
        return ExchangeRateManager.formatPrice(price)
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