# UI and UX implementation — 29 October 2026

Implemented the review against the existing working tree, preserving the prior uncommitted work. No production deployment or physical-phone installation was performed.

## Android experience

- Home now uses a compact prayer summary and three useful-tool shortcuts. Continue Reading stays prominent; repeated Quran/Sunnah shortcuts and static catalog counters were removed from Home. Dataset information is available in Settings.
- All four navigation labels remain visible on phones. Reader, Mushaf, Home takeovers, and Sunnah book navigation handle system Back only while active.
- Onboarding now has two steps: language, then a valid Riwaya/script combination. Language applies immediately; Back is available; Skip retains current choices. Footer controls respect the system navigation area.
- Light-theme actions use a darker emerald with approximately 5:1 contrast against white. Header gradients and system-bar icons follow the selected app theme.
- Reader focus mode reduces chrome. Persistent Arabic size, translation size, and line-spacing controls include a preview; English and French translations have explicit source labels.
- Long Mushaf pages, verse actions, Tafsir, comparison, and word-study content scroll. Memorization controls have localized action/state labels. Repeated word-study tokens have unique list keys.
- Settings uses focused edition, font, reciter, translation, and Tafsir pickers rather than long chip strips for those choices.
- Tafsir search opens the matching source in place, highlights the query, and preserves search context. Search adds result expansion, pending-result protection, and retryable indexing/search failure states.
- Hadith previews can open a full reader with previous/next navigation within the verified section. Stable source coordinates restore the actual bundled narration and scroll position after recreation.

## Correctness fixes

- Unsupported legacy script combinations resolve within their selected Riwaya. New selection controls offer verified combinations only. Entering Hafs page view from another edition requires explicit confirmation and preserves the original reading preference.
- Home's Juz label uses verified boundary metadata for the supported Hafs/Uthmani mapping, rather than dividing verse counts into 30 equal groups. Its progress caption explicitly describes Quran position.
- Prayer calculations use the selected place's timezone and local date. City presets have IANA zones; new GPS selections retain the device's configured zone, and legacy fixed-offset GPS locations show a refresh hint.
- Prayer reminders use a rolling five-prayer schedule renewed after delivery and refreshed after configuration, boot, time, permission, and foreground changes. Permission/delivery status and system-settings actions are visible.
- Notification delivery explicitly checks runtime permission and handles revocation. Local midnight conversion, tomorrow's Fajr, and clock-change handling have regression coverage.
- The equation-of-time calculation now normalizes its signed angular difference across the March equinox; regression tests inspect raw minutes so formatting cannot conceal a whole-day error.

## iOS foundation

- Persisted visible-ayah position and Continue Reading.
- Registered bundled Quran fonts, Dynamic Type support, and persistent reader size/spacing settings.
- English/French translations and a Jalalayn Tafsir sheet, all loaded from verified shared sources. Preview text also comes from a loaded verse.
- Search loading, empty, failure, retry, debounce, and stale-response protection; missing localization repaired.
- Plain verse-number badges and VoiceOver reference/text labels. Removed the KMP suffix from installed app names.
- The Xcode project language configuration matches the Arabic-source catalog with Arabic, English, and French localizations.

## Validation

Final results: **125 shared JVM tests, 30 Android unit tests, and all 12 Android emulator UI tests passed**. Debug app/test APK builds passed. Android lint passed with zero errors and one Compose parameter-order style warning. `git diff --check` passed.

Screen inspection identified a bidirectional-text issue, so reading surfaces received explicit Arabic/Latin paragraph directions. A rendered regression verifies the physical Arabic right edge and Latin left edge, paragraph directions, bracket/period order, and unchanged source text. The final UI suite passed after this correction. An existing Settings test was also corrected to wait for asynchronously loaded preferences rather than treating UI-thread idleness as completion of disk work.

The Android UI suite includes long-verse actions and Mushaf scrolling at 200% font scale, onboarding selection preservation, Settings, Sunnah, Downloads, and More navigation. Prayer unit cases cover local/UTC day boundaries, device/place timezone differences, DST, Morocco clock changes, daily renewal, and equinox dates. Hadith route tests resolve exact bundled rows, stale section hints, and unavailable sources.

Test dependencies were updated to Espresso 3.7.0 / AndroidX runner 1.7.0 / JUnit extension 1.3.0 after the older framework failed before test execution on API 37. The stable Espresso release replaces the problematic reflective InputManager call. See the [official AndroidX Test release notes](https://developer.android.com/jetpack/androidx/releases/test).

The 315 localization keys match across the oracle, generated Android resources, and iOS catalog. Native plist/font-resource checks and `git diff --check` passed.

## Screenshots and artifacts

- [Arabic Home](../artifacts/ui-review-20261029/home-ar.png)
- [Arabic onboarding](../artifacts/ui-review-20261029/onboarding-ar.png)
- [Arabic reader](../artifacts/ui-review-20261029/reader-ar.png)
- [Reader settings](../artifacts/ui-review-20261029/reader-settings-ar.png)
- [Android UI test output](../artifacts/ui-review-20261029/all-ui-tests.txt)
- Debug APK: `kmp/androidApp/build/outputs/apk/debug/androidApp-debug.apk`

## Remaining device validation

Native iOS compilation, iPhone/iPad simulation, and VoiceOver validation require macOS/Xcode and were not performed on this Windows host. Physical-device swipe latency, TalkBack, and multi-day notification delivery remain device QA; no emulator performance claim is made. GPS zone correctness follows the device's configured timezone, so a manually incorrect device timezone still requires correction by the user.

The existing Android build-plugin migration warnings and content-distribution gate remain recorded project constraints. Broader iOS parity and iOS audio remain roadmap work.
