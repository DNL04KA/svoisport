plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
}

android {
    namespace = "com.svoysport.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.svoysport.tv"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
}

// Автоматическая очистка некорректных SVG файлов перед сборкой
tasks.register("deleteSvgResources") {
    doFirst {
        val drawableDir = file("src/main/res/drawable")
        if (drawableDir.exists()) {
            drawableDir.listFiles { _, name -> name.endsWith(".svg") }?.forEach { 
                println("Deleting invalid resource: ${it.name}")
                it.delete() 
            }
        }
    }
}

tasks.whenTaskAdded {
    if (name == "preBuild") {
        dependsOn("deleteSvgResources")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3.tv)
    implementation(libs.androidx.foundation.tv)
    implementation(libs.androidx.navigation.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)   // HLS (.m3u8) — потоки sport-tv.by
    implementation(libs.androidx.media3.ui)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    // Appcompat for Theme.AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Gson for SerializedName
    implementation("com.google.code.gson:gson:2.10.1")

    debugImplementation(libs.androidx.ui.tooling)
}
