package com.example.cryptolistings.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.cryptolistings.data.ExchangeRateManager
import com.example.cryptolistings.network.CryptoApiService
import com.example.cryptolistings.network.NetworkModule
import com.example.cryptolistings.notification.NotificationHelper
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class PriceAlertService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val activeAlerts = ConcurrentHashMap<String, Double>()
    private lateinit var apiService: CryptoApiService

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHECK_INTERVAL = 60_000L // 1 minute

        fun startService(context: Context) {
            val intent = Intent(context, PriceAlertService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PriceAlertService::class.java)
            context.stopService(intent)
        }

        fun hasActiveAlerts(context: Context): Boolean {
            val prefs = context.getSharedPreferences("price_alerts", Context.MODE_PRIVATE)
            return prefs.all.any { it.key.startsWith("alert_") }
        }
    }

    override fun onCreate() {
        super.onCreate()
        apiService = NetworkModule.provideCryptoApiService()
        NotificationHelper.createNotificationChannel(this)
        loadActiveAlerts()
        startForeground(NOTIFICATION_ID, createServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (isActive) {
                checkPrices()
                delay(CHECK_INTERVAL)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun loadActiveAlerts() {
        val prefs = getSharedPreferences("price_alerts", Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("alert_")) {
                val cryptoId = key.removePrefix("alert_")
                val targetPrice = (value as String).toDoubleOrNull()
                if (targetPrice != null) {
                    activeAlerts[cryptoId] = targetPrice
                }
            }
        }
        updateServiceNotification()
    }

    private fun createServiceNotification(): Notification {
        return NotificationHelper.createServiceNotification(
            this,
            activeAlerts.size
        ).build()
    }

    private fun updateServiceNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createServiceNotification())
    }

    private suspend fun checkPrices() {
        try {
            activeAlerts.forEach { (cryptoId, targetPrice) ->
                try {
                    // Get current price from API
                    val symbol = "${cryptoId}USDT"
                    val response = apiService.getTopCryptos()
                    val currentPrice = response.find { it.symbol == symbol }?.lastPrice?.toDoubleOrNull() ?: return@forEach

                    // Check if price has crossed the target
                    val priceDiff = currentPrice - targetPrice
                    if (abs(priceDiff) < 0.01) { // Within 1 cent of target
                        // Send notification
                        val notification = NotificationHelper.createPriceAlertNotification(
                            context = this,
                            cryptoName = cryptoId,
                            currentPrice = currentPrice,
                            targetPrice = targetPrice,
                            notificationId = cryptoId.hashCode()
                        ).build()

                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(cryptoId.hashCode(), notification)

                        // Remove the alert
                        activeAlerts.remove(cryptoId)
                        getSharedPreferences("price_alerts", Context.MODE_PRIVATE)
                            .edit()
                            .remove("alert_$cryptoId")
                            .apply()

                        // Update service notification
                        updateServiceNotification()

                        // Stop service if no more alerts
                        if (activeAlerts.isEmpty()) {
                            stopSelf()
                        }
                    }
                } catch (e: Exception) {
                    // Log error but continue checking other alerts
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 