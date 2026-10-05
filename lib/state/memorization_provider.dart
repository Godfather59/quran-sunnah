import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Memorized ayahs as "surah:ayah" canonical keys. Local-only, offline.
class MemorizationNotifier extends StateNotifier<Set<String>> {
  MemorizationNotifier() : super(const {}) {
    _load();
  }

  static const _key = 'memorized.ayahs.v1';

  Future<void> _load() async {
    try {
      final p = await SharedPreferences.getInstance();
      state = (p.getStringList(_key) ?? []).toSet();
    } catch (_) {}
  }

  Future<void> _save() async {
    try {
      final p = await SharedPreferences.getInstance();
      await p.setStringList(_key, state.toList());
    } catch (_) {}
  }

  bool isMemorized(int surah, int ayah) =>
      state.contains('$surah:$ayah');

  Future<void> toggle(int surah, int ayah) async {
    final key = '$surah:$ayah';
    final next = Set<String>.from(state);
    if (next.contains(key)) {
      next.remove(key);
    } else {
      next.add(key);
    }
    state = next;
    await _save();
  }

  int countForSurah(int surah, int ayahCount) {
    var n = 0;
    for (var a = 1; a <= ayahCount; a++) {
      if (state.contains('$surah:$a')) n++;
    }
    return n;
  }

  /// First unmemorized ayah in [surah], or 1 if all done.
  int firstUnmemorized(int surah, int ayahCount) {
    for (var a = 1; a <= ayahCount; a++) {
      if (!state.contains('$surah:$a')) return a;
    }
    return 1;
  }
}

final memorizationProvider =
    StateNotifierProvider<MemorizationNotifier, Set<String>>(
        (ref) => MemorizationNotifier());
