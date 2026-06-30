package com.menzo.kanal.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

// Joins M3U channels to a slim metadata map (id -> "CC|cat1,cat2") to attach a
// reliable country + categories. The map is hosted on the site, fetched once,
// and cached on disk. Much lighter than the full iptv-org database.
object Metadata {
    private const val META_URL = "https://canmenzo.com/meta.json"
    private const val CACHE_FILE = "meta.json"
    private const val MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000 // 1 week

    data class Meta(val country: String, val categories: List<String>)

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var index: Map<String, Meta>? = null

    suspend fun index(context: Context): Map<String, Meta> {
        index?.let { return it }
        return withContext(Dispatchers.IO) {
            index?.let { return@withContext it }
            val text = cachedOrFetch(context).let { if (it.startsWith('﻿')) it.substring(1) else it }
            val raw = runCatching {
                json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), text)
            }.getOrDefault(emptyMap())
            val map = raw.mapValues { (_, v) ->
                val pipe = v.indexOf('|')
                val country = if (pipe >= 0) v.substring(0, pipe) else ""
                val cats = if (pipe >= 0) v.substring(pipe + 1) else ""
                Meta(country, cats.split(',').filter { it.isNotEmpty() })
            }
            index = map
            map
        }
    }

    fun enrich(channels: List<Channel>, index: Map<String, Meta>): List<Channel> =
        channels.map { ch ->
            val meta = index[ch.tvgId] ?: return@map ch
            ch.copy(
                country = meta.country.takeIf { it.isNotEmpty() } ?: ch.country,
                categories = meta.categories
            )
        }

    private fun cachedOrFetch(context: Context): String {
        val f = File(context.filesDir, CACHE_FILE)
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < MAX_AGE_MS) {
            return f.readText()
        }
        return try {
            val fresh = Net.get(META_URL)
            runCatching { f.writeText(fresh) }
            fresh
        } catch (e: Exception) {
            if (f.exists()) f.readText() else "{}"
        }
    }
}
