import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/seed/hadith_collections.dart';
import '../../state/download_state.dart';

/// Source filter (§16) with presets. Persisted in filter provider.
class CollectionSelectorScreen extends ConsumerWidget {
  const CollectionSelectorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final filter = ref.watch(hadithFilterProvider);
    final dl = ref.watch(downloadProvider);

    void set(Set<String> ids) => ref
        .read(hadithFilterProvider.notifier)
        .state = filter.copyWith(collectionIds: ids);

    return Scaffold(
      appBar: AppBar(title: const Text('Sources · المصادر')),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          Wrap(
            spacing: 8,
            children: [
              ActionChip(
                  label: const Text('Select All'),
                  onPressed: () => set(kHadithCollections
                      .map((c) => c.id)
                      .toSet())),
              ActionChip(
                  label: const Text('Deselect All'),
                  onPressed: () => set({})),
              ActionChip(
                  label: const Text('Only Sahihayn'),
                  onPressed: () => set(kSahihayn)),
              ActionChip(
                  label: const Text('Kutub al-Sittah'),
                  onPressed: () => set(kKutubSittah)),
            ],
          ),
          const SizedBox(height: 8),
          FutureBuilder(
            future:
                ref.read(hadithRepositoryProvider).collections(),
            builder: (context, snap) {
              final items = snap.data ?? kHadithCollections;
              return Column(
                children: items
                    .map((c) => Card(
                          child: CheckboxListTile(
                            value: filter.collectionIds
                                .contains(c.id),
                            onChanged: (v) {
                              final next = {
                                ...filter.collectionIds
                              };
                              v == true
                                  ? next.add(c.id)
                                  : next.remove(c.id);
                              set(next);
                            },
                            title: Text(
                                '${c.nameAr} · ${c.nameEn}'),
                            subtitle: Text(
                                '${c.compiler} · ${c.totalHadith ?? '?'} hadith · ${c.downloadSizeMb ?? '?'} MB · ${(c.isDownloaded || dl.installed.contains('hadith:${c.id}')) ? 'Downloaded' : 'Not downloaded'}'),
                          ),
                        ))
                    .toList(),
              );
            },
          ),
        ],
      ),
    );
  }
}
