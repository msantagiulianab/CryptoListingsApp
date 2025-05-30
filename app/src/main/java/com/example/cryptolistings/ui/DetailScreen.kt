package com.example.cryptolistings.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.service.PriceAlertService
import java.text.NumberFormat
import java.util.*

@Composable
fun DetailScreen(
    crypto: CryptoModel,
    onBackClick: () -> Unit
) {
    var showAlertDialog by remember { mutableStateOf(false) }
    var alertPrice by remember { mutableStateOf("") }
    val context = LocalContext.current
    val numberFormat = remember { 
        NumberFormat.getCurrencyInstance(Locale.UK).apply {
            maximumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Price Alert Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Price Alert",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showAlertDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Price Alert")
                }
            }
        }
    }

    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Set Price Alert") },
            text = {
                Column {
                    Text("Set a price alert for ${crypto.symbol}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alertPrice,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                alertPrice = newValue
                            }
                        },
                        label = { Text("Target Price (USD)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetPrice = alertPrice.toDoubleOrNull()
                        if (targetPrice != null && targetPrice > 0) {
                            // Save alert
                            context.getSharedPreferences("price_alerts", Context.MODE_PRIVATE)
                                .edit()
                                .putString("alert_${crypto.symbol.lowercase()}", targetPrice.toString())
                                .apply()
                            
                            // Start service if not running
                            PriceAlertService.startService(context)
                            
                            showAlertDialog = false
                            alertPrice = ""
                        }
                    }
                ) {
                    Text("Set Alert")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAlertDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
} 