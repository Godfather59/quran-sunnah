// Prayer notifications: daily reminders for each prayer (local only).
//
// Uses flutter_local_notifications + timezone. All scheduling is best-effort:
// failures (denied permission, exact-alarm restrictions) are swallowed and
// the manual prayer screen remains the source of truth.

import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/data/latest_all.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

import 'prayer_service.dart';

final _plugin = FlutterLocalNotificationsPlugin();
bool _inited = false;

Future<void> _ensureInit() async {
  if (_inited) return;
  tzdata.initializeTimeZones();
  const android = AndroidInitializationSettings('@mipmap/ic_launcher');
  const ios = DarwinInitializationSettings();
  await _plugin.initialize(
    const InitializationSettings(android: android, iOS: ios),
  );
  _inited = true;
}

Future<bool> requestPrayerNotificationPermission() async {
  try {
    await _ensureInit();
    final android =
        _plugin.resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>();
    final granted = await android?.requestNotificationsPermission();
    return granted ?? true;
  } catch (_) {
    return false;
  }
}

Future<void> schedulePrayerNotifications(
  PrayerTimes times, {
  String localeCode = 'en',
}) async {
  try {
    await _ensureInit();
    await _plugin.cancelAll();
    final entries = times.ordered
        .where((e) => e.$1 != 'sunrise')
        .toList();
    for (var i = 0; i < entries.length; i++) {
      final (key, at) = entries[i];
      if (at.isBefore(DateTime.now())) continue;
      final title = switch (localeCode) {
        'ar' => 'حان وقت الصلاة',
        'fr' => 'Heure de la prière',
        _ => 'Prayer time',
      };
      await _plugin.zonedSchedule(
        100 + i,
        title,
        _prayerLabel(key, localeCode),
        tz.TZDateTime.from(at, tz.local),
        const NotificationDetails(
          android: AndroidNotificationDetails(
            'prayer_times',
            'Prayer times',
            importance: Importance.high,
            priority: Priority.high,
          ),
        ),
        androidScheduleMode:
            AndroidScheduleMode.inexactAllowWhileIdle,
        uiLocalNotificationDateInterpretation: null,
      );
    }
  } catch (_) {}
}

Future<void> cancelPrayerNotifications() async {
  try {
    await _ensureInit();
    await _plugin.cancelAll();
  } catch (_) {}
}

String _prayerLabel(String key, String code) {
  if (code == 'ar') {
    return switch (key) {
      'fajr' => 'الفجر',
      'dhuhr' => 'الظهر',
      'asr' => 'العصر',
      'maghrib' => 'المغرب',
      'isha' => 'العشاء',
      _ => key,
    };
  }
  return key[0].toUpperCase() + key.substring(1);
}
