package com.example.cryptolistings.data

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

private const val TAG = "CryptoModel"

@JsonClass(generateAdapter = true)
data class BinanceTickerResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "priceChange") val priceChange: String,
    @Json(name = "priceChangePercent") val priceChangePercent: String,
    @Json(name = "lastPrice") val lastPrice: String,
    @Json(name = "volume") val volume: String,
    @Json(name = "quoteVolume") val quoteVolume: String
)

@JsonClass(generateAdapter = true)
data class BinancePriceResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "price") val price: String
)

data class CryptoModel(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChangePercentage24h: Double?,
    val imageUrl: String
) {
    companion object {
        // Updated CoinGecko IDs - verified working
        private val coinImages = mapOf(
            "btc" to "1",      // Bitcoin
            "eth" to "279",    // Ethereum
            "bnb" to "825",    // Binance Coin
            "sol" to "4128",   // Solana
            "xrp" to "44",     // Ripple
            "ada" to "975",    // Cardano
            "avax" to "12559", // Avalanche
            "doge" to "5",     // Dogecoin
            "dot" to "6636",   // Polkadot
            "matic" to "4713", // Polygon
            "link" to "1975",  // Chainlink
            "uni" to "12504",  // Uniswap
            "ltc" to "2",      // Litecoin
            "atom" to "3794",  // Cosmos
            "etc" to "29",     // Ethereum Classic
            "xlm" to "100",    // Stellar
            "algo" to "4030",  // Algorand
            "icp" to "14495",  // Internet Computer
            "fil" to "12826",  // Filecoin
            "vet" to "498",    // VeChain
            "mana" to "878",   // Decentraland
            "sand" to "12129", // The Sandbox
            "axs" to "11636",  // Axie Infinity
            "theta" to "2419", // Theta Token
            "eos" to "73",     // EOS
            "aave" to "7278",  // Aave
            "cake" to "8256",  // PancakeSwap
            "klay" to "2840",  // Klaytn
            "near" to "11165", // NEAR Protocol
            "grt" to "6713"    // The Graph
        )

        fun fromBinanceResponse(response: BinanceTickerResponse): CryptoModel {
            // Remove USDT from the symbol (e.g., "BTCUSDT" -> "BTC")
            val symbol = response.symbol.replace("USDT", "")
            
            return CryptoModel(
                id = symbol.lowercase(),
                symbol = symbol,
                name = symbol, // Binance doesn't provide names, using symbol as name
                currentPrice = response.lastPrice.toDoubleOrNull() ?: 0.0,
                priceChangePercentage24h = response.priceChangePercent.toDoubleOrNull(),
                imageUrl = "" // Binance doesn't provide image URLs
            )
        }
    }
} 