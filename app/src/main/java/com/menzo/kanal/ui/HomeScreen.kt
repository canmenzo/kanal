package com.menzo.kanal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.menzo.kanal.R
import com.menzo.kanal.data.AppConfig
import com.menzo.kanal.data.Channel

private val EDGE = 48.dp // TV overscan-safe horizontal margin

@Composable
fun HomeScreen(
    config: AppConfig,
    onEmbed: (Channel) -> Unit,
    onM3uSource: (Channel) -> Unit
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 44.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionHeader("Live")
            LazyRow(
                contentPadding = PaddingValues(horizontal = EDGE, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(config.embedChannels) { index, ch ->
                    ChannelCard(
                        title = ch.name,
                        modifier = if (index == 0) Modifier.focusRequester(firstFocus) else Modifier,
                        onClick = { onEmbed(ch) }
                    )
                }
            }

            SectionHeader("Playlists")
            LazyRow(
                contentPadding = PaddingValues(horizontal = EDGE, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(config.m3uSources) { src ->
                    ChannelCard(title = src.name, onClick = { onM3uSource(src) })
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = EDGE)
    )
}

@Composable
private fun ChannelCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = modifier.size(width = 300.dp, height = 168.dp)) {
        Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomStart) {
            Text(
                title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
