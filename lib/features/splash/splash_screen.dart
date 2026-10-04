import 'package:flutter/material.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Subtle geometric mark: octagon outline, no gradients.
            Container(
              width: 96,
              height: 96,
              decoration: BoxDecoration(
                border: Border.all(color: scheme.primary, width: 2),
                borderRadius: BorderRadius.circular(28),
              ),
              child: Icon(Icons.menu_book_rounded,
                  size: 44, color: scheme.primary),
            ),
            const SizedBox(height: 24),
            const Text(
              'ٱلْقُرْآن وَٱلسُّنَّة',
              style: TextStyle(fontSize: 30, fontWeight: FontWeight.w600),
              textDirection: TextDirection.rtl,
            ),
            const SizedBox(height: 8),
            Text('Quran & Sunnah',
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(color: scheme.onSurfaceVariant)),
            const SizedBox(height: 32),
            const SizedBox(
                width: 32,
                height: 32,
                child: CircularProgressIndicator(strokeWidth: 3)),
          ],
        ),
      ),
    );
  }
}
