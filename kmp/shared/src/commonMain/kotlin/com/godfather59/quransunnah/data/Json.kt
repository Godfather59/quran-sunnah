package com.godfather59.quransunnah.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

// Shared JSON entry point. Verified asset files may carry a UTF-8 BOM
// (e.g. hadith index/sections, tafsir); strip it before parsing.
// Unknown keys are ignored so additive upstream fields never break us.
private val json = Json { ignoreUnknownKeys = true }

internal fun parseJsonObject(raw: String): JsonObject {
    val clean = if (raw.isNotEmpty() && raw[0].code == 0xFEFF) {
        raw.substring(1)
    } else {
        raw
    }
    return json.parseToJsonElement(clean).jsonObject
}
