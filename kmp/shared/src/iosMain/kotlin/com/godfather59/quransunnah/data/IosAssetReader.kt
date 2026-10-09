package com.godfather59.quransunnah.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

/**
 * iOS bundle reader (Phase 6 wiring, compilable on any host).
 * Bundle resources must be added in Xcode (Phase 6 macOS step); until then
 * missing entries return null so the UI renders "Content unavailable"
 * instead of crashing. Never throws.
 */
class IosBundleAssetReader : AssetReader {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun readBytes(path: String): ByteArray? {
        return try {
            // "assets/quran/..." -> bundle resource "quran/..." (directories
            // preserved as folder references in Xcode).
            val resource = path.removePrefix("assets/").removePrefix("/")
            val extIndex = resource.lastIndexOf('.')
            val name = if (extIndex < 0) resource else resource.substring(0, extIndex)
            val ext = if (extIndex < 0) null else resource.substring(extIndex + 1)
            val bundlePath = NSBundle.mainBundle.pathForResource(name, ext)
                ?: NSBundle.mainBundle.pathForResource(resource, null)
                ?: return null
            val data: NSData = NSData.dataWithContentsOfFile(bundlePath) ?: return null
            val size = data.length.toInt()
            if (size <= 0) return ByteArray(0)
            ByteArray(size).apply {
                usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}

/** Backwards-compatible alias: unavailable assets read as null. */
object MissingAssetReader : AssetReader by IosBundleAssetReader()
