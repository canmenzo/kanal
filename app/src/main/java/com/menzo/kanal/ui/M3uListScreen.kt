package com.menzo.kanal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.menzo.kanal.data.Channel
import com.menzo.kanal.data.ConfigRepository

@Composable
fun M3uListScreen(
    sources: List<Channel>,
    onPlay: (Channel) -> Unit
) {
    val channels by produceState<List<Channel>?>(initialValue = null, sources) {
        value = ConfigRepository.loadM3u(sources)
    }

    val list = channels
    when {
        list == null -> CenterText("Loading channels…")
        list.isEmpty() -> CenterText("No channels found. Check your M3U source URL.")
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(list) { ch ->
                Card(onClick = { onPlay(ch) }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        ch.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}
