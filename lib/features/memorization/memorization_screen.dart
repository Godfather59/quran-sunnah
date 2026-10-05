import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/seed/surah_metadata.dart';
import '../../state/memorization_provider.dart';
import '../quran/quran_reader_screen.dart';

/// Memorization dashboard: per-surah progress + resume at first unmemorized.
class MemorizationScreen extends ConsumerWidget {
  const MemorizationScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final memorized = ref.watch(memorizationProvider);
    final totalMemorized = memorized.length;

    return Scaffold(
      appBar: AppBar(
        title: Text(s.isArabic
            ? 'الحفظ'
            : s.locale.languageCode == 'fr'
                ? 'Mémorisation'
                : 'Memorization'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            color: Theme.of(context).colorScheme.primaryContainer,
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    s.isArabic
                        ? 'محفوظ: $totalMemorized / 6236'
                        : '$totalMemorized / 6236',
                    style: Theme.of(context)
                        .textTheme
                        .headlineSmall
                        ?.copyWith(
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimaryContainer),
                  ),
                  const SizedBox(height: 8),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: LinearProgressIndicator(
                      value: (totalMemorized / 6236).clamp(0.0, 1.0),
                      minHeight: 6,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    s.isArabic
                        ? 'أخفِ الآيات من زر العين في القارئ، سمّع، ثم اكشف وعلّم ✓'
                        : s.locale.languageCode == 'fr'
                            ? 'Masquez via l’œil dans le lecteur, récitez, révèlez, cochez ✓'
                            : 'Hide via the eye in the reader, recite, reveal, check ✓',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          for (final m in kSurahMetadata)
            Builder(
              builder: (context) {
                final done = ref
                    .read(memorizationProvider.notifier)
                    .countForSurah(m.number, m.ayahCount);
                if (done == 0) {
                  return ListTile(
                    leading: CircleAvatar(
                        child: Text('${m.number}')),
                    title: Text(s.isArabic
                        ? m.nameAr
                        : '${m.nameAr} · ${m.nameEn}'),
                    subtitle: Text('${m.ayahCount}'),
                    trailing:
                        const Icon(Icons.chevron_right),
                    onTap: () =>
                        Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => QuranReaderScreen(
                          surah: m.number,
                          initialAyah: 1,
                        ),
                      ),
                    ),
                  );
                }
                return Card(
                  child: ListTile(
                    leading: CircleAvatar(
                        child: Text('${m.number}')),
                    title: Text(s.isArabic
                        ? m.nameAr
                        : '${m.nameAr} · ${m.nameEn}'),
                    subtitle: Column(
                      crossAxisAlignment:
                          CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 4),
                        LinearProgressIndicator(
                          value: (done / m.ayahCount)
                              .clamp(0.0, 1.0),
                          minHeight: 4,
                        ),
                        const SizedBox(height: 4),
                        Text('$done / ${m.ayahCount}'),
                      ],
                    ),
                    trailing:
                        const Icon(Icons.chevron_right),
                    onTap: () {
                      final start = ref
                          .read(memorizationProvider
                              .notifier)
                          .firstUnmemorized(
                              m.number, m.ayahCount);
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) =>
                              QuranReaderScreen(
                            surah: m.number,
                            initialAyah: start,
                          ),
                        ),
                      );
                    },
                  ),
                );
              },
            ),
        ],
      ),
    );
  }
}
