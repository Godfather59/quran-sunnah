import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/hadith_repository.dart';

/// Filters backed by fields actually present in the bundled datasets.
/// Unsupported narrator/grade filters are explained rather than faked.
class HadithFilterScreen extends ConsumerStatefulWidget {
  const HadithFilterScreen({super.key});

  @override
  ConsumerState<HadithFilterScreen> createState() =>
      _HadithFilterScreenState();
}

class _HadithFilterScreenState
    extends ConsumerState<HadithFilterScreen> {
  final _book = TextEditingController();
  final _number = TextEditingController();

  @override
  void dispose() {
    _book.dispose();
    _number.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final filter = ref.watch(hadithFilterProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Hadith filters')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          TextField(
              controller: _book,
              decoration:
                  const InputDecoration(labelText: 'Book')),
          TextField(
              controller: _number,
              decoration: const InputDecoration(
                  labelText: 'Hadith number')),
          const SizedBox(height: 12),
          const Card(
            child: ListTile(
              leading: Icon(Icons.info_outline),
              title: Text('Narrator and grading filters are unavailable'),
              subtitle: Text(
                'The bundled Arabic editions do not provide structured '
                'narrator or grading fields. These filters stay disabled '
                'until a verified structured source is added.',
              ),
            ),
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: () {
              ref.read(hadithFilterProvider.notifier).state = HadithFilter(
                collectionIds: filter.collectionIds,
                book: _book.text.trim().isEmpty ? null : _book.text.trim(),
                number:
                    _number.text.trim().isEmpty ? null : _number.text.trim(),
                query: filter.query,
              );
              Navigator.pop(context);
            },
            child: const Text('Apply filters'),
          ),
        ],
      ),
    );
  }
}
