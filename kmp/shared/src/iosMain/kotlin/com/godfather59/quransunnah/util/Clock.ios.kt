package com.godfather59.quransunnah.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

// Verified: platform.Foundation compiles on Windows hosts (Kotlin/Native
// ships the iOS platform libs; only linking/signing needs macOS).
actual fun currentEpochSeconds(): Long =
    NSDate().timeIntervalSince1970.toLong()
