// Root project — all configuration lives in :shared and :androidApp.
// Plugin markers are applied here with `apply false` so the Kotlin/Native
// bundle build service loads once in a shared classloader. Without this,
// realizing any Kotlin/Native task (e.g. :shared:compileKotlinIosSimulatorArm64,
// needed for the Phase 6 iOS job) fails with a cross-project
// KotlinNativeBundleBuildService classloader conflict.
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
}
