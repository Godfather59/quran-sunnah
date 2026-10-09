import SwiftUI

// Phase 6 shell. Shared Kotlin framework compiles on any host
// (:shared:compileKotlinIosX64/Arm64/SimulatorArm64 green); the .xcodeproj
// link + bundle resources still need macOS:
//  1. New iOS App project over iosApp/ sources.
//  2. Run Script phase: kmp/gradlew :shared:embedAndSignAppleFrameworkForXcode
//     (or XCFramework integration).
//  3. Add assets/ (Quran/Hadith/fonts/licenses) as folder references so
//     IosBundleAssetReader finds "quran/...", "hadith/...", "fonts/...".
//  4. quran:// URL type + audio background mode are in Info.plist.
// Deep links (quran://2/255) parse with shared parseQuranRef; prayer math,
// search, library, and download-queue logic all come from the framework.
struct ContentView: View {
    var body: some View {
        Text("Quran & Sunnah KMP")
            .font(.title)
    }
}
