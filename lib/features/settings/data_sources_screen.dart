import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class DataSourcesScreen extends StatelessWidget {
  const DataSourcesScreen({super.key});

  Future<String> _load() =>
      rootBundle.loadString('assets/licenses/DATA_NOTICES.txt');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Data sources & licenses')),
      body: FutureBuilder<String>(
        future: _load(),
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData) {
            return const Center(
              child: Text('Data notices are unavailable.'),
            );
          }
          return SelectionArea(
            child: ListView(
              padding: const EdgeInsets.all(20),
              children: [
                Text(
                  'Content provenance',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 8),
                Text(
                  'Quran text, translations, tafsir, Hadith, morphology, '
                  'Tajweed annotations and fonts have separate provenance '
                  'and redistribution terms. Unresolved entries are stated '
                  'explicitly rather than inferred from an aggregator license.',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 20),
                Text(
                  snapshot.data!,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        height: 1.5,
                      ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
