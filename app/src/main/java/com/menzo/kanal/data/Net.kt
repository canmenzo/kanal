package com.menzo.kanal.data

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object Net {
    // The largest list we load (iptv-org index.m3u) is ~3 MB.
    private const val MAX_BYTES = 32 * 1024 * 1024

    fun get(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "kanal/1.0 (AndroidTV)")
        }
        val out = ByteArrayOutputStream()
        conn.inputStream.use { input ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                out.write(buf, 0, n)
                if (out.size() > MAX_BYTES) throw IOException("Response from $url exceeds $MAX_BYTES bytes")
            }
        }
        return out.toString("UTF-8")
    }
}
