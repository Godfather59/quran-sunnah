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
    'download': {'ar': 'تنزيل', 'en': 'Download', 'fr': 'Télécharger'},
    'resume': {'ar': 'متابعة', 'en': 'Resume', 'fr': 'Reprendre'},
    'cancelDownload': {'ar': 'إلغاء التنزيل', 'en': 'Cancel download', 'fr': 'Annuler le téléchargement'},
    'coreContent': {'ar': 'المحتوى الأساسي', 'en': 'Core content', 'fr': 'Contenu principal'},
    'optionalContent': {'ar': 'محتوى اختياري', 'en': 'Optional content', 'fr': 'Contenu facultatif'},
    'selectedDownloadSize': {'ar': 'حجم التنزيل المحدد', 'en': 'Selected download size', 'fr': 'Taille sélectionnée'},
    'downloadBeforeUse': {'ar': 'يلزم تنزيل هذه الحزمة قبل استخدامها.', 'en': 'Download this package before using it.', 'fr': 'Téléchargez ce paquet avant de l’utiliser.'},
    'packageSource': {'ar': 'المصدر', 'en': 'Source', 'fr': 'Source'},
    'licenseStatus': {'ar': 'حالة الترخيص', 'en': 'License status', 'fr': 'Statut de licence'},
    'downloadingContent': {'ar': 'جارٍ تنزيل المحتوى المحدد…', 'en': 'Downloading selected content…', 'fr': 'Téléchargement du contenu sélectionné…'},
    'downloadFailed': {'ar': 'فشل تنزيل المحتوى. تحقق من الاتصال وحاول مجددًا.', 'en': 'Content download failed. Check your connection and try again.', 'fr': 'Échec du téléchargement. Vérifiez la connexion et réessayez.'},
    'wordMorphology': {'ar': 'تحليل الكلمات', 'en': 'Word morphology', 'fr': 'Morphologie des mots'},
    'wordMorphologyHint': {'ar': 'بيانات الكلمات والجذور والصيغ من المصدر الموثق.', 'en': 'Word, root and morphology data from the verified source.', 'fr': 'Mots, racines et morphologie depuis la source vérifiée.'},
    'includeTafsir': {'ar': 'تنزيل التفسير المحدد', 'en': 'Download selected Tafsir', 'fr': 'Télécharger le tafsir sélectionné'},
    'packageCount': {'ar': 'حزمة', 'en': 'packages', 'fr': 'paquets'},
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
    'page': {'ar': 'صفحة', 'en': 'Page', 'fr': 'Page'},
    'goToPage': {'ar': 'الانتقال إلى صفحة', 'en': 'Go to page', 'fr': 'Aller à la page'},
    'previousPage': {'ar': 'الصفحة السابقة', 'en': 'Previous page', 'fr': 'Page précédente'},
    'nextPage': {'ar': 'الصفحة التالية', 'en': 'Next page', 'fr': 'Page suivante'},
    'of': {'ar': 'من', 'en': 'of', 'fr': 'sur'},
    'go': {'ar': 'انتقال', 'en': 'Go', 'fr': 'Aller'},
    'juz': {'ar': 'الجزء', 'en': 'Juz', 'fr': 'Juz'},
    'hizb': {'ar': 'الحزب', 'en': 'Hizb', 'fr': 'Hizb'},
    'rub': {'ar': 'الربع', 'en': 'Rubʿ', 'fr': 'Rubʿ'},
    'sajda': {'ar': 'سجدة', 'en': 'Sajda', 'fr': 'Sajda'},
    'mushafPageMapUnavailable': {
      'ar': 'تخطيط صفحات مصحف المدينة موثق حاليًا لحفص بالرسم العثماني فقط.',
      'en': 'Verified Medina Mushaf page mapping is currently available only for Hafs/Uthmani.',
      'fr': 'La pagination vérifiée du Moushaf de Médine est actuellement disponible uniquement pour Hafs/Uthmani.',
    },
    'noVerifiedRecitation': {
      'ar': 'لا توجد تلاوة موثقة لهذه الرواية.',
      'en': 'No verified recitation is available for this Riwaya.',
      'fr': 'Aucune récitation vérifiée n’est disponible pour cette riwaya.',
    },
    'playingFrom': {'ar': 'التشغيل من', 'en': 'Playing from', 'fr': 'Lecture depuis'},
    'range': {'ar': 'النطاق', 'en': 'Range', 'fr': 'Plage'},
    'surah': {'ar': 'السورة', 'en': 'Surah', 'fr': 'Sourate'},
    'fromAyah': {'ar': 'من الآية', 'en': 'From ayah', 'fr': 'Depuis le verset'},
    'toOptional': {'ar': 'إلى (اختياري)', 'en': 'To (optional)', 'fr': 'Jusqu’à (facultatif)'},
    'end': {'ar': 'النهاية', 'en': 'End', 'fr': 'Fin'},
    'streamingOfflineHint': {
      'ar': 'البث يحتاج إنترنت، أما السور المنزلة فتعمل دون اتصال.',
      'en': 'Streaming requires internet; downloaded surahs play offline.',
      'fr': 'Le streaming nécessite Internet ; les sourates téléchargées fonctionnent hors ligne.',
    },
    'quranRiwayat': {'ar': 'روايات القرآن', 'en': 'Quran Riwayat', 'fr': 'Riwayat du Coran'},
    'hadithCollections': {'ar': 'مجموعات الحديث', 'en': 'Hadith collections', 'fr': 'Collections de hadiths'},
    'quranAudioDownloads': {'ar': 'تنزيلات الصوت القرآني', 'en': 'Quran audio downloads', 'fr': 'Téléchargements audio du Coran'},
    'audioDownloadsHint': {
      'ar': 'تُدار التنزيلات الصوتية حسب السورة والقارئ.',
      'en': 'Audio downloads are managed per surah and reciter.',
      'fr': 'Les téléchargements audio sont gérés par sourate et réciteur.',
    },
    'verifiedDatasetsInstalled': {
      'ar': 'حزم موثقة مثبتة',
      'en': 'verified bundled datasets installed',
      'fr': 'jeux de données vérifiés installés',
    },
    'installed': {'ar': 'مثبت', 'en': 'Installed', 'fr': 'Installé'},
    'shipsWithApp': {'ar': 'مضمن مع التطبيق', 'en': 'ships with app', 'fr': 'inclus avec l’application'},
    'datasetUnavailable': {
      'ar': 'لا توجد حزمة موثقة مضمنة بعد.',
      'en': 'Verified dataset is not bundled yet.',
      'fr': 'Le jeu de données vérifié n’est pas encore inclus.',
    },
    'exportBackup': {'ar': 'تصدير نسخة احتياطية', 'en': 'Export backup', 'fr': 'Exporter la sauvegarde'},
    'importBackup': {'ar': 'استيراد نسخة احتياطية', 'en': 'Import backup', 'fr': 'Importer la sauvegarde'},
    'backupRestored': {'ar': 'تمت استعادة النسخة الاحتياطية.', 'en': 'Backup restored.', 'fr': 'Sauvegarde restaurée.'},
    'invalidBackup': {'ar': 'ملف النسخة الاحتياطية غير صالح.', 'en': 'Invalid backup file.', 'fr': 'Fichier de sauvegarde invalide.'},
    'searchLibrary': {'ar': 'البحث في المكتبة', 'en': 'Search library', 'fr': 'Rechercher dans la bibliothèque'},
    'noLibraryResults': {'ar': 'لا توجد نتائج في المكتبة.', 'en': 'No library results.', 'fr': 'Aucun résultat dans la bibliothèque.'},
    'dataSourcesLicenses': {'ar': 'مصادر البيانات والتراخيص', 'en': 'Data sources & licenses', 'fr': 'Sources de données et licences'},
    'book': {'ar': 'الكتاب', 'en': 'Book', 'fr': 'Livre'},
    'hadithNumber': {'ar': 'رقم الحديث', 'en': 'Hadith number', 'fr': 'Numéro du hadith'},
    'structuredFiltersUnavailable': {
      'ar': 'تصفية الراوي والدرجة غير متاحة حتى نضيف مصدرًا موثقًا ومنظمًا.',
      'en': 'Narrator and grade filters stay unavailable until a verified structured source is added.',
      'fr': 'Les filtres narrateur et degré restent indisponibles jusqu’à l’ajout d’une source structurée vérifiée.',
    },
    'storageUsed': {'ar': 'المساحة المستخدمة', 'en': 'Storage used', 'fr': 'Espace utilisé'},
    'downloadQueue': {'ar': 'قائمة التنزيل', 'en': 'Download queue', 'fr': 'File de téléchargement'},
    'reciterStorage': {'ar': 'مساحة القارئ', 'en': 'Reciter storage', 'fr': 'Espace du réciteur'},
    'surahStorage': {'ar': 'مساحة السورة', 'en': 'Surah storage', 'fr': 'Espace de la sourate'},
    'queued': {'ar': 'في الانتظار', 'en': 'Queued', 'fr': 'En attente'},
    'downloading': {'ar': 'جارٍ التنزيل', 'en': 'Downloading', 'fr': 'Téléchargement'},
    'retry': {'ar': 'إعادة المحاولة', 'en': 'Retry', 'fr': 'Réessayer'},
    'remove': {'ar': 'إزالة', 'en': 'Remove', 'fr': 'Retirer'},
    'dataNoticesUnavailable': {
      'ar': 'إشعارات مصادر البيانات غير متاحة.',
      'en': 'Data notices are unavailable.',
      'fr': 'Les notices de données sont indisponibles.',
    },
    'contentProvenance': {
      'ar': 'مصدر المحتوى',
      'en': 'Content provenance',
      'fr': 'Provenance du contenu',
    },
    'provenanceExplanation': {
      'ar': 'نص القرآن والترجمات والتفسير والحديث والصرف والتجويد والخطوط لكل منها مصدر وشروط إعادة نشر مستقلة. الحالات غير المحسومة مذكورة صراحة.',
      'en': 'Quran text, translations, tafsir, Hadith, morphology, Tajweed annotations and fonts have separate provenance and redistribution terms. Unresolved entries are stated explicitly.',
      'fr': 'Le texte coranique, les traductions, le tafsir, les hadiths, la morphologie, les annotations de tajwid et les polices ont des provenances et conditions distinctes. Les cas non résolus sont indiqués explicitement.',
    },
    'source': {'ar': 'المصدر', 'en': 'Source', 'fr': 'Source'},
    'tafsirDatasetRequired': {
      'ar': 'يحتاج هذا التفسير إلى حزمة موثقة ومسموح بإعادة نشرها قبل عرضه.',
      'en': 'This tafsir requires a verified redistributable dataset before it can be shown.',
      'fr': 'Ce tafsir nécessite un jeu de données vérifié et redistribuable avant affichage.',
    },
    'dynamicColorHint': {
      'ar': 'أندرويد · اختياري',
      'en': 'Android · optional',
      'fr': 'Android · facultatif',
    },
    'tajweedLegend': {
      'ar': 'دليل ألوان التجويد',
      'en': 'Tajweed legend',
      'fr': 'Légende du tajwid',
    },
    'mushafReading': {
      'ar': 'المصحف / القراءة',
      'en': 'Mushaf / Reading',
      'fr': 'Moushaf / Lecture',
    },
    'fullscreen': {
      'ar': 'ملء الشاشة',
      'en': 'Fullscreen',
      'fr': 'Plein écran',
    },
    'verifiedDatasetRequired': {
      'ar': 'يلزم مصدر موثق لعرض هذا المحتوى.',
      'en': 'A verified dataset is required to display this content.',
      'fr': 'Un jeu de données vérifié est requis pour afficher ce contenu.',
    },
    'riwayaExplanation': {
      'ar': 'القراءة هي الأصل، والرواية طريق نقلها. كل رواية هنا تعتمد على نص موثق مستقل، ولا يتم استبدال الكلمات برمجيًا.',
      'en': 'Qira’a is the canonical reading and Riwaya is its transmission. Each Riwaya uses its own verified dataset; wording is never substituted programmatically.',
      'fr': 'La qira’a est la lecture de référence et la riwaya sa transmission. Chaque riwaya utilise son propre jeu de données vérifié ; le texte n’est jamais remplacé artificiellement.',
    },
    'qiraaLabel': {'ar': 'القراءة', 'en': 'Qira’a', 'fr': 'Qira’a'},
    'versionLabel': {'ar': 'الإصدار', 'en': 'Version', 'fr': 'Version'},
    'scriptExplanation': {
      'ar': 'تغيير الرسم يغيّر طريقة العرض فقط، ولا يغيّر ألفاظ القرآن أو معناه.',
      'en': 'Changing the script changes presentation only; it never changes Quran wording or meaning.',
      'fr': 'Changer le rasm modifie uniquement l’affichage, jamais les mots ni le sens du Coran.',
    },
    'uthmani': {'ar': 'عثماني', 'en': 'Uthmani', 'fr': 'Uthmani'},
    'uthmaniHint': {
      'ar': 'الرسم التقليدي بعلامات المصحف.',
      'en': 'Traditional orthography with Quranic marks.',
      'fr': 'Orthographe traditionnelle avec les signes coraniques.',
    },
    'imlai': {'ar': 'إملائي مبسط', 'en': 'Simple / Imla’i', 'fr': 'Simple / Imla’i'},
    'imlaiHint': {
      'ar': 'كتابة مبسطة للقراءة والبحث.',
      'en': 'Simplified modern reading and search-friendly text.',
      'fr': 'Écriture simplifiée adaptée à la lecture et à la recherche.',
    },
    'indopak': {'ar': 'هندي باكستاني', 'en': 'IndoPak', 'fr': 'IndoPak'},
    'indopakHint': {
      'ar': 'رسم هندي باكستاني متوفر لحفص.',
      'en': 'IndoPak script bundled for Hafs.',
      'fr': 'Rasm IndoPak inclus pour Hafs.',
    },
    'datasetUnavailableForRiwaya': {
      'ar': 'هذا الرسم غير متوفر لهذه الرواية ضمن مصدر موثق.',
      'en': 'This script is unavailable for this Riwaya from a verified dataset.',
      'fr': 'Ce rasm est indisponible pour cette riwaya dans un jeu de données vérifié.',
    },
    'tajweedColors': {'ar': 'ألوان التجويد', 'en': 'Tajweed colors', 'fr': 'Couleurs de tajwid'},
    'tajweedVerifiedHint': {
      'ar': 'ألوان موثقة فوق نص حفص العثماني، من دون تغيير النص.',
      'en': 'Verified annotations over Hafs/Uthmani; Quran text remains unchanged.',
      'fr': 'Annotations vérifiées sur Hafs/Uthmani sans modifier le texte coranique.',
    },
    'tajweedHafsOnly': {
      'ar': 'متاح حاليًا فقط مع حفص بالرسم العثماني.',
      'en': 'Currently available only with Hafs + Uthmani.',
      'fr': 'Disponible actuellement uniquement avec Hafs + Uthmani.',
    },
    'quranTypography': {'ar': 'تنسيق خط القرآن', 'en': 'Quran typography', 'fr': 'Typographie du Coran'},
    'fontSize': {'ar': 'حجم الخط', 'en': 'Font size', 'fr': 'Taille du texte'},
    'lineHeight': {'ar': 'تباعد السطور', 'en': 'Line height', 'fr': 'Hauteur de ligne'},
    'ayahSpacing': {'ar': 'تباعد الآيات', 'en': 'Ayah spacing', 'fr': 'Espacement des versets'},
    'allCollections': {'ar': 'كل المجموعات', 'en': 'All collections', 'fr': 'Toutes les collections'},
    'additionalContent': {'ar': 'محتوى إضافي', 'en': 'Additional content', 'fr': 'Contenu supplémentaire'},
    'additionalContentHint': {
      'ar': 'هذه المواد مضمنة أصلًا وتعمل دون اتصال. اختر فقط ما تريد تفعيله افتراضيًا.',
      'en': 'These items are already bundled and work offline. Choose what you want enabled by default.',
      'fr': 'Ces contenus sont déjà inclus et fonctionnent hors ligne. Choisissez ce que vous souhaitez activer par défaut.',
    },
    'translations': {'ar': 'الترجمات', 'en': 'Translations', 'fr': 'Traductions'},
    'saheehInternational': {'ar': 'الترجمة الإنجليزية — صحيح إنترناشونال', 'en': 'English — Saheeh International', 'fr': 'Anglais — Saheeh International'},
    'hamidullahFrench': {'ar': 'الترجمة الفرنسية — محمد حميد الله', 'en': 'French — Muhammad Hamidullah', 'fr': 'Français — Muhammad Hamidullah'},
    'chooseTafsir': {'ar': 'اختر التفسير الافتراضي', 'en': 'Choose default Tafsir', 'fr': 'Choisir le tafsir par défaut'},
    'jalalayn': {'ar': 'تفسير الجلالين', 'en': 'Tafsir al-Jalalayn', 'fr': 'Tafsir al-Jalalayn'},
    'siraj': {'ar': 'السراج في تفسير القرآن', 'en': 'Al-Siraj Tafsir', 'fr': 'Tafsir Al-Siraj'},
    'noHadithMatches': {'ar': 'لا توجد أحاديث تطابق هذه التصفية.', 'en': 'No hadith match these filters.', 'fr': 'Aucun hadith ne correspond à ces filtres.'},
    'sourcesNone': {'ar': 'المصادر: لا شيء محدد', 'en': 'Sources: none', 'fr': 'Sources : aucune'},
    'sourcesSelected': {'ar': 'مصادر محددة', 'en': 'sources selected', 'fr': 'sources sélectionnées'},
    'hadithCountUnit': {'ar': 'حديث', 'en': 'hadith', 'fr': 'hadiths'},
    'numbersLabel': {'ar': 'الأرقام', 'en': 'nos.', 'fr': 'n°'},
    'chainOfNarration': {'ar': 'سلسلة السند', 'en': 'Chain of Narration', 'fr': 'Chaîne de transmission'},
    'relatedNarrations': {'ar': 'الروايات ذات الصلة', 'en': 'Related Narrations', 'fr': 'Narrations liées'},
    'narratorLabel': {'ar': 'الراوي', 'en': 'Narrator', 'fr': 'Narrateur'},
    'profileLabel': {'ar': 'الملف', 'en': 'profile', 'fr': 'profil'},
    'gradeLabel': {'ar': 'الدرجة', 'en': 'Grade', 'fr': 'Degré'},
    'gradeUnavailable': {
      'ar': 'الدرجة غير متوفرة في هذا المصدر، ولن يتم افتراضها.',
      'en': 'Grade unavailable for this source; it is never assumed.',
      'fr': 'Le degré est indisponible pour cette source et n’est jamais supposé.',
    },
    'narratorDetailsUnavailable': {
      'ar': 'لا تتوفر حاليًا بيانات موثقة كافية لعرض سلسلة الرواة أو السيرة التفصيلية لهذا الراوي.',
      'en': 'There is not enough verified structured metadata to show a narrator chain or detailed biography yet.',
      'fr': 'Les métadonnées structurées vérifiées sont insuffisantes pour afficher la chaîne ou une biographie détaillée.',
    },
    'relatedNarrationsHint': {
      'ar': 'تُعرض الروايات الموازية من المصادر الأخرى منفصلة بمراجعها الأصلية، ولا يتم دمجها.',
      'en': 'Parallel narrations from other collections are shown separately with their own references and are never merged.',
      'fr': 'Les narrations parallèles d’autres collections sont affichées séparément avec leurs propres références et ne sont jamais fusionnées.',
    },
    'arabicMatnPlaceholderHint': {
      'ar': 'يظهر المتن والسند والراوي والدرجة والترجمة والروايات الموازية هنا فقط عندما يوفرها مصدر موثق.',
      'en': 'Matn, sanad, narrator, grade, translation and parallel narrations appear here only when provided by a verified source.',
      'fr': 'Le matn, le sanad, le narrateur, le degré, la traduction et les narrations parallèles apparaissent ici uniquement lorsqu’une source vérifiée les fournit.',
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
    'ayahLabel': {'ar': 'آية', 'en': 'Ayah', 'fr': 'Verset'},
    'progress': {'ar': 'التقدم', 'en': 'Progress', 'fr': 'Progression'},
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
      'ar': 'محتوى إضافي',
      'en': 'Additional content',
      'fr': 'Contenu supplémentaire'
    },
    'obDownloadsHint': {
      'ar':
          'اختر المحتوى الإضافي الذي تريد تنزيله الآن. بعد التنزيل يعمل دون اتصال ويمكن حذفه أو إعادة تنزيله لاحقًا.',
      'en':
          'Choose additional content to download now. Once installed it works offline and can be removed or downloaded again later.',
      'fr':
          'Choisissez le contenu supplémentaire à télécharger maintenant. Une fois installé, il fonctionne hors ligne et peut être supprimé ou retéléchargé plus tard.',
    },
    'obNext': {'ar': 'التالي', 'en': 'Next', 'fr': 'Suivant'},
    'obDone': {'ar': 'ابدأ', 'en': 'Start', 'fr': 'Commencer'},
  };

  String t(String key) => _values[key]?[_code] ?? _values[key]?['en'] ?? key;

  String get _code => locale.languageCode;
}
