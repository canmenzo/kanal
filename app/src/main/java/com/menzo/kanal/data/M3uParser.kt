package com.menzo.kanal.data

object M3uParser {
    // Parses an M3U/M3U8 playlist into a flat channel list.
    // Channel name comes from the text after the comma on #EXTINF lines.
    fun parse(text: String): List<Channel> {
        val out = mutableListOf<Channel>()
        var pendingName: String? = null
        var id = 0
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    pendingName = line.substringAfter(',', "").trim().ifEmpty { null }
                }
                line.isEmpty() || line.startsWith("#") -> Unit
                else -> {
                    id++
                    out += Channel(
                        id = id,
                        name = pendingName ?: "Channel $id",
                        type = "hls",
                        url = line
                    )
                    pendingName = null
                }
            }
        }
        return out
    }
}
