plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.veyra.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.veyra.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin { jvmToolchain(17) }

dependencies {
    testImplementation("junit:junit:4.13.2")
}
