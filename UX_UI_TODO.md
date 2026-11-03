# UX, UI, and swipe responsiveness

Review date: 2026-10-11

Implementation follow-up (2026-10-29): see [UI/UX changes and verification](docs/UI_UX_IMPLEMENTATION_2026-10-29.md).

## Swipe responsiveness

- [x] Keep off-screen composition bounded to the selected destination and its nearest neighbors; warm those neighbors after the first frame and pause destination work during a swipe.
- [x] Load Settings preferences away from the Compose UI thread.
- [x] Avoid gesture competition between the top-level pager and Quran index modes.
- [x] Gate Home dashboard and daily content loading until Home is the settled destination.
- [x] Cache the daily Quran verse, daily Hadith, prayer/activity summary, Hadith indexes, and combined All collections directory across tab recreation.
- [x] Seed Home's first frame from its process cache so returning to the tab does not wait for `LaunchedEffect`; refresh the prayer countdown for the current minute.
- [x] Preserve Continue Reading's saved surah, ayah, streak, and loaded state across pager tab changes.
- [x] Skip repeated Continue Reading preference reads unless its saved position changes; unrelated Home dashboard refreshes no longer reload it.
- [x] Preserve Search query/filter state across tab changes and pause indexing while its destination is inactive.
- [x] Show loading separately from unavailable content in Quran Reader, with a retry action for failures.
- [x] Refresh the Home prayer countdown at minute boundaries while Home is active, without reloading dashboard preferences.
- [x] Profile a manual swipe pass between Home, Quran, and Sunnah on a physical device and record frame statistics.
- [ ] Recheck swipe latency and frame stalls on the physical Xiaomi after the pager neighbor-prewarm change.
- [x] Audit other destination composables for synchronous disk, database, font, or large asset work.

Device checks (2026-10-11): the Arabic Home screen rendered on a Xiaomi 2211133G phone at 1080×2400, 440 dpi, and default 1.0 font scale. The RTL navigation, Home hero, Continue Reading card, and shortcut grid fit. Wireless ADB screenshot capture works; synthetic taps/swipes are denied by `INJECT_EVENTS`, so physical swipe profiling used a manual pass. After resetting Android's graphics counters, the manual Home/Quran/Sunnah pass had 2 janky frames out of 4,205 (0.05%); frame percentiles were 7 ms p50, 11 ms p90, and 117 ms p99. This aggregate is not a per-swipe latency trace. A controlled Home→Quran→Home return on the phone kept the same Continue Reading reference (Al Imran 3:1) visible without a loading placeholder; that pass reported 3 janky frames out of 2,682 (0.11%), with p50 7 ms, p90 9 ms, and p99 97 ms. The prayer countdown refreshed on return, but stayed unchanged across a minute while Home remained open. That was fixed with a lightweight active-Home minute ticker. The rebuilt debug APK was installed on the Xiaomi; its screen timed out before the physical minute rollover could be captured. A phone emulator confirmed the countdown decremented from 7h25m to 7h24m across a minute boundary. Android phone and tablet emulators were also used for interactive checks. At 1.3 font scale, Home, Quran, Sunnah, More, and Downloads stayed readable in Arabic; Quran Juz selection and scroll position survived switching tabs. On a 1280×800 dp tablet, the navigation rail and split Quran reader/index layout fit. The headless emulator's frame stats showed severe jank even on static screens, so they are not reliable for judging phone performance.

Pager follow-up: the bounded neighbor prewarm change compiled successfully and was checked with emulator swipes in both directions; the nav selection moved Home→Quran and Quran→Home. No emulator frame-time claim is recorded because its software-rendered frame stats are not representative. The physical Xiaomi retest is pending: wireless ADB currently lists only the emulator, so the updated APK could not be installed on the phone for this pass.

Follow-up (2026-10-12): replaced the fixed 250 ms wait before neighbor prewarming with a Compose first-frame barrier (`withFrameNanos`) and an idle-state wait (`snapshotFlow`). `:androidApp:assembleDebug` succeeded. Per request, this build was not installed on the Xiaomi.

Emulator diagnostic (2026-10-11): a host-GPU phone AVD completed eight alternating Home/Quran swipes in 6.09 seconds of ADB-driven gesture commands and ended on the expected tab; the app process stayed alive. `gfxinfo` reported 103/229 janky frames in that run (p50 44 ms, p90 69 ms, p99 150 ms); a one-swipe sample reported 11/32 janky frames (p50 46 ms, p90 73 ms). GPU frame percentiles were 19 ms p50 and 24 ms p90, so this AVD cannot validate phone latency. Android's recorded input timeout named the system `edge-swipe` Gesture Monitor, not the app. These measurements are diagnostics only; repeat them on the Xiaomi before drawing a release conclusion.

Audit follow-up: Quran reader, Mushaf, prayer, dhikr, theme, and audio player preference reads found in composition now run in background effects. Playback speed is also read on IO when starting audio.

## Navigation and screen hierarchy

- [x] Reduce the bottom navigation from six items to four by grouping secondary tools under More.
- [x] Remove the Library destination and its personal bookmarks, notes, highlights, collections, backup, and reading-history features.
- [x] Keep Search reachable from Home and add it to More.
- [x] Use a More icon for the More destination instead of the Settings gear.
- [x] Remove misleading no-op click affordances from informational badges and the prayer summary card.
- [x] Correct back and clear-search accessibility labels across locales.
- [x] Reduce the Home shortcut grid and put Continue Reading before shortcuts.
- [x] Allow Home shortcut labels to wrap on narrow screens and translated locales.
- [x] Review the More menu and primary destinations on phone and tablet layouts.
- [x] Review and clarify loading/error/empty states in Quran Reader and Sunnah catalog/sections.
- [x] Add a visible Downloads scan state and localized failure/retry state.
- [ ] Verify Downloads scan success and forced failure/retry states on device.

Downloads device check: the scan showed a loading state and completed successfully on the phone emulator and tablet emulator. A forced scan failure was not reproduced, so its error/retry branch remains unverified.

Tablet refinement: More menu actions are centered in a column capped at 760 dp on wide screens and fill the available width on phones. The rebuilt debug app was checked on the 1280×800 dp tablet emulator at 1.0 and 1.3 font scales; the Arabic title and all three actions stayed centered and readable.

## Visual and accessibility review

- [x] Check spacing, typography, contrast, touch targets, and selected states on phone and tablet layouts.
- [x] Check the Arabic Home RTL layout on a phone at the default font scale.
- [x] Check dynamic text scaling and RTL layouts at larger font scales and on a tablet.
- [x] Confirm Quran sub-tab selection and Surah list scroll use saveable Compose state across pager disposal.
- [x] Verify Quran sub-tab position and list scroll state on an Android phone emulator after navigation.
- [x] Verify Quran translations name Saheeh International and Hadith detail shows collection source, grade, and grading authority when provided.
