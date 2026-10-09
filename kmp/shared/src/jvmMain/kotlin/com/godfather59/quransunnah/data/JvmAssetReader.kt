package com.godfather59.quransunnah.data

import java.io.File

/** Reads assets from a root directory (desktop/tests). */
class JvmFileAssetReader(private val root: File) : AssetReader {
    override fun readBytes(path: String): ByteArray? {
        return try {
            val file = File(root, path)
            if (!file.isFile) null else file.readBytes()
        } catch (_: Exception) {
            null
        }
    }
}
