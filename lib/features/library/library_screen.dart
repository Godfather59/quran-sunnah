import 'dart:convert';
import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../../core/l10n/app_strings.dart';
import '../../core/widgets/common.dart';
import '../../data/models/library.dart';
import '../../state/database_provider.dart';
import '../../state/download_state.dart';
import '../../state/library_state.dart';
import '../quran/quran_reader_screen.dart';

class LibraryScreen extends ConsumerStatefulWidget {
  const LibraryScreen({super.key});

  @override
  ConsumerState<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends ConsumerState<LibraryScreen> {
  String _query = '';

  String get _normalizedQuery => _query.trim().toLowerCase();

  bool _matches(Iterable<String?> fields) {
    final q = _normalizedQuery;
    if (q.isEmpty) return true;
    return fields
        .whereType<String>()
        .any((value) => value.toLowerCase().contains(q));
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final bookmarks = ref.watch(libraryProvider);
    final notes = ref.watch(notesProvider);
    final collections = ref.watch(collectionsProvider);
    final highlights = ref.watch(highlightsProvider);
    final dl = ref.watch(downloadProvider);

    final filteredBookmarks = bookmarks
        .where((b) => _matches([
              b.refKey,
              b.title,
              b.subtitle,
              b.collectionId == null
                  ? null
                  : _collectionName(collections, b.collectionId!),
            ]))
        .toList(growable: false);
    final filteredNotes = notes
        .where((n) => _matches([n.refKey, n.text]))
        .toList(growable: false);
    final filteredCollections = collections
        .where((c) {
          if (_matches([c.name])) return true;
          return bookmarks
              .where((b) => b.collectionId == c.id)
              .any((b) => _matches([b.refKey, b.title, b.subtitle]));
        })
        .toList(growable: false);
    final filteredHighlights = highlights
        .where((h) => _matches([h.refKey]))
        .toList(growable: false);

    final hasAnyFiltered = filteredBookmarks.isNotEmpty ||
        filteredNotes.isNotEmpty ||
        filteredCollections.isNotEmpty ||
        filteredHighlights.isNotEmpty;

    return Scaffold(
      appBar: AppBar(
        title: Text(s.t('library')),
        actions: [
          IconButton(
            icon: const Icon(Icons.file_upload_outlined),
            tooltip: s.t('importBackup'),
            onPressed: () => _importBackup(context),
          ),
          IconButton(
            icon: const Icon(Icons.ios_share_outlined),
            tooltip: s.t('exportBackup'),
            onPressed: () => _exportBackup(context),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          TextField(
            decoration: InputDecoration(
              prefixIcon: const Icon(Icons.search),
              hintText: s.t('searchLibrary'),
              suffixIcon: _query.isEmpty
                  ? null
                  : IconButton(
                      icon: const Icon(Icons.clear),
                      onPressed: () => setState(() => _query = ''),
                    ),
            ),
            onChanged: (value) => setState(() => _query = value),
          ),
          const SizedBox(height: 12),
          if (_normalizedQuery.isNotEmpty && !hasAnyFiltered)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 24),
              child: Center(child: Text(s.t('noLibraryResults'))),
            ),
          SectionHeader(
            title: s.t('bookmarks'),
            action: '${filteredBookmarks.length}',
          ),
          if (filteredBookmarks.isEmpty)
            Text(
              s.t('noBookmarksYet'),
              style: Theme.of(context).textTheme.bodySmall,
            )
          else
            ...filteredBookmarks.map(
              (b) => Dismissible(
                key: ValueKey(b.id),
                background: Container(color: Colors.redAccent),
                onDismissed: (_) =>
                    ref.read(libraryProvider.notifier).remove(b.id),
                child: ListTile(
                  leading: Icon(
                    b.kind == BookmarkKind.ayah
                        ? Icons.bookmark
                        : Icons.auto_stories,
                  ),
                  title: Text(b.title),
                  subtitle: Text(
                    '${b.subtitle}${b.collectionId != null ? ' · ${_collectionName(collections, b.collectionId!)}' : ''}',
                  ),
                  onTap: b.kind == BookmarkKind.ayah
                      ? () {
                          final parts = b.refKey.split(':');
                          if (parts.length != 2) return;
                          final surah = int.tryParse(parts[0]);
                          final ayah = int.tryParse(parts[1]);
                          if (surah == null || ayah == null) return;
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (_) => QuranReaderScreen(
                                surah: surah,
                                initialAyah: ayah,
                              ),
                            ),
                          );
                        }
                      : null,
                ),
              ),
            ),
          SectionHeader(title: s.t('customCollections')),
          ...filteredCollections.map((collection) {
            final items = bookmarks
                .where((b) => b.collectionId == collection.id)
                .where((b) => _matches([b.refKey, b.title, b.subtitle]))
                .toList(growable: false);
            return Card(
              child: ExpansionTile(
                leading: const Icon(Icons.folder_outlined),
                title: Text(collection.name),
                subtitle: Text('${items.length}'),
                trailing: _CollectionMenu(collection: collection),
                children: items
                    .map(
                      (b) => ListTile(
                        dense: true,
                        title: Text(b.title),
                        subtitle: Text(b.subtitle),
                      ),
                    )
                    .toList(growable: false),
              ),
            );
          }),
          TextButton.icon(
            onPressed: () => _nameDialog(context, null),
            icon: const Icon(Icons.add),
            label: Text(s.t('newCollection')),
          ),
          SectionHeader(
            title: s.t('noteLabel'),
            action: '${filteredNotes.length}',
          ),
          if (filteredNotes.isEmpty && _normalizedQuery.isEmpty)
            UserNoteCard(
              label: s.t('userNoteLabel'),
              text: s.isArabic
                  ? 'ملاحظاتك الشخصية تظهر هنا، منفصلة بصريًا عن النص المقدس.'
                  : s.locale.languageCode == 'fr'
                      ? 'Vos notes personnelles apparaissent ici, visuellement séparées du texte sacré.'
                      : 'Your personal notes appear here, visually separated from sacred text.',
            ),
          for (final note in filteredNotes) ...[
            UserNoteCard(
              label: '${s.t('userNoteLabel')} · ${note.refKey}',
              text: note.text,
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(
                  onPressed: () => _editNote(context, note),
                  child: Text(s.t('notes')),
                ),
                TextButton(
                  onPressed: () =>
                      ref.read(notesProvider.notifier).remove(note.id),
                  child: Text(s.t('delete')),
                ),
              ],
            ),
          ],
          SectionHeader(
            title: s.t('highlight'),
            action: '${filteredHighlights.length}',
          ),
          if (filteredHighlights.isNotEmpty)
            Wrap(
              spacing: 8,
              children: filteredHighlights
                  .map(
                    (h) => Chip(
                      avatar: CircleAvatar(
                        backgroundColor: Color(h.colorValue),
                      ),
                      label: Text(h.refKey),
                      onDeleted: () => ref
                          .read(highlightsProvider.notifier)
                          .remove(h.id),
                    ),
                  )
                  .toList(growable: false),
            ),
          SectionHeader(title: s.t('downloadedContent')),
          Text('${dl.installed.length} ${s.t('downloadedCount')}'),
          const SizedBox(height: 80),
        ],
      ),
    );
  }

  String _collectionName(List<CustomCollection> all, String id) {
    return all.where((c) => c.id == id).firstOrNull?.name ?? id;
  }

  void _nameDialog(BuildContext context, CustomCollection? collection) {
    final ctrl = TextEditingController(text: collection?.name ?? '');
    final s = AppStrings.of(context);
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(
          collection == null
              ? s.t('newCollection')
              : s.t('customCollections'),
        ),
        content: TextField(controller: ctrl, autofocus: true),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(s.t('cancel')),
          ),
          FilledButton(
            onPressed: () {
              if (collection == null) {
                ref.read(collectionsProvider.notifier).add(ctrl.text);
              } else {
                ref
                    .read(collectionsProvider.notifier)
                    .rename(collection.id, ctrl.text);
              }
              Navigator.pop(ctx);
            },
            child: Text(
              collection == null ? s.t('newCollection') : s.t('rename'),
            ),
          ),
        ],
      ),
    ).whenComplete(ctrl.dispose);
  }

  void _editNote(BuildContext context, UserNote note) {
    final ctrl = TextEditingController(text: note.text);
    final s = AppStrings.of(context);
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('${s.t('notes')} · ${note.refKey}'),
        content: TextField(
          controller: ctrl,
          maxLines: 4,
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(s.t('cancel')),
          ),
          FilledButton(
            onPressed: () {
              ref
                  .read(notesProvider.notifier)
                  .upsert(note.refKey, ctrl.text);
              Navigator.pop(ctx);
            },
            child: Text(s.t('notes')),
          ),
        ],
      ),
    ).whenComplete(ctrl.dispose);
  }

  Future<void> _exportBackup(BuildContext context) async {
    final store = await ref.read(libraryStoreProvider);
    final data = await store.backup();
    final dir = await getTemporaryDirectory();
    final file = File('${dir.path}/quran-sunnah-library-v1.json');
    await file.writeAsString(
      const JsonEncoder.withIndent('  ').convert(data),
      flush: true,
    );
    if (!context.mounted) return;
    await SharePlus.instance.share(
      ShareParams(
        files: [XFile(file.path)],
        text: AppStrings.of(context).t('exportBackup'),
      ),
    );
  }

  Future<void> _importBackup(BuildContext context) async {
    final s = AppStrings.of(context);
    try {
      final file = await FilePicker.pickFile(
        type: FileType.custom,
        allowedExtensions: const ['json'],
      );
      if (file == null) return;
      final bytes = await file.readAsBytes();
      final decoded = jsonDecode(utf8.decode(bytes));
      if (decoded is! Map) {
        throw const FormatException('Backup must be a JSON object.');
      }
      final store = await ref.read(libraryStoreProvider);
      await store.restoreBackup(Map<String, dynamic>.from(decoded));
      await Future.wait([
        ref.read(libraryProvider.notifier).reload(),
        ref.read(notesProvider.notifier).reload(),
        ref.read(collectionsProvider.notifier).reload(),
        ref.read(highlightsProvider.notifier).reload(),
        ref.read(recentProvider.notifier).reload(),
      ]);
      if (!context.mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(s.t('backupRestored'))),
      );
    } catch (_) {
      if (!context.mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(s.t('invalidBackup'))),
      );
    }
  }
}

