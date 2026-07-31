plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Release signing credentials are injected via environment variables
// (GitHub Secrets in CI). CI passes an absolute KEYSTORE_FILE path
// (${{ github.workspace }}/app/release.keystore); locally we fall back to an
// absolute path under the app module dir so both scenarios resolve correctly.
// The credentials themselves have no fallbacks so a real `assembleRelease`
// always requires valid values and can never silently degrade to an unsigned
// APK.
val keystoreFilePath = System.getenv("KEYSTORE_FILE") ?: "${project.projectDir}/release.keystore"
val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
val keyAliasName = System.getenv("KEY_ALIAS")
val keyPasswordValue = System.getenv("KEY_PASSWORD")

android {
    println("=== 🔍 GRADLE CONFIGURATION PHASE SIGNING DIAGNOSTICS ===")
    println("KEYSTORE_FILE Env: ${System.getenv("KEYSTORE_FILE")}")
    println("KEYSTORE_PASSWORD Env Is Null: ${System.getenv("KEYSTORE_PASSWORD") == null}")
    println("KEY_ALIAS Env: ${System.getenv("KEY_ALIAS")}")
    println("KEY_PASSWORD Env Is Null: ${System.getenv("KEY_PASSWORD") == null}")
    
    val releaseConfig = signingConfigs.findByName("release")
    println("Release Signing Config Object Found: ${releaseConfig != null}")
    if (releaseConfig != null) {
        println("  - storeFile Resolved Path: ${releaseConfig.storeFile?.absolutePath}")
        println("  - storeFile Exists On Disk: ${releaseConfig.storeFile?.exists()}")
    }
    println("Target release buildType signingConfig Name: ${buildTypes.getByName("release").signingConfig?.name}")
    println("=======================================================")

    namespace = "com.example.cryptolistings"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cryptolistings"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreFilePath)
            storePassword = keystorePassword
            keyAlias = keyAliasName
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")

    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
}