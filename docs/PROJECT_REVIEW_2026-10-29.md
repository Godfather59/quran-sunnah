# Quran & Sunnah: project, UI, and UX review

Implementation follow-up: [changes and verification](UI_UX_IMPLEMENTATION_2026-10-29.md).

Review date: 29 October 2026. This review covers the current working tree, including uncommitted work. Application source was not changed.

## Scope and confidence

- Inspected Android Compose screens, theming, navigation, onboarding, search, prayer scheduling, and supporting QA documentation.
- Inspected the iOS SwiftUI browser, reader, search, localization, and documented platform gaps.
- Reviewed the repository's saved Android Home screenshot from 11 October 2026. It is useful design evidence, but is not a fresh capture of the current build.
- No Android device or emulator was connected. Overflow and gesture findings below are code-supported risks requiring runtime confirmation. No iOS simulator was available on this Windows host.
- Verification results are recorded at the end of this document.

## Overall assessment

Keep the emerald identity, restrained surfaces, bundled Arabic fonts, offline reading, and explicit source attribution. Android has substantial functionality, four primary destinations, loading/error handling, and tablet adaptations already implemented. The next investment should make the reading journey more focused and predictable, while fixing source selection and prayer reminder behavior.

iOS is a smaller Quran browser and reader. Its roadmap should be assessed separately from Android rather than treating the two as equivalent experiences.

## Priority 1: correctness and dependable interaction

### 1. Preserve the user's Quran edition

**Code behavior:** Settings and onboarding offer script choices independently of Riwaya. Unsupported combinations resolve to the first reader edition, Hafs/Uthmani. Entering Mushaf also coerces any non-Hafs/Uthmani selection to Hafs/Uthmani.

**Improve:** Derive allowed script choices from the verified edition catalog. Keep the active Riwaya/script visible in the reader. When a page view is only available for Hafs/Uthmani, explain that limitation before changing the edition and offer to stay in the selected edition's verse view.

**Evidence:** `SettingsScreens.kt:375-405`; `Onboarding.kt:612-614,682-702`; `AndroidScreens.kt:1487-1490`; `QuranScreens.kt:1285-1286`, under `kmp/androidApp/src/main/java/com/godfather59/quransunnah/`.

**Acceptance:** Select Warsh or Qalun, inspect available scripts, reopen the reader, enter/leave Mushaf, and verify that every edition change is explicit and the displayed content matches its label.

### 2. Repair prayer reminder scheduling and timezone handling

**Code behavior:** `midnightMillisForOffset` adds the UTC offset to UTC midnight; local midnight requires the inverse conversion and the selected place's calendar date. On the same UTC/local calendar date, a hypothetical 06:00 event at UTC+1 should fire at 05:00 UTC; the current formula produces 07:00 UTC. The receiver posts a notification but does not schedule the following day. City/method changes do not refresh the existing schedule.

Separately, the prayer snapshot uses the device calendar date/time while calculating with the selected place's numeric offset. Presets and GPS persist fixed offsets, which can become stale across clock changes.

**Improve:** Introduce one scheduler responsible for place-local dates, date-specific timezone offsets, configuration changes, reboot/time changes, and daily renewal. Persist an IANA timezone with the place. Add a small status row showing location, calculation method, next reminder, notification permission, and whether delivery is approximate.

**Evidence:** `PrayerNotifications.kt:45-76,181-190,197-223,240-256`; `PrayerScreens.kt:244-297,501-507,527-533,597-606`; shared `prayer/PrayerPresets.kt:15`.

**Acceptance:** Check differing device/place zones, UTC date boundaries, Morocco clock transitions, tomorrow's Fajr, several days without reopening the app, permission denial, and city/method changes.

### 3. Keep long Quran content and actions reachable

**Layout risk:** Mushaf places every verse in a bounded non-scrollable column. The ayah action sheet includes the full verse, translation, and many full-width actions in another non-scrollable column. Long pages, 2:282, landscape, and large text can exceed the available viewport.

**Improve:** Make the content body scrollable. Keep a compact action area reachable, with primary actions such as Play and Tafsir immediately visible and other actions in clearly labeled rows. Preserve readable text rather than shrinking a whole page to fit.

**Evidence:** `QuranScreens.kt:1648-1675,1725-1899`.

