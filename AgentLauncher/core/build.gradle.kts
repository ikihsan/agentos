plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.agentos.core"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Kotlin
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Logging
    api(libs.timber)

    // Testing
    testImplementation(libs.bundles.testing)
}
