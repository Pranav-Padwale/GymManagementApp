plugins {
    // Kotlin Multiplatform and Compose Multiplatform plugins
    kotlin("multiplatform") version "2.0.0" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("androidx.room") version "2.7.0-alpha01" apply false
    kotlin("plugin.serialization") version "2.0.0" apply false
}
