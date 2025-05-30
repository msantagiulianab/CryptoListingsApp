package com.example.cryptolistings.ui

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cryptolistings.data.CryptoModel
import com.example.cryptolistings.data.PricePoint
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.format.DateTimeFormatter

private const val TAG = "CryptoDetailScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    crypto: CryptoModel,
    viewModel: CryptoDetailViewModel,
    onBackClick: () -> Unit
) {
    Log.d(TAG, "Opening detail screen for ${crypto.name}")
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(crypto) {
        Log.d(TAG, "Loading details for ${crypto.name}")
        viewModel.loadCryptoDetails(crypto)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${crypto.name} (${crypto.symbol})") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Price Card
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Price",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "£${crypto.currentPrice}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (crypto.priceChangePercentage24h != null) {
                        val color = if (crypto.priceChangePercentage24h >= 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                        Text(
                            text = "${String.format("%.2f", crypto.priceChangePercentage24h)}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                when (uiState) {
                    is DetailUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is DetailUiState.Success -> {
                        val pricePoints = (uiState as DetailUiState.Success).pricePoints
                        if (pricePoints.isNotEmpty()) {
                            PriceChart(pricePoints = pricePoints)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No price data available")
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
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceChart(pricePoints: List<PricePoint>) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val entries = pricePoints.mapIndexed { index, point ->
        Entry(index.toFloat(), point.price.toFloat())
    }
    
    // Get the primary color outside of AndroidView
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(true)
                setPinchZoom(true)
                
                // Configure X axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    labelRotationAngle = -45f
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
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "£${String.format("%.2f", value)}"
                        }
                    }
                    // Set exact min and max values to remove extra space
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