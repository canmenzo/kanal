package com.menzo.kanal.ui

import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.menzo.kanal.data.Channel
import com.menzo.kanal.data.refererUrl

@Composable
fun WebPlayerScreen(channel: Channel) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                // Embed pages routinely pull http subresources; don't let mixed-content block them.
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                // Some providers open the real stream via window.open() from the iframe.
                settings.setSupportMultipleWindows(true)
                settings.javaScriptCanOpenWindowsAutomatically = true
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    // Load window.open() popups in this same WebView instead of dropping them.
                    override fun onCreateWindow(
                        view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
                    ): Boolean {
                        (resultMsg.obj as WebView.WebViewTransport).webView = view
                        resultMsg.sendToTarget()
                        return true
                    }
                }
                val referer = channel.refererUrl
                if (referer.isBlank()) {
                    loadUrl(channel.url)
                } else {
                    // Wrap the embed in an iframe and load it under `referer` as the page
                    // origin, so the iframe's request carries that Referer/Origin header.
                    // `referrer content=unsafe-url` forces WebView to send the full Referer
                    // even cross-origin (default strict-origin-when-cross-origin strips/omits it).
                    val html = """
                        <!DOCTYPE html><html><head>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <meta name="referrer" content="unsafe-url">
                        <style>html,body{margin:0;height:100%;background:#000;overflow:hidden}
                        iframe{border:0;width:100%;height:100%}</style>
                        </head><body>
                        <iframe src="${channel.url}" referrerpolicy="unsafe-url" allow="encrypted-media; autoplay; fullscreen" allowfullscreen></iframe>
                        </body></html>
                    """.trimIndent()
                    loadDataWithBaseURL(referer, html, "text/html", "UTF-8", null)
                }
            }
        }
    )
}
