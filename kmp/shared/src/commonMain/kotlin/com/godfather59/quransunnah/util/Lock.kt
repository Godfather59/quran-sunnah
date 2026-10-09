package com.godfather59.quransunnah.util

// Portable mutex for shared mutable state (SearchEngine docs,
// DownloadQueue). Kotlin's `synchronized` is JVM-only and does not resolve
// on Kotlin/Native, so platform actuals provide it.
expect class CommonLock() {
    inline fun <T> withLock(action: () -> T): T
}
