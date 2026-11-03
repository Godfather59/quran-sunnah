# Reviewed Android screenshots

48 production-screen baselines reviewed on 2 November 2026:
Home, Quran reader, full Mushaf reader, and Settings in Arabic, English, and
French; light/dark themes; 100%/200% font scale.

The capture fixture uses verified bundled text, fixed preferences, a UTC clock
at `1768478400000`, and a 360×740 dp content viewport at density 2. System bars
are excluded. Scrollable screens show their initial viewport; behavior tests
cover long-content scrolling separately.

The exact API 35 AOSP emulator fingerprint and complete planned capture matrix
are recorded in `manifest.json`. Compare fresh, independent captures against
these PNGs. CI never creates or updates them.

See [the regression guide](../../../../scripts/VISUAL_REGRESSION.md) for local
capture, comparison, and explicit baseline replacement commands.
