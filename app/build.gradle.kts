
plugins {
    id("com.android.application")
    
}

android {
    namespace = "hitmargin.adofai.effects"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "hitmargin.adofai.effects"
        minSdk = 21
        targetSdk = 28
        versionCode = 40
        versionName = "0.4.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        
    }
    
}

dependencies {


    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
}
