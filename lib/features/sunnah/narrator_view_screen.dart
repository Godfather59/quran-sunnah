import 'package:flutter/material.dart';

import '../../core/l10n/app_strings.dart';

/// Narrator view shown only when the source exposes a narrator name.
///
/// We intentionally do not synthesize an isnad chain or biography. Detailed
/// narrator metadata is rendered only after a verified structured dataset
/// provides it.
class NarratorViewScreen extends StatelessWidget {
  const NarratorViewScreen({super.key, required this.name});

  final String name;

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);

    return Scaffold(
      appBar: AppBar(
        title: Text('${s.t('narratorLabel')} · $name'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Card(
            child: ListTile(
              leading: const Icon(Icons.person_outline),
              title: Text(name),
              subtitle: Text(s.t('narratorDetailsUnavailable')),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Text(s.t('relatedNarrationsHint')),
            ),
          ),
        ],
      ),
    );
  }
}
