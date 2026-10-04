import '../../data/models/hadith.dart';

// Collection metadata only — matn comes from verified dataset files.
const List<HadithCollection> kHadithCollections = [
  HadithCollection(
    id: 'bukhari', nameAr: 'صحيح البخاري', nameEn: 'Sahih al-Bukhari',
    nameFr: 'Sahih al-Boukhari', compiler: 'Muḥammad al-Bukhārī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 7563, downloadSizeMb: 18.4,
  ),
  HadithCollection(
    id: 'muslim', nameAr: 'صحيح مسلم', nameEn: 'Sahih Muslim',
    nameFr: 'Sahih Mouslim', compiler: 'Muslim ibn al-Ḥajjāj',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 7470, downloadSizeMb: 17.1,
  ),
  HadithCollection(
    id: 'abudawud', nameAr: 'سنن أبي داود', nameEn: 'Sunan Abi Dawud',
    nameFr: 'Sunan Abou Dawoud', compiler: 'Abū Dāwūd',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 5274, downloadSizeMb: 12.0,
  ),
  HadithCollection(
    id: 'tirmidhi', nameAr: 'جامع الترمذي', nameEn: 'Jami‘ al-Tirmidhi',
    nameFr: 'Jami‘ at-Tirmidhi', compiler: 'Al-Tirmidhī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 3956, downloadSizeMb: 9.8,
  ),
  HadithCollection(
    id: 'nasai', nameAr: 'سنن النسائي', nameEn: 'Sunan al-Nasa’i',
    nameFr: 'Sunan an-Nasa’i', compiler: 'Al-Nasā’ī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 5760, downloadSizeMb: 11.2,
  ),
  HadithCollection(
    id: 'ibnmajah', nameAr: 'سنن ابن ماجه', nameEn: 'Sunan Ibn Majah',
    nameFr: 'Sunan Ibn Majah', compiler: 'Ibn Mājah',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 4341, downloadSizeMb: 10.5,
  ),
  HadithCollection(
    id: 'malik', nameAr: 'موطأ مالك', nameEn: 'Muwatta Malik',
    nameFr: 'Mouwatta Malik', compiler: 'Mālik ibn Anas',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 1720, downloadSizeMb: 5.2,
  ),
  HadithCollection(
    id: 'ahmad', nameAr: 'مسند أحمد', nameEn: 'Musnad Ahmad',
    nameFr: 'Mousnad Ahmad', compiler: 'Aḥmad ibn Ḥanbal',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 27199, downloadSizeMb: 48.0,
  ),
  HadithCollection(
    id: 'riyad', nameAr: 'رياض الصالحين', nameEn: 'Riyad as-Salihin',
    nameFr: 'Les Jardins des vertueux', compiler: 'Al-Nawawī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 1896, downloadSizeMb: 6.4,
  ),
  HadithCollection(
    id: 'adab', nameAr: 'الأدب المفرد', nameEn: 'Al-Adab al-Mufrad',
    nameFr: 'Al-Adab al-Moufrad', compiler: 'Al-Bukhārī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 1322, downloadSizeMb: 4.1,
  ),
  HadithCollection(
    id: 'bulugh', nameAr: 'بلوغ المرام', nameEn: 'Bulugh al-Maram',
    nameFr: 'Boulough al-Maram', compiler: 'Ibn Ḥajar',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 1358, downloadSizeMb: 3.9,
  ),
  HadithCollection(
    id: 'nawawi', nameAr: 'الأربعون النووية', nameEn: 'Forty Hadith of an-Nawawi',
    nameFr: 'Les Quarante hadiths d’an-Nawawi', compiler: 'Al-Nawawī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 42, downloadSizeMb: 0.3,
  ),
  HadithCollection(
    id: 'qudsi', nameAr: 'الأحاديث القدسية', nameEn: 'Forty Hadith Qudsi',
    nameFr: 'Les Quarante hadiths qudsi', compiler: 'Various',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 40, downloadSizeMb: 0.3,
  ),
  HadithCollection(
    id: 'dehlawi', nameAr: 'أربعون شاه ولي الله الدهلوي', nameEn: 'Forty Hadith of Shah Waliullah Dehlawi',
    nameFr: 'Les Quarante hadiths de Shah Waliullah', compiler: 'Shāh Walīullāh ad-Dihlawī',
    source: 'Verified dataset required', version: '1.0.0',
    totalHadith: 40, downloadSizeMb: 0.3,
  ),
];

const kSahihayn = {'bukhari', 'muslim'};
const kKutubSittah = {
  'bukhari', 'muslim', 'abudawud', 'tirmidhi', 'nasai', 'ibnmajah'
};
