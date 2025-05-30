package com.example.cryptolistings.data

import java.time.LocalDateTime

data class PricePoint(
    val timestamp: LocalDateTime,
    val price: Double
) 