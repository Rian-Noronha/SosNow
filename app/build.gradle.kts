plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.rn.sosnow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rn.sosnow"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    secrets{
        propertiesFileName = "secrets.properties"
        defaultPropertiesFileName = "local.defaults.properties"
        ignoreList.add("keyToIgnore")
        ignoreList.add("sdk.*")
    }

}

    val glide_version = "4.16.0"
    val bom_version = "32.8.1"
    val auth_version = "22.1.2"
    val services_version = "4.3.15"
    val core_version = "1.13.0"
    val app_compat_version = "1.6.1"
    val material_version = "1.11.0"
    val constraint_layout_version = "2.1.4"
    val junit_version = "4.13.2"
    val test_junit_version = "1.1.5"
    val espresso_core_version = "3.5.1"
    val firestore_version = "24.8.1"
    val lifecycle_version="2.5.1"
    val storage_version = "20.2.1"
    val maps_version = "18.2.0"
    val services_auth_version = "21.1.0"
    val services_location = "21.2.0"
    val coroutines_version = "1.3.9"
    val coroutines_core_version = "1.5.0"
    val maps_utils_version = "2.3.0"
    val okhttp_version = "4.10.0"
    val gson_version = "2.10.1"
    val secrets_version = "2.0.1"

dependencies {

    implementation("androidx.core:core-ktx:$core_version")
    implementation("androidx.appcompat:appcompat:$app_compat_version")
    implementation("com.google.android.material:material:$material_version")
    implementation("androidx.constraintlayout:constraintlayout:$constraint_layout_version")
    implementation("com.google.android.gms:play-services-auth:$services_auth_version")



    implementation("com.github.bumptech.glide:glide:$glide_version")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    kapt("com.github.bumptech.glide:compiler:$glide_version")

    implementation(platform("com.google.firebase:firebase-bom:$bom_version"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-auth-ktx:$auth_version")
    implementation("com.google.gms:google-services:$services_version")
    implementation("com.google.firebase:firebase-firestore-ktx:$firestore_version")
    implementation("com.google.firebase:firebase-storage-ktx:$storage_version")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")

    kapt("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycle_version")

    implementation("com.google.android.gms:play-services-maps:$maps_version")
    implementation("com.google.android.gms:play-services-location:$services_location")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_core_version")

    implementation("com.google.maps.android:android-maps-utils:$maps_utils_version")

    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
    implementation("com.google.code.gson:gson:$gson_version")

    implementation("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:$secrets_version")

    testImplementation("junit:junit:$junit_version")
    androidTestImplementation("androidx.test.ext:junit:$test_junit_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_core_version")
}