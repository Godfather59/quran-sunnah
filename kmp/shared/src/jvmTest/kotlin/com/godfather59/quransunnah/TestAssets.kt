package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.AssetReader
import com.godfather59.quransunnah.data.JvmFileAssetReader
import java.io.File

/**
 * Single-copy repo assets. Resolves the repo root by walking up from the
 * test working directory (Gradle does not guarantee it), so paths like
 * "assets/quran/..." (as pinned in integrity_manifest.json) always work.
 */
object TestAssets {
    val repoRoot: File by lazy {
        var dir = File(System.getProperty("user.dir"))
        repeat(8) {
            if (File(dir, "assets/integrity_manifest.json").isFile) {
                return@lazy dir
            }
            dir = dir.parentFile ?: return@lazy dir
        }
        dir
    }

    val reader: AssetReader by lazy { JvmFileAssetReader(repoRoot) }
}