**Acceptance:** Test long verses and pages on a small phone in portrait/landscape at 100%, 150%, and 200% text scale. Every verse and action must remain reachable.

### 4. Make system Back follow the visible hierarchy

**Code behavior:** Quran index → reader, reader → Mushaf, and Sunnah books → open book are state-based transitions without their corresponding active BackHandlers. The visible back buttons update state, but system Back does not use those same transitions. Reader entry from Home already has a parent handler; the Quran-tab path needs its own treatment.

**Improve:** Use a navigation stack or scoped handlers active only for the settled destination. Return Mushaf → reader → index and hadith list → books, restoring scroll position and selection.

**Evidence:** `QuranScreens.kt:274-280,693-750`; `SunnahScreens.kt:337-345`; contrast the existing search handler at `QuranScreens.kt:283`.

### 5. Use real metadata for Home's Juz indicator

**Code behavior:** Home estimates Juz by dividing the global verse position into 30 equal groups. Juz boundaries are not equal verse counts. For example, 2:142 has global position 149 and is shown as Juz 1 by this formula, while the bundled metadata starts Juz 2 at 2:142.

**Improve:** Use the existing `MushafMetadata.juzOf` lookup for the supported edition mapping. Present the streak separately with a clear label; the current combined `streak · Juz N` chip is difficult to interpret. Clarify that the progress bar represents position in the Quran, unless actual completed-reading progress is tracked.

**Evidence:** `AndroidScreens.kt:694-695,709-723`; `assets/quran/metadata/quran-data.xml:121`; shared `quran/MushafMetadata.kt:34`.

## Priority 2: UI and UX improvements

### 6. Make Home more compact and task-focused

**Design recommendation:** The saved Home screenshot gives substantial space to the prayer header, a second green hero card, and six equal shortcut tiles. This pushes daily content lower and makes many elements compete for attention.

Keep a compact prayer summary with city and next time. Give Continue Reading one clear action, a precise reference, and a concise progress caption. Reduce repeated shortcuts to the most useful tools; Quran and Sunnah already have persistent destinations. Move static catalog counts and dataset installation information to an About/Data section. Retain a single daily-content preview below the main actions.

Suggested order: compact prayer summary → Continue Reading → a small useful-tools row → daily verse/hadith. Use the existing 20dp outer margin consistently and a smaller set of surface treatments and corner radii.

**Evidence:** `AndroidScreens.kt:380-567,586-639,697-730`; saved `ux-latency-current.png`.

### 7. Improve contrast and reader accessibility

**Measured source-color issue:** White on the light-theme primary `#0A9E6C` has a contrast ratio of approximately **3.43:1**. This falls below the 4.5:1 benchmark for normal-size text. A darker action green such as `#087F5B` yields approximately **5.00:1** with white. Preserve brighter emerald for decoration and larger elements. Check the bright end of hero gradients and their muted text too.

**Code behavior:** The memorization reveal eye and per-ayah memorized toggle have no accessible action labels. Mushaf's English `Ayah s:a` content description also needs a localized semantics review to ensure the actual verse remains available to assistive technology.

Add localized names and state descriptions, separate Arabic/translation size controls, line spacing, and a live reader preview. Android currently offers font selection but no reader text-size control. Validate actual TalkBack behavior and all custom hit areas; Android recommends at least 48×48dp targets.

**Evidence:** `AppTheme.kt:45-46`; `QuranScreens.kt:882,1179,1665-1668`; fixed `titleLarge` reader styling.

**References:** [W3C contrast guidance](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html), [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps).

### 8. Keep navigation labels visible

Below 420dp, unselected bottom-navigation items lose their text labels. With only four destinations, show every label consistently, especially More. Verify Arabic and French at larger text scales.

**Evidence:** `MainActivity.kt:444,480-492`.

### 9. Shorten onboarding and respect choices already made

Five pages delay the first reading session. Source and additional-content selections are saved but have no downstream reads in the repository. Skip completes with new defaults, discarding choices already made. There is no previous-step control, and selected language does not update the surrounding interface until completion.

Start with language and a valid edition selection, apply language immediately, then open reading. Defer other preferences until their relevant feature is used. If later setup remains, add Back and preserve selected values when skipping the remainder. Remove selection steps that have no effect.

**Evidence:** `Onboarding.kt:98-99,594-642,705-757`.

### 10. Open search results in the context the user selected

Tafsir results currently route to the Quran reference rather than the matching commentary. Carry source, verse, and query through navigation, open the correct tafsir, and highlight the match. Preserve query and scroll position on return. Provide a visible way to expand capped results.

**Evidence:** `SearchScreens.kt:451,650-666`.

### 11. Simplify the reader and Settings presentation

- Offer a focus mode that reduces header, progress, and mode controls during sustained reading, with an obvious way to restore them.
- Group Settings into labeled rows opening focused pickers, instead of long sequences of horizontal chip strips.
- Name translations explicitly in their controls. The Android reader currently loads `en-sahih` when translation is enabled; expose the bundled French translation as a selectable option.
- Consider a full-screen hadith reader with previous/next navigation for long reading; retain the sheet for short previews.
- Make custom header/status-bar styling follow the selected app theme. Several theme helpers read the system theme independently of the explicit app theme preference.

**Evidence:** `QuranScreens.kt` reader/settings presentation; `SettingsScreens.kt:366-407`; `AppTheme.kt:151,248`; `MainActivity.kt:291-304`.

## iOS priorities

1. **Reading continuity:** Persist the visible surah/ayah and expose Continue Reading. Current routes are transient state and catalog links open without a saved ayah. Evidence: `kmp/iosApp/ContentView.swift:96-99,140,167-170,233-237`.
2. **Comfortable typography:** Replace fixed `.system(size: 27)` with scalable Quran typography, register/use the bundled face, and add size/spacing controls. Evidence: `ContentView.swift:215-216`; font registration is absent from `Info.plist`.
3. **Complete search states:** Add searching, no-results, and failure feedback. The current list only renders result sections when nonempty. Evidence: `ContentView.swift:119-140`.
4. **Prevent stale result replacement:** Detached search work is not cancelled with the query task, and publishes results without checking request identity. Guard the result before assigning it. Evidence: `ContentView.swift:68-78,175-180`.
5. **Fix localization:** `searchResults` is used as a localized key but is absent from `Localizable.xcstrings` and the oracle. Add it through the localization generation workflow. Evidence: `ContentView.swift:120`.
6. **Define the MVP:** Prioritize resume, accessible reading, translation/tafsir, and essential audio. Prayer, Sunnah, settings, and downloads remain separate platform work. Remove the implementation suffix KMP from the installed display name before beta. Evidence: `kmp/README.md:90`; `kmp/iosApp/Info.plist:8`.

## Recommended delivery order

| Pass | Scope | Completion evidence |
| --- | --- | --- |
| 1. Trust and navigation | Edition selection, prayer scheduler/timezones, Back behavior, real Juz metadata | Deterministic regression checks plus device walkthroughs |
| 2. Reading comfort | Long-content scrolling, reachable actions, text controls, contrast, assistive labels | Small phone, landscape, RTL, 200% text, TalkBack |
| 3. Simplification | Home hierarchy, four visible nav labels, shorter onboarding, contextual search | First-session and return-reading walkthroughs |
| 4. iOS foundation | Resume, typography, search states/localization, then content/audio | iPhone/iPad and VoiceOver validation on macOS |

Existing QA documents already identify physical-device swipe retesting and forced Downloads failure/retry checks as unfinished. Finish those checks before making performance claims. The saved emulator frame statistics are explicitly unsuitable for judging phone performance.

Prefer behavioral tests covering resume after process death, edition identity, reference navigation, offline reading, failure/retry, and notification renewal over tests that only assert a screen tag exists. Update the Phase 3 QA instructions that still reference removed Flutter/Dart tools. Treat the production-release restrictions already recorded in `docs/DATA_LICENSES.md` as an existing project constraint.

## Verification

Ran `:shared:jvmTest --offline --console=plain` with JDK 17: **124 tests across 20 suites passed; zero failures, errors, or skips**. Gradle reported a successful build. These shared tests do not validate Android notification scheduling, Compose layout, or SwiftUI interactions.

Build configuration emitted existing Android Gradle plugin/Kotlin DSL deprecation warnings. Plan the documented KMP Android-plugin migration before upgrading the toolchain further; this run did not change build configuration.

No fresh UI runtime validation was possible because no Android device/emulator was connected, and iOS simulator execution requires macOS. Application source remains unchanged; this report is the only authored file.
