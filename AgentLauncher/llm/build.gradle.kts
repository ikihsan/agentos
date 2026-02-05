plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.agentos.llm"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // API keys should be provided via local.properties or environment variables
        buildConfigField("String", "OPENAI_API_KEY", "\"${findProperty("OPENAI_API_KEY") ?: ""}\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"${findProperty("ANTHROPIC_API_KEY") ?: ""}\"")
    }
}

dependencies {
    implementation(project(":core"))

    // Networking
    implementation(libs.bundles.networking)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.okhttp)
}
