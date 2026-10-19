package com.menzo.kanal.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.menzo.kanal.data.Channel
import com.menzo.kanal.data.refererUrl

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(channel: Channel) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var buffering by remember { mutableStateOf(true) }

    val player = remember {
        // Browser-like UA + optional Referer: many streams reject ExoPlayer's default UA
        // or gate on Referer, surfacing as "stream unavailable".
        val http = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        val referer = channel.refererUrl
        if (referer.isNotBlank()) {
            http.setDefaultRequestProperties(mapOf("Referer" to referer))
        }
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(http))
            .build().apply {
            setMediaItem(MediaItem.fromUri(channel.url))
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state != Player.STATE_BUFFERING) buffering = false
                }

                override fun onPlayerError(e: PlaybackException) {
                    // Show the real cause so referer/UA/404/403 issues are diagnosable on-screen.
                    error = "${e.errorCodeName}\n${e.cause?.message ?: ""}"
                }
            })
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                }
            }
        )
        when {
            error != null -> Overlay("Stream unavailable\n${channel.name}")
            buffering -> Overlay("Loading ${channel.name}…")
        }
    }
}

@Composable
private fun Overlay(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = Color.White, style = MaterialTheme.typography.titleLarge)
    }
}
