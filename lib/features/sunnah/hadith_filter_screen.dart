import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/repositories/hadith_repository.dart';

class HadithFilterScreen extends ConsumerStatefulWidget {
  const HadithFilterScreen({super.key});

  @override
  ConsumerState<HadithFilterScreen> createState() =>
      _HadithFilterScreenState();
}

class _HadithFilterScreenState extends ConsumerState<HadithFilterScreen> {
  final _book = TextEditingController();
  final _number = TextEditingController();
  final _narrator = TextEditingController();
  final _grade = TextEditingController();
  final _topic = TextEditingController();

  @override
  void initState() {
    super.initState();
    final filter = ref.read(hadithFilterProvider);
    _book.text = filter.book ?? '';
    _number.text = filter.number ?? '';
    _narrator.text = filter.narrator ?? '';
    _grade.text = filter.grade ?? '';
    _topic.text = filter.topic ?? '';
  }

  @override
  void dispose() {
    _book.dispose();
    _number.dispose();
    _narrator.dispose();
    _grade.dispose();
    _topic.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final filter = ref.watch(hadithFilterProvider);
    final repo = ref.watch(hadithRepositoryProvider);

    return Scaffold(
      appBar: AppBar(title: Text(s.t('filters'))),
      body: FutureBuilder<HadithCapabilities>(
        future: repo.capabilities(filter.collectionIds),
        builder: (context, snapshot) {
          final caps = snapshot.data ?? const HadithCapabilities();
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              TextField(
                controller: _book,
                decoration: InputDecoration(labelText: s.t('book')),
              ),
              TextField(
                controller: _number,
                keyboardType: TextInputType.number,
                decoration: InputDecoration(labelText: s.t('hadithNumber')),
              ),
              const SizedBox(height: 12),
              // Rawi chooser: famous narrators as one-tap shortcuts plus
              // free text. Always an honest matn text search — no
              // structured narrator dataset ships yet.
              DropdownMenu<String>(
                controller: _narrator,
                enableFilter: true,
                requestFocusOnTap: true,
                expandedInsets: EdgeInsets.zero,
                label: Text(s.t('tabNarrator')),
                helperText: s.isArabic
                    ? 'اختر راويًا أو اكتب اسمًا — بحث نصي في المتن.'
                    : 'Pick a narrator or type a name — matn text search.',
                dropdownMenuEntries: const [
                  DropdownMenuEntry(value: 'أبو هريرة', label: 'أبو هريرة'),
                  DropdownMenuEntry(value: 'عائشة', label: 'عائشة'),
                  DropdownMenuEntry(
                      value: 'عبد الله بن عباس',
                      label: 'عبد الله بن عباس'),
                  DropdownMenuEntry(
                      value: 'عبد الله بن عمر',
                      label: 'عبد الله بن عمر'),
                  DropdownMenuEntry(
                      value: 'أنس بن مالك', label: 'أنس بن مالك'),
                  DropdownMenuEntry(
                      value: 'جابر بن عبد الله',
                      label: 'جابر بن عبد الله'),
                  DropdownMenuEntry(
                      value: 'أبو سعيد الخدري',
                      label: 'أبو سعيد الخدري'),
                  DropdownMenuEntry(
                      value: 'عبد الله بن مسعود',
                      label: 'عبد الله بن مسعود'),
                  DropdownMenuEntry(
                      value: 'عمر بن الخطاب', label: 'عمر بن الخطاب'),
                  DropdownMenuEntry(
                      value: 'علي بن أبي طالب',
                      label: 'علي بن أبي طالب'),
                ],
              ),
              TextField(
                controller: _grade,
                enabled: caps.grade,
                decoration: InputDecoration(
                  labelText: s.t('displayGrade'),
                  suffixIcon: caps.grade ? null : const Icon(Icons.lock_outline),
                ),
              ),
              TextField(
                controller: _topic,
                enabled: caps.topics,
                decoration: InputDecoration(
                  labelText: s.t('tabTopic'),
                  suffixIcon:
                      caps.topics ? null : const Icon(Icons.lock_outline),
                ),
              ),
              if (!caps.anyStructured) ...[
                const SizedBox(height: 12),
                Card(
                  child: ListTile(
                    leading: const Icon(Icons.verified_user_outlined),
                    title: Text(s.t('structuredFiltersUnavailable')),
                  ),
                ),
              ],
              const SizedBox(height: 20),
              FilledButton(
                onPressed: () {
                  String? value(TextEditingController controller, bool enabled) {
                    if (!enabled) return null;
                    final trimmed = controller.text.trim();
                    return trimmed.isEmpty ? null : trimmed;
                  }

                  ref.read(hadithFilterProvider.notifier).state = HadithFilter(
                    collectionIds: filter.collectionIds,
                    book: value(_book, true),
                    number: value(_number, true),
                    narrator: value(_narrator, true),
                    grade: value(_grade, caps.grade),
                    topic: value(_topic, caps.topics),
                    query: filter.query,
                  );
                  Navigator.pop(context);
                },
                child: Text(s.t('applyFilters')),
              ),
            ],
          );
        },
      ),
    );
  }
}
