// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {

    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false

    //FIREBASE CRASH ANALYTICS
    alias(libs.plugins.com.google.gms.google.services) apply false

    //Crashlytics Gradle plugin
    alias(libs.plugins.com.google.firebase.crashlytics) apply false

    // Performance Monitoring plugin
    alias(libs.plugins.com.google.firebase.firebase.perf) apply false

    //KSP
    alias(libs.plugins.com.google.devtools.ksp) apply false

    //Google maps secrets
    alias(libs.plugins.com.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false

    //Sonaqube
    alias(libs.plugins.org.sonarqube)

    //Version catalog updater
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

versionCatalogUpdate {
    // These options will be set as default for all version catalogs
    sortByKey.set(false)
}


// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    resolutionStrategy {
        componentSelection {
            all {
                if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                    reject("Release candidate")
                }
            }
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}