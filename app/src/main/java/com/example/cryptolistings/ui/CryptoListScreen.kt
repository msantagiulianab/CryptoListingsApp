package com.example.cryptolistings.ui

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cryptolistings.data.CryptoModel
import java.text.NumberFormat
import java.util.*

private const val TAG = "CryptoListScreen"

// Custom colors
private val CardBackground = Color(0xFF1E1E1E)
private val TextPrimary = Color(0xFFE0E0E0)
private val TextSecondary = Color(0xFFB0B0B0)
private val PositiveGreen = Color(0xFF00C853)
private val NegativeRed = Color(0xFFFF3D00)
private val BackgroundColor = Color(0xFF121212)
private val TopBarColor = Color(0xFF1F1F1F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoListScreen(
    viewModel: CryptoViewModel,
    onCryptoClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    val refreshRotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Cryptocurrencies",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            isRefreshing = true
                            viewModel.refresh()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.rotate(refreshRotation),
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundColor)
        ) {
            when (uiState) {
                is CryptoUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                }
                is CryptoUiState.Success -> {
                    val cryptos = (uiState as CryptoUiState.Success).cryptos
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = cryptos,
                            key = { it.id }
                        ) { crypto ->
                            CryptoItem(
                                crypto = crypto,
                                onClick = { onCryptoClick(crypto.id) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
                is CryptoUiState.Error -> {
                    val error = (uiState as CryptoUiState.Error).message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error,
                            color = NegativeRed,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CryptoItem(
    crypto: CryptoModel,
    onClick: () -> Unit,
    viewModel: CryptoViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Crypto Info
            Column {
                Text(
                    text = crypto.symbol.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Price Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = viewModel.formatPrice(crypto.currentPrice),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                crypto.priceChangePercentage24h?.let { change ->
                    Text(
                        text = String.format("%.2f%%", change),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (change >= 0) 
                            PositiveGreen
                        else 
                            NegativeRed
                    )
                }
            }
        }
    }
} 