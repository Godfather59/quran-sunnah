package com.godfather59.quransunnah.integrity

import com.godfather59.quransunnah.crypto.gitBlobSha1Hex
import com.godfather59.quransunnah.data.AssetReader
import com.godfather59.quransunnah.data.parseJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Integrity gate. Ported from the asset_integrity test + manifest logic:
// assets/integrity_manifest.json pins exact bytes of every religious source
// file with git-blob-sha1. Fail-closed: any mismatch means
// "Content unavailable for this source", never fallback text.

data class IntegrityManifest(
    val algorithm: String,
    /** Asset path -> expected hex digest. */
    val files: Map<String, String>,
)

fun parseIntegrityManifest(raw: String): IntegrityManifest {
    val root = parseJsonObject(raw)
    val algorithm = root["algorithm"]!!.jsonPrimitive.content
    val files = root["files"]!!.jsonObject.mapValues { (_, v) ->
        v.jsonPrimitive.content
    }
    return IntegrityManifest(algorithm = algorithm, files = files)
}

/**
 * Returns the list of asset paths that are missing or whose bytes do not
 * match the manifest. Empty list = all verified. Only "git-blob-sha1" is
 * supported; anything else fails every file (fail-closed).
 */
fun verifyIntegrity(
    manifest: IntegrityManifest,
    reader: AssetReader,
): List<String> {
    if (manifest.algorithm != "git-blob-sha1") {
        return manifest.files.keys.toList()
    }
    val bad = mutableListOf<String>()
    for ((path, expected) in manifest.files) {
        val bytes = reader.readBytes(path)
        if (bytes == null || gitBlobSha1Hex(bytes) != expected.lowercase()) {
            bad.add(path)
        }
    }
    return bad
}
