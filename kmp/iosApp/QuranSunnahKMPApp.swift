import SwiftUI

// Native SwiftUI app entry point. Xcode builds and embeds the KMP framework;
// the initial reader still loads the verified bundle assets directly.
@main
struct QuranSunnahKMPApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
