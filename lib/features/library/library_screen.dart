import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../../core/l10n/app_strings.dart';
import '../../core/widgets/common.dart';
import '../../data/models/library.dart';
import '../../state/download_state.dart';
import '../../state/library_state.dart';
import '../quran/quran_reader_screen.dart';

/// Library (§22–23): bookmarks, highlights, notes (edit/delete),
/// custom collections (create/rename/delete, items grouped),
/// recently viewed, downloads, JSON export of personal data.
class LibraryScreen extends ConsumerWidget {
  const LibraryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final bookmarks = ref.watch(libraryProvider);
    final notes = ref.watch(notesProvider);
    final collections = ref.watch(collectionsProvider);
    final highlights = ref.watch(highlightsProvider);
    final dl = ref.watch(downloadProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(s.t('library')),
        actions: [
          IconButton(
            icon: const Icon(Icons.ios_share_outlined),
            tooltip: 'Export',
            onPressed: () => _export(context, ref),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          SectionHeader(
              title: s.t('bookmarks'),
              action: '${bookmarks.length}'),
          if (bookmarks.isEmpty)
            Text(s.t('noBookmarksYet'),
                style: Theme.of(context).textTheme.bodySmall)
          else
            ...bookmarks.map((b) => Dismissible(
                  key: ValueKey(b.id),
                  background:
                      Container(color: Colors.redAccent),
                  onDismissed: (_) => ref
                      .read(libraryProvider.notifier)
                      .remove(b.id),
                  child: ListTile(
                    leading: Icon(b.kind == BookmarkKind.ayah
                        ? Icons.bookmark
                        : Icons.auto_stories),
                    title: Text(b.title),
                    subtitle: Text(
                        '${b.subtitle}${b.collectionId != null ? ' · ${_collectionName(collections, b.collectionId!)}' : ''}'),
                    onTap: b.kind == BookmarkKind.ayah
                        ? () {
                            final parts =
                                b.refKey.split(':');
                            Navigator.of(context).push(
                                MaterialPageRoute(
                                    builder: (_) =>
                                        QuranReaderScreen(
                                          surah: int.parse(
                                              parts[0]),
                                          initialAyah:
                                              int.parse(
                                                  parts[1]),
                                        )));
                          }
                        : null,
                  ),
                )),
          SectionHeader(title: s.t('customCollections')),
          ...collections.map((c) {
            final items = bookmarks
                .where((b) => b.collectionId == c.id)
                .toList();
            return Card(
              child: ExpansionTile(
                leading: const Icon(Icons.folder_outlined),
                title: Text(c.name),
                subtitle: Text('${items.length}'),
                trailing: _CollectionMenu(collection: c),
                children: items
                    .map((b) => ListTile(
                          dense: true,
                          title: Text(b.title),
                          subtitle: Text(b.subtitle),
                        ))
                    .toList(),
              ),
            );
          }),
          TextButton.icon(
            onPressed: () =>
                _nameDialog(context, ref, null),
            icon: const Icon(Icons.add),
            label: Text(s.t('newCollection')),
          ),
          SectionHeader(
              title: s.t('noteLabel'),
              action: '${notes.length}'),
          if (notes.isEmpty)
            UserNoteCard(
                label: s.t('userNoteLabel'),
                text: s.isArabic
                    ? 'ملاحظاتك الشخصية تظهر هنا، منفصلة بصريًا عن النص المقدس.'
                    : 'Your personal notes appear here, visually separated from sacred text.'),
          ...notes.map((n) => UserNoteCard(
                label: '${s.t('userNoteLabel')} · ${n.refKey}',
                text: n.text,
              )),
          ...notes.map((n) => Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton(
                      onPressed: () =>
                          _editNote(context, ref, n),
                      child: Text(s.t('notes'))),
                  TextButton(
                      onPressed: () => ref
                          .read(notesProvider.notifier)
                          .remove(n.id),
                      child: Text(s.t('cancel'))),
                ],
              )),
          SectionHeader(
              title: s.t('highlight'),
              action: '${highlights.length}'),
          if (highlights.isNotEmpty)
            Wrap(
              spacing: 8,
              children: highlights
                  .map((h) => Chip(
                        avatar: CircleAvatar(
                            backgroundColor:
                                Color(h.colorValue)),
                        label: Text(h.refKey),
                        onDeleted: () => ref
                            .read(highlightsProvider
                                .notifier)
                            .remove(h.id),
                      ))
                  .toList(),
            ),
          SectionHeader(title: s.t('downloadedContent')),
          Text(
              '${dl.installed.length} ${s.t('downloadedCount')}'),
          const SizedBox(height: 80),
        ],
      ),
    );
  }

  String _collectionName(
      List<CustomCollection> all, String id) {
    return all
            .where((c) => c.id == id)
            .firstOrNull
            ?.name ??
        id;
  }

  void _nameDialog(
      BuildContext context, WidgetRef ref, CustomCollection? c) {
    final ctrl = TextEditingController(text: c?.name ?? '');
    final s = AppStrings.of(context);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(c == null
            ? s.t('newCollection')
            : s.t('customCollections')),
        content: TextField(
            controller: ctrl, autofocus: true),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: Text(s.t('cancel'))),
          FilledButton(
              onPressed: () {
                if (c == null) {
                  ref
                      .read(collectionsProvider.notifier)
                      .add(ctrl.text);
                } else {
                  ref
                      .read(collectionsProvider.notifier)
                      .rename(c.id, ctrl.text);
                }
                Navigator.pop(ctx);
              },
              child: Text(s.t('newCollection'))),
        ],
      ),
    );
  }

  void _editNote(
      BuildContext context, WidgetRef ref, UserNote n) {
    final ctrl = TextEditingController(text: n.text);
    final s = AppStrings.of(context);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('${s.t('notes')} · ${n.refKey}'),
        content: TextField(
            controller: ctrl, maxLines: 4, autofocus: true),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: Text(s.t('cancel'))),
          FilledButton(
              onPressed: () {
                ref
                    .read(notesProvider.notifier)
                    .upsert(n.refKey, ctrl.text);
                Navigator.pop(ctx);
              },
              child: Text(s.t('notes'))),
        ],
      ),
    );
  }

  /// Export personal library data (bookmarks/notes/highlights/
  /// collections) as JSON via share sheet. Sacred texts are NOT
  /// exported — only references + user content.
  Future<void> _export(BuildContext context, WidgetRef ref) async {
    final data = {
      'bookmarks': ref.read(libraryProvider).map((b) => {
            'kind': b.kind.name,
            'ref': b.refKey,
            'title': b.title,
            'collection': b.collectionId,
          }).toList(),
      'notes': ref.read(notesProvider).map((n) => {
            'ref': n.refKey,
            'text': n.text,
          }).toList(),
      'highlights': ref.read(highlightsProvider).map((h) => {
            'ref': h.refKey,
            'color': h.colorValue,
          }).toList(),
      'collections':
          ref.read(collectionsProvider).map((c) => {
                'id': c.id,
                'name': c.name,
              }).toList(),
    };
    final dir = await getTemporaryDirectory();
    final file = File('${dir.path}/quran-sunnah-library.json');
    await file.writeAsString(
        const JsonEncoder.withIndent('  ').convert(data));
    await Share.shareXFiles([XFile(file.path)],
        text: 'Quran & Sunnah library export');
  }
}

