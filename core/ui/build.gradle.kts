import kotlin.math.min

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    id("kotlin-android")
    id("android-module-dependencies")
}

android {
    namespace = "app.aaps.core.ui"
    defaultConfig {
        minSdk = min(Versions.minSdk, Versions.wearMinSdk)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.androidx.core)
    api(libs.androidx.appcompat)
    api(libs.androidx.preference)
    api(libs.androidx.gridlayout)


    api(libs.com.google.android.material)

    api(libs.com.google.dagger.android)
    api(libs.com.google.dagger.android.support)
    implementation(project(":core:interfaces"))
    implementation(project(":core:keys"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
}