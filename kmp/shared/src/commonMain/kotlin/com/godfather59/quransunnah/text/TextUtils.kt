package com.godfather59.quransunnah.text

// Arabic search normalization: strip tashkeel/tatweel, unify alef forms.
// Ported 1:1 from lib/core/utils/text_utils.dart (incl. edge behavior:
// U+0670 superscript alef is NOT stripped; result is trimmed).
// ONLY applied to the search index — never to displayed text.
//
// NOTE: character classes are built from code points (not regex escapes)
// so this file stays pure ASCII and reviewable. Semantics match Dart.

private fun codeRange(first: Int, last: Int): Set<Char> =
    (first..last).map { it.toChar() }.toSet()

// Dart: [\u0610-\u061A\u064B-\u065F\u06D6-\u06ED]
private val tashkeel: Set<Char> =
    codeRange(0x0610, 0x061A) + codeRange(0x064B, 0x065F) + codeRange(0x06D6, 0x06ED)

// Dart: \u0640 (tatweel)
private const val TATWEEL_CODE = 0x0640

// Dart: [أإآٱ]->ا, ة->ه, ى->ي, ؤ->و, ئ->ي
private val alefUnify: Map<Char, Char> = mapOf(
    0x0623.toChar() to 0x0627.toChar(),
    0x0625.toChar() to 0x0627.toChar(),
    0x0622.toChar() to 0x0627.toChar(),
    0x0671.toChar() to 0x0627.toChar(),
    0x0629.toChar() to 0x0647.toChar(),
    0x0649.toChar() to 0x064A.toChar(),
    0x0624.toChar() to 0x0648.toChar(),
    0x0626.toChar() to 0x064A.toChar(),
)

fun normalizeArabic(input: String): String {
    val unified = input
        .filter { c -> c !in tashkeel && c.code != TATWEEL_CODE }
        .map { c -> alefUnify[c] ?: c }
        .joinToString("")
    return unified.trim()
}

/** Latin/fuzzy helper: lowercase + trim. */
fun normalizeLatin(input: String): String = input.lowercase().trim()

// Dart: '٠١٢٣٤٥٦٧٨٩'
private val arabicIndicDigits = charArrayOf(
    0x0660.toChar(), 0x0661.toChar(), 0x0662.toChar(), 0x0663.toChar(),
    0x0664.toChar(), 0x0665.toChar(), 0x0666.toChar(), 0x0667.toChar(),
    0x0668.toChar(), 0x0669.toChar(),
)

/** Convert digits for ayah markers. */
fun toArabicIndic(n: Int): String = n.toString().map { c ->
    val d = c.digitToIntOrNull()
    if (d == null) c.toString() else arabicIndicDigits[d].toString()
}.joinToString("")

private fun isAsciiDigit(c: Char): Boolean = c in '0'..'9'

private fun isOneToThreeDigits(s: String): Boolean =
    s.length in 1..3 && s.all(::isAsciiDigit)

/**
 * Parses "2:255", "/quran/2/255", "quran://2/255" -> (surah, ayah).
 * Manual port of parseQuranRef in lib/app.dart (no regex):
 * - Dart ^(\d{1,3})\s*:\s*(\d{1,3})$ : whole string, 1-3 ASCII digits each
 *   side, whitespace tolerated around the colon.
 * - Dart quran[:/]+(\d{1,3})/(\d{1,3}) : unanchored find; first group is the
 *   longest 1-3 digit run that is followed by '/', second group is the
 *   longest 1-3 digit run after that slash (trailing text ignored).
 */
fun parseQuranRef(raw: String): Pair<Int, Int>? {
    val s = raw.trim()
    parseColonRef(s)?.let { return it }
    // Dart find() semantics: try every "quran" occurrence in order.
    var from = 0
    while (true) {
        val idx = s.indexOf("quran", from)
        if (idx < 0) return null
        parseUriNumbers(s.substring(idx + 5))?.let { return it }
        from = idx + 1
    }
}

private fun parseColonRef(s: String): Pair<Int, Int>? {
    val colon = s.indexOf(':')
    if (colon < 0) return null
    val left = s.substring(0, colon).trim()
    val right = s.substring(colon + 1).trim()
    if (!isOneToThreeDigits(left) || !isOneToThreeDigits(right)) return null
    return Pair(left.toInt(), right.toInt())
}

private fun parseUriNumbers(s: String): Pair<Int, Int>? {
    var i = 0
    while (i < s.length && (s[i] == ':' || s[i] == '/')) i++
    // Longest 1-3 digit run followed by '/' (regex backtracking order).
    for (len in 3 downTo 1) {
        if (i + len < s.length &&
            s.substring(i, i + len).all(::isAsciiDigit) &&
            s[i + len] == '/'
        ) {
            val rest = s.substring(i + len + 1)
            val digits = rest.takeWhile(::isAsciiDigit).take(3)
            if (digits.isNotEmpty()) {
                return Pair(
                    s.substring(i, i + len).toInt(),
                    digits.toInt(),
                )
            }
            return null
        }
    }
    return null
}
