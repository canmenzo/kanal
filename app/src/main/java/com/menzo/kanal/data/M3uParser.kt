package com.menzo.kanal.data

object M3uParser {
    private val attrRegex = Regex("([A-Za-z0-9_-]+)=\"([^\"]*)\"")

    // Parses an M3U/M3U8 playlist into channels, pulling EXTINF attributes
    // (tvg-id, tvg-country, tvg-logo). Categories are filled later via Metadata.
    fun parse(text: String): List<Channel> {
        val out = mutableListOf<Channel>()
        var attrs: Map<String, String> = emptyMap()
        var pendingName = ""
        var id = 0
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    attrs = attrRegex.findAll(line).associate {
                        it.groupValues[1].lowercase() to it.groupValues[2]
                    }
                    pendingName = line.substringAfterLast(',').trim()
                }
                line.isEmpty() || line.startsWith("#") -> Unit
                else -> {
                    id++
                    val country = (attrs["tvg-country"] ?: "")
                        .split(';', ',').firstOrNull()?.trim()?.uppercase().orEmpty()
                    out += Channel(
                        id = id,
                        name = pendingName.ifEmpty { attrs["tvg-name"] ?: "Channel $id" },
                        type = "hls",
                        url = line,
                        tvgId = attrs["tvg-id"].orEmpty(),
                        country = country,
                        logo = attrs["tvg-logo"].orEmpty()
                    )
                    attrs = emptyMap()
                    pendingName = ""
                }
            }
        }
        return out
    }
}
