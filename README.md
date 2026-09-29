# 📺 kanal

[![build](https://github.com/canmenzo/kanal/actions/workflows/build.yml/badge.svg)](https://github.com/canmenzo/kanal/actions/workflows/build.yml) ![platform](https://img.shields.io/badge/platform-Android%20TV-3DDC84?logo=android&logoColor=white) ![kotlin](https://img.shields.io/badge/kotlin-2.0-7F52FF?logo=kotlin&logoColor=white) ![min sdk](https://img.shields.io/badge/min%20SDK-21-lightgrey) [![license](https://img.shields.io/github/license/canmenzo/kanal)](LICENSE)

A small sideloaded Android TV player (built and tested on the Onn 4K, Google TV / Android 12). It reads a channel list from a JSON file you host, and plays embed pages in a WebView and M3U playlist streams in Media3/ExoPlayer. Not on the Play Store.

### ✨ Features
- 🧩 Embed channels: fixed tiles on the home screen, opened fullscreen in a WebView.
- 📋 M3U sources: each playlist is browsed on its own screen, sorted A to Z, with a country flag and category tag per row.
- 🔎 Left rail with search plus multi-select country and category filters (OR within a filter, AND across filters).
- 🏷️ Country comes from `tvg-country` or the iptv-org style `tvg-id` suffix; categories come from an optional metadata map, cached on the device for a week.
- ▶️ Direct streams play via Media3/ExoPlayer (HLS included) at the source resolution, with loading and "stream unavailable" overlays.
- 💾 A bundled copy of `channels.json` is used when the hosted one can't be reached.

### 🚀 Quick start
1. Host your own copy of [`channels.json`](channels.json) somewhere the TV can reach (GitHub raw URL, a Gist, any static host).
2. Build with your URL (see Configuration) and install:

```sh
./gradlew assembleDebug -Pkanal.configUrl=https://example.com/channels.json
adb connect <tv-ip>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

You need JDK 17 and the Android SDK (opening the project once in Android Studio sets both up). Enable developer options and network debugging on the TV first.

### ⚙️ Configuration
The URLs are baked in at build time from two Gradle properties, so changing them means a rebuild. Editing the hosted JSON itself does not.

| Property | What | Default |
|---|---|---|
| `kanal.configUrl` | Channel list | `https://example.com/channels.json` |
| `kanal.metaUrl` | Country/category map (optional) | none: channels only get the country from the playlist |

Pass them with `-P` as above, or set them once in `~/.gradle/gradle.properties`:

```properties
kanal.configUrl=https://example.com/channels.json
kanal.metaUrl=https://example.com/meta.json
```

The app falls back to the bundled `app/src/main/assets/channels.json` when the hosted list can't be reached, so update it too if you want it to match.

<details>
<summary>File formats</summary>

`channels.json`:

```json
{
  "embedChannels": [
    { "id": 1, "name": "My channel", "type": "embed", "url": "https://example.com/embed/1" }
  ],
  "m3uSources": [
    { "id": 1, "name": "IPTV-org", "type": "m3u", "url": "https://iptv-org.github.io/iptv/index.m3u" }
  ]
}
```

The metadata map is a flat JSON object from `tvg-id` to `"CC|category1,category2"`, for example `{ "TRT1.tr": "TR|general" }`. Generate it from the iptv-org database with `python3 tools/make_meta.py` and host it next to `channels.json`. If it can't be fetched, channels still get a country from the playlist, just no categories.

</details>

See [SPEC.md](SPEC.md) for the original design notes.

### 🛠️ Stack
Kotlin, Jetpack Compose for TV, Media3/ExoPlayer, Android WebView, Gradle (KTS).

### 📄 License
[MIT](LICENSE)
