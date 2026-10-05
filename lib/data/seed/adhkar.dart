// Bundled adhkar (offline). Short, well-known wordings with sources.
// Full Morning/Evening collections plug in via the same shape.

class Dhikr {
  const Dhikr({
    required this.id,
    required this.textAr,
    required this.transliteration,
    required this.translationEn,
    required this.translationFr,
    required this.target,
    required this.source,
  });

  final String id;
  final String textAr;
  final String transliteration;
  final String translationEn;
  final String translationFr;
  final int target;
  final String source;
}

const List<Dhikr> kAdhkar = [
  Dhikr(
    id: 'tasbih',
    textAr: 'سُبْحَانَ اللَّهِ',
    transliteration: 'SubhanAllah',
    translationEn: 'Glory be to Allah',
    translationFr: 'Gloire à Allah',
    target: 33,
    source: 'Muslim',
  ),
  Dhikr(
    id: 'hamd',
    textAr: 'الْحَمْدُ لِلَّهِ',
    transliteration: 'Alhamdulillah',
    translationEn: 'Praise be to Allah',
    translationFr: 'Louange à Allah',
    target: 33,
    source: 'Muslim',
  ),
  Dhikr(
    id: 'takbir',
    textAr: 'اللَّهُ أَكْبَرُ',
    transliteration: 'Allahu Akbar',
    translationEn: 'Allah is Greatest',
    translationFr: 'Allah est le Plus Grand',
    target: 34,
    source: 'Muslim',
  ),
  Dhikr(
    id: 'tahlil',
    textAr: 'لَا إِلَٰهَ إِلَّا اللَّهُ',
    transliteration: 'La ilaha illa Allah',
    translationEn: 'None is worthy of worship but Allah',
    translationFr: 'Nul n’est digne d’adoration sauf Allah',
    target: 100,
    source: 'Bukhari & Muslim',
  ),
  Dhikr(
    id: 'hawqala',
    textAr: 'لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ',
    transliteration: 'La hawla wa la quwwata illa billah',
    translationEn: 'There is no power except with Allah',
    translationFr: 'Il n’y a de force qu’en Allah',
    target: 10,
    source: 'Bukhari & Muslim',
  ),
  Dhikr(
    id: 'istighfar',
    textAr: 'أَسْتَغْفِرُ اللَّهَ',
    transliteration: 'Astaghfirullah',
    translationEn: 'I seek Allah’s forgiveness',
    translationFr: 'Je demande pardon à Allah',
    target: 100,
    source: 'Muslim',
  ),
  Dhikr(
    id: 'salawat',
    textAr: 'اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ',
    transliteration: 'Allahumma salli ala Muhammad',
    translationEn: 'O Allah, bless Muhammad',
    translationFr: 'Ô Allah, bénis Muhammad',
    target: 10,
    source: 'Tirmidhi',
  ),
  Dhikr(
    id: 'ayatul-kursi',
    textAr:
        'اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ',
    transliteration: 'Ayat al-Kursi (opening)',
    translationEn: 'Allah — none worthy but He, the Ever-Living (2:255 opening)',
    translationFr: 'Allah — nul digne sauf Lui, le Vivant (début 2:255)',
    target: 1,
    source: 'Quran 2:255 · Bukhari (virtue)',
  ),
];
