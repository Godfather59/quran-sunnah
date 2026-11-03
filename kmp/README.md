# Quran & Sunnah — KMP app (post-cutover)

Shared Kotlin logic + native UI (Android Jetpack Compose, iOS SwiftUI).
Full plan + history: `../TODO_KMP.md`. Last Flutter commit: tag `flutter-final`.

```
kmp/
  settings.gradle.kts / build.gradle.kts / gradle.properties
  gradle/libs.versions.toml   # kotlin 2.4.0, agp 9.1.0 (pinned, proven here)
  shared/                     # commonMain (domain, seed, loaders, integrity,
                              #   search, prayer, prefs, audio logic)
                              # + androidMain (Media3, downloader, GPS, compass,
                              #   share) + jvmMain (tests)
                              # + iosMain (NSDate clock, NSBundle reader,
                              #   native driver, fail-soft device)
  androidApp/                 # Compose UI (home, reader, mushaf, sunnah,
                              #   search, downloads, prayer, settings)
  iosApp/                     # SwiftUI Quran browser, reader, and search
  iosApp.xcodeproj/            # Xcode project and shared scheme
```

## Prereqs

- JDK 17+ (`java -version`), Android SDK with `sdk.dir` in `kmp/local.properties`
  (git-ignored, never commit it):
  `sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`
- Gradle wrapper is committed (`gradlew`, `gradlew.bat`); first run downloads
  Gradle 9.3.1 automatically.

## Commands (run inside `kmp/`)

```sh
./gradlew :shared:jvmTest          # shared logic tests (Windows-safe)
./gradlew :shared:compileKotlinIosX64 :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64
./gradlew :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
CI: `.github/workflows/kmp-ci.yml` (shared tests, Android unit tests, lint, offline UI flows, screenshot comparison, debug/release test APKs, and iOS compilation on macOS).

Android UI regression commands, screenshot review, and explicit baseline updates:
[scripts/VISUAL_REGRESSION.md](scripts/VISUAL_REGRESSION.md).

## Troubleshooting / build traps

These are non-obvious failures already hit and solved here. Keep this list
current instead of rediscovering them:

- **`android.newDsl=false` + `android.builtInKotlin=false`** (`gradle.properties`).
  AGP 9.1 defaults both to `true`, which breaks this layout: `com.android.library`
  applied on top of the Kotlin multiplatform plugin produces a
  `KotlinNativeBundleBuildService` **cross-project classloader** conflict when
  any iOS task is realized. Root `build.gradle.kts` therefore applies all
  plugin markers with `apply false`. Both flags are deprecated in AGP 9 and
  will be removed in 10.0 — migrating off them is open work.
- **Root plugin markers.** Without `apply false` in the root build file, iOS
  tasks fail with a classloader conflict rather than a compile error.
- **Media3 1.11.x / androidx.core 1.19.x need compileSdk 37** (not installed
  here), so versions are pinned at Media3 1.10.1 + core 1.18.0 with
  compileSdk 36. Bump deliberately; `targetSdk` stays 35.
- **Windows host.** iOS targets cannot be *run* (`iosX64Test` and
  `iosSimulatorArm64Test` are disabled: "simulator tests require macOS").
  `compileKotlinIos*` still works on any host.
- **`KotlinNativeBundleBuildService` classloader** is the root cause of the
  `apply false` requirement above — do not remove that wiring to "clean up".
- **FTS5 may be missing on the device** (`no such module: fts5` during
  `Schema.create`). Search schema creation is fail-soft by design; the shared
  `SearchEngine` falls back to in-memory search. JVM tests cover the full FTS
  round-trip because the JDK SQLite bundles the module.

## Assets (single copy, no duplication)

`../assets/` (63 MB: Quran, Hadith, fonts, licenses, manifests) stays the
single source of truth at repo root. The shared module references it with
Gradle `resources.srcDir`; Android packaging also references the same directory
as app assets, so APK paths appear under `assets/...` without copying anything
into `kmp/`. Do NOT copy it into `kmp/`.

iOS bundle resources must be added as a folder reference in Xcode;
`IosBundleAssetReader` resolves `assets/<path>` against that reference. The
SwiftUI Quran reader uses the verified Hafs/Uthmani and metadata files from
the same bundle.

## iOS (macOS only)

The shared framework compiles on any host (see commands above) with the module
name `QuranSunnahShared`. Open `iosApp.xcodeproj` on macOS to build and run the
SwiftUI Hafs/Uthmani surah browser and reader. The project includes
`iosApp/Info.plist`, a shared Xcode scheme, the localized string catalog, the repository-root
`assets/` folder reference, and a build phase that runs
`:shared:embedAndSignAppleFrameworkForXcode`. SwiftUI calls `IosQuranApi` from
the shared framework for its surah catalog, verified Hafs/Uthmani verses, and
Quran search. Prayer, Sunnah, audio, downloads, settings, and device-service
parity remain open. `quran://` URL type and background-audio mode are in
`iosApp/Info.plist`.
