plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    // ADD THIS PLUGIN FOR NAVIGATION SAFE ARGS (Already there, good!)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.proplanetperson"
    compileSdk = 35
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.proplanetperson"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Core AndroidX and Material Design dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation Component dependencies
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//     REMOVED Firebase/Google Services dependencies (commented out, which is good)
     implementation(libs.androidx.credentials)
     implementation(libs.androidx.credentials.play.services.auth)
     implementation(libs.googleid)
     implementation("com.google.android.gms:play-services-auth:21.3.0")

//     Networking: Retrofit, OkHttp, and Gson Converter (CORRECTED VERSIONS)
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // CHANGED FROM 3.0.0
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // CHANGED FROM 3.0.0
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Other existing project dependencies (CLEANED DUPLICATES)
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.androidx.core.splashscreen)
    implementation("com.airbnb.android:lottie:6.6.6")
    implementation("de.hdodenhof:circleimageview:3.1.0") // Kept this one, removed the duplicate
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.coil-kt:coil:2.7.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.CanHub:Android-Image-Cropper:4.3.2")
    implementation("com.github.shts:StoriesProgressView:3.0.0")
}