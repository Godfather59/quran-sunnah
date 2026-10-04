// Trilingual UI chrome (AR / EN / FR).
// Rule: when locale is Arabic, screens show ONLY these Arabic strings
// plus verbatim sacred text — no Latin chrome. Sacred text itself is
// NEVER translated by this map; only interface labels are localized.

import 'package:flutter/widgets.dart';

class AppStrings {
  const AppStrings(this.locale);

  final Locale locale;

  static const supported = [Locale('ar'), Locale('en'), Locale('fr')];

  static AppStrings of(BuildContext context) {
    final locale = Localizations.localeOf(context);
    return AppStrings(locale);
  }

  bool get isArabic => locale.languageCode == 'ar';

  static const _values = <String, Map<String, String>>{
    // ── Tabs ──
    'appTitle': {
      'ar': 'القرآن والسنة',
      'en': 'Quran & Sunnah',
      'fr': 'Coran & Sunna',
    },
    'home': {'ar': 'الرئيسية', 'en': 'Home', 'fr': 'Accueil'},
    'quran': {'ar': 'القرآن', 'en': 'Quran', 'fr': 'Coran'},
    'sunnah': {'ar': 'السنة', 'en': 'Sunnah', 'fr': 'Sunna'},
    'library': {'ar': 'المكتبة', 'en': 'Library', 'fr': 'Bibliothèque'},
    'settings': {'ar': 'الإعدادات', 'en': 'Settings', 'fr': 'Paramètres'},
    // ── Home ──
    'continueReading': {
      'ar': 'مواصلة القراءة',
      'en': 'Continue Reading',
      'fr': 'Continuer la lecture',
    },
    'dailyAyah': {'ar': 'آية اليوم', 'en': 'Daily Ayah', 'fr': 'Verset du jour'},
    'dailyHadith': {
      'ar': 'حديث اليوم',
      'en': 'Daily Hadith',
      'fr': 'Hadith du jour',
    },
    'quickQuran': {'ar': 'القرآن', 'en': 'Quran', 'fr': 'Coran'},
    'quickSunnah': {'ar': 'السنة', 'en': 'Sunnah', 'fr': 'Sunna'},
    'quickAudio': {'ar': 'الصوت', 'en': 'Audio', 'fr': 'Audio'},
    'statBookmarks': {'ar': 'العلامات', 'en': 'Bookmarks', 'fr': 'Favoris'},
    'statDownloaded': {
      'ar': 'المحمّل',
      'en': 'Downloaded',
      'fr': 'Téléchargés'
    },
    'statCollections': {
      'ar': 'المجموعات',
      'en': 'Collections',
      'fr': 'Collections'
    },
    'atAGlance': {'ar': 'لمحة', 'en': 'At a glance', 'fr': 'En bref'},
    'customizeHome': {
      'ar': 'تخصيص الرئيسية',
      'en': 'Customize home',
      'fr': 'Personnaliser l’accueil'
    },
    'customizeHint': {
      'ar': 'اسحب للترتيب · العين للإخفاء. يُحفظ الترتيب.',
      'en': 'Drag to reorder · eye to hide. Order is saved.',
      'fr': 'Glisser pour réordonner · œil pour masquer. Ordre enregistré.',
    },
    'noBookmarksYet': {
      'ar': 'لا علامات بعد — انقر أي آية أو حديث لحفظه هنا.',
      'en': 'No bookmarks yet — tap any ayah or hadith to save it here.',
      'fr': 'Aucun favori — touchez un verset ou hadith pour l’enregistrer ici.',
    },
    // ── Search ──
    'search': {'ar': 'بحث', 'en': 'Search', 'fr': 'Rechercher'},
    'searchHint': {
      'ar': 'ابحث في القرآن والحديث…',
      'en': 'Search Quran & Hadith…',
      'fr': 'Rechercher Coran & Hadith…',
    },
    // ── Quran ──
    'surahs': {'ar': 'السور', 'en': 'Surahs', 'fr': 'Sourates'},
    'riwaya': {'ar': 'الرواية', 'en': 'Riwaya', 'fr': 'Riwaya'},
    'scriptStyle': {
      'ar': 'طريقة الكتابة / رسم المصحف',
      'en': 'Quran Text Style',
      'fr': 'Style du texte coranique',
    },
    'compareRiwayat': {
      'ar': 'مقارنة الروايات',
      'en': 'Compare Riwayat',
      'fr': 'Comparer les riwayat',
    },
    'mushaf': {'ar': 'المصحف', 'en': 'Mushaf', 'fr': 'Moushaf'},
    'readingMode': {
      'ar': 'وضع القراءة',
      'en': 'Reading mode',
      'fr': 'Mode lecture'
    },
    // ── Ayah sheet ──
    'play': {'ar': 'تشغيل', 'en': 'Play', 'fr': 'Lecture'},
    'repeat': {'ar': 'تكرار', 'en': 'Repeat', 'fr': 'Répéter'},
    'tafsir': {'ar': 'التفسير', 'en': 'Tafsir', 'fr': 'Tafsir'},
    'translation': {'ar': 'الترجمة', 'en': 'Translation', 'fr': 'Traduction'},
    'wordMeanings': {
      'ar': 'معاني الكلمات',
      'en': 'Word meanings',
      'fr': 'Sens des mots'
    },
    'bookmark': {'ar': 'علامة', 'en': 'Bookmark', 'fr': 'Favori'},
    'highlight': {'ar': 'تظليل', 'en': 'Highlight', 'fr': 'Surligner'},
    'addToCollection': {
      'ar': 'إضافة لمجموعة',
      'en': 'Add to collection',
      'fr': 'Ajouter à une collection',
    },
    'copy': {'ar': 'نسخ', 'en': 'Copy', 'fr': 'Copier'},
    'share': {'ar': 'مشاركة', 'en': 'Share', 'fr': 'Partager'},
    'notes': {'ar': 'ملاحظات', 'en': 'Notes', 'fr': 'Notes'},
    // ── Audio ──
    'quranAudio': {
      'ar': 'الصوت القرآني',
      'en': 'Quran audio',
      'fr': 'Audio du Coran'
    },
    'pause': {'ar': 'إيقاف مؤقت', 'en': 'Pause', 'fr': 'Pause'},
    'sleepTimer': {
      'ar': 'مؤقت النوم',
      'en': 'Sleep timer',
      'fr': 'Minuteur de sommeil'
    },
    'off': {'ar': 'مغلق', 'en': 'Off', 'fr': 'Désactivé'},
    // ── Sunnah ──
    'sources': {'ar': 'المصادر', 'en': 'Sources', 'fr': 'Sources'},
    'filters': {'ar': 'التصفية', 'en': 'Filters', 'fr': 'Filtres'},
    'booksChapters': {
      'ar': 'الكتب والأبواب',
      'en': 'Books & chapters',
      'fr': 'Livres & chapitres',
    },
    'applyFilters': {
      'ar': 'تطبيق التصفية',
      'en': 'Apply filters',
      'fr': 'Appliquer les filtres',
    },
    'selectAll': {
      'ar': 'تحديد الكل',
      'en': 'Select All',
      'fr': 'Tout sélectionner'
    },
    'deselectAll': {
      'ar': 'إلغاء الكل',
      'en': 'Deselect All',
      'fr': 'Tout désélectionner'
    },
    'onlySahihayn': {
      'ar': 'الصحيحان فقط',
      'en': 'Only Sahihayn',
      'fr': 'Seulement les Sahihayn',
    },
    'kutubSittah': {
      'ar': 'الكتب الستة',
      'en': 'Kutub al-Sittah',
      'fr': 'Les six livres'
    },
    // ── Library ──
    'bookmarks': {'ar': 'العلامات', 'en': 'Bookmarks', 'fr': 'Favoris'},
    'customCollections': {
      'ar': 'مجموعات مخصصة',
      'en': 'Custom collections',
      'fr': 'Collections personnalisées',
    },
    'newCollection': {
      'ar': 'مجموعة جديدة',
      'en': 'New collection',
      'fr': 'Nouvelle collection'
    },
    'noteLabel': {
      'ar': 'ملاحظات',
      'en': 'Notes',
      'fr': 'Notes',
    },
    'recentlyViewed': {
      'ar': 'شوهد مؤخرًا',
      'en': 'Recently viewed',
      'fr': 'Récemment consultés',
    },
    'downloadedContent': {
      'ar': 'المحتوى المحمّل',
      'en': 'Downloaded content',
      'fr': 'Contenu téléchargé',
    },
    // ── Downloads ──
    'downloads': {'ar': 'التنزيلات', 'en': 'Downloads', 'fr': 'Téléchargements'},
    'downloaded': {'ar': 'مُنزَّل', 'en': 'Downloaded', 'fr': 'Téléchargé'},
    'notDownloaded': {
      'ar': 'غير مُنزَّل',
      'en': 'Not downloaded',
      'fr': 'Non téléchargé',
    },
    // ── Settings groups ──
    'quranGroup': {'ar': 'القرآن', 'en': 'Quran', 'fr': 'Coran'},
    'sunnahGroup': {'ar': 'السنة', 'en': 'Sunnah', 'fr': 'Sunna'},
    'appearanceGroup': {'ar': 'المظهر', 'en': 'Appearance', 'fr': 'Apparence'},
    'audioGroup': {'ar': 'الصوت', 'en': 'Audio', 'fr': 'Audio'},
    'languageGroup': {'ar': 'اللغة', 'en': 'Language', 'fr': 'Langue'},
    'storageGroup': {'ar': 'التخزين', 'en': 'Storage', 'fr': 'Stockage'},
    'defaultRiwaya': {
      'ar': 'الرواية الافتراضية',
      'en': 'Default Riwaya',
      'fr': 'Riwaya par défaut',
    },
    'quranScript': {
      'ar': 'رسم المصحف',
      'en': 'Quran script',
      'fr': 'Rasm du Coran',
    },
    'quranFont': {'ar': 'الخط', 'en': 'Font', 'fr': 'Police'},
    'showTranslation': {
      'ar': 'إظهار الترجمة',
      'en': 'Show translation',
      'fr': 'Afficher la traduction',
    },
    'preferredTafsir': {
      'ar': 'التفسير المفضل',
      'en': 'Preferred tafsir',
      'fr': 'Tafsir préféré',
    },
    'ayahNumberStyle': {
      'ar': 'شكل أرقام الآيات',
      'en': 'Ayah number style',
      'fr': 'Style des numéros de versets',
    },
    'displaySanad': {
      'ar': 'إظهار السند',
      'en': 'Display Sanad',
      'fr': 'Afficher la chaîne',
    },
    'displayGrade': {
      'ar': 'إظهار الدرجة',
      'en': 'Display grade',
      'fr': 'Afficher le degré',
    },
    'themeMode': {'ar': 'السمة', 'en': 'Theme', 'fr': 'Thème'},
    'light': {'ar': 'فاتح', 'en': 'Light', 'fr': 'Clair'},
    'dark': {'ar': 'داكن', 'en': 'Dark', 'fr': 'Sombre'},
    'system': {'ar': 'النظام', 'en': 'System', 'fr': 'Système'},
    'dynamicColor': {
      'ar': 'ألوان ديناميكية',
      'en': 'Dynamic Color',
      'fr': 'Couleurs dynamiques',
    },
    'downloadManager': {
      'ar': 'مدير التنزيلات',
      'en': 'Download manager',
      'fr': 'Gestionnaire de téléchargements',
    },
    'privacyNote': {
      'ar':
          'الخصوصية: القراءة تعمل دون حساب. العلامات والملاحظات تبقى على جهازك.',
      'en':
          'Privacy: reading works without any account. Bookmarks & notes stay on your device.',
      'fr':
          'Confidentialité : la lecture fonctionne sans compte. Favoris et notes restent sur votre appareil.',
    },
    // ── Shared ──
    'contentUnavailable': {
      'ar': 'المحتوى غير متوفر لهذا المصدر.',
      'en': 'Content unavailable for this source.',
      'fr': 'Contenu indisponible pour cette source.',
    },
    'userNoteLabel': {
      'ar': 'ملاحظة شخصية (ليست من النص المقدس)',
      'en': 'Personal note (not sacred text)',
      'fr': 'Note personnelle (pas un texte sacré)',
    },
    'translationLabel': {
      'ar': 'الترجمة (ليست قرآنًا)',
      'en': 'Translation (not Quran)',
      'fr': 'Traduction (pas le Coran)',
    },
    'cancel': {'ar': 'إلغاء', 'en': 'Cancel', 'fr': 'Annuler'},
    'rename': {'ar': 'إعادة تسمية', 'en': 'Rename', 'fr': 'Renommer'},
    'delete': {'ar': 'حذف', 'en': 'Delete', 'fr': 'Supprimer'},
    'downloadedCount': {
      'ar': 'مثبتة',
      'en': 'installed',
      'fr': 'installés',
    },
    'makki': {'ar': 'مكية', 'en': 'Makki', 'fr': 'Mecquoise'},    'madani': {'ar': 'مدنية', 'en': 'Madani', 'fr': 'Médinoise'},
    'ayat': {'ar': 'آية', 'en': 'ayat', 'fr': 'versets'},
    'surahWord': {'ar': 'سورة', 'en': 'Surah', 'fr': 'Sourate'},
    // ── Search tabs ──
    'tabAll': {'ar': 'الكل', 'en': 'All', 'fr': 'Tout'},
    'hadith': {'ar': 'حديث', 'en': 'Hadith', 'fr': 'Hadith'},
    'tabNarrator': {'ar': 'الراوي', 'en': 'Narrator', 'fr': 'Narrateur'},
    'tabTopic': {'ar': 'الموضوع', 'en': 'Topic', 'fr': 'Sujet'},
    // ── Onboarding ──
    'obLanguage': {
      'ar': 'اختر اللغة',
      'en': 'Choose language',
      'fr': 'Choisir la langue'
    },
    'obRiwaya': {
      'ar': 'اختر الرواية',
      'en': 'Choose Quran Riwaya',
      'fr': 'Choisir la riwaya'
    },
    'obScript': {
      'ar': 'اختر رسم المصحف',
      'en': 'Choose Quran text style',
      'fr': 'Choisir le rasm'
    },
    'obSources': {
      'ar': 'اختر مصادر الحديث',
      'en': 'Choose Hadith sources',
      'fr': 'Choisir les sources de hadiths'
    },
    'obDownloads': {
      'ar': 'تنزيلات اختيارية',
      'en': 'Optional downloads',
      'fr': 'Téléchargements facultatifs'
    },
    'obDownloadsHint': {
      'ar':
          'يمكن تنزيل القرآن والترجمات والتفاسير والحديث والصوت لاحقًا من المكتبة. لا شيء مطلوب الآن.',
      'en':
          'Quran, translations, tafsir, hadith and audio can be downloaded later from Library. Nothing is required now.',
      'fr':
          'Coran, traductions, tafsir, hadiths et audio pourront être téléchargés plus tard. Rien n’est requis.',
    },
    'obNext': {'ar': 'التالي', 'en': 'Next', 'fr': 'Suivant'},
    'obDone': {'ar': 'ابدأ', 'en': 'Start', 'fr': 'Commencer'},
  };

  String t(String key) => _values[key]?[_code] ?? _values[key]?['en'] ?? key;

  String get _code => locale.languageCode;
}
