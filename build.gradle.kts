plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("androidx.navigation.safeargs") version "2.7.7" apply false
    // Tambahkan ini
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
}