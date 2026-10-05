import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../data/seed/surah_metadata.dart';
import 'providers.dart';

class KhatmaState {
  const KhatmaState({
    this.globalAyah = 1,
    this.progress = 0,
    this.juz = 1,
    this.streakDays = 0,
  });

  final int globalAyah;
  final double progress;
  final int juz;
  final int streakDays;
}

int _globalAyahNumber(int surah, int ayah) {
  var n = 0;
  for (final m in kSurahMetadata) {
    if (m.number < surah) {
      n += m.ayahCount;
    } else if (m.number == surah) {
      return n + ayah.clamp(1, m.ayahCount);
    } else {
      break;
    }
  }
  return n.clamp(1, 6236);
}

int _juzOfGlobal(int global) {
  var acc = 0;
  var juz = 1;
  for (final m in kSurahMetadata) {
    // Approximate Juz from cumulative counts (30 equal parts).
    // Exact Juz boundaries come from metadata loader; this is for hero only.
    acc += m.ayahCount;
    if (global <= acc) break;
  }
  // Map 6236 range to 1..30.
  juz = ((global - 1) * 30 ~/ 6236) + 1;
  return juz.clamp(1, 30);
}

final khatmaProvider = Provider<KhatmaState>((ref) {
  final q = ref.watch(quranPrefsProvider);
  final g = _globalAyahNumber(q.lastSurah, q.lastAyah);
  return KhatmaState(
    globalAyah: g,
    progress: (g / 6236).clamp(0.0, 1.0),
    juz: _juzOfGlobal(g),
    streakDays: 0,
  );
});

class StreakNotifier extends StateNotifier<int> {
  StreakNotifier() : super(0) {
    _load();
  }

  static const _lastDayKey = 'khatma.lastDay';
  static const _streakKey = 'khatma.streak';

  Future<void> _load() async {
    try {
      final p = await SharedPreferences.getInstance();
      state = p.getInt(_streakKey) ?? 0;
    } catch (_) {}
  }

  /// Call on reader open / daily open to bump streak.
  Future<void> touchToday() async {
    try {
      final p = await SharedPreferences.getInstance();
      final now = DateTime.now();
      final today = DateTime(now.year, now.month, now.day);
      final lastMs = p.getInt(_lastDayKey);
      if (lastMs != null) {
        final last = DateTime.fromMillisecondsSinceEpoch(lastMs);
        final lastDay = DateTime(last.year, last.month, last.day);
        final diff = today.difference(lastDay).inDays;
        if (diff == 0) return;
        if (diff == 1) {
          state = state + 1;
        } else {
          state = 1;
        }
      } else {
        state = 1;
      }
      await p.setInt(_lastDayKey, today.millisecondsSinceEpoch);
      await p.setInt(_streakKey, state);
    } catch (_) {}
  }
}

final streakProvider =
    StateNotifierProvider<StreakNotifier, int>((ref) => StreakNotifier());
