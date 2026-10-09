package com.godfather59.quransunnah.audio

// All reciters below are documented Hafs ‘an ‘Asim reciters with
// individually verified reachable streams. Never add a reciter here
// without verifying BOTH their riwaya and stream reachability.
// Ported from kReciters in lib/data/services/audio_service.dart.

data class Reciter(
    /** CDN edition id. */
    val identifier: String,
    val nameEn: String,
    val nameAr: String,
    /** Must match RiwayaId.storageKey. */
    val riwayaKey: String,
) {
    fun fileUrl(globalAyah: Int): String =
        "https://cdn.islamic.network/quran/audio/128/$identifier/$globalAyah.mp3"
}

val reciters: List<Reciter> = listOf(
    Reciter(
        identifier = "ar.alafasy",
        nameEn = "Mishary Alafasy",
        nameAr = "مشاري العفاسي",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.husary",
        nameEn = "Mahmoud Al-Husary",
        nameAr = "محمود خليل الحصري",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.mahermuaiqly",
        nameEn = "Maher Al-Muaiqly",
        nameAr = "ماهر المعيقلي",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.ahmedajamy",
        nameEn = "Ahmed Ibn Ali Al-Ajamy",
        nameAr = "أحمد بن علي العجمي",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.shaatree",
        nameEn = "Abu Bakr Ash-Shatree",
        nameAr = "أبو بكر الشاطري",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.hudhaify",
        nameEn = "Ali Al-Hudhaify",
        nameAr = "علي الحذيفي",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.muhammadayyoub",
        nameEn = "Muhammad Ayyoub",
        nameAr = "محمد أيوب",
        riwayaKey = "hafs-an-asim",
    ),
    Reciter(
        identifier = "ar.muhammadjibreel",
        nameEn = "Muhammad Jibreel",
        nameAr = "محمد جبريل",
        riwayaKey = "hafs-an-asim",
    ),
)

fun reciterById(identifier: String): Reciter? =
    reciters.firstOrNull { it.identifier == identifier }

fun recitersForRiwaya(riwayaKey: String): List<Reciter> =
    reciters.filter { it.riwayaKey == riwayaKey }
