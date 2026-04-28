import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "in2000.prosjekt.in2000project"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "in2000.prosjekt.in2000project"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.foundation)
    implementation(libs.foundation)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.core)

    // Mapbox
    implementation(libs.mapbox.maps.android)
    implementation(libs.mapbox.maps.compose)

    // Ktor client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Test
    testImplementation(libs.kotlinx.coroutines.test)

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    //Graph
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("androidx.compose.foundation:foundation:1.6.0")

}

// Read local.properties to pass secrets to JVM test processes as system properties.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

tasks.withType<Test> {
    systemProperty("FROST_CLIENT_ID", localProps.getProperty("FROST_CLIENT_ID") ?: "")
    // Integration tests auto-skip via Assume.assumeTrue when FROST_CLIENT_ID is absent.
    // Run them from CLI: ./gradlew test --tests "*.FrostApiIntegrationTest"
    // Or click the green gutter arrow in Android Studio.
}