package com.example.cryptolistings.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cryptolistings.MainActivity
import com.example.cryptolistings.R
import com.example.cryptolistings.data.ExchangeRateManager

object NotificationHelper {
    private const val CHANNEL_ID = "price_alerts"
    private const val CHANNEL_NAME = "Price Alerts"
    private const val CHANNEL_DESCRIPTION = "Notifications for cryptocurrency price alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createPriceAlertNotification(
        context: Context,
        cryptoName: String,
        currentPrice: Double,
        targetPrice: Double,
        notificationId: Int
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val formattedCurrentPrice = ExchangeRateManager.formatPrice(currentPrice)
        val formattedTargetPrice = ExchangeRateManager.formatPrice(targetPrice)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Price Alert: $cryptoName")
            .setContentText("Current price: $formattedCurrentPrice (Target: $formattedTargetPrice)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$cryptoName has reached your target price!\n" +
                        "Current price: $formattedCurrentPrice\n" +
                        "Target price: $formattedTargetPrice"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    fun createServiceNotification(
        context: Context,
        activeAlertsCount: Int
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Price Alerts Active")
            .setContentText("Monitoring $activeAlertsCount price alerts")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
    }
} 