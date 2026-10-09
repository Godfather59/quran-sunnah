package com.godfather59.quransunnah.data

import android.content.Context

/**
 * Reads assets packaged with the app. Initialized from the host
 * Application/Activity in Phase 5; until then it is never constructed.
 */
class AndroidAssetReader(context: Context) : AssetReader {
    private val assets = context.applicationContext.assets

    override fun readBytes(path: String): ByteArray? {
        return try {
            assets.open(path.removePrefix("assets/")).use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }
}
