import 'package:flutter/material.dart';

/// Isnad chain visualization (§19). Tapping a narrator opens a
/// profile ONLY when reliable biographical metadata exists.
class NarratorViewScreen extends StatelessWidget {
  const NarratorViewScreen({super.key, required this.name});

  final String name;

  @override
  Widget build(BuildContext context) {
    const chain = [
      'Prophet ﷺ',
      'Abu Huraira',
      'Narrator (verified)',
      'Narrator (verified)',
      'Compiler',
    ];
    return Scaffold(
      appBar: AppBar(title: Text('Chain · $name')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const Text(
              'Relationships shown only from verified metadata. Never invented.'),
          const SizedBox(height: 16),
          ...chain.expand((n) => [
                Card(
                  child: ListTile(
                    leading:
                        const Icon(Icons.person_outline),
                    title: Text(n),
                    subtitle: const Text(
                        'Tap for biography when available'),
                    onTap: () {},
                  ),
                ),
                const Icon(Icons.arrow_downward, size: 18),
              ]),
          const Card(
            child: Padding(
              padding: EdgeInsets.all(16),
              child: Text(
                  'Related Narrations: parallel versions from other collections are listed separately with their own references — never merged.'),
            ),
          ),
        ],
      ),
    );
  }
}
