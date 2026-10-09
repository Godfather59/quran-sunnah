package com.godfather59.quransunnah.device

// Platform device capabilities. Android implementations live in
// androidMain (this phase); iOS ones land in Phase 6 on macOS.
// Every call fails soft (null/false) — callers always keep a manual path.

// GPS with graceful manual fallback (mirrors requestGpsLocation).
data class GpsFix(
    val latitude: Double,
    val longitude: Double,
    /** Display label, e.g. "GPS 33.57, -7.59". */
    val city: String,
)

interface Locator {
    /** Null when services off / denied / unavailable / timed out. */
    suspend fun currentFix(timeoutMs: Long = 15_000L): GpsFix?
}

// Live Qibla compass (sensor heading, degrees from true north).
interface Compass {
    fun start(listener: (degreesTrueNorth: Float) -> Unit)
    fun stop()
}

interface Sharer {
    fun shareText(text: String, subject: String? = null)

    /** Shares a local file (exported backup, audio). */
    fun shareFile(path: String, mimeType: String)
}

interface Clipboard {
    fun copy(text: String)
}

interface BackupFilePicker {
    /** Returns picked backup file bytes, or null when cancelled. */
    suspend fun pickJsonBytes(): ByteArray?
}
