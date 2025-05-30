package com.example.cryptolistings.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.data.PricePoint
import com.example.cryptolistings.service.PriceAlertService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.format.DateTimeFormatter
import android.content.Context
import android.widget.Toast

private const val TAG = "CryptoDetailScreen"

// Custom colors (same as CryptoListScreen)
private val CardBackground = Color(0xFF1E1E1E)
private val TextPrimary = Color(0xFFE0E0E0)
private val TextSecondary = Color(0xFFB0B0B0)
private val PositiveGreen = Color(0xFF00C853)
private val NegativeRed = Color(0xFFFF3D00)
private val BackgroundColor = Color(0xFF121212)
private val TopBarColor = Color(0xFF1F1F1F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    crypto: CryptoModel,
    viewModel: CryptoDetailViewModel,
    onBackClick: () -> Unit
) {
    Log.d(TAG, "Opening detail screen for ${crypto.name}")
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // State for price alert input
    var alertPrice by remember { mutableStateOf("") }
    var isAlertActive by remember { mutableStateOf(false) }
    
    // Load saved alert price if exists
    LaunchedEffect(crypto.id) {
        val savedPrice = context.getSharedPreferences("price_alerts", Context.MODE_PRIVATE)
            .getString("alert_${crypto.id}", "")
        if (savedPrice != null && savedPrice.isNotEmpty()) {
            alertPrice = savedPrice
            isAlertActive = true
        }
    }

    LaunchedEffect(crypto) {
        Log.d(TAG, "Loading details for ${crypto.name}")
        viewModel.loadCryptoDetails(crypto)
    }

    // Add this at the top of the CryptoDetailScreen composable, after the existing state variables
    var showAlertDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${crypto.name} (${crypto.symbol})",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarColor,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Price Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Current Price",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = viewModel.formatPrice(crypto.currentPrice),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        if (crypto.priceChangePercentage24h != null) {
                            val color = if (crypto.priceChangePercentage24h >= 0)
                                PositiveGreen
                            else
                                NegativeRed
                            Text(
                                text = "${String.format("%.2f", crypto.priceChangePercentage24h)}%",
                                style = MaterialTheme.typography.bodyLarge,
                                color = color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Alert Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Price Alert",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showAlertDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Set Price Alert")
                        }
                        if (isAlertActive) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Current Alert: ${alertPrice.toDoubleOrNull()?.let { viewModel.formatPrice(it) } ?: alertPrice}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )
                ) {
                    when (uiState) {
                        is DetailUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 4.dp
                                )
                            }
                        }

                        is DetailUiState.Success -> {
                            val pricePoints = (uiState as DetailUiState.Success).pricePoints
                            if (pricePoints.isNotEmpty()) {
                                PriceChart(
                                    pricePoints = pricePoints,
                                    formatPrice = { price -> viewModel.formatPrice(price) }
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No price data available",
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        is DetailUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (uiState as DetailUiState.Error).message,
                                    color = NegativeRed,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add this after the Scaffold closing brace but before the end of the composable
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Set Price Alert", color = TextPrimary) },
            text = {
                Column {
                    Text("Set a price alert for ${crypto.symbol}", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alertPrice,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                                alertPrice = newValue
                            }
                        },
                        label = { Text("Target Price (USD)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
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
                                .putString("alert_${crypto.id}", alertPrice)
                                .apply()
                            
                            // Start service if not running
                            PriceAlertService.startService(context)
                            
                            isAlertActive = true
                            showAlertDialog = false
                            
                            Toast.makeText(
                                context,
                                "Alert set for ${viewModel.formatPrice(targetPrice)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a valid price",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Set Alert")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAlertDialog = false }
                ) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun PriceChart(
    pricePoints: List<PricePoint>,
    formatPrice: (Double) -> String
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val entries = pricePoints.mapIndexed { index, point ->
        Entry(index.toFloat(), point.price.toFloat())
    }

    // Get the primary color outside of AndroidView
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = TextPrimary.toArgb()
    val gridColor = TextSecondary.copy(alpha = 0.2f).toArgb()
    val backgroundColor = CardBackground.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(true)
                setPinchZoom(true)

                // Set background color
                setBackgroundColor(backgroundColor)

                // Configure X axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    labelRotationAngle = -45f
                    this.textColor = textColor
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index in pricePoints.indices) {
                                timeFormatter.format(pricePoints[index].timestamp)
                            } else ""
                        }
                    }
                }

                // Calculate Y axis range with padding
                val minPrice = pricePoints.minOfOrNull { it.price } ?: 0.0
                val maxPrice = pricePoints.maxOfOrNull { it.price } ?: 0.0
                val priceRange = maxPrice - minPrice
                val padding = priceRange * 0.1 // 10% padding
                val minY = minPrice - padding
                val maxY = maxPrice + padding

                // Configure Y axis
                axisLeft.apply {
                    setDrawGridLines(true)
                    this.gridColor = gridColor
                    this.textColor = textColor
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatPrice(value.toDouble())
                        }
                    }
                    axisMinimum = minY.toFloat()
                    axisMaximum = maxY.toFloat()
                }
                axisRight.isEnabled = false

                // Set data
                val dataSet = LineDataSet(entries, "Price").apply {
                    color = primaryColor
                    setDrawCircles(false)
                    setDrawValues(false)
                    lineWidth = 2f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                data = LineData(dataSet)

                // Disable auto scaling
                setAutoScaleMinMaxEnabled(false)

                invalidate() // Refresh the chart
            }
        }
    )
} 