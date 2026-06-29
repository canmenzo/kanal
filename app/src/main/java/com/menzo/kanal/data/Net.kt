package com.menzo.kanal.data

import java.net.HttpURLConnection
import java.net.URL

object Net {
    fun get(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "kanal/1.0 (AndroidTV)")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
