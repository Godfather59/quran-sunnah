import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:share_plus/share_plus.dart';
import '../../../core/l10n/app_strings.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/quran.dart';
import '../../../data/content/content_packages.dart';
import '../../../data/repositories/translation_repository.dart';
import '../../../data/repositories/word_repository.dart';
import '../../../data/services/audio_service.dart';
import '../../../state/library_state.dart';
import '../../../state/download_state.dart';
import '../compare_riwayat_screen.dart';
import '../tafsir_screen.dart';

/// Ayah bottom sheet per spec §10. No permanent clutter on reader.
Future<void> showAyahActionSheet(BuildContext context, Ayah ayah) {
  return showModalBottomSheet(
    context: context,
    showDragHandle: true,
    isScrollControlled: true,
    builder: (ctx) => _AyahSheet(ayah: ayah),
  );
}

class _AyahSheet extends ConsumerWidget {
  const _AyahSheet({required this.ayah});

  final Ayah ayah;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final bookmarked =
        ref.watch(libraryProvider).any((b) => b.refKey == ayah.key);
    final audio = ref.watch(audioServiceProvider);
    final riwayaKey = ayah.editionId.split('__').first;
    final canStream =
        ref.read(audioServiceProvider.notifier).streamPrefixFor(riwayaKey) !=
            null;
    Future<void> play({required bool repeat}) async {
      Navigator.pop(context);
      await ref.read(audioServiceProvider.notifier).playRange(
            riwayaKey: riwayaKey,
            surah: ayah.surah,
            fromAyah: ayah.canonicalAyahNumber,
            repeatAyah: repeat,
          );
      if (context.mounted &&
          ref.read(audioServiceProvider).error != null) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text(
                ref.read(audioServiceProvider).error!)));
      }
    }

    final actions = <_Action>[
      _Action(
          audio.playing && audio.refKey == ayah.key
              ? Icons.pause
              : Icons.play_arrow,
          s.t('play'),
          canStream ? () => play(repeat: false) : () {}),
      _Action(Icons.repeat, s.t('repeat'),
          canStream ? () => play(repeat: true) : () {}),
      _Action(Icons.menu_book, s.t('tafsir'), () {
        Navigator.pop(context);
        Navigator.of(context).push(MaterialPageRoute(
            builder: (_) =>
                TafsirScreen(surah: ayah.canonicalSurahNumber, ayah: ayah.canonicalAyahNumber)));
      }),
      _Action(Icons.translate, s.t('translation'), () {
        Navigator.pop(context);
        showModalBottomSheet(
          context: context,
          showDragHandle: true,
          isScrollControlled: true,
          builder: (_) => _TranslationSheet(ayah: ayah),
        );
      }),
      _Action(Icons.spellcheck, s.t('wordMeanings'), () {
        Navigator.pop(context);
        showModalBottomSheet(
          context: context,
          showDragHandle: true,
          isScrollControlled: true,
          builder: (_) => _WordMeaningsSheet(ayah: ayah),
        );
      }),
      _Action(bookmarked ? Icons.bookmark : Icons.bookmark_outline,
          s.t('bookmark'), () {
        ref.read(libraryProvider.notifier).toggleAyah(ayah.canonicalSurahNumber, ayah.canonicalAyahNumber);
        Navigator.pop(context);
      }),
      _Action(Icons.create_new_folder_outlined,
          s.t('addToCollection'), () {
        Navigator.pop(context);
        showModalBottomSheet(
          context: context,
          showDragHandle: true,
          builder: (_) => _CollectionSheet(ayah: ayah),
        );
      }),
      _Action(Icons.copy, s.t('copy'), () {
        Clipboard.setData(ClipboardData(text: ayah.text));
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(s.t('copy'))));
      }),
      _Action(Icons.share, s.t('share'), () {
        SharePlus.instance.share(
          ShareParams(
            text: '${s.t('quran')} ${ayah.surah}:${ayah.displayAyahNumber}',
          ),
        );
      }),
      _Action(Icons.edit_note, s.t('notes'), () {
        Navigator.pop(context);
        showModalBottomSheet(
          context: context,
          showDragHandle: true,
          isScrollControlled: true,
          builder: (_) => _NoteSheet(refKey: ayah.key),
        );
      }),
      _Action(Icons.highlight_outlined, s.t('highlight'), () {
        Navigator.pop(context);
        showModalBottomSheet(
          context: context,
          showDragHandle: true,
          builder: (_) => _HighlightSheet(refKey: ayah.key),
        );
      }),
      _Action(Icons.compare_arrows, s.t('compareRiwayat'), () {
        Navigator.pop(context);
        Navigator.of(context).push(MaterialPageRoute(
            builder: (_) => CompareRiwayatScreen(
                surah: ayah.canonicalSurahNumber, ayah: ayah.canonicalAyahNumber)));
      }),
    ];

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('ayahLabel')} ${ayah.surah}:${ayah.displayAyahNumber}',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              gridDelegate:
                  const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 4, childAspectRatio: 0.85),
              itemCount: actions.length,
              itemBuilder: (_, i) => InkWell(
                borderRadius: BorderRadius.circular(12),
                onTap: actions[i].onTap,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(actions[i].icon),
                    const SizedBox(height: 6),
                    Text(actions[i].label,
                        textAlign: TextAlign.center,
                        style: Theme.of(context).textTheme.labelSmall),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Action {
  _Action(this.icon, this.label, this.onTap);
  final IconData icon;
  final String label;
  final VoidCallback onTap;
}

/// All bundled translations for one ayah, translators credited.
class _TranslationSheet extends ConsumerWidget {
  const _TranslationSheet({required this.ayah});

  final Ayah ayah;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('translation')} · ${ayah.surah}:${ayah.displayAyahNumber}',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            Flexible(
              child: ListView(
                shrinkWrap: true,
                children: kTranslationCatalog
                    .where((t) => kTranslationAssets.containsKey(t.id))
                    .map((t) {
                  final packageId = 'quran:${t.id}';
                  final installed =
                      ref.watch(downloadProvider).installed.contains(packageId);
                  if (!installed) {
                    return Card(
                      child: ListTile(
                        title: Text(t.translator),
                        subtitle: Text(s.t('downloadBeforeUse')),
                        trailing: IconButton(
                          tooltip: s.t('download'),
                          icon: const Icon(Icons.download),
                          onPressed: () async {
                            try {
                              await ref
                                  .read(downloadProvider.notifier)
                                  .install(packageId);
                              ref
                                  .read(contentRevisionProvider.notifier)
                                  .bump();
                            } catch (_) {}
                          },
                        ),
                      ),
                    );
                  }
                  final texts =
                      ref.watch(translationTextsProvider(t.id));
                  return texts.when(
                    loading: () => const Padding(
                      padding: EdgeInsets.all(16),
                      child: Center(child: CircularProgressIndicator()),
                    ),
                    error: (_, _) => Text(s.t('contentUnavailable')),
                    data: (map) {
                      final text = map[ayah.key];
                      if (text == null) {
                        return Text(s.t('contentUnavailable'));
                      }
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              t.translator,
                              style: Theme.of(context).textTheme.labelSmall,
                            ),
                            Text(
                              text,
                              style: AppTheme.translation(context),
                            ),
                          ],
                        ),
                      );
                    },
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Personal note editor. Notes are user content, visually distinct.
class _NoteSheet extends ConsumerStatefulWidget {
  const _NoteSheet({required this.refKey});

  final String refKey;

  @override
  ConsumerState<_NoteSheet> createState() => _NoteSheetState();
}

class _NoteSheetState extends ConsumerState<_NoteSheet> {
  late final TextEditingController _ctrl;

  @override
  void initState() {
    super.initState();
    _ctrl = TextEditingController(
        text: ref
                .read(notesProvider)
                .where((n) => n.refKey == widget.refKey)
                .firstOrNull
                ?.text ??
            '');
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return SafeArea(
      child: Padding(
        padding: EdgeInsets.fromLTRB(
            20, 8, 20, MediaQuery.of(context).viewInsets.bottom + 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('notes')} · ${widget.refKey}',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 4),
            Text(s.t('userNoteLabel'),
                style: Theme.of(context).textTheme.labelSmall),
            const SizedBox(height: 12),
            TextField(
              controller: _ctrl,
              maxLines: 4,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(
                    onPressed: () =>
                        Navigator.pop(context),
                    child: Text(s.t('cancel'))),
                const SizedBox(width: 8),
                FilledButton(
                    onPressed: () {
                      ref
                          .read(notesProvider.notifier)
                          .upsert(widget.refKey, _ctrl.text);
                      Navigator.pop(context);
                    },
                    child: Text(s.t('notes'))),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// Assign the ayah bookmark to a custom collection.
class _CollectionSheet extends ConsumerWidget {
  const _CollectionSheet({required this.ayah});

  final Ayah ayah;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final collections = ref.watch(collectionsProvider);
    final bookmarks = ref.watch(libraryProvider);
    final current = bookmarks
        .where((b) => b.refKey == ayah.key)
        .firstOrNull
        ?.collectionId;
    final nameCtrl = TextEditingController();
    void pick(String? id) {
      ref
          .read(libraryProvider.notifier)
          .setAyahCollection(ayah.canonicalSurahNumber, ayah.canonicalAyahNumber, id);
      Navigator.pop(context);
    }

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('addToCollection')} · ${ayah.key}',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            Flexible(
              child: RadioGroup<String?>(
                groupValue: current,
                onChanged: (v) => pick(v),
                child: ListView(
                  shrinkWrap: true,
                  children: [
                    ...collections.map((c) =>
                        RadioListTile<String?>(
                          value: c.id,
                          title: Text(c.name),
                        )),
                    RadioListTile<String?>(
                      value: null,
                      title: Text(s.t('cancel')),
                    ),
                  ],
                ),
              ),
            ),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: nameCtrl,
                    decoration: InputDecoration(
                        hintText: s.t('newCollection')),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.add),
                  onPressed: () {
                    ref
                        .read(collectionsProvider.notifier)
                        .add(nameCtrl.text);
                    nameCtrl.clear();
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// Highlight color picker (user markup, translucent wash).
class _HighlightSheet extends ConsumerWidget {
  const _HighlightSheet({required this.refKey});

  final String refKey;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final current = ref
        .watch(highlightsProvider)
        .where((h) => h.refKey == refKey)
        .firstOrNull
        ?.colorValue;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('highlight')} · $refKey',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ...kHighlightColors.map((c) => InkWell(
                      borderRadius: BorderRadius.circular(24),
                      onTap: () {
                        ref
                            .read(highlightsProvider.notifier)
                            .toggle(refKey, c.toARGB32());
                        Navigator.pop(context);
                      },
                      child: Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          color: c,
                          shape: BoxShape.circle,
                          border: current == c.toARGB32()
                              ? Border.all(
                                  width: 3,
                                  color: Theme.of(context)
                                      .colorScheme
                                      .primary)
                              : null,
                        ),
                      ),
                    )),
                InkWell(
                  borderRadius: BorderRadius.circular(24),
                  onTap: () {
                    final existing = ref
                        .read(highlightsProvider)
                        .where((h) => h.refKey == refKey)
                        .firstOrNull;
                    if (existing != null) {
                      ref
                          .read(highlightsProvider.notifier)
                          .remove(existing.id);
                    }
                    Navigator.pop(context);
                  },
                  child: Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      border: Border.all(
                          color: Theme.of(context)
                              .colorScheme
                              .outline),
                    ),
                    child: const Icon(
                        Icons.highlight_remove_outlined),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
/// Word-by-word view backed by the verified morphology dataset:
/// each token shows lemma/root/POS; tokens without an entry show the
/// word only (never guessed). Source credited in the footer.
class _WordMeaningsSheet extends ConsumerWidget {
  const _WordMeaningsSheet({required this.ayah});

  final Ayah ayah;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final isHafs = ayah.editionId.startsWith('hafs-an-asim__');
    final wordsInstalled =
        ref.watch(downloadProvider).installed.contains('quran:words-hafs');
    final wordsAsync = isHafs && wordsInstalled
        ? ref.watch(wordSurahProvider(ayah.surah))
        : null;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('wordMeanings')} · ${ayah.key}',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            Flexible(
              child: !isHafs
                  ? Text(s.t('contentUnavailable'))
                  : !wordsInstalled
                      ? Center(
                          child: FilledButton.icon(
                            onPressed: () async {
                              try {
                                await ref
                                    .read(downloadProvider.notifier)
                                    .install('quran:words-hafs');
                                ref
                                    .read(contentRevisionProvider.notifier)
                                    .bump();
                              } catch (_) {}
                            },
                            icon: const Icon(Icons.download),
                            label: Text(s.t('download')),
                          ),
                        )
                      : wordsAsync!.when(
                          loading: () => const Center(
                            child: CircularProgressIndicator(),
                          ),
                          error: (_, _) =>
                              Text(s.t('contentUnavailable')),
                          data: (map) {
                            final words =
                                map[ayah.canonicalAyahNumber];
                            if (words == null || words.isEmpty) {
                              return Text(s.t('contentUnavailable'));
                            }
                            return ListView.separated(
                              shrinkWrap: true,
                              itemCount: words.length,
                              separatorBuilder: (_, _) =>
                                  const Divider(height: 1),
                              itemBuilder: (context, i) {
                                final w = words[i];
                                if (w.isMark) {
                                  return ListTile(
                                    dense: true,
                                    title: Text(
                                      w.word,
                                      textDirection: TextDirection.rtl,
                                      textAlign: TextAlign.right,
                                      style: const TextStyle(fontSize: 18),
                                    ),
                                    subtitle: const Text('۝'),
                                  );
                                }
                                return ListTile(
                                  dense: true,
                                  title: Text(
                                    w.word,
                                    textDirection: TextDirection.rtl,
                                    textAlign: TextAlign.right,
                                    style: const TextStyle(fontSize: 20),
                                  ),
                                  subtitle: w.hasGloss
                                      ? Text(
                                          '${w.lemma}'
                                          '${w.root.isNotEmpty ? ' · √${w.root}' : ''}'
                                          '${w.pos.isNotEmpty ? ' · ${w.pos}' : ''}',
                                          textDirection: TextDirection.rtl,
                                        )
                                      : null,
                                );
                              },
                            );
                          },
                        ),
            ),
            const SizedBox(height: 8),
            Text(kWordSource,
                style: Theme.of(context).textTheme.labelSmall),
            const SizedBox(height: 8),
            FilledButton.tonal(
              onPressed: () {
                Navigator.pop(context);
                Navigator.of(context).push(MaterialPageRoute(
                    builder: (_) => TafsirScreen(
                        surah: ayah.canonicalSurahNumber,
                        ayah: ayah.canonicalAyahNumber)));
              },
              child: Text(s.t('tafsir')),
            ),
          ],
        ),
      ),
    );
  }
}
