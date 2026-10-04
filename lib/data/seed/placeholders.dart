// Placeholder rows shown UNTIL verified datasets are connected.
// Each placeholder carries isPlaceholder=true and the UI renders
// "Content unavailable for this source." No sacred text is invented.

import '../models/quran.dart';
import '../models/hadith.dart';

List<Ayah> placeholderAyahs({
  required int surah,
  required int count,
  required String editionId,
}) {
  return List.generate(count, (i) {
    return Ayah(
      surah: surah,
      ayah: i + 1,
      editionId: editionId,
      text: '',
      juz: 1,
      page: 1,
      isPlaceholder: true,
    );
  });
}

const Hadith placeholderHadith = Hadith(
  id: 'bukhari:1',
  collectionId: 'bukhari',
  book: 'Book of Revelation',
  bookAr: 'كتاب بدء الوحي',
  chapter: 'How revelation began',
  chapterAr: 'باب كيف كان بدء الوحي',
  hadithNumber: '1',
  matnAr: '',
  narrator: '',
  grade: null,
  gradingAuthority: null,
  isPlaceholder: true,
);
