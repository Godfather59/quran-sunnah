// GPS location with graceful manual fallback (no crash without permission).
import 'package:geolocator/geolocator.dart';

class GpsResult {
  const GpsResult({
    required this.latitude,
    required this.longitude,
    required this.city,
  });

  final double latitude;
  final double longitude;
  final String city;
}

/// Returns null when services off / denied / unavailable — caller keeps manual.
Future<GpsResult?> requestGpsLocation() async {
  try {
    final enabled = await Geolocator.isLocationServiceEnabled();
    if (!enabled) return null;
    var perm = await Geolocator.checkPermission();
    if (perm == LocationPermission.denied) {
      perm = await Geolocator.requestPermission();
    }
    if (perm == LocationPermission.denied ||
        perm == LocationPermission.deniedForever) {
      return null;
    }
    final pos = await Geolocator.getCurrentPosition(
      locationSettings: const LocationSettings(
        accuracy: LocationAccuracy.low,
        timeLimit: Duration(seconds: 15),
      ),
    );
    return GpsResult(
      latitude: pos.latitude,
      longitude: pos.longitude,
      city:
          'GPS ${pos.latitude.toStringAsFixed(2)}, ${pos.longitude.toStringAsFixed(2)}',
    );
  } catch (_) {
    return null;
  }
}
