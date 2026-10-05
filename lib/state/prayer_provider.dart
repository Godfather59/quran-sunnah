import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../data/services/prayer_service.dart';

class PrayerPrefs {
  const PrayerPrefs({
    this.method = PrayerCalcMethod.muslimWorldLeague,
    this.latitude = 21.4225,
    this.longitude = 39.8262,
    this.city = 'Mecca',
    this.tzOffsetHours,
    this.notifEnabled = false,
  });

  final PrayerCalcMethod method;
  final double latitude;
  final double longitude;
  final String city;
  // Null = device local offset.
  final double? tzOffsetHours;
  final bool notifEnabled;

  PrayerPrefs copyWith({
    PrayerCalcMethod? method,
    double? latitude,
    double? longitude,
    String? city,
    double? tzOffsetHours,
    bool? notifEnabled,
    bool clearTz = false,
  }) =>
      PrayerPrefs(
        method: method ?? this.method,
        latitude: latitude ?? this.latitude,
        longitude: longitude ?? this.longitude,
        city: city ?? this.city,
        tzOffsetHours: clearTz ? null : (tzOffsetHours ?? this.tzOffsetHours),
        notifEnabled: notifEnabled ?? this.notifEnabled,
      );
}

const List<(String, double, double, double)> kPrayerCityPresets = [
  ('Mecca', 21.4225, 39.8262, 3),
  ('Medina', 24.5247, 39.5692, 3),
  ('Cairo', 30.0444, 31.2357, 2),
  ('Istanbul', 41.0082, 28.9784, 3),
  ('Paris', 48.8566, 2.3522, 1),
  ('London', 51.5074, -0.1278, 0),
  ('New York', 40.7128, -74.0060, -5),
  ('Jakarta', -6.2088, 106.8456, 7),
];

class PrayerPrefsNotifier extends StateNotifier<PrayerPrefs> {
  PrayerPrefsNotifier() : super(const PrayerPrefs()) {
    _load();
  }

  Future<void> _load() async {
    try {
      final p = await SharedPreferences.getInstance();
      final mName = p.getString('prayer.method');
      PrayerCalcMethod m = PrayerCalcMethod.muslimWorldLeague;
      if (mName != null) {
        for (final v in PrayerCalcMethod.values) {
          if (v.name == mName) {
            m = v;
            break;
          }
        }
      }
      state = PrayerPrefs(
        method: m,
        latitude: p.getDouble('prayer.lat') ?? 21.4225,
        longitude: p.getDouble('prayer.lng') ?? 39.8262,
        city: p.getString('prayer.city') ?? 'Mecca',
        tzOffsetHours: p.getDouble('prayer.tz'),
        notifEnabled: p.getBool('prayer.notif') ?? false,
      );
    } catch (_) {}
  }

  Future<void> update(PrayerPrefs next) async {
    state = next;
    try {
      final p = await SharedPreferences.getInstance();
      await p.setString('prayer.method', next.method.name);
      await p.setDouble('prayer.lat', next.latitude);
      await p.setDouble('prayer.lng', next.longitude);
      await p.setString('prayer.city', next.city);
      await p.setBool('prayer.notif', next.notifEnabled);
      if (next.tzOffsetHours == null) {
        await p.remove('prayer.tz');
      } else {
        await p.setDouble('prayer.tz', next.tzOffsetHours!);
      }
    } catch (_) {}
  }
}

final prayerPrefsProvider =
    StateNotifierProvider<PrayerPrefsNotifier, PrayerPrefs>(
        (ref) => PrayerPrefsNotifier());

/// Today's times for current prefs (recomputed per build/day).
final todayPrayerProvider = Provider<PrayerTimes>((ref) {
  final prefs = ref.watch(prayerPrefsProvider);
  final now = DateTime.now();
  final tz = prefs.tzOffsetHours ??
      now.timeZoneOffset.inMinutes / 60.0;
  return calculatePrayerTimes(
    date: now,
    latitude: prefs.latitude,
    longitude: prefs.longitude,
    tzOffsetHours: tz,
    method: prefs.method,
  );
});
