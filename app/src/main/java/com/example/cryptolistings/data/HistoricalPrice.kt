package com.example.cryptolistings.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class HistoricalPrice(private val klines: List<List<String>>) {
    fun getDailyPrices(): List<PricePoint> {
        return klines.map { kline ->
            val timestamp = kline[0].toLong()
            val closePrice = kline[4].toDoubleOrNull() ?: 0.0
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
            )
            PricePoint(dateTime, closePrice)
        }
    }
} 