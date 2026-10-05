import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_compass/flutter_compass.dart';

/// Live Qibla compass: rotates dial by (qiblaBearing - heading).
/// Falls back to static bearing arrow when sensor unavailable.
class LiveQiblaCompass extends StatefulWidget {
  const LiveQiblaCompass({super.key, required this.qiblaBearing});

  final double qiblaBearing;

  @override
  State<LiveQiblaCompass> createState() => _LiveQiblaCompassState();
}

class _LiveQiblaCompassState extends State<LiveQiblaCompass> {
  StreamSubscription<CompassEvent>? _sub;
  double? _heading;
  bool _noSensor = false;

  @override
  void initState() {
    super.initState();
    try {
      _sub = FlutterCompass.events?.listen(
        (e) {
          if (!mounted) return;
          setState(() => _heading = e.heading);
        },
        onError: (_) {
          if (mounted) setState(() => _noSensor = true);
        },
      );
      if (FlutterCompass.events == null) _noSensor = true;
    } catch (_) {
      _noSensor = true;
    }
  }

  @override
  void dispose() {
    _sub?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    if (_noSensor) {
      return Text(
        '${widget.qiblaBearing.toStringAsFixed(0)}° from true north',
        style: Theme.of(context).textTheme.bodySmall,
      );
    }
    final heading = _heading;
    if (heading == null) {
      return const SizedBox(
        width: 140,
        height: 140,
        child: Center(child: CircularProgressIndicator()),
      );
    }
    // Dial rotates opposite heading; Qibla marker at bearing.
    final dialTurns = -heading * math.pi / 180;
    final qiblaTurns =
        (widget.qiblaBearing - heading) * math.pi / 180;
    return Column(
      children: [
        SizedBox(
          width: 180,
          height: 180,
          child: Stack(
            alignment: Alignment.center,
            children: [
              Transform.rotate(
                angle: dialTurns,
                child: Container(
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(
                        color: scheme.outlineVariant),
                  ),
                  child: const Stack(
                    children: [
                      Align(
                          alignment: Alignment.topCenter,
                          child: Padding(
                              padding:
                                  EdgeInsets.only(top: 8),
                              child: Text('N'))),
                      Align(
                          alignment: Alignment.bottomCenter,
                          child: Padding(
                              padding: EdgeInsets.only(
                                  bottom: 8),
                              child: Text('S'))),
                      Align(
                          alignment: Alignment.centerLeft,
                          child: Padding(
                              padding:
                                  EdgeInsets.only(left: 8),
                              child: Text('W'))),
                      Align(
                          alignment: Alignment.centerRight,
                          child: Padding(
                              padding: EdgeInsets.only(
                                  right: 8),
                              child: Text('E'))),
                    ],
                  ),
                ),
              ),
              Transform.rotate(
                angle: qiblaTurns,
                child: const Align(
                  alignment: Alignment.topCenter,
                  child: Padding(
                    padding: EdgeInsets.only(top: 20),
                    child: Icon(Icons.navigation, size: 44),
                  ),
                ),
              ),
              Container(
                width: 10,
                height: 10,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: scheme.primary,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 8),
        Text(
          'Heading ${heading.toStringAsFixed(0)}° · Qibla ${widget.qiblaBearing.toStringAsFixed(0)}°',
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ],
    );
  }
}
