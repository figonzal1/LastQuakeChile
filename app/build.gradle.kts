import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.com.google.devtools.ksp)
    id("kotlin-parcelize")
    alias(libs.plugins.com.google.gms.google.services)
    alias(libs.plugins.com.google.firebase.crashlytics)
    alias(libs.plugins.com.google.firebase.firebase.perf)

    alias(libs.plugins.com.google.android.libraries.mapsplatform.secrets.gradle.plugin)
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

        with(gradleLocalProperties(rootDir, providers)) {

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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    //Splash screen
    implementation(libs.androidx.core.splashscreen)

    //Coil
    implementation(libs.coil)

    //Life cycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    //Dependency injection KOIN
    implementation(libs.koin.android)

    //Appodeal
    //implementation("com.appodeal.ads:sdk:3.0.0.4")

    //Google Play
    implementation(libs.bundles.google.play)

    //Preference
    implementation(libs.androidx.preference.ktx)

    //Room components
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.logging.interceptor)

    // Moshi
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    //SANDWICH
    implementation(libs.sandwich)
    implementation(libs.sandwich.retrofit)

    //Firebase BOM
    implementation(platform(libs.com.google.firebase.firebase.bom))
    implementation(libs.bundles.firebase)

    //TIMBER
    implementation(libs.com.jakewharton.timber)

    //junit
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.app.cash.turbine)

    // Instrumented Unit Tests
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.espresso.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1") {
        exclude(module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.test.espresso.espresso.intents)
    androidTestImplementation(libs.androidx.test.ext.truth)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test.junit4)

    //Debug dependencies
    debugImplementation(libs.androidx.fragment.fragment.testing.manifest)
    debugImplementation(libs.androidx.fragment.fragment.testing)
    debugImplementation(libs.com.squareup.leakcanary.leakcanary.android)

    coreLibraryDesugaring(libs.com.android.tools.desugar.jdk.libs)
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
