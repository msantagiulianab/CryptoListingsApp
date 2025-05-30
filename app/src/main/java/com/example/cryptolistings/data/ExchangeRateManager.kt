package com.example.cryptolistings.data

import java.text.NumberFormat
import java.util.*

object ExchangeRateManager {
    private val usdFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    
    fun formatPrice(usdPrice: Double): String {
        return usdFormat.format(usdPrice)
    }
} 