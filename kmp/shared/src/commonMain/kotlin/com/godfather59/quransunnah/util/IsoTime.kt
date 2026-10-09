package com.godfather59.quransunnah.util

// Minimal UTC ISO-8601 for the v1 library backup format
// ("2023-11-14T22:13:20Z"). Pure arithmetic (Howard Hinnant's civil
// algorithms) so common code needs no datetime dependency.
// Parsing is lenient like Dart DateTime.tryParse: garbage -> null.

private fun daysFromCivil(y: Int, m: Int, d: Int): Long {
    val yy = if (m <= 2) y - 1 else y
    val era = (if (yy >= 0) yy else yy - 399) / 400
    val yoe = (yy - era * 400).toLong()
    val mp = ((m + 9) % 12).toLong()
    val doy = (153 * mp + 2) / 5 + (d - 1)
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

private fun civilFromDays(z: Long): Triple<Int, Int, Int> {
    val zz = z + 719468
    val era = (if (zz >= 0) zz else zz - 146096) / 146097
    val doe = (zz - era * 146097).toInt()
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = (yoe + era * 400).toInt()
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    return Triple(if (m <= 2) y + 1 else y, m, d)
}

private fun two(n: Int): String = if (n < 10) "0$n" else "$n"

/** Epoch seconds -> "YYYY-MM-DDTHH:MM:SSZ". */
fun epochSecondsToIsoUtc(seconds: Long): String {
    // Floor division (Kotlin % truncates toward zero; pre-1970 needs floor).
    val days = if (seconds >= 0) seconds / 86400 else (seconds - 86399) / 86400
    val secs = (seconds - days * 86400).toInt()
    val (y, m, d) = civilFromDays(days)
    return "${y.toString().padStart(4, '0')}-${two(m)}-${two(d)}T" +
        "${two(secs / 3600)}:${two((secs % 3600) / 60)}:${two(secs % 60)}Z"
}

/**
 * Parses "YYYY-MM-DD[THH:MM[:SS[.mmm]][Z|±hh:mm]]" (and date-only) to epoch
 * seconds. Timezone offsets are applied. Null on garbage (Dart tryParse parity).
 */
fun isoToEpochSeconds(raw: String): Long? {
    return try {
        parseIso(raw.trim())
    } catch (_: Exception) {
        null
    }
}

private fun parseIso(s: String): Long? {
    if (s.length < 10) return null
    val year = s.substring(0, 4).toIntOrNull() ?: return null
    if (s[4] != '-' || s[7] != '-') return null
    val month = s.substring(5, 7).toIntOrNull() ?: return null
    val day = s.substring(8, 10).toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31) return null
    var hour = 0
    var minute = 0
    var second = 0
    var offsetMinutes = 0
    if (s.length > 10) {
        if (s[10] != 'T' && s[10] != ' ') return null
        if (s.length < 16) return null
        if (s[13] != ':') return null
        hour = s.substring(11, 13).toIntOrNull() ?: return null
        minute = s.substring(14, 16).toIntOrNull() ?: return null
        var i = 16
        if (i < s.length && s[i] == ':') {
            if (s.length < i + 3) return null
            second = s.substring(i + 1, i + 3).toIntOrNull() ?: return null
            i += 3
            if (i < s.length && s[i] == '.') {
                i++
                while (i < s.length && s[i] in '0'..'9') i++
            }
        }
        if (i < s.length) {
            when (s[i]) {
                'Z' -> i++
                '+', '-' -> {
                    val sign = if (s[i] == '+') 1 else -1
                    // Accept +hh:mm, +hhmm, +hh.
                    val digits = s.substring(i + 1).filter { it in '0'..'9' }
                    if (digits.length < 2) return null
                    val oh = digits.substring(0, 2).toIntOrNull() ?: return null
                    val om = if (digits.length >= 4) {
                        digits.substring(2, 4).toIntOrNull() ?: return null
                    } else {
                        0
                    }
                    if (oh !in 0..23 || om !in 0..59) return null
                    offsetMinutes = sign * (oh * 60 + om)
                    i = s.length
                }
                else -> return null
            }
        }
        if (i != s.length) return null
        if (hour !in 0..23 || minute !in 0..59 || second !in 0..60) return null
    }
    val days = daysFromCivil(year, month, day)
    return days * 86400 + hour * 3600 + minute * 60 + second - offsetMinutes * 60
}
