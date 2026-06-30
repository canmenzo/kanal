package com.menzo.kanal.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object ConfigRepository {
    // Host your channel list here; edit it to change channels without rebuilding.
    const val CONFIG_URL = "https://canmenzo.com/channels.json"

    private val json = Json { ignoreUnknownKeys = true }

    // Try the remote config; fall back to the copy bundled in assets/.
    suspend fun load(context: Context): AppConfig = withContext(Dispatchers.IO) {
        val text = runCatching { Net.get(CONFIG_URL) }.getOrNull()
            ?: context.assets.open("channels.json").bufferedReader().use { it.readText() }
        json.decodeFromString(AppConfig.serializer(), text)
    }

    // Channels for a single M3U source (each source is browsed on its own screen).
    suspend fun loadM3uSource(source: Channel): List<Channel> = withContext(Dispatchers.IO) {
        runCatching { M3uParser.parse(Net.get(source.url)) }.getOrDefault(emptyList())
    }
}
