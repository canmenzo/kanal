package com.menzo.kanal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.menzo.kanal.data.Channel
import com.menzo.kanal.data.ConfigRepository
import java.util.Locale

// Countries pinned to the top of the country picker, in this order.
private val PRIORITY = listOf("TR", "PT", "US")

private enum class Picker { COUNTRY, CATEGORY }

@Composable
fun M3uListScreen(source: Channel, onPlay: (Channel) -> Unit) {
    val context = LocalContext.current
    val all by produceState<List<Channel>?>(initialValue = null, source) {
        value = ConfigRepository.loadM3uSource(context, source)
    }
    when (val list = all) {
        null -> CenterText("Loading ${source.name}…")
        else -> if (list.isEmpty()) CenterText("No channels found in ${source.name}.")
        else Loaded(source, list, onPlay)
    }
}

@Composable
private fun Loaded(source: Channel, channels: List<Channel>, onPlay: (Channel) -> Unit) {
    var query by remember { mutableStateOf("") }
    val selCountries = remember { mutableStateListOf<String>() }
    val selCats = remember { mutableStateListOf<String>() }
    var picker by remember { mutableStateOf<Picker?>(null) }
    val railFocus = remember { FocusRequester() }

    val countries = remember(channels) {
        channels.map { it.country }.filter { it.length == 2 }.distinct().sortedWith(
            compareBy({ PRIORITY.indexOf(it).let { i -> if (i < 0) Int.MAX_VALUE else i } }, { countryName(it) })
        )
    }
    val categories = remember(channels) {
        channels.flatMap { it.categories }.distinct().sorted()
    }
    val filtered = remember(channels, query, selCountries.toList(), selCats.toList()) {
        channels.asSequence()
            .filter { selCountries.isEmpty() || it.country in selCountries }
            .filter { selCats.isEmpty() || it.categories.any { c -> c in selCats } }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    BackHandler(enabled = picker != null) { picker = null }
    LaunchedEffect(Unit) { runCatching { railFocus.requestFocus() } }

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize().padding(36.dp)) {
            // Left rail — always one LEFT-press away from the list.
            Column(Modifier.width(300.dp).padding(end = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(source.name, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                SearchField(query) { query = it }
                RailButton("Country" + countLabel(selCountries.size), Modifier.focusRequester(railFocus)) { picker = Picker.COUNTRY }
                RailButton("Category" + countLabel(selCats.size)) { picker = Picker.CATEGORY }
                if (selCountries.isNotEmpty() || selCats.isNotEmpty() || query.isNotEmpty()) {
                    RailButton("Clear filters") { selCountries.clear(); selCats.clear(); query = "" }
                }
                Spacer(Modifier.height(4.dp))
                Text("${filtered.size} channels", color = Color(0x99FFFFFF), style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { ch -> ChannelRow(ch) { onPlay(ch) } }
            }
        }

        when (picker) {
            Picker.COUNTRY -> PickerOverlay(
                title = "Filter by country",
                options = countries,
                selected = selCountries,
                label = { cc -> (flagEmoji(cc).let { if (it.isNotEmpty()) "$it " else "" }) + countryName(cc) }
            )
            Picker.CATEGORY -> PickerOverlay(
                title = "Filter by category",
                options = categories,
                selected = selCats,
                label = { it.replaceFirstChar(Char::uppercase) }
            )
            null -> Unit
        }
    }
}

@Composable
private fun PickerOverlay(
    title: String,
    options: List<String>,
    selected: SnapshotStateList<String>,
    label: (String) -> String
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(options) { runCatching { first.requestFocus() } }
    Box(Modifier.fillMaxSize().background(Color(0xF2000000)).padding(48.dp)) {
        Column(Modifier.fillMaxSize()) {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Select any number • press Back to apply", color = Color(0x99FFFFFF), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(20.dp))
            if (options.isEmpty()) {
                Text("Nothing to filter here.", color = Color.White)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(options) { i, key ->
                        FilterChip(
                            label = label(key),
                            selected = key in selected,
                            modifier = (if (i == 0) Modifier.focusRequester(first) else Modifier).fillMaxWidth()
                        ) { if (!selected.remove(key)) selected.add(key) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(ch: Channel, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val flag = flagEmoji(ch.country)
            if (flag.isNotEmpty()) Text(flag, fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
            Text(
                ch.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (ch.categories.isNotEmpty()) {
                Text(
                    ch.categories.first().replaceFirstChar(Char::uppercase),
                    color = Color(0x99FFFFFF),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RailButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color(0x26FFFFFF),
            contentColor = Color.White,
            focusedContainerColor = Color(0xFF2563EB),
            focusedContentColor = Color.White
        )
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp))
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Color(0xFF2563EB) else Color(0x33FFFFFF),
            contentColor = Color.White,
            focusedContainerColor = if (selected) Color(0xFF3B82F6) else Color(0x55FFFFFF),
            focusedContentColor = Color.White
        )
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().background(Color(0x33FFFFFF), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (value.isEmpty()) Text("Search…", color = Color(0x99FFFFFF), fontSize = 18.sp)
                inner()
            }
        }
    )
}

@Composable
private fun CenterText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, style = MaterialTheme.typography.titleLarge)
    }
}

private fun countLabel(n: Int): String = if (n > 0) "  ($n)" else ""

private fun flagEmoji(cc: String): String {
    if (cc.length != 2 || !cc.all { it in 'A'..'Z' }) return ""
    val a = Character.toChars(0x1F1E6 + (cc[0] - 'A'))
    val b = Character.toChars(0x1F1E6 + (cc[1] - 'A'))
    return String(a) + String(b)
}

private fun countryName(cc: String): String = Locale("", cc).displayCountry.ifEmpty { cc }