class _CollectionMenu extends ConsumerWidget {
  const _CollectionMenu({required this.collection});

  final CustomCollection collection;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    return PopupMenuButton<String>(
      onSelected: (value) {
        if (value == 'rename') {
          final ctrl = TextEditingController(text: collection.name);
          showDialog<void>(
            context: context,
            builder: (ctx) => AlertDialog(
              title: Text(s.t('customCollections')),
              content: TextField(controller: ctrl),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(ctx),
                  child: Text(s.t('cancel')),
                ),
                FilledButton(
                  onPressed: () {
                    ref
                        .read(collectionsProvider.notifier)
                        .rename(collection.id, ctrl.text);
                    Navigator.pop(ctx);
                  },
                  child: Text(s.t('rename')),
                ),
              ],
            ),
          ).whenComplete(ctrl.dispose);
        } else if (value == 'delete') {
          ref.read(collectionsProvider.notifier).remove(collection.id);
        }
      },
      itemBuilder: (ctx) => [
        PopupMenuItem(
          value: 'rename',
          child: Row(
            children: [
              const Icon(Icons.edit_outlined),
              const SizedBox(width: 8),
              Text(s.t('rename')),
            ],
          ),
        ),
        PopupMenuItem(
          value: 'delete',
          child: Row(
            children: [
              const Icon(Icons.delete_outline),
              const SizedBox(width: 8),
              Text(s.t('delete')),
            ],
          ),
        ),
      ],
    );
  }
}
