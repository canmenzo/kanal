# kanal

Personal Android TV media player for the **Onn 4K** (Google TV / Android 12).
Plays channels from a remote-hosted JSON config. Sideload only — no Play Store.

- **Embed channels** — fixed tiles, played in a fullscreen WebView.
- **M3U channels** — one or more `.m3u` playlists, parsed and listed, played via Media3/ExoPlayer at the source's native resolution + fps (4K-capable, hardware decode).

See [SPEC.md](SPEC.md) for the design. Channel list lives in [channels.json](channels.json)
(host it at `https://canmenzo.com/channels.json`; a copy is bundled in
`app/src/main/assets/` as offline fallback).

## Build
1. Open in Android Studio (it provisions the SDK on first run).
2. Let Gradle sync.
3. `adb connect <onn-ip>:5555` then Run, or build an APK and sideload.

## Stack
Kotlin · Jetpack Compose for TV · Media3/ExoPlayer · WebView · Gradle (KTS)
