package com.menzo.kanal.data

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: Int = 0,
    val name: String,
    val type: String, // "embed" | "hls" | "m3u" | "mp4"
    val url: String,
    val referer: String = "",            // origin to spoof for embed iframes, e.g. "https://example.com/"
    val tvgId: String = "",
    val country: String = "",           // ISO 3166 alpha-2, e.g. "TR"
    val categories: List<String> = emptyList(), // e.g. ["sports"]
    val logo: String = ""
)

// A usable http(s) Referer/base URL from the (possibly scheme-less) config value,
// e.g. "timstreams.st" -> "https://timstreams.st/". Blank stays blank.
val Channel.refererUrl: String
    get() = when {
        referer.isBlank() -> ""
        referer.startsWith("http") -> referer
        else -> "https://$referer/"
    }

@Serializable
data class AppConfig(
    val embedChannels: List<Channel> = emptyList(),
    val m3uSources: List<Channel> = emptyList()
)
