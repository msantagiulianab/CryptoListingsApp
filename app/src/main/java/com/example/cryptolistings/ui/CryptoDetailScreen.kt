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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

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
    val currentTimeFrame by viewModel.currentTimeFrame.collectAsState()
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
                .background(BackgroundColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Price Card
                item {
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
                }

                // Price Alert Card
                item {
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
                }

                // Timeframe Selection
                item {
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
                                text = "Timeframe",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(TimeFrame.values()) { timeFrame ->
                                    FilterChip(
                                        selected = currentTimeFrame == timeFrame,
                                        onClick = { viewModel.setTimeFrame(timeFrame) },
                                        label = { Text(timeFrame.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Price Chart
                item {
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
                                        formatPrice = { price -> viewModel.formatPrice(price) },
                                        timeFrame = currentTimeFrame
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
    }

    // Alert Dialog
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
    formatPrice: (Double) -> String,
    timeFrame: TimeFrame
) {
    val entries = pricePoints.mapIndexed { index, point ->
        Entry(index.toFloat(), point.price.toFloat())
    }

    // Get the primary color outside of AndroidView
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val textColor = TextPrimary.toArgb()
    val backgroundColor = CardBackground.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                
                // Disable all touch interactions
                setTouchEnabled(false)
                setDragEnabled(false)
                setScaleEnabled(false)
                setPinchZoom(false)
                setScaleXEnabled(false)
                setScaleYEnabled(false)
                setDoubleTapToZoomEnabled(false)
                
                // Set background color
                setBackgroundColor(backgroundColor)

                // Configure X axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    labelRotationAngle = -45f
                    this.textColor = textColor
                    granularity = 1f
                    setDrawAxisLine(true)
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            if (index !in pricePoints.indices) return ""
                            
                            // For Last Week, show every day
                            if (timeFrame == TimeFrame.LAST_WEEK) {
                                val step = entries.size / 7 // Divide into 7 parts for each day
                                if (index % step == 0) {
                                    return pricePoints[index].timestamp.dayOfWeek.toString()
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                }
                                return ""
                            }
                            
                            // For Last 3 Months, show labels at regular intervals
                            if (timeFrame == TimeFrame.LAST_3_MONTHS) {
                                val step = entries.size / 3 // Divide into 3 parts
                                if (index % step == 0) {
                                    return pricePoints[index].timestamp.format(DateTimeFormatter.ofPattern("MMM"))
                                }
                                return ""
                            }

                            // For Last Year, show only 1 year ago and 6 months ago
                            if (timeFrame == TimeFrame.LAST_YEAR) {
                                if (index == 0) { // First point (1 year ago)
                                    return pricePoints[index].timestamp.format(DateTimeFormatter.ofPattern("MMM yyyy"))
                                } else if (index == entries.size / 2) { // Middle point (6 months ago)
                                    return pricePoints[index].timestamp.format(DateTimeFormatter.ofPattern("MMM yyyy"))
                                }
                                return ""
                            }
                            
                            return timeFrame.customFormatter?.invoke(pricePoints[index].timestamp) ?: ""
                        }
                    }
                }

                // Calculate Y axis range with padding
                val minPrice = pricePoints.minOfOrNull { it.price } ?: 0.0
                val maxPrice = pricePoints.maxOfOrNull { it.price } ?: 0.0
                val priceRange = maxPrice - minPrice
                val padding = priceRange * 0.1
                val minY = minPrice - padding
                val maxY = maxPrice + padding

                // Configure Y axis
                axisLeft.apply {
                    setDrawGridLines(false)
                    this.textColor = textColor
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return formatPrice(value.toDouble())
                        }
                    }
                    axisMinimum = minY.toFloat()
                    axisMaximum = maxY.toFloat()
                    granularity = ((maxY - minY) / 5f).toFloat()
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

                // Configure viewport
                setVisibleXRange(entries.size.toFloat(), entries.size.toFloat())
                moveViewToX(0f)
                
                // Disable auto scaling
                setAutoScaleMinMaxEnabled(false)
                
                // Set chart padding
                setExtraOffsets(20f, 20f, 20f, 20f)

                invalidate()
            }
        }
    )
} 