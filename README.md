# CryptoListings 📈

![Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4)
![API](https://img.shields.io/badge/API-Binance-FCD535)
![License](https://img.shields.io/badge/License-MIT-yellow)

> A real-time cryptocurrency price tracker for Android built with Jetpack Compose and the Binance API.

---

## ✨ Features

- **📊 Live Prices** — Track real-time prices of 30+ top cryptocurrencies including Bitcoin, Ethereum, Solana, XRP, Cardano, and more.
- **📈 24-Hour Price Charts** — Interactive line charts showing hourly price movements over the last 24 hours for any cryptocurrency.
- **🔔 Custom Price Alerts** — Set target prices and receive push notifications when your desired price is reached.
- **🌙 Dark Theme** — Sleek, modern dark UI built with Material 3 Design.
- **🔄 Pull-to-Refresh** — One-tap refresh to get the latest market data instantly.
- **🚀 Smooth Animations** — Custom splash screen animation and fluid transitions between screens.

## 📸 Screenshots

*(Coming soon — screenshots of the app)*

## 🛠️ Built With

| Technology | Purpose |
|---|---|
| [Kotlin](https://kotlinlang.org/) | Programming language |
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | Modern declarative UI toolkit |
| [Material 3](https://m3.material.io/) | Material Design 3 components & theming |
| [Binance API](https://binance-docs.github.io/apidocs/) | Real-time & historical cryptocurrency data |
| [Retrofit](https://square.github.io/retrofit/) | HTTP client for API communication |
| [Moshi](https://github.com/square/moshi) | JSON serialization/deserialization |
| [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) | Interactive price charts |
| [Jetpack Navigation](https://developer.android.com/guide/navigation) | Screen navigation & routing |
| [Coroutines + Flow](https://kotlinlang.org/docs/coroutines-overview.html) | Asynchronous operations & state management |
| [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) | UI state management |
| [Coil](https://coil-kt.github.io/coil/) | Image loading |
| [OkHttp](https://square.github.io/okhttp/) | Logging interceptors & network layer |
| [Foreground Service](https://developer.android.com/guide/components/foreground-services) | Background price monitoring for alerts |

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 21+
- Android SDK 35+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/msantagiulianab/CryptoListingsApp.git
   cd CryptoListingsApp
   ```

2. **Open in Android Studio**
   - Select `File → Open` and navigate to the project directory.
   - Wait for Gradle sync to complete.

3. **Run the app**
   - Select a device or emulator running Android 9.0 (API 28)+.
   - Click the **Run** button or press `Shift + F10`.

## 🏗️ Architecture

```
app/
├── src/main/java/com/example/cryptolistings/
│   ├── MainActivity.kt              # Entry point with navigation setup
│   ├── data/
│   │   ├── CryptoModel.kt           # Data models & Binance API DTOs
│   │   ├── ExchangeRateManager.kt   # Price formatting utilities
│   │   ├── HistoricalPrice.kt       # Kline/historical data parser
│   │   └── PricePoint.kt            # Timestamped price data point
│   ├── network/
│   │   ├── CryptoApiService.kt      # Retrofit API interface
│   │   └── NetworkModule.kt         # Retrofit + OkHttp + Moshi setup
│   ├── notification/
│   │   └── NotificationHelper.kt    # Notification channels & builders
│   ├── service/
│   │   └── PriceAlertService.kt     # Foreground service for alerts
│   └── ui/
│       ├── CryptoListScreen.kt      # List of cryptocurrencies
│       ├── CryptoViewModel.kt       # List screen ViewModel
│       ├── CryptoDetailScreen.kt    # Detail screen with chart & alerts
│       ├── CryptoDetailViewModel.kt # Detail screen ViewModel
│       └── theme/                   # Compose theme definitions
└── src/main/res/                    # Resources (icons, themes, strings)
```

### Key Components

- **`CryptoApiService`** — Defines Retrofit endpoints for Binance's public API (24hr ticker, klines/candlestick data, price feed).
- **`CryptoViewModel`** — Fetches and filters top cryptocurrency data from Binance, mapping API responses to app models.
- **`CryptoDetailViewModel`** — Loads 24-hour historical price data and renders interactive charts.
- **`PriceAlertService`** — Android Foreground Service that runs in the background, periodically checking prices against user-defined targets and firing notifications when matched.
- **`NotificationHelper`** — Manages notification channels (API 26+) and builds both alert and service status notifications.

## 🔌 API Reference

This app uses the **Binance public API** (no API key required for public endpoints):

| Endpoint | Description |
|---|---|
| `GET /api/v3/ticker/24hr` | 24-hour price change statistics for all symbols |
| `GET /api/v3/klines` | Candlestick/Kline data for historical prices |
| `GET /api/v3/ticker/price` | Latest price for a trading pair |

> **Note:** The app filters for USDT trading pairs and focuses on 30 major cryptocurrencies.

## 💡 How Price Alerts Work

1. User navigates to any cryptocurrency detail screen.
2. Taps **"Set Price Alert"** and enters a target USD price.
3. Alert is saved to `SharedPreferences` and a **Foreground Service** starts.
4. The service checks prices every **60 seconds** against active alerts.
5. When a price matches the target (within $0.01), a **push notification** is sent.
6. The alert is automatically removed after firing.
7. The service stops itself when no active alerts remain.

## 🎨 Theme

The app uses a custom **dark theme** with:
- Background: `#121212` (deep dark)
- Cards: `#1E1E1E` (elevated surfaces)
- Green: `#00C853` (positive price changes)
- Red: `#FF3D00` (negative price changes)
- Text: `#E0E0E0` (primary) / `#B0B0B0` (secondary)

## 📱 Minimum Requirements

- **Android 9.0 (API 28)** or higher
- **Internet connection** (for live price data)
- **Notification permission** (for price alerts on Android 13+)

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 🤝 Contributing

Contributions are welcome! Feel free to:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Binance](https://www.binance.com/) for providing free public API access
- [Jetpack Compose](https://developer.android.com/jetpack/compose) team for the modern UI toolkit
- All the open-source libraries that made this project possible
