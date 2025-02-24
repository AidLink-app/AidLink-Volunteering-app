plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.welcom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.welcom"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11" // Align Kotlin JVM target with Java
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.espresso.intents)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.sun.mail:android-mail:1.6.0")
    implementation("com.sun.mail:android-activation:1.6.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.8.9")
    // Espresso Core
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Espresso Contrib for advanced interactions (optional)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    // JUnit Test Rules
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    // Android Test Runner
    androidTestImplementation("androidx.test:runner:1.5.2")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // Idling Resources for async tasks (optional)
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")
    implementation("androidx.security:security-crypto:1.0.0-rc03")


}