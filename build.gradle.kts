// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false // Keep this if other modules might use Firebase, otherwise remove it too if no other modules depend on Firebase. For now, it's fine.

    // ADD THIS LINE for Navigation Safe Args
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false // IMPORTANT: Version must match navigation-fragment-ktx in app/build.gradle.kts
}