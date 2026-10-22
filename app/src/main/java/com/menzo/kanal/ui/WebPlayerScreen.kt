package com.menzo.kanal.ui

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.menzo.kanal.data.Channel
import com.menzo.kanal.data.refererUrl

// Embeds come from a remote list; never let them hand off to intent:, market:, file:, etc.
private fun isWebUrl(url: String?): Boolean =
    Uri.parse(url ?: "").scheme?.lowercase() in setOf("http", "https", "about")

private fun htmlEscape(s: String): String =
    s.replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&#39;")
        .replace("<", "&lt;").replace(">", "&gt;")

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
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                // Embed pages routinely pull http subresources; don't let mixed-content block them.
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                // Some providers open the real stream via window.open() from the iframe.
                settings.setSupportMultipleWindows(true)
                settings.javaScriptCanOpenWindowsAutomatically = true
                webViewClient = object : WebViewClient() {
                    @Deprecated("Called below API 24")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean =
                        !isWebUrl(url)

                    @TargetApi(Build.VERSION_CODES.N)
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                        !isWebUrl(request.url.toString())
                }
                webChromeClient = object : WebChromeClient() {
                    // A WebView cannot host its own popup, so catch the popup's first URL in a
                    // throwaway WebView and load it here if it passes the same scheme check.
                    override fun onCreateWindow(
                        view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message
                    ): Boolean {
                        val probe = WebView(view.context)
                        probe.webViewClient = object : WebViewClient() {
                            private fun take(v: WebView, url: String?): Boolean {
                                if (url != null && isWebUrl(url)) view.loadUrl(url)
                                v.destroy()
                                return true
                            }

                            @Deprecated("Called below API 24")
                            override fun shouldOverrideUrlLoading(v: WebView, url: String?): Boolean = take(v, url)

                            @TargetApi(Build.VERSION_CODES.N)
                            override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean =
                                take(v, request.url.toString())
                        }
                        (resultMsg.obj as WebView.WebViewTransport).webView = probe
                        resultMsg.sendToTarget()
                        return true
                    }
                }
                val referer = channel.refererUrl
                val scheme = Uri.parse(channel.url).scheme?.lowercase()
                if (scheme != "http" && scheme != "https") {
                    loadUrl("about:blank")
                } else if (referer.isBlank()) {
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
                        <iframe src="${htmlEscape(channel.url)}" referrerpolicy="unsafe-url" allow="encrypted-media; autoplay; fullscreen" allowfullscreen></iframe>
                        </body></html>
                    """.trimIndent()
                    loadDataWithBaseURL(referer, html, "text/html", "UTF-8", null)
                }
            }
        }
    )
}
