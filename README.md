# 📺 kanal

![platform](https://img.shields.io/badge/platform-Android%20TV-3DDC84?logo=android&logoColor=white) ![kotlin](https://img.shields.io/badge/kotlin-2.0-7F52FF?logo=kotlin&logoColor=white) ![min sdk](https://img.shields.io/badge/min%20SDK-21-lightgrey)

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
2. Point the app at it (see Configuration), then build and install:

```sh
./gradlew assembleDebug
adb connect <tv-ip>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

You need JDK 17 and the Android SDK (opening the project once in Android Studio sets both up). Enable developer options and network debugging on the TV first.

### ⚙️ Configuration
The URLs are constants in the Kotlin source, so changing them means a rebuild. Editing the hosted JSON itself does not.

| What | File | Constant |
|---|---|---|
| Channel list | `app/src/main/java/com/menzo/kanal/data/ConfigRepository.kt` | `CONFIG_URL` |
| Country/category map (optional) | `app/src/main/java/com/menzo/kanal/data/Metadata.kt` | `META_URL` |

```kotlin
const val CONFIG_URL = "https://example.com/channels.json"
```

Also update the offline fallback at `app/src/main/assets/channels.json` if you want it to match your list.

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

The metadata map is a flat JSON object from `tvg-id` to `"CC|category1,category2"`, for example `{ "TRT1.tr": "TR|general" }`. If it can't be fetched, channels still get a country from the playlist, just no categories.

</details>

See [SPEC.md](SPEC.md) for the original design notes.

### 🛠️ Stack
Kotlin, Jetpack Compose for TV, Media3/ExoPlayer, Android WebView, Gradle (KTS).

### 📄 License
No license yet.
