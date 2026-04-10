package com.widgetlauncher.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView
import com.widgetlauncher.bridge.AndroidBridge
import com.widgetlauncher.data.Widget

class WidgetViewHolder(
    itemView: View,
    private val webView: WebView
) : RecyclerView.ViewHolder(itemView) {

    private var currentWidgetId: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun bind(widget: Widget, context: Context) {
        configureWebView(widget, context)
        loadWidget(widget)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(widget: Widget, context: Context) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = false
            allowContentAccess = false
        }
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.webChromeClient = WebChromeClient()
        webView.isScrollbarFadingEnabled = true
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.isHorizontalScrollBarEnabled = false
        webView.isVerticalScrollBarEnabled = false

        // Remove previous bridge if widget changed
        if (currentWidgetId != null && currentWidgetId != widget.id) {
            webView.removeJavascriptInterface("AndroidBridge")
        }

        val bridge = AndroidBridge(context, widget.id) { message ->
            // Handle widget messages
        }
        webView.addJavascriptInterface(bridge, "AndroidBridge")
        currentWidgetId = widget.id
    }

    private fun loadWidget(widget: Widget) {
        // Inject the bridge script before user code
        val fullHtml = injectBridge(widget.code, widget.id)
        webView.loadDataWithBaseURL(
            "http://localhost",
            fullHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun injectBridge(html: String, widgetId: String): String {
        val bridgeScript = """
            <script>
            ${AndroidBridge.getInjectionScript(widgetId)}
            </script>
        """.trimIndent()

        return if (html.contains("<head>", ignoreCase = true)) {
            html.replace("<head>", "<head>\n$bridgeScript", ignoreCase = true)
        } else if (html.contains("<html>", ignoreCase = true)) {
            html.replace("<html>", "<html><head>$bridgeScript</head>", ignoreCase = true)
        } else {
            "$bridgeScript\n$html"
        }
    }

    fun pauseWebView() {
        webView.onPause()
        webView.pauseTimers()
    }

    fun resumeWebView() {
        webView.onResume()
        webView.resumeTimers()
    }

    fun destroyWebView() {
        webView.destroy()
    }
}
