# Quran & Sunnah — KMP app (post-cutover)

Shared Kotlin logic + native UI (Android Jetpack Compose, iOS SwiftUI).
Full plan + history: `../TODO_KMP.md`. Last Flutter commit: tag `flutter-final`.

```
kmp/
  settings.gradle.kts / build.gradle.kts / gradle.properties
  gradle/libs.versions.toml   # kotlin 2.4.0, agp 9.1.0 (pinned, proven here)
  shared/                     # commonMain (domain, seed, loaders, integrity,
                              #   search, prayer, library, prefs, audio logic)
                              # + androidMain (Media3, downloader, GPS, compass,
                              #   share, backup) + jvmMain (tests)
                              # + iosMain (NSDate clock, NSBundle reader,
                              #   native driver, fail-soft device)
  androidApp/                 # Compose UI (home, reader, mushaf, sunnah,
                              #   search, library, downloads, prayer, settings)
  iosApp/                     # SwiftUI shell sources (no .xcodeproj yet — see below)
```

## Prereqs

- JDK 17+ (`java -version`), Android SDK with `sdk.dir` in `kmp/local.properties`
  (git-ignored, never commit it):
  `sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`
- Gradle wrapper is committed (`gradlew`, `gradlew.bat`); first run downloads
  Gradle 9.3.1 automatically.

## Commands (run inside `kmp/`)

```sh
./gradlew :shared:jvmTest          # shared logic tests, 115 green (Windows-safe)
./gradlew :shared:compileKotlinIosX64 :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64
./gradlew :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
CI: `.github/workflows/kmp-ci.yml` (shared tests, Android unit tests + debug/release APK, iOS framework compile on macOS).

## Assets (single copy, no duplication)

`../assets/` (63 MB: Quran, Hadith, fonts, licenses, manifests) stays the
single source of truth at repo root. The shared module references it with
Gradle `resources.srcDir`; Android packaging also references the same directory
as app assets, so APK paths appear under `assets/...` without copying anything
into `kmp/`. Do NOT copy it into `kmp/`.

iOS bundle resources are added as folder references in Xcode (Phase 6);
`IosBundleAssetReader` resolves `assets/<path>` against them and returns
null (honest unavailable state) until wired.

## iOS (.xcodeproj, macOS only)

The shared framework compiles on any host (see commands above). The
`.xcodeproj` itself is created on macOS: new iOS App project using `iosApp/`
sources, then a Run Script build phase executing
`kmp/gradlew :shared:embedAndSignAppleFrameworkForXcode`
(or an XCFramework integration) to link the shared Kotlin framework.
`quran://` URL type + background-audio mode are already in
`iosApp/Info.plist`.
