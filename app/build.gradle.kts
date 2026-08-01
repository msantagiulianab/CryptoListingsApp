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
    println("====================================================")
    println("=== 🔍 GRADLE RUNTIME SIGNING CONFIG DIAGNOSTICS ===")
    println("1. KEYSTORE_FILE value: ${System.getenv("KEYSTORE_FILE")}")
    println("2. KEYSTORE_PASSWORD is null? ${System.getenv("KEYSTORE_PASSWORD") == null}")
    println("3. KEYSTORE_PASSWORD length: ${System.getenv("KEYSTORE_PASSWORD")?.length ?: 0}")
    println("4. KEY_ALIAS value: ${System.getenv("KEY_ALIAS")}")
    println("5. KEY_PASSWORD is null? ${System.getenv("KEY_PASSWORD") == null}")
    println("====================================================")

    namespace = "com.example.cryptolistings"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cryptolistings"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // signingConfigs MUST be declared before buildTypes so that
    // buildTypes.release can bind to it via signingConfigs.getByName("release").
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

            // Force a hard build failure if the signing configuration was skipped or resolved as null
            if (signingConfig == null) {
                throw GradleException("❌ FATAL: 'release' buildType has NO signingConfig assigned! Check for duplicate or overriding buildTypes blocks later in the file.")
            }

            // Verify the credentials inside the attached signing config are actively populated
            val currentConfig = signingConfig!!
            if (currentConfig.storePassword.isNullOrEmpty()) {
                throw GradleException("❌ FATAL: KEYSTORE_PASSWORD environment variable resolved to an empty or null string during compilation configuration.")
            }
            if (currentConfig.keyAlias.isNullOrEmpty()) {
                throw GradleException("❌ FATAL: KEY_ALIAS environment variable resolved to an empty or null string during compilation configuration.")
            }
            if (currentConfig.keyPassword.isNullOrEmpty()) {
                throw GradleException("❌ FATAL: KEY_PASSWORD environment variable resolved to an empty or null string during compilation configuration.")
            }
        }
    }

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