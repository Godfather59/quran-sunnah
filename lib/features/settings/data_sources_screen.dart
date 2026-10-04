import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../core/l10n/app_strings.dart';

class DataSourcesScreen extends StatelessWidget {
  const DataSourcesScreen({super.key});

  Future<String> _load() =>
      rootBundle.loadString('assets/licenses/DATA_NOTICES.txt');

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(s.t('dataSourcesLicenses'))),
      body: FutureBuilder<String>(
        future: _load(),
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (!snapshot.hasData) {
            return Center(child: Text(s.t('dataNoticesUnavailable')));
          }
          return SelectionArea(
            child: ListView(
              padding: const EdgeInsets.all(20),
              children: [
                Text(
                  s.t('contentProvenance'),
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 8),
                Text(
                  s.t('provenanceExplanation'),
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
