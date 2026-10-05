import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Per-dhikr tap counts. Persisted daily (key includes date) + lifetime total.
class DhikrState {
  const DhikrState({this.today = const {}, this.total = 0});

  final Map<String, int> today;
  final int total;
}

class DhikrNotifier extends StateNotifier<DhikrState> {
  DhikrNotifier() : super(const DhikrState()) {
    _load();
  }

  String get _dayKey {
    final n = DateTime.now();
    final d =
        '${n.year.toString().padLeft(4, '0')}-${n.month.toString().padLeft(2, '0')}-${n.day.toString().padLeft(2, '0')}';
    return 'dhikr.$d';
  }

  Future<void> _load() async {
    try {
      final p = await SharedPreferences.getInstance();
      final keys = p.getKeys().where((k) => k.startsWith('dhikr.')).toList();
      var total = 0;
      Map<String, int> today = {};
      for (final k in keys) {
        if (k == _dayKey) {
          final m = <String, int>{};
          for (final id in p.getStringList(k) ?? []) {
            final parts = id.split(':');
            if (parts.length == 2) {
              m[parts[0]] = int.tryParse(parts[1]) ?? 0;
            }
          }
          today = m;
        }
        // Lifetime approx: sum today's + stored total.
      }
      total = p.getInt('dhikr.total') ?? 0;
      state = DhikrState(today: today, total: total);
    } catch (_) {}
  }

  Future<void> tap(String id) async {
    final next = Map<String, int>.from(state.today);
    next[id] = (next[id] ?? 0) + 1;
    state = DhikrState(today: next, total: state.total + 1);
    try {
      final p = await SharedPreferences.getInstance();
      await p.setStringList(
        _dayKey,
        [for (final e in next.entries) '${e.key}:${e.value}'],
      );
      await p.setInt('dhikr.total', state.total);
    } catch (_) {}
  }

  Future<void> reset(String id) async {
    final next = Map<String, int>.from(state.today)..remove(id);
    state = DhikrState(today: next, total: state.total);
    try {
      final p = await SharedPreferences.getInstance();
      await p.setStringList(
        _dayKey,
        [for (final e in next.entries) '${e.key}:${e.value}'],
      );
    } catch (_) {}
  }
}

final dhikrProvider =
    StateNotifierProvider<DhikrNotifier, DhikrState>(
        (ref) => DhikrNotifier());
