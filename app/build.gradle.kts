plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.financasdacasa.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.financasdacasa.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Default API URL for development (override in local.properties or CI)
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/api\"")
        buildConfigField("String", "GP_MONTHLY_PRODUCT_ID", "\"financas_monthly\"")
        buildConfigField("String", "GP_ANNUAL_PRODUCT_ID", "\"financas_annual\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Exclude Moshi codegen from Hilt's annotation processing (already using KSP)
configurations.configureEach {
    if (name.contains("AnnotationProcessor", ignoreCase = true)) {
        exclude(group = "com.squareup.moshi", module = "moshi-kotlin-codegen")
    }
}

dependencies {
    // Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Security
    implementation(libs.security.crypto)

    // Google Sign-In (Credential Manager)
    implementation(libs.credentials)
    implementation(libs.credentials.play)
    implementation(libs.googleid)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    // EXIF
    implementation(libs.exifinterface)

    // Charts
    implementation(libs.vico.compose.m3)

    // Lucide Icons
    implementation(libs.lucide)

    // Drag reorder
    implementation(libs.reorderable)

    // Google Play Billing
    implementation(libs.billing.ktx)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
}
