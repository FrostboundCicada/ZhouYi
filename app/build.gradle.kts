plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.frostboundcicada.zhouyi"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.frostboundcicada.zhouyi"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("org.jetbrains.compose.ui:ui:1.10.3")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.10.3")
    implementation("org.jetbrains.compose.material3:material3:1.10.3")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.10.3")
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.8.8")
    implementation("com.google.android.material:material:1.13.0")
}