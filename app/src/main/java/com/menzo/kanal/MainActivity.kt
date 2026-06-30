package com.menzo.kanal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.menzo.kanal.data.AppConfig
import com.menzo.kanal.data.Channel
import com.menzo.kanal.data.ConfigRepository
import com.menzo.kanal.ui.HomeScreen
import com.menzo.kanal.ui.LoadingScreen
import com.menzo.kanal.ui.M3uListScreen
import com.menzo.kanal.ui.PlayerScreen
import com.menzo.kanal.ui.WebPlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) { App() }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data class Web(val channel: Channel) : Screen
    data class M3uList(val source: Channel) : Screen
    data class Play(val channel: Channel, val source: Channel) : Screen
}

@Composable
private fun App() {
    val context = LocalContext.current
    var config by remember { mutableStateOf<AppConfig?>(null) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    LaunchedEffect(Unit) { config = ConfigRepository.load(context) }

    val cfg = config ?: run { LoadingScreen(); return }

    when (val s = screen) {
        is Screen.Home -> HomeScreen(
            config = cfg,
            onEmbed = { screen = Screen.Web(it) },
            onM3uSource = { screen = Screen.M3uList(it) }
        )

        is Screen.Web -> {
            BackHandler { screen = Screen.Home }
            WebPlayerScreen(s.channel)
        }

        is Screen.M3uList -> {
            BackHandler { screen = Screen.Home }
            M3uListScreen(
                source = s.source,
                onPlay = { screen = Screen.Play(it, s.source) }
            )
        }

        is Screen.Play -> {
            BackHandler { screen = Screen.M3uList(s.source) }
            PlayerScreen(s.channel)
        }
    }
}
