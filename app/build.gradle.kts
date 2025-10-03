plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Necesario para que Firebase use google-services.json
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.brigadist"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.brigadist"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Google Maps API Key placeholder
        // Maps Placeholders
        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""

        // Auth0 Placeholders
        manifestPlaceholders["auth0Domain"] = "dev-qjv13guqjegxhr3l.us.auth0.com"
        manifestPlaceholders["auth0Scheme"] = "com.example.brigadist"
        
        // Groq API Key
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${project.findProperty("GROQ_API_KEY") ?: ""}\""
        )
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Google Maps dependencies
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Permission handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Firebase BOM centraliza versiones
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")

    // Firebase Analytics (opcional pero recomendado)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Auth0 dependency
    implementation(libs.auth0)
    implementation(libs.java.jwt)
    
    // OkHttp for Groq API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

}
