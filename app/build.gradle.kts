plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // ※ Compose Compiler プラグインを使う場合は↓を追加（任意）
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.negi.survey"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.negi.survey"
        minSdk = 23
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // freeCompilerArgs += listOf("-Xcontext-receivers") // 必要なら
    }

    buildFeatures {
        compose = true
    }

    // Kotlin Compose Compiler プラグインを使わない場合のみ必要（使うなら不要）
    composeOptions {
        // 例: kotlinCompilerExtensionVersion = "1.5.14"
        // ↑ 使用中の Android Gradle Plugin / Kotlin に合わせて調整してください
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/**.version"
        }
    }
}

dependencies {
    // ---- Compose BOM（バージョンは一括管理）----
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00") // うまくいかない場合は最新版に更新
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1") // LocaleListCompat を使うなら推奨

    // ---- Compose Core / Material3 / 基本UI ----
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")                 // KeyboardOptions 等
    implementation("androidx.compose.foundation:foundation")      // Row/Column/Checkbox/Radio など
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // rememberSaveable
    implementation("androidx.compose.runtime:runtime-saveable")

    // Activity + Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Navigation（Compose）
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Lifecycle（Compose）
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Coroutines（任意）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // テスト
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
