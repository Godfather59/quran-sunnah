import '../../data/models/quran.dart';

/// Extensible catalog. Adding a riwaya = appending here + shipping its
/// verified dataset file. No UI change required.
const List<RiwayaInfo> kRiwayaCatalog = [
  RiwayaInfo(
    id: RiwayaId.hafsAsim,
    qiraaAr: 'عاصم', qiraaEn: 'ʿĀṣim', qiraaFr: 'ʿÂsim',
    riwayaAr: 'حفص عن عاصم', riwayaEn: 'Ḥafṣ ʿan ʿĀṣim', riwayaFr: 'Ḥafṣ ʿan ʿĀṣim',
    datasetVersion: '1.1 (Tanzil, Feb 2021)',
    source: 'Tanzil Project — https://tanzil.net (bundled)',
    isAvailableOfflineSeed: true,
  ),
  RiwayaInfo(
    id: RiwayaId.shubahAsim,
    qiraaAr: 'عاصم', qiraaEn: 'ʿĀṣim', qiraaFr: 'ʿÂsim',
    riwayaAr: 'شعبة عن عاصم', riwayaEn: 'Shuʿbah ʿan ʿĀṣim', riwayaFr: 'Shuʿbah ʿan ʿĀṣim',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.warshNafi,
    qiraaAr: 'نافع', qiraaEn: 'Nāfiʿ', qiraaFr: 'Nāfiʿ',
    riwayaAr: 'ورش عن نافع', riwayaEn: 'Warsh ʿan Nāfiʿ', riwayaFr: 'Warsh ʿan Nāfiʿ',
    datasetVersion: 'quran-api@1 v8 (QuranComplex)',
    source: 'quran-api ara-quranwarsh — bundled',
    isAvailableOfflineSeed: true,
  ),
  RiwayaInfo(
    id: RiwayaId.qalunNafi,
    qiraaAr: 'نافع', qiraaEn: 'Nāfiʿ', qiraaFr: 'Nāfiʿ',
    riwayaAr: 'قالون عن نافع', riwayaEn: 'Qālūn ʿan Nāfiʿ', riwayaFr: 'Qālūn ʿan Nāfiʿ',
    datasetVersion: 'quran-api@1 v8 (QuranComplex)',
    source: 'quran-api ara-quranqaloon — bundled',
    isAvailableOfflineSeed: true,
  ),
  RiwayaInfo(
    id: RiwayaId.bazziIbnKathir,
    qiraaAr: 'ابن كثير', qiraaEn: 'Ibn Kathīr', qiraaFr: 'Ibn Kathīr',
    riwayaAr: 'البزي عن ابن كثير', riwayaEn: 'Al-Bazzī ʿan Ibn Kathīr', riwayaFr: 'Al-Bazzī ʿan Ibn Kathīr',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.qunbulIbnKathir,
    qiraaAr: 'ابن كثير', qiraaEn: 'Ibn Kathīr', qiraaFr: 'Ibn Kathīr',
    riwayaAr: 'قنبل عن ابن كثير', riwayaEn: 'Qunbul ʿan Ibn Kathīr', riwayaFr: 'Qunbul ʿan Ibn Kathīr',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.duriAbiAmr,
    qiraaAr: 'أبو عمرو', qiraaEn: 'Abū ʿAmr', qiraaFr: 'Abū ʿAmr',
    riwayaAr: 'الدوري عن أبي عمرو', riwayaEn: 'Al-Dūrī ʿan Abī ʿAmr', riwayaFr: 'Al-Dūrī ʿan Abī ʿAmr',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.susiAbiAmr,
    qiraaAr: 'أبو عمرو', qiraaEn: 'Abū ʿAmr', qiraaFr: 'Abū ʿAmr',
    riwayaAr: 'السوسي عن أبي عمرو', riwayaEn: 'Al-Sūsī ʿan Abī ʿAmr', riwayaFr: 'Al-Sūsī ʿan Abī ʿAmr',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.hishamIbnAmir,
    qiraaAr: 'ابن عامر', qiraaEn: 'Ibn ʿĀmir', qiraaFr: 'Ibn ʿÂmir',
    riwayaAr: 'هشام عن ابن عامر', riwayaEn: 'Hishām ʿan Ibn ʿĀmir', riwayaFr: 'Hishām ʿan Ibn ʿÂmir',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.ibnDhakwanIbnAmir,
    qiraaAr: 'ابن عامر', qiraaEn: 'Ibn ʿĀmir', qiraaFr: 'Ibn ʿÂmir',
    riwayaAr: 'ابن ذكوان عن ابن عامر', riwayaEn: 'Ibn Dhakwān ʿan Ibn ʿĀmir', riwayaFr: 'Ibn Dhakwān ʿan Ibn ʿÂmir',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.khalafHamzah,
    qiraaAr: 'حمزة', qiraaEn: 'Ḥamzah', qiraaFr: 'Ḥamzah',
    riwayaAr: 'خلف عن حمزة', riwayaEn: 'Khalaf ʿan Ḥamzah', riwayaFr: 'Khalaf ʿan Ḥamzah',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.khalladHamzah,
    qiraaAr: 'حمزة', qiraaEn: 'Ḥamzah', qiraaFr: 'Ḥamzah',
    riwayaAr: 'خلاد عن حمزة', riwayaEn: 'Khallād ʿan Ḥamzah', riwayaFr: 'Khallād ʿan Ḥamzah',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.abulHarithKisai,
    qiraaAr: 'الكسائي', qiraaEn: 'Al-Kisāʾī', qiraaFr: 'Al-Kisāʾī',
    riwayaAr: 'أبو الحارث عن الكسائي', riwayaEn: 'Abū al-Ḥārith ʿan Al-Kisāʾī', riwayaFr: 'Abū al-Ḥārith ʿan Al-Kisāʾī',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
  RiwayaInfo(
    id: RiwayaId.duriKisai,
    qiraaAr: 'الكسائي', qiraaEn: 'Al-Kisāʾī', qiraaFr: 'Al-Kisāʾī',
    riwayaAr: 'الدوري عن الكسائي', riwayaEn: 'Al-Dūrī ʿan Al-Kisāʾī', riwayaFr: 'Al-Dūrī ʿan Al-Kisāʾī',
    datasetVersion: 'pending', source: 'Verified dataset required',
  ),
];
