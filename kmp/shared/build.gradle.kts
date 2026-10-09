plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

val repoAssetsDir = rootProject.file("../assets")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.serialization.json)
            implementation(libs.coroutines.core)
            implementation(libs.mpsettings)
        }
        commonMain {
            resources.srcDir(repoAssetsDir)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android)
            implementation(libs.media3.exoplayer)
            implementation(libs.androidx.core)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.mpsettings.test)
            implementation(libs.coroutines.core)
        }
    }

    sqldelight {
        databases {
            create("Library") {
                packageName.set("com.godfather59.quransunnah.db")
            }
        }
    }

    // NOTE: repo-root ../assets stay the single copy; Gradle references the
    // directory for shared resources and platform packaging instead of copying
    // it into kmp/.
}

android {
    namespace = "com.godfather59.quransunnah.shared"
    // 36 = newest installed platform here; targetSdk stays 35 (no behavior change).
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            assets.srcDir(repoAssetsDir)
        }
    }
}
