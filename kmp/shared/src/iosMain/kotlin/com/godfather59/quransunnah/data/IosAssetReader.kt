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
 * iOS bundle reader. The Xcode app includes the repository assets as an
 * `assets` folder reference, so paths are resolved relative to that folder.
 * Missing entries return null so the UI renders "Content unavailable"
 * instead of crashing. Never throws.
 */
class IosBundleAssetReader : AssetReader {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun readBytes(path: String): ByteArray? {
        return try {
            val resource = path.removePrefix("/")
            val extIndex = resource.lastIndexOf('.')
            val name = if (extIndex < 0) resource else resource.substring(0, extIndex)
            val ext = if (extIndex < 0) null else resource.substring(extIndex + 1)
            val directory = name.substringBeforeLast('/', "")
            val fileName = name.substringAfterLast('/')
            val bundlePath = NSBundle.mainBundle.pathForResource(fileName, ext, directory.ifEmpty { null })
                ?: NSBundle.mainBundle.pathForResource(name, ext)
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
