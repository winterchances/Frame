# Native Android port — source-of-truth checklist

This document is derived from the immutable `main/index.html` source (83,881 characters, 1,592 lines; source blob `ce7c5fffbe2b91b31144fbb1484cb2d18be25340`). Do not redesign the interface. The native implementation is accepted only when each item below has a real implementation and has been compared against the original.

## Visual specification

- Base palette: background `#F5F5F7`, main text `#1A1A1A`, teal `#7FB5B1`, glass white `rgba(255,255,255,.85)`, subtle border `rgba(0,0,0,.05)`.
- Mobile reference viewport: 375 × 812 CSS px; mobile layout fills the device viewport with no outer frame radius.
- Content side inset: 20 px; page bottom reserve: 100 px.
- Header: top inset 60 px, bottom 15 px; title 30 px / weight 800 / letter spacing .5 px. Header action circles 36 × 36 px, SVG 20 × 20 px, stroke width 2.2.
- Sticky translucent headers use 10 px blur.
- Statistics panel: 15 × 10 px padding, 20 px radius, 15 px blur, bottom gap 25 px.
- Section title: 18 px / weight 700; leading teal marker 4 × 16 px.
- Home hero cards: 280 × 160 px, radius 20 px, horizontal gap 15 px; gradient overlay, title 16 px, progress line 4 px.
- Home recent cards: 100 × 65 px thumbnails, radius 10 px; card radius 16 px, padding 12 px, gap 15 px; title 14 px / weight 600.
- Library: primary tabs 16 px with 20 × 3 px active underline; category pills radius 20 px and 8 × 16 px padding; compact grid 3 columns / 10 px gap; normal grid 2 columns / 15 px gap; poster ratio 100:140.
- Recent timeline cards: thumbnail 120 × 84 px; info height 84 px; title 15 px / weight 500 / max 2 lines; grouped by date with sticky group headers.
- Records: card radius 20 px / padding 15 px; thumbnail 75 × 100 px; title 16 px / weight 700; stars #FFB800; note background #F9F9FB with 3 px teal leading border.
- Bottom navigation: 16 px horizontal inset, 16 px bottom offset, pill height 56 px, radius 28 px, 4 px inner padding. Sliding indicator transition 300 ms, cubic-bezier(.25,1,.5,1). SVG 20 px; labels 10 px.
- FAB: main 56 × 56 px; sub buttons 48 × 48 px; original rotation/opacity/translation stagger 50/100 ms.
- Modal overlay: black alpha .4 + 5 px blur. Bottom sheet: top radius 30 px, padding 25 px 20 px 40 px, max height 85%; transition 300 ms cubic-bezier(.2,.8,.2,1).
- Player: preserve original full-screen layout and CSS dimensions, not a generic system player UI.

## Pages and interactions

### Home
- Header settings action.
- Statistics: total videos, watched videos, current-week watch time.
- Continue-watching horizontal hero carousel; show at most five entries with progress.
- Collapsible Continue Watching and Recently Added sections, with original chevron rotation and collapse animation.
- Recently added list, four entries; tap opens player.

### Library
- Tabs: all / playing / watched.
- Horizontal category pills: all categories, favorites, custom categories.
- Grid switch: normal 2-column / compact 3-column.
- Sorting by added time or filename.
- Tap poster to play; long-press poster opens action sheet (no permanent three-dot icon).
- Actions: play, favorite toggle, move category, rename, custom cover, remove from library.
- Category management: create and delete custom categories; protect built-in categories.

### Recent
- Date groups: today / yesterday / earlier.
- Each row shows thumbnail, title, progress, and metadata as in source.
- Per-item menu and delete-playback-record confirmation.
- Clear all playback progress while retaining library entries.

### Records
- Watched-only entries, original statistics, poster, watched date, rating and review.
- Search reviews; sort by watched time or rating.
- Edit record: 1–5 star rating and review text.
- Delete/clear behavior must match source logic.

### Shared UI
- Four-page horizontal swipe navigation, with nested horizontal scrollers not hijacking page swipes.
- Animated sliding navigation indicator.
- FAB opens search and import actions with staggered animation.
- Search sheets and all modal sheets, overlay dismissal, keyboard-aware sheet layout.
- Toasts with original ~2 second display.
- Settings persisted: background playback and keep-screen-awake.
- Clear-all-data confirmation and local-data privacy notice.

### Player
- Full-screen video surface, title, close, orientation toggle, lock.
- Timeline scrubber, current / total time, play-pause, speed cycling: 1.0 → 1.25 → 1.5 → 2.0 → 0.75 → 1.0.
- Auto-hide player controls after 3 seconds while playing.
- Tap toggles controls/playback; double tap left/right seeks ±10 seconds.
- Long press temporarily sets 2.0× playback.
- Left-third vertical gesture adjusts screen brightness; right-third adjusts volume.
- Middle horizontal gesture scrubs timeline.
- Lock mode prevents accidental gestures and exposes unlock affordance.
- Playback resumes from stored progress; reaching end sets watched status and 100% progress.
- Background playback and keep-screen-awake obey persisted settings.

## Data and native behavior

- Persist video metadata, categories, status, progress, favorite, review, rating and custom cover.
- Keep persistable Storage Access Framework URI permissions so imported files remain playable after restart.
- Generate and persist real video thumbnails; use the same thumbnail in all relevant cards and player backdrop. Read the actual media duration from the selected local URI during import, persist it, and refresh the UI when asynchronous thumbnail/metadata extraction finishes.
- Do not seed demo videos or sample viewing records. The original starts with an empty database (`myCinemaDB || []`); its fixed Recent-page values `02:15:00` and `00:45:10` are display placeholders, not real records. Native UI must derive total duration and watched position from the imported file's actual duration and saved progress.
- Keep playback position and duration accurate; do not use decorative placeholder thumbnails or fake play actions.
- Use native Kotlin/Compose UI and Media3 playback. Do not wrap the old HTML in WebView.

## Release gate

Do not trigger the APK workflow until every item is implemented, the source-to-native checklist has been reviewed, and the project has passed a complete build. A successful build alone is not evidence of visual or interaction parity.
