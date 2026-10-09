package com.godfather59.quransunnah.content

import com.godfather59.quransunnah.data.parseJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

// Optional content packages (translations, tafsir, extra collections).
// Ported from the content_packages.json model. Core datasets ship with
// the app; these describe downloadable extras with SHA-256 pins.

data class ContentPackageFile(
    val path: String,
    val sizeBytes: Long,
    val sha256: String,
)

data class ContentPackage(
    val id: String, // e.g. "quran:en-sahih"
    val titleAr: String,
    val titleEn: String,
    val version: String? = null,
    val source: String,
    val licenseStatus: String,
    val sizeBytes: Long,
    val sha256: String,
    val files: List<ContentPackageFile> = emptyList(),
)

data class ContentManifest(
    val schemaVersion: Int,
    val sourceRevision: String,
    val sourceBaseUrl: String,
    val packages: List<ContentPackage> = emptyList(),
)

fun parseContentManifest(raw: String): ContentManifest {
    val root = parseJsonObject(raw)
    return ContentManifest(
        schemaVersion = root["schemaVersion"]!!.jsonPrimitive.int,
        sourceRevision = root["sourceRevision"]!!.jsonPrimitive.content,
        sourceBaseUrl = root["sourceBaseUrl"]!!.jsonPrimitive.content,
        packages = root["packages"]!!.jsonArray.map { element ->
            val p = element.jsonObject
            ContentPackage(
                id = p["id"]!!.jsonPrimitive.content,
                titleAr = p["titleAr"]!!.jsonPrimitive.content,
                titleEn = p["titleEn"]!!.jsonPrimitive.content,
                version = p["version"]?.jsonPrimitive?.content,
                source = p["source"]!!.jsonPrimitive.content,
                licenseStatus = p["licenseStatus"]!!.jsonPrimitive.content,
                sizeBytes = p["sizeBytes"]!!.jsonPrimitive.long,
                sha256 = p["sha256"]!!.jsonPrimitive.content,
                files = p["files"]?.jsonArray?.map { fileElement ->
                    val f = fileElement.jsonObject
                    ContentPackageFile(
                        path = f["path"]!!.jsonPrimitive.content,
                        sizeBytes = f["sizeBytes"]!!.jsonPrimitive.long,
                        sha256 = f["sha256"]!!.jsonPrimitive.content,
                    )
                } ?: emptyList(),
            )
        },
    )
}
