import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/services/location_service.dart';
import '../../data/services/prayer_notifications.dart';
import '../../data/services/prayer_service.dart';
import '../../state/prayer_provider.dart';
import 'live_qibla_compass.dart';

String _fmt(DateTime t) =>
    '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

String _methodLabel(PrayerCalcMethod m, AppStrings s) {
  if (s.isArabic) {
    return switch (m) {
      PrayerCalcMethod.muslimWorldLeague => 'رابطة العالم الإسلامي',
      PrayerCalcMethod.isna => 'ISNA أمريكا',
      PrayerCalcMethod.egypt => 'مصر',
      PrayerCalcMethod.ummAlQura => 'أم القرى',
      PrayerCalcMethod.karachi => 'كراتشي',
      PrayerCalcMethod.jafari => 'جعفري',
    };
  }
  return switch (m) {
    PrayerCalcMethod.muslimWorldLeague => 'Muslim World League',
    PrayerCalcMethod.isna => 'ISNA',
    PrayerCalcMethod.egypt => 'Egypt',
    PrayerCalcMethod.ummAlQura => 'Umm al-Qura',
    PrayerCalcMethod.karachi => 'Karachi',
    PrayerCalcMethod.jafari => 'Jafari',
  };
}

String _prayerName(String key, AppStrings s) {
  if (s.isArabic) {
    return switch (key) {
      'fajr' => 'الفجر',
      'sunrise' => 'الشروق',
      'dhuhr' => 'الظهر',
      'asr' => 'العصر',
      'maghrib' => 'المغرب',
      'isha' => 'العشاء',
      _ => key,
    };
  }
  if (s.locale.languageCode == 'fr') {
    return switch (key) {
      'fajr' => 'Fajr',
      'sunrise' => 'Lever du soleil',
      'dhuhr' => 'Dhuhr',
      'asr' => 'Asr',
      'maghrib' => 'Maghrib',
      'isha' => 'Isha',
      _ => key,
    };
  }
  return switch (key) {
    'fajr' => 'Fajr',
    'sunrise' => 'Sunrise',
    'dhuhr' => 'Dhuhr',
    'asr' => 'Asr',
    'maghrib' => 'Maghrib',
    'isha' => 'Isha',
    _ => key,
  };
}

class PrayerScreen extends ConsumerWidget {
  const PrayerScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final prefs = ref.watch(prayerPrefsProvider);
    final times = ref.watch(todayPrayerProvider);
    final now = DateTime.now();
    final (nextKey, nextAt) = nextPrayer(times, now);
    final bearing = qiblaBearing(prefs.latitude, prefs.longitude);
    final remaining = nextAt.difference(now);

    return Scaffold(
      appBar: AppBar(
          title: Text(s.isArabic
              ? 'الصلاة والقبلة'
              : s.locale.languageCode == 'fr'
                  ? 'Prière & Qibla'
                  : 'Prayer & Qibla')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            color: Theme.of(context).colorScheme.primaryContainer,
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '${_prayerName(nextKey, s)} · ${_fmt(nextAt)}',
                    style: Theme.of(context)
                        .textTheme
                        .headlineSmall
                        ?.copyWith(
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimaryContainer),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    s.isArabic
                        ? 'المتبقي ${remaining.inHours} س ${remaining.inMinutes % 60} د · ${prefs.city}'
                        : s.locale.languageCode == 'fr'
                            ? 'Reste ${remaining.inHours}h ${remaining.inMinutes % 60}m · ${prefs.city}'
                            : 'In ${remaining.inHours}h ${remaining.inMinutes % 60}m · ${prefs.city}',
                    style: Theme.of(context)
                        .textTheme
                        .bodySmall
                        ?.copyWith(
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimaryContainer),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Column(
              children: [
                for (final e in times.ordered)
                  ListTile(
                    leading: Icon(
                      e.$1 == nextKey
                          ? Icons.notifications_active
                          : Icons.schedule_outlined,
                    ),
                    title: Text(_prayerName(e.$1, s)),
                    trailing: Text(
                      _fmt(e.$2),
                      style: Theme.of(context)
                          .textTheme
                          .titleMedium
                          ?.copyWith(
                              fontWeight: e.$1 == nextKey
                                  ? FontWeight.w800
                                  : null),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      const Icon(Icons.explore_outlined),
                      const SizedBox(width: 8),
                      Text(
                        s.isArabic
                            ? 'القبلة'
                            : s.locale.languageCode == 'fr'
                                ? 'Qibla'
                                : 'Qibla',
                        style: Theme.of(context)
                            .textTheme
                            .titleMedium
                            ?.copyWith(fontWeight: FontWeight.w700),
                      ),
                      const Spacer(),
                      Text('${bearing.toStringAsFixed(0)}°',
                          style: Theme.of(context)
                              .textTheme
                              .titleLarge),
                    ],
                  ),
                  const SizedBox(height: 12),
                  // Live compass (sensor) with static fallback inside widget.
                  Center(
                    child: LiveQiblaCompass(qiblaBearing: bearing),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    s.isArabic
                        ? 'اتجه ${bearing.toStringAsFixed(0)}° من الشمال الحقيقي نحو الكعبة. تحقق مع مسجدك المحلي.'
                        : s.locale.languageCode == 'fr'
                            ? 'Dirigez-vous à ${bearing.toStringAsFixed(0)}° du nord vrai vers la Kaaba. Vérifiez avec votre mosquée.'
                            : 'Face ${bearing.toStringAsFixed(0)}° from true north toward the Kaaba. Confirm with your local mosque.',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    s.isArabic ? 'الموقع وطريقة الحساب' : 'Location & method',
                    style: const TextStyle(
                        fontWeight: FontWeight.w700),
                  ),
                  DropdownButtonFormField<PrayerCalcMethod>(
                    initialValue: prefs.method,
                    decoration: InputDecoration(
                        labelText: s.isArabic
                            ? 'الطريقة'
                            : 'Method'),
                    items: PrayerCalcMethod.values
                        .map((m) => DropdownMenuItem(
                              value: m,
                              child: Text(
                                  _methodLabel(m, s)),
                            ))
                        .toList(),
                    onChanged: (v) {
                      if (v == null) return;
                      ref
                          .read(prayerPrefsProvider.notifier)
                          .update(prefs.copyWith(method: v));
                    },
                  ),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    initialValue: kPrayerCityPresets
                            .any((c) => c.$1 == prefs.city)
                        ? prefs.city
                        : null,
                    decoration: InputDecoration(
                        labelText:
                            s.isArabic ? 'المدينة' : 'City'),
                    items: kPrayerCityPresets
                        .map((c) => DropdownMenuItem(
                              value: c.$1,
                              child: Text(c.$1),
                            ))
                        .toList(),
                    onChanged: (v) {
                      if (v == null) return;
                      final hit = kPrayerCityPresets
                          .firstWhere((c) => c.$1 == v);
                      ref
                          .read(prayerPrefsProvider.notifier)
                          .update(prefs.copyWith(
                            city: hit.$1,
                            latitude: hit.$2,
                            longitude: hit.$3,
                            tzOffsetHours: hit.$4,
                          ));
                    },
                  ),
                  const SizedBox(height: 8),
                  FilledButton.tonalIcon(
                    onPressed: () async {
                      final gps = await requestGpsLocation();
                      if (!context.mounted) return;
                      if (gps == null) {
                        ScaffoldMessenger.of(context)
                            .showSnackBar(SnackBar(
                                content: Text(s.isArabic
                                    ? 'تعذر تحديد الموقع — أدخل المدينة يدويًا'
                                    : 'GPS unavailable — pick a city manually')));
                        return;
                      }
                      await ref
                          .read(prayerPrefsProvider.notifier)
                          .update(prefs.copyWith(
                            city: gps.city,
                            latitude: gps.latitude,
                            longitude: gps.longitude,
                            clearTz: true,
                          ));
                    },
                    icon: const Icon(Icons.my_location),
                    label: Text(s.isArabic
                        ? 'استخدام GPS'
                        : s.locale.languageCode == 'fr'
                            ? 'Utiliser le GPS'
                            : 'Use GPS'),
                  ),
                  const SizedBox(height: 8),
                  SwitchListTile(
                    title: Text(s.isArabic
                        ? 'تنبيهات الصلاة'
                        : s.locale.languageCode == 'fr'
                            ? 'Notifications de prière'
                            : 'Prayer notifications'),
                    value: prefs.notifEnabled,
                    onChanged: (v) async {
                      if (v) {
                        final ok =
                            await requestPrayerNotificationPermission();
                        if (!context.mounted) return;
                        if (!ok) {
                          ScaffoldMessenger.of(context)
                              .showSnackBar(SnackBar(
                                  content: Text(s.isArabic
                                      ? 'تم رفض إذن التنبيهات'
                                      : 'Notification permission denied')));
                          return;
                        }
                        await ref
                            .read(prayerPrefsProvider.notifier)
                            .update(
                                prefs.copyWith(notifEnabled: true));
                        await schedulePrayerNotifications(
                          times,
                          localeCode: s.locale.languageCode,
                        );
                      } else {
                        await ref
                            .read(prayerPrefsProvider.notifier)
                            .update(
                                prefs.copyWith(notifEnabled: false));
                        await cancelPrayerNotifications();
                      }
                    },
                  ),
                  Text(
                    '${prefs.latitude.toStringAsFixed(2)}, ${prefs.longitude.toStringAsFixed(2)} · UTC${prefs.tzOffsetHours == null ? ' (device)' : '${prefs.tzOffsetHours! >= 0 ? '+' : ''}${prefs.tzOffsetHours}'}',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                  Text(
                    s.isArabic
                        ? 'حساب فلكي دون اتصال (±2 دقيقة). اعتمد مسجدك للأذان.'
                        : 'Offline astronomical calc (±2 min). Follow your mosque for adhan.',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
