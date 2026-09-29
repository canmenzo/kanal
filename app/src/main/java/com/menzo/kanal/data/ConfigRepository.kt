package com.menzo.kanal.data

import android.content.Context
import com.menzo.kanal.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object ConfigRepository {
    // Set at build time from the kanal.configUrl Gradle property (see app/build.gradle.kts).
    // Editing the hosted file changes channels without rebuilding.
    val CONFIG_URL: String = BuildConfig.CONFIG_URL

    private val json = Json { ignoreUnknownKeys = true }

    // Try the remote config; fall back to the copy bundled in assets/.
    suspend fun load(context: Context): AppConfig = withContext(Dispatchers.IO) {
        val text = runCatching { Net.get(CONFIG_URL) }.getOrNull()
            ?: context.assets.open("channels.json").bufferedReader().use { it.readText() }
        json.decodeFromString(AppConfig.serializer(), text)
    }

    // Channels for a single M3U source (each source is browsed on its own screen),
    // enriched with country + categories from the iptv-org database for filtering.
    suspend fun loadM3uSource(context: Context, source: Channel): List<Channel> = withContext(Dispatchers.IO) {
        val channels = runCatching { M3uParser.parse(Net.get(source.url)) }.getOrDefault(emptyList())
        if (channels.isEmpty()) channels
        else Metadata.enrich(channels, Metadata.index(context))
    }
}
