package com.example.cryptolistings

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cryptolistings.network.CryptoApiService
import com.example.cryptolistings.network.NetworkModule
import com.example.cryptolistings.ui.CryptoDetailScreen
import com.example.cryptolistings.ui.CryptoDetailViewModel
import com.example.cryptolistings.ui.CryptoListScreen
import com.example.cryptolistings.ui.CryptoViewModel
import com.example.cryptolistings.ui.theme.CryptoListingsTheme

class MainActivity : ComponentActivity() {
    private lateinit var apiService: CryptoApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize the API service
        apiService = NetworkModule.provideCryptoApiService()

        // Customize the splash screen animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create a custom animation
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.view.height.toFloat()
            ).apply {
                setInterpolator(AnticipateInterpolator())
                setDuration(500L)
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        splashScreenView.remove()
                    }
                })
                start()
            }
        }

        setContent {
            CryptoListingsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CryptoApp(apiService)
                }
            }
        }
    }
}

@Composable
fun CryptoApp(apiService: CryptoApiService) {
    val navController = rememberNavController()
    val listViewModel: CryptoViewModel = viewModel(
        factory = CryptoViewModel.Factory(apiService)
    )

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CryptoListScreen(
                viewModel = listViewModel,
                onCryptoClick = { cryptoId ->
                    navController.navigate("detail/$cryptoId")
                }
            )
        }
        composable("detail/{cryptoId}") { backStackEntry ->
            val cryptoId = backStackEntry.arguments?.getString("cryptoId") ?: return@composable
            val crypto = listViewModel.getCryptoById(cryptoId)
            if (crypto != null) {
                val detailViewModel: CryptoDetailViewModel = viewModel(
                    factory = CryptoDetailViewModel.Factory(apiService)
                )
                CryptoDetailScreen(
                    crypto = crypto,
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
} 