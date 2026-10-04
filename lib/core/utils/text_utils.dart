// Arabic search normalization: strip tashkeel/tatweel, unify alef
// forms. ONLY applied to the search index — never to displayed text.

final _tashkeel = RegExp(r'[\u0610-\u061A\u064B-\u065F\u06D6-\u06ED]');
final _tatweel = RegExp(r'\u0640');

String normalizeArabic(String input) {
  var s = input.replaceAll(_tashkeel, '').replaceAll(_tatweel, '');
  s = s
      .replaceAll(RegExp(r'[أإآٱ]'), 'ا')
      .replaceAll('ة', 'ه')
      .replaceAll('ى', 'ي')
      .replaceAll('ؤ', 'و')
      .replaceAll('ئ', 'ي');
  return s.trim();
}

/// Latin/fuzzy helper: lowercase + trim.
String normalizeLatin(String input) => input.toLowerCase().trim();

/// Convert digits for ayah markers.
String toArabicIndic(int n) {
  const digits = '٠١٢٣٤٥٦٧٨٩';
  return n.toString().split('').map((c) {
    final d = int.tryParse(c);
    return d == null ? c : digits[d];
  }).join();
}
