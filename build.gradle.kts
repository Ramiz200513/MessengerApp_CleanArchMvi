// Top-level build file
plugins {
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.android.application") version "8.6.0" apply false
    id("com.android.library") version "8.6.0" apply false
    // Google Services нужен для Firebase. Версию указываем явно, чтобы не было ошибок.
    id("com.google.gms.google-services") version "4.4.0" apply false
}