import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
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

    compileSdk = 34

    defaultConfig {
        applicationId = "cl.figonzal.lastquakechile"
        minSdk = 23
        targetSdk = 34
        versionCode = 51
        versionName = "1.7.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        with(gradleLocalProperties(rootDir)) {

            //GOOGLE MAPS API KEY
            buildConfigField("String", "MAPS_API_KEY", getProperty("MAPS_API_KEY"))

            //APPO DEAL KEY
            //buildConfigField("String", "APPO_DEAL_KEY", getProperty("APPO_DEAL_KEY"))

            //META API KEYS
            //buildConfigField("String", "FB_APP_ID", getProperty("FB_APP_ID"))
            //buildConfigField("String", "FB_CLIENT_ID", getProperty("FB_CLIENT_ID"))

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

            resValue("string", "ADMOB_ID_BANNER", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "ADMOB_ID_NATIVE_FRAGMENT", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "ADMOB_ID_NATIVE_DETAILS", "ca-app-pub-3940256099942544/2247696110")
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

            resValue("string", "ADMOB_ID_BANNER", "ca-app-pub-6355378855577476/5493893896")
            resValue("string", "ADMOB_ID_NATIVE_FRAGMENT", "ca-app-pub-6355378855577476/2611250693")
            resValue("string", "ADMOB_ID_NATIVE_DETAILS", "ca-app-pub-6355378855577476/2723765487")
        }

    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    flavorDimensions += listOf("version")
    productFlavors {
        create("dev") {
            dimension = "version"
            versionNameSuffix = "-dev"
        }
        create("beta") {
            dimension = "version"
            versionNameSuffix = "-beta"
        }
        create("prod") {
            dimension = "version"
        }
    }
    namespace = "cl.figonzal.lastquakechile"
}

dependencies {

    implementation(fileTree("libs") { include(listOf("*.jar")) })
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")

    implementation("com.google.android.material:material:1.11.0")//Material design
    implementation("androidx.core:core-splashscreen:1.0.1") //splash screen

    //GLIDE
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    //Life cycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    //Dependency injection KOIN
    val koinVersion = "3.5.3"
    implementation("io.insert-koin:koin-android:$koinVersion")

    //Appodeal
    //implementation("com.appodeal.ads:sdk:3.0.0.4")

    //Google Play
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation("com.google.android.play:review-ktx:2.0.1")
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    //Google Maps
    // KTX for the Maps SDK for Android
    implementation("com.google.maps.android:maps-ktx:3.4.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("androidx.preference:preference-ktx:1.2.1") //preference

    //Room components
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    //SANDWICH
    implementation("com.github.skydoves:sandwich:2.0.5")
    implementation("com.github.skydoves:sandwich-retrofit:2.0.5")

    //MOSHI
    implementation("com.squareup.moshi:moshi:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    //Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    //TIMBER
    implementation("com.jakewharton.timber:timber:5.0.1")

    //junit
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit4:$koinVersion")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.test.ext:truth:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("app.cash.turbine:turbine:0.13.0")

    // Instrumented Unit Tests
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1") {
        exclude(module = "protobuf-lite")
    }
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.ext:truth:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("io.insert-koin:koin-test:$koinVersion")
    androidTestImplementation("io.insert-koin:koin-test-junit4:$koinVersion")

    //Debug dependencies
    debugImplementation("androidx.fragment:fragment-testing-manifest:1.6.2")
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.11")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
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
