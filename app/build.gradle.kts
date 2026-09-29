plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.menzo.kanal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.menzo.kanal"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Where the app fetches its channel list and the optional country/category map.
        // Set kanal.configUrl / kanal.metaUrl in ~/.gradle/gradle.properties or pass -P.
        val configUrl = providers.gradleProperty("kanal.configUrl")
            .getOrElse("https://example.com/channels.json")
        val metaUrl = providers.gradleProperty("kanal.metaUrl").getOrElse("")
        buildConfigField("String", "CONFIG_URL", "\"$configUrl\"")
        buildConfigField("String", "META_URL", "\"$metaUrl\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.tv:tv-material:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
