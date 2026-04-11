import java.util.Properties
import java.io.FileInputStream

plugins {
    // Standard Android and Kotlin plugins for a modern Compose-based app
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

/**
 * SECURITY: LOCAL PROPERTIES LOADER
 * We load keystore credentials from 'local.properties' (which is git-ignored).
 * This keeps your sensitive passwords out of public GitHub repositories.
 */
val keystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "app.banana.bananaknowledge97"
    compileSdk = 36 // Targeting the latest Android API (Android 15+)

    defaultConfig {
        applicationId = "app.banana.bananaknowledge97"
        minSdk = 26     // Android 8.0 (Oreo) or higher
        targetSdk = 36  // Optimized for the latest Android behavior
        versionCode = 2 
        versionName = "2.97"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    /**
     * SIGNING CONFIGURATION
     * Automatically pulls passwords from the local.properties loader above.
     */
    signingConfigs {
        create("release") {
            val path = keystoreProperties["RELEASE_STORE_PATH"] as String?
            if (!path.isNullOrEmpty()) {
                storeFile = file(path)
                storePassword = keystoreProperties["RELEASE_STORE_PASSWORD"] as String?
                keyAlias = keystoreProperties["RELEASE_KEY_ALIAS"] as String?
                keyPassword = keystoreProperties["RELEASE_KEY_PASSWORD"] as String?
            }
        }
    }

    buildTypes {
        getByName("release") {
            // Minification (R8) shrinks the app and obfuscates code for security
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        // Required for Jetpack Compose UI
        compose = true
    }

    compileOptions {
        // Sets the Java compatibility level
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
        }
    }
}

dependencies {
    /**
     * 1. BOM (Bill of Materials)
     * This ensures all Compose libraries (UI, Material, etc.) versions are compatible.
     */
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    /**
     * 2. Core Essentials
     * activity-compose: Bridges the standard Activity to Compose UI.
     * core-ktx: Essential Kotlin extensions for the Android SDK.
     */
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    
    /**
     * 3. UI & Styling
     * splashscreen: Modern API for the app startup logo.
     * material3: The latest "Material You" UI components.
     * material-icons-extended: Provides the full set of icons (Menu, Refresh, Home, etc.).
     */
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    
    /**
     * 4. Lifecycle & Browser
     * lifecycle-runtime-compose: Safely handles UI state changes (like OnResume/OnPause).
     * browser: Used for "Chrome Custom Tabs" when opening external links.
     */
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation ("androidx.browser:browser:1.8.0")

    /**
     * 5. Legacy/Material Support
     * material: Standard Material design components for any remaining XML/Theme parts.
     */
    implementation("com.google.android.material:material:1.11.0")
}
