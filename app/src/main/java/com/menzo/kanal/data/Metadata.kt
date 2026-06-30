package com.menzo.kanal.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

// Joins M3U channels to iptv-org's channel database (by tvg-id) to attach a
// reliable country + categories. The DB is fetched once and cached on disk.
object Metadata {
    private const val DB_URL = "https://iptv-org.github.io/api/channels.json"
    private const val CACHE_FILE = "iptv_channels.json"
    private const val MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000 // 1 week

    @Serializable
    data class Meta(
        val id: String? = null,
        val country: String? = null,
        val categories: List<String> = emptyList()
    )

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var index: Map<String, Meta>? = null

    suspend fun index(context: Context): Map<String, Meta> {
        index?.let { return it }
        return withContext(Dispatchers.IO) {
            index?.let { return@withContext it }
            val text = cachedOrFetch(context)
            val list = runCatching {
                json.decodeFromString(ListSerializer(Meta.serializer()), text)
            }.getOrDefault(emptyList())
            val map = list.filter { !it.id.isNullOrEmpty() }.associateBy { it.id!! }
            index = map
            map
        }
    }

    fun enrich(channels: List<Channel>, index: Map<String, Meta>): List<Channel> =
        channels.map { ch ->
            val meta = index[ch.tvgId] ?: return@map ch
            ch.copy(
                country = meta.country?.uppercase()?.takeIf { it.isNotEmpty() } ?: ch.country,
                categories = meta.categories
            )
        }

    private fun cachedOrFetch(context: Context): String {
        val f = File(context.filesDir, CACHE_FILE)
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < MAX_AGE_MS) {
            return f.readText()
        }
        return try {
            val fresh = Net.get(DB_URL)
            runCatching { f.writeText(fresh) }
            fresh
        } catch (e: Exception) {
            if (f.exists()) f.readText() else "[]"
        }
    }
}
