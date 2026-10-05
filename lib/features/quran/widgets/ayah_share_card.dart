import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../../../data/models/quran.dart';

final GlobalKey ayahShareCardKey = GlobalKey();

/// Pretty, brand-styled ayah card for image sharing.
/// Wrap in RepaintBoundary keyed by [ayahShareCardKey], capture via shareAyahImage().
class AyahShareCard extends StatelessWidget {
  const AyahShareCard({
    super.key,
    required this.ayah,
    required this.surahNameAr,
  });

  final Ayah ayah;
  final String surahNameAr;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return RepaintBoundary(
      key: ayahShareCardKey,
      child: Container(
        width: 1080,
        padding: const EdgeInsets.all(64),
        decoration: BoxDecoration(
          color: scheme.primaryContainer,
          borderRadius: BorderRadius.circular(32),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              surahNameAr,
              textDirection: TextDirection.rtl,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontFamily: 'Amiri Quran',
                fontSize: 40,
                color: scheme.onPrimaryContainer,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              ayah.text,
              textDirection: TextDirection.rtl,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontFamily: 'Amiri Quran',
                fontSize: 56,
                height: 2.0,
                color: scheme.onPrimaryContainer,
              ),
            ),
            const SizedBox(height: 24),
            Text(
              '﴿${ayah.displayAyahNumber}﴾ · ${ayah.surah}:${ayah.displayAyahNumber}',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 28,
                color:
                    scheme.onPrimaryContainer.withValues(alpha: 0.8),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Quran & Sunnah · verified text',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 22,
                color:
                    scheme.onPrimaryContainer.withValues(alpha: 0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Captures [ayahShareCardKey] to PNG and shares it. Returns true on success.
Future<bool> shareAyahImage() async {
  try {
    final boundary = ayahShareCardKey.currentContext
        ?.findRenderObject() as RenderRepaintBoundary?;
    if (boundary == null) return false;
    final image = await boundary.toImage(pixelRatio: 3.0);
    final bytes =
        await image.toByteData(format: ui.ImageByteFormat.png);
    if (bytes == null) return false;
    final data = bytes.buffer.asUint8List();
    final dir = await getTemporaryDirectory();
    final file = File(
        '${dir.path}/ayah-${DateTime.now().millisecondsSinceEpoch}.png');
    await file.writeAsBytes(data, flush: true);
    await SharePlus.instance.share(
      ShareParams(files: [XFile(file.path)]),
    );
    return true;
  } catch (_) {
    return false;
  }
}
