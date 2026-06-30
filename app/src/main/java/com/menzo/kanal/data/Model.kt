package com.menzo.kanal.data

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: Int = 0,
    val name: String,
    val type: String, // "embed" | "hls" | "m3u" | "mp4"
    val url: String,
    val tvgId: String = "",
    val country: String = "",           // ISO 3166 alpha-2, e.g. "TR"
    val categories: List<String> = emptyList(), // e.g. ["sports"]
    val logo: String = ""
)

@Serializable
data class AppConfig(
    val embedChannels: List<Channel> = emptyList(),
    val m3uSources: List<Channel> = emptyList()
)
