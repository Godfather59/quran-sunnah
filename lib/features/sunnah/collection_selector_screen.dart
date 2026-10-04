import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/models/hadith.dart';
import '../../data/content/content_packages.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/seed/hadith_collections.dart';
import '../../state/download_state.dart';

class CollectionSelectorScreen extends ConsumerWidget {
  const CollectionSelectorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final filter = ref.watch(hadithFilterProvider);
    final dl = ref.watch(downloadProvider);

    void set(Set<String> ids) {
      ref.read(hadithFilterProvider.notifier).state =
          filter.copyWith(collectionIds: ids);
    }

    return Scaffold(
      appBar: AppBar(title: Text(s.t('sources'))),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              ActionChip(
                label: Text(s.t('selectAll')),
                onPressed: () =>
                    set(kHadithCollections.map((c) => c.id).toSet()),
              ),
              ActionChip(
                label: Text(s.t('deselectAll')),
                onPressed: () => set({}),
              ),
              ActionChip(
                label: Text(s.t('onlySahihayn')),
                onPressed: () => set(kSahihayn),
              ),
              ActionChip(
                label: Text(s.t('kutubSittah')),
                onPressed: () => set(kKutubSittah),
              ),
            ],
          ),
          const SizedBox(height: 8),
          FutureBuilder(
            future: ref.read(hadithRepositoryProvider).collections(),
            builder: (context, snap) {
              final items = snap.data ?? kHadithCollections;
              return Column(
                children: items
                    .map(
                      (collection) => Card(
                        child: CheckboxListTile(
                          value: filter.collectionIds.contains(collection.id),
                          onChanged: (enabled) async {
                            final next = {...filter.collectionIds};
                            if (enabled == true) {
                              final packageId =
                                  'hadith:${collection.id}';
                              final installed = ref
                                  .read(downloadProvider)
                                  .installed
                                  .contains(packageId);
                              if (!installed) {
                                try {
                                  await ref
                                      .read(downloadProvider.notifier)
                                      .install(packageId);
                                  ref
                                      .read(contentRevisionProvider.notifier)
                                      .state++;
                                } catch (_) {
                                  return;
                                }
                              }
                              next.add(collection.id);
                            } else {
                              next.remove(collection.id);
                            }
                            set(next);
                          },
                          title: Text(
                            s.isArabic
                                ? collection.nameAr
                                : '${collection.nameAr} · ${collection.nameEn}',
                          ),
                          subtitle: Text(
                            _subtitle(
                              s,
                              collection,
                              collection.isDownloaded ||
                                  dl.installed.contains(
                                    'hadith:${collection.id}',
                                  ),
                            ),
                          ),
                        ),
                      ),
                    )
                    .toList(),
              );
            },
          ),
        ],
      ),
    );
  }

  String _subtitle(
    AppStrings s,
    HadithCollection collection,
    bool installed,
  ) {
    final count = collection.totalHadith ?? '?';
    final state = installed ? s.t('downloaded') : s.t('notDownloaded');
    if (s.isArabic) {
      return '$count ${s.t('hadithCountUnit')} · $state';
    }
    return '${collection.compiler} · $count ${s.t('hadithCountUnit')} · '
        '${collection.downloadSizeMb ?? '?'} MB · $state';
  }
}
