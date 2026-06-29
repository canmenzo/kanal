package com.menzo.kanal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.menzo.kanal.R
import com.menzo.kanal.data.AppConfig
import com.menzo.kanal.data.Channel

@Composable
fun HomeScreen(
    config: AppConfig,
    onEmbed: (Channel) -> Unit,
    onM3u: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.padding(48.dp)) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "kanal",
                modifier = Modifier.height(72.dp)
            )
            Spacer(Modifier.height(32.dp))

            Text("Live", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(config.embedChannels) { ch ->
                    ChannelCard(title = ch.name) { onEmbed(ch) }
                }
            }

            Spacer(Modifier.height(40.dp))
            Text("Playlists", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            ChannelCard(title = "M3U Channels ▸", onClick = onM3u)
        }
    }
}

@Composable
private fun ChannelCard(title: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.size(width = 260.dp, height = 150.dp)) {
        Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomStart) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
