# Quran & Sunnah — KMP scaffold (Phase 0)

Shared Kotlin logic + native shells. Full plan: `../TODO_KMP.md`.

```
kmp/
  settings.gradle.kts / build.gradle.kts / gradle.properties
  gradle/libs.versions.toml   # kotlin 2.4.0, agp 9.1.0 (pinned, proven here)
  shared/                     # commonMain/androidMain/jvmMain/iosMain + commonTest
  androidApp/                 # framework-Activity shell (Compose lands in Phase 5)
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
./gradlew :shared:jvmTest          # shared logic tests (Windows-safe)
./gradlew :shared:compileKotlinJvm # common code check
./gradlew :androidApp:assembleDebug
```

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

## Assets (single copy, no duplication)

`../assets/` (63 MB: Quran, Hadith, fonts, licenses, manifests) stays the
single source of truth at repo root. The shared module references it with
Gradle `resources.srcDir`; Android packaging also references the same directory
as app assets, so APK paths appear under `assets/...` without copying anything
into `kmp/`. Do NOT copy it into `kmp/`.

iOS bundle resources are wired in Phase 6 when the Xcode project exists.

## iOS (.xcodeproj, macOS only)

The `.xcodeproj` cannot be built on Windows, so it is created in Phase 6 on
macOS: new iOS App project using `iosApp/` sources, then a Run Script build
phase executing `kmp/gradlew :shared:embedAndSignAppleFrameworkForXcode`
(or an XCFramework integration) to link the shared Kotlin framework.
`quran://` URL type + background-audio mode are also added in Phase 6.
