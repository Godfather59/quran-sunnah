plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Bundled source terms are not yet cleared for every dataset. Keep ordinary
// release packaging fail-closed until the audit is updated. CI may build a
// clearly non-distributable, temporary test APK with -PtestArtifact=true.
val testArtifact = providers.gradleProperty("testArtifact")
    .map(String::toBoolean)
    .getOrElse(false)
val verifyContentDistribution by tasks.registering {
    group = "verification"
    description = "Prevents release packaging while bundled content rights remain unresolved."
    doLast {
        if (!testArtifact) {
            throw GradleException(
                "Release packaging is blocked: see docs/DATA_LICENSES.md. " +
                    "Use -PtestArtifact=true only for a temporary, non-distributable CI test APK.",
            )
        }
        logger.lifecycle("Building a test artifact; this APK is not approved for distribution.")
    }
}

tasks.configureEach {
    if (name == "preReleaseBuild") {
        dependsOn(verifyContentDistribution)
    }
}

android {
    namespace = "com.godfather59.quransunnah"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.godfather59.quransunnah"
        minSdk = 24
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 1
        versionName = "1.0.0-kmp0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.text)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.compose.ui.test.manifest)
}
