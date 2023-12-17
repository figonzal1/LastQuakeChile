// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {

    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false

    //FIREBASE CRASH ANALYTICS
    id("com.google.gms.google-services") version "4.3.15" apply false

    //Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "2.9.5" apply false

    // Performance Monitoring plugin
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false

    //Google maps secrets
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false

    //Sonaqube
    id("org.sonarqube") version "4.0.0.2929"

    //KSP
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
}