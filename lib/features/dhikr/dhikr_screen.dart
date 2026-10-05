import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/seed/adhkar.dart';
import '../../state/dhikr_provider.dart';

class DhikrScreen extends ConsumerWidget {
  const DhikrScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final state = ref.watch(dhikrProvider);
    final todayTotal =
        state.today.values.fold<int>(0, (a, b) => a + b);

    return Scaffold(
      appBar: AppBar(
        title: Text(s.isArabic
            ? 'الأذكار'
            : s.locale.languageCode == 'fr'
                ? 'Dhikr'
                : 'Dhikr'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            color: Theme.of(context).colorScheme.primaryContainer,
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  const Icon(Icons.fingerprint, size: 32),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment:
                          CrossAxisAlignment.start,
                      children: [
                        Text(
                          s.isArabic
                              ? 'اليوم: $todayTotal · الكل: ${state.total}'
                              : 'Today: $todayTotal · Total: ${state.total}',
                          style: Theme.of(context)
                              .textTheme
                              .titleMedium
                              ?.copyWith(
                                  fontWeight:
                                      FontWeight.w700),
                        ),
                        Text(
                          s.isArabic
                              ? 'اضغط على البطاقة للعد'
                              : 'Tap a card to count',
                          style: Theme.of(context)
                              .textTheme
                              .bodySmall,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          for (final d in kAdhkar)
            Builder(
              builder: (context) {
                final count = state.today[d.id] ?? 0;
                final done = count >= d.target;
                return Card(
                  child: InkWell(
                    borderRadius:
                        BorderRadius.circular(12),
                    onTap: () {
                      HapticFeedback.lightImpact();
                      ref
                          .read(dhikrProvider.notifier)
                          .tap(d.id);
                    },
                    onLongPress: () => ref
                        .read(dhikrProvider.notifier)
                        .reset(d.id),
                    child: Padding(
                      padding:
                          const EdgeInsets.all(18),
                      child: Column(
                        crossAxisAlignment:
                            CrossAxisAlignment.stretch,
                        children: [
                          Text(
                            d.textAr,
                            textDirection:
                                TextDirection.rtl,
                            textAlign: TextAlign.right,
                            style: const TextStyle(
                                fontSize: 26,
                                height: 2.0),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            s.isArabic
                                ? d.transliteration
                                : s.locale.languageCode ==
                                        'fr'
                                    ? '${d.transliteration} · ${d.translationFr}'
                                    : '${d.transliteration} · ${d.translationEn}',
                            style: Theme.of(context)
                                .textTheme
                                .bodySmall,
                          ),
                          const SizedBox(height: 8),
                          ClipRRect(
                            borderRadius:
                                BorderRadius.circular(
                                    8),
                            child:
                                LinearProgressIndicator(
                              value: (count / d.target)
                                  .clamp(0.0, 1.0),
                              minHeight: 6,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              Text(
                                '$count / ${d.target}',
                                style: Theme.of(context)
                                    .textTheme
                                    .titleMedium
                                    ?.copyWith(
                                        fontWeight:
                                            FontWeight
                                                .w700,
                                        color: done
                                            ? Theme.of(
                                                    context)
                                                .colorScheme
                                                .primary
                                            : null),
                              ),
                              if (done)
                                const Padding(
                                  padding:
                                      EdgeInsets.only(
                                          left: 8),
                                  child: Icon(
                                      Icons
                                          .check_circle,
                                      size: 20),
                                ),
                              const Spacer(),
                              Text(
                                d.source,
                                style: Theme.of(
                                        context)
                                    .textTheme
                                    .labelSmall,
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
          Center(
            child: TextButton(
              onPressed: () => showDialog(
                context: context,
                builder: (_) => AlertDialog(
                  title: const Text('ℹ'),
                  content: Text(s.isArabic
                      ? 'اضغط مطولًا لإعادة التصفير. العد يومي ويُحفظ على جهازك.'
                      : 'Long-press to reset. Counts are daily, stored on-device.'),
                  actions: [
                    TextButton(
                      onPressed: () =>
                          Navigator.pop(context),
                      child: const Text('OK'),
                    ),
                  ],
                ),
              ),
              child: Text(s.isArabic
                  ? 'كيف يعمل؟'
                  : 'How it works?'),
            ),
          ),
        ],
      ),
    );
  }
}
