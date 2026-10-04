# Internal Beta QA Protocol

This beta protocol is intentionally distribution-channel agnostic. It focuses
on correctness, offline behavior, data integrity, accessibility and device
behavior.

## Minimum beta matrix

Test on at least:

| Class | Example coverage |
| --- | --- |
| Small Android phone | ~360×640 logical px |
| Modern Android phone | ~400–430 px wide, gesture navigation |
| Large Android / tablet | ≥840 px wide, NavigationRail path |
| iPhone-size device | portrait + landscape |
| iPad-size device | large layout / safe areas |
| Low-memory device/emulator | app background/resume + long reading session |

For each device, repeat key flows in Arabic RTL and one LTR locale, and in
light + dark mode.

## Sacred-text checks

- Quran text must always come from a verified edition asset.
- Switching Riwaya/script must never show another edition under the wrong
  label.
- Tajweed must remain a color overlay; it must never alter Quran characters.
- Translation and Tafsir styling must remain visually distinct from Quran text.
- Hadith rows with unavailable grades/narrators must show them as unavailable,
  never inferred.
- Compare Riwayat must only offer editions backed by verified assets.
- Spot-check a sample of Quran/Hadith references against the documented source.

## Offline checks

Start once with airplane mode / network disabled:

- Quran reading works for all bundled editions/scripts.
- Hadith browsing and global search work from bundled data + local FTS5.
- Tafsir and translations work from bundled assets.
- Bookmarks, notes, highlights, collections and recent items work.
- Data sources/licenses page works.
- Bundled fonts render without network access.
- Audio streaming must fail gracefully; already downloaded audio remains
  local-first.

## Persistence and migration

- Create bookmark, note, highlight and custom collection; kill the app; reopen.
- Verify all personal library data survives.
- Upgrade from a build that used SharedPreferences library storage and verify
  one-time migration to SQLite without duplicates or loss.
- Reopen repeatedly to ensure migration does not run twice.
- Delete a collection and verify its bookmarks remain but are detached from
  the deleted collection.

## Search

- Arabic with tashkeel and without tashkeel should resolve the same content.
- Direct verse references such as 2:255 should navigate correctly.
- Search Quran, Hadith and Tafsir after a cold launch.
- Repeat the same search after restart; the persisted index should be reused.
- Change a verified dataset/integrity fingerprint in a development build and
  verify only the affected search scope rebuilds.
- Narrator/topic search stays unavailable until a verified structured source
  is integrated.

## Audio

- Start recitation, lock screen, unlock, background/foreground.
- Receive an interruption (call/audio focus), then resume.
- Change playback speed, restart app, verify preference survives.
- Download a surah, disconnect network, replay local audio.
- Interrupt a download and verify no partial file is treated as complete.
- Verify Hafs recordings are never labeled as Warsh/Qalun.

## Accessibility and layout

- Arabic uses RTL direction throughout navigation and content chrome.
- Large screens use NavigationRail; phones use NavigationBar.
- Test system font scaling at 100%, 150% and 200%.
- Check 48dp+ interactive targets and screen-reader labels.
- Test portrait/landscape where supported.
- Ensure no clipped Quran text, modal bottom sheets, dialogs or safe-area
  overlap.

## Bug severity

- **P0:** wrong/fabricated/mislabeled sacred text, data corruption, crash on
  core reading.
- **P1:** bookmark/note loss, wrong navigation/reference, search returning
  mismatched source text, broken offline core flow.
- **P2:** audio/download defect, serious layout/accessibility issue.
- **P3:** cosmetic/polish issue.

A beta is ready to widen only when there are zero open P0/P1 issues and all
automated CI checks are green.
