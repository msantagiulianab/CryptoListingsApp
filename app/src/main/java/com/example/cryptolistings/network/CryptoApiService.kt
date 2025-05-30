package com.example.cryptolistings.network

import com.example.cryptolistings.data.BinanceTickerResponse
import com.example.cryptolistings.data.BinancePriceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoApiService {
    @GET("v3/ticker/24hr")
    suspend fun getTopCryptos(): List<BinanceTickerResponse>

    @GET("v3/klines")
    suspend fun getHistoricalPrices(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1h",
        @Query("limit") limit: Int = 24
    ): List<List<String>>

    @GET("v3/ticker/price")
    suspend fun getExchangeRate(
        @Query("symbol") symbol: String = "USDT"
    ): BinancePriceResponse
} 