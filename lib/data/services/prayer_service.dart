// Offline prayer-time calculation (no network, no new deps).
//
// Standard solar algorithm: declination + equation of time + hour angles.
// Angles per method; Asr shadow factor 1 (Standard) / 2 (Hanafi).
// Times are local wall-clock for the given date + tz offset. Accuracy ~±2min,
// suitable for a companion app — user confirms with local mosque.

import 'dart:math' as math;

enum PrayerCalcMethod {
  muslimWorldLeague,
  isna,
  egypt,
  ummAlQura,
  karachi,
  jafari,
}

class PrayerMethodParams {
  const PrayerMethodParams({
    required this.fajrAngle,
    required this.ishaAngle,
    this.ishaMinutesAfterMaghrib,
    this.asrFactor = 1.0,
  });

  final double fajrAngle;
  final double ishaAngle;
  final double? ishaMinutesAfterMaghrib;
  final double asrFactor;
}

const Map<PrayerCalcMethod, PrayerMethodParams> kPrayerMethods = {
  PrayerCalcMethod.muslimWorldLeague:
      PrayerMethodParams(fajrAngle: 18, ishaAngle: 17),
  PrayerCalcMethod.isna:
      PrayerMethodParams(fajrAngle: 15, ishaAngle: 15),
  PrayerCalcMethod.egypt:
      PrayerMethodParams(fajrAngle: 19.5, ishaAngle: 17.5),
  PrayerCalcMethod.ummAlQura: PrayerMethodParams(
      fajrAngle: 18.5, ishaAngle: 0, ishaMinutesAfterMaghrib: 90),
  PrayerCalcMethod.karachi:
      PrayerMethodParams(fajrAngle: 18, ishaAngle: 18),
  PrayerCalcMethod.jafari:
      PrayerMethodParams(fajrAngle: 16, ishaAngle: 14),
};

class PrayerTimes {
  const PrayerTimes({
    required this.fajr,
    required this.sunrise,
    required this.dhuhr,
    required this.asr,
    required this.maghrib,
    required this.isha,
  });

  final DateTime fajr;
  final DateTime sunrise;
  final DateTime dhuhr;
  final DateTime asr;
  final DateTime maghrib;
  final DateTime isha;

  List<(String, DateTime)> get ordered => [
        ('fajr', fajr),
        ('sunrise', sunrise),
        ('dhuhr', dhuhr),
        ('asr', asr),
        ('maghrib', maghrib),
        ('isha', isha),
      ];
}

/// Next prayer after [now] (wraps to tomorrow Fajr).
(String, DateTime) nextPrayer(PrayerTimes t, DateTime now) {
  for (final e in t.ordered) {
    if (!e.$2.isBefore(now)) return e;
  }
  return ('fajr', t.fajr.add(const Duration(days: 1)));
}

/// Qibla bearing from (lat,lng) to Kaaba (21.4225, 39.8262), degrees 0..360.
double qiblaBearing(double lat, double lng) {
  const kaabaLat = 21.4225 * math.pi / 180;
  const kaabaLng = 39.8262 * math.pi / 180;
  final phi = lat * math.pi / 180;
  final lambda = lng * math.pi / 180;
  final dLng = kaabaLng - lambda;
  final y = math.sin(dLng);
  final x = math.cos(phi) * math.tan(kaabaLat) -
      math.sin(phi) * math.cos(dLng);
  final brng = math.atan2(y, x) * 180 / math.pi;
  return (brng + 360) % 360;
}

PrayerTimes calculatePrayerTimes({
  required DateTime date,
  required double latitude,
  required double longitude,
  required double tzOffsetHours,
  PrayerCalcMethod method = PrayerCalcMethod.muslimWorldLeague,
}) {
  final p = kPrayerMethods[method]!;
  final julian = _julianDay(date);
  final decl = _sunDeclination(julian);
  final eqt = _equationOfTime(julian);
  final noon = _midday(longitude, tzOffsetHours, eqt);
  final t = (double minutes) => noon + minutes;

  final sunriseHA = _hourAngle(-0.833, latitude, decl);
  final fajrHA = _hourAngle(-p.fajrAngle, latitude, decl);
  final ishaHA = p.ishaAngle > 0
      ? _hourAngle(-p.ishaAngle, latitude, decl)
      : double.nan;
  final asrHA = _asrHourAngle(p.asrFactor, latitude, decl);

  final base = DateTime(date.year, date.month, date.day);
  DateTime at(double minutesFromMidnight) =>
      base.add(Duration(minutes: minutesFromMidnight.round()));

  final dhuhr = at(t(0));
  final sunrise = at(t(-sunriseHA));
  final sunset = at(t(sunriseHA));
  final fajr = at(t(-fajrHA));
  final asr = at(t(asrHA));
  final maghrib = sunset;
  final isha = p.ishaMinutesAfterMaghrib != null
      ? maghrib.add(
          Duration(minutes: p.ishaMinutesAfterMaghrib!.round()))
      : at(t(ishaHA));

  return PrayerTimes(
    fajr: fajr,
    sunrise: sunrise,
    dhuhr: dhuhr,
    asr: asr,
    maghrib: maghrib,
    isha: isha,
  );
}

double _julianDay(DateTime date) {
  var y = date.year;
  var m = date.month;
  final d = date.day.toDouble();
  if (m <= 2) {
    y -= 1;
    m += 12;
  }
  final a = (y / 100).floor();
  final b = 2 - a + (a / 4).floor();
  return (365.25 * (y + 4716)).floor() +
      (30.6001 * (m + 1)).floor() +
      d +
      b -
      1524.5;
}

double _sunDeclination(double jd) {
  final d = jd - 2451545.0;
  final g = _fixAngle(357.529 + 0.98560028 * d);
  final q = _fixAngle(280.459 + 0.98564736 * d);
  final l = _fixAngle(
      q + 1.915 * _sinDeg(g) + 0.020 * _sinDeg(2 * g));
  final e = 23.439 - 0.00000036 * d;
  final decl = math.asin(
          _sinDeg(e) * _sinDeg(l)) *
      180 /
      math.pi;
  return decl;
}

double _equationOfTime(double jd) {
  final d = jd - 2451545.0;
  final g = _fixAngle(357.529 + 0.98560028 * d);
  final q = _fixAngle(280.459 + 0.98564736 * d);
  final l = _fixAngle(
      q + 1.915 * _sinDeg(g) + 0.020 * _sinDeg(2 * g));
  final e = 23.439 - 0.00000036 * d;
  final ra = math.atan2(
          math.cos(e * math.pi / 180) * _sinDeg(l),
          math.cos(l * math.pi / 180)) *
      180 /
      math.pi /
      15;
  final eqt = q / 15 - _fixHour(ra);
  return eqt * 60;
}

double _midday(double lng, double tz, double eqt) =>
    720 - 4 * lng - eqt + tz * 60;

double _hourAngle(double angleDeg, double lat, double decl) =>
    _hourAngleMinutes(angleDeg, lat, decl);

double _asrHourAngle(double factor, double lat, double decl) {
  // Shadow length factor: cot(alt) = factor + tan(|lat-decl|).
  final diff = (lat - decl).abs() * math.pi / 180;
  final alt = math.atan(1 / (factor + math.tan(diff)));
  final altDeg = alt * 180 / math.pi;
  return _hourAngleMinutes(altDeg, lat, decl);
}

double _hourAngleMinutes(double angleDeg, double lat, double decl) {
  final num = _sinDeg(angleDeg) -
      _sinDeg(lat) * _sinDeg(decl);
  final den = math.cos(lat * math.pi / 180) *
      math.cos(decl * math.pi / 180);
  final v = (num / den).clamp(-1.0, 1.0);
  // acos gives hour angle in degrees -> convert to minutes (4 min/deg).
  return math.acos(v) * 180 / math.pi * 4;
}

double _sinDeg(double d) => math.sin(d * math.pi / 180);

double _fixAngle(double a) => a - 360 * (a / 360).floorToDouble();

double _fixHour(double h) =>
    h - 24 * (h / 24).floorToDouble();