class _CollectionMenu extends ConsumerWidget {
  const _CollectionMenu({required this.collection});

  final CustomCollection collection;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    return PopupMenuButton<String>(
      onSelected: (v) {
        if (v == 'rename') {
          final ctrl =
              TextEditingController(text: collection.name);
          final s = AppStrings.of(context);
          showDialog(
            context: context,
            builder: (ctx) => AlertDialog(
              title: Text(s.t('customCollections')),
              content: TextField(controller: ctrl),
              actions: [
                TextButton(
                    onPressed: () => Navigator.pop(ctx),
                    child: Text(s.t('cancel'))),
                FilledButton(
                    onPressed: () {
                      ref
                          .read(
                              collectionsProvider.notifier)
                          .rename(collection.id, ctrl.text);
                      Navigator.pop(ctx);
                    },
                    child: Text(s.t('notes'))),
              ],
            ),
          );
        } else if (v == 'delete') {
          ref
              .read(collectionsProvider.notifier)
              .remove(collection.id);
        }
      },
      itemBuilder: (ctx) => [
        PopupMenuItem(
            value: 'rename',
            child: Row(children: [
              const Icon(Icons.edit_outlined),
              const SizedBox(width: 8),
              Text(s.t('rename')),
            ])),
        PopupMenuItem(
            value: 'delete',
            child: Row(children: [
              const Icon(Icons.delete_outline),
              const SizedBox(width: 8),
              Text(s.t('delete')),
            ])),
      ],
    );
  }
}
