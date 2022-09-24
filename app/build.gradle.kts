import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    id("org.sonarqube") version "3.4.0.2513"
}

android {

    val prop = Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "keystore.properties")))
    }

    signingConfigs {
        create("lastquakechilesign") {
            storeFile = file(prop.getProperty("storeFile"))
            storePassword = prop.getProperty("storePassword").toString()
            keyPassword = prop.getProperty("keyPassword").toString()
            keyAlias = prop.getProperty("keyAlias").toString()
        }
    }

    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        applicationId = "cl.figonzal.lastquakechile"
        minSdk = 23
        targetSdk = 32
        versionCode = 38
        versionName = "1.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        with(gradleLocalProperties(rootDir)) {

            //GOOGLE MAPS API KEY
            buildConfigField("String", "MAPS_API_KEY", getProperty("MAPS_API_KEY"))

            //APPO DEAL KEY
            buildConfigField("String", "APPO_DEAL_KEY", getProperty("APPO_DEAL_KEY"))

            //META API KEYS
            buildConfigField("String", "FB_APP_ID", getProperty("FB_APP_ID"))
            buildConfigField("String", "FB_CLIENT_ID", getProperty("FB_CLIENT_ID"))

            //ADMOB MASTER KEY
            buildConfigField("String", "ADMOB_MASTER_KEY", getProperty("ADMOB_MASTER_KEY"))
        }

    }
    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "app_name", "LastQuakeChile-debug")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("lastquakechilesign")
            resValue("string", "app_name", "LastQuakeChile")
        }

    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
    flavorDimensions += listOf("version")
    productFlavors {
        create("beta") {
            dimension = "version"
            versionNameSuffix = "-beta"
        }
        create("dev") {
            dimension = "version"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "version"
        }
    }
}

dependencies {

    implementation(fileTree("libs") { include(listOf("*.jar")) })
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.fragment:fragment-ktx:1.5.1")
    implementation("androidx.activity:activity-ktx:1.5.1")

    implementation("com.google.android.material:material:1.6.1")//Material design
    implementation("androidx.core:core-splashscreen:1.0.0") //splash screen

    //GLIDE
    implementation("com.github.bumptech.glide:glide:4.13.2")

    //Life cycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    //Dependency injection KOIN
    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("io.insert-koin:koin-android:3.2.0")

    //Appodeal
    implementation("com.appodeal.ads:sdk:3.0.0.4")

    //Google Play
    implementation("com.google.android.play:app-update-ktx:2.0.0")

    //Google Maps
    // KTX for the Maps SDK for Android
    implementation("com.google.maps.android:maps-ktx:3.2.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    implementation("androidx.preference:preference-ktx:1.2.0") //preference

    //Room components
    implementation("androidx.room:room-ktx:2.4.3")
    implementation("androidx.room:room-runtime:2.4.3")
    kapt("androidx.room:room-compiler:2.4.3")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //SANDWICH
    implementation("com.github.skydoves:sandwich:1.2.7")

    //MOSHI
    implementation("com.squareup.moshi:moshi:1.13.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    //Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:30.3.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-dynamic-links")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    //TIMBER
    implementation("com.jakewharton.timber:timber:5.0.1")

    //junit
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core-ktx:1.4.0")
    testImplementation("io.insert-koin:koin-test:3.2.0")
    testImplementation("io.insert-koin:koin-test-junit4:3.2.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.test.ext:truth:1.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    // Instrumented Unit Tests
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("androidx.room:room-testing:2.4.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0") {
        exclude(module = "protobuf-lite")
    }
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
    androidTestImplementation("androidx.test.ext:truth:1.4.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("io.insert-koin:koin-test:3.2.0")
    androidTestImplementation("io.insert-koin:koin-test-junit4:3.2.0")

    //Debug dependencies
    debugImplementation("androidx.fragment:fragment-testing:1.5.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
}

sonarqube {
    properties {
        property("sonar.projectName", "LastQuakeChile")
        property("sonar.projectKey", "LastQuakeChile")
        property("sonar.test.inclusions", "**/*Test*/**")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main/java")
    }
}
