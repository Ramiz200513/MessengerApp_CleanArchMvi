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
    id ("io.gitlab.arturbosch.detekt") version "1.23.5"
}
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        toolVersion = "1.23.5"
        // Все модули будут использовать один и тот же конфиг
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true

        // Чтобы отчеты всех модулей собирались в одном месте
        reports {
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
        }
    }
}