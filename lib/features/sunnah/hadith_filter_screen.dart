import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/repositories/hadith_repository.dart';

/// Hadith filters (§17): collection/book/chapter/narrator/topic/
/// number/grade + grading authority. Disputed grades shown as-is.
class HadithFilterScreen extends ConsumerStatefulWidget {
  const HadithFilterScreen({super.key});

  @override
  ConsumerState<HadithFilterScreen> createState() =>
      _HadithFilterScreenState();
}

class _HadithFilterScreenState
    extends ConsumerState<HadithFilterScreen> {
  final _book = TextEditingController();
  final _narrator = TextEditingController();
  final _number = TextEditingController();
  String? _grade;

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
              controller: _narrator,
              decoration: const InputDecoration(
                  labelText: 'Narrator / Companion')),
          TextField(
              controller: _number,
              decoration: const InputDecoration(
                  labelText: 'Hadith number')),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _grade,
            hint: const Text('Grade (Sahih / Hasan / Da’if…)'),
            items: const ['Sahih', 'Hasan', 'Da’if', 'Mawdu‘']
                .map((g) =>
                    DropdownMenuItem(value: g, child: Text(g)))
                .toList(),
            onChanged: (v) => setState(() => _grade = v),
          ),
          const SizedBox(height: 8),
          const Text(
            'Grade is always shown with its scholar/source. Disputed grading is never presented as consensus.',
            style: TextStyle(fontSize: 12),
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: () {
              ref.read(hadithFilterProvider.notifier).state =
                  filter.copyWith(
                book:
                    _book.text.isEmpty ? null : _book.text,
                narrator: _narrator.text.isEmpty
                    ? null
                    : _narrator.text,
                grade: _grade,
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
