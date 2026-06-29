# Onn TV — personal Android TV media player

Personal sideloaded app for the Onn 4K (Google TV / Android 12). Plays channels from a
remote-hosted JSON config. No Play Store, sideload only.

## Sources
- **Embed channels** — fixed set (4 for v1), played in a fullscreen WebView.
- **M3U channels** — one or more `.m3u` playlist URLs; app fetches + parses, lists every
  channel inside, each plays via ExoPlayer.

## Config
App fetches `channels.json` (hosted at e.g. canmenzo.com) on launch. Editing the JSON
changes channels without rebuilding the APK. See `channels.json` for the shape.

## Home screen
- Row of embed tiles: `Channel 1 (Embed URL 1)` … `Channel 4 (Embed URL 4)`
- `M3U Channels` entry → drills into parsed playlist → click any channel to play

## Playback
- Direct streams (HLS / `.m3u8` / `.mp4`, and entries from `.m3u`) → **Media3 / ExoPlayer**,
  hardware decode (HEVC/AV1), native res + fps, frame-rate matching (`Surface.setFrameRate`).
- Embed pages → **WebView**, fullscreen, `encrypted-media` + PiP allowed.

## Quality
Plays source as-is — no up/downscale. 4K60 HDR ceiling = Onn hardware. WebView path is the
unreliable one for high res/fps; direct/M3U path is the quality path.

## Stack
Kotlin · Jetpack Compose for TV · Media3/ExoPlayer · Android WebView · Gradle (KTS)

## Deploy
Android Studio → build APK → `adb connect <onn-ip>` → `adb install`. Test on the real box.
