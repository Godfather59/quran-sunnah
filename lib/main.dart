import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'app.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Background audio must NEVER block or crash startup: a stuck
  // platform channel here used to mean a permanent black screen.
  // Foreground playback keeps working even if this fails.
  try {
    await JustAudioBackground.init(
      androidNotificationChannelId: 'com.quran_sunnah.audio',
      androidNotificationChannelName: 'Quran recitation',
      androidNotificationOngoing: true,
    ).timeout(const Duration(seconds: 8));
  } catch (_) {
    // Background controls unavailable; foreground audio still works.
  }
  runApp(const ProviderScope(child: QuranSunnahApp()));
}
