package com.widgetlauncher.bridge

import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject

class AndroidBridge(
    private val context: Context,
    private val widgetId: String,
    private val onMessage: (String) -> Unit = {}
) {

    @JavascriptInterface
    fun getTime(): String = java.text.SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        java.util.Locale.US
    ).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        .format(java.util.Date())

    @JavascriptInterface
    fun getWidgetId(): String = widgetId

    @JavascriptInterface
    fun postMessage(json: String) {
        onMessage(json)
    }

    @JavascriptInterface
    fun storageGet(key: String): String? {
        val prefs = context.getSharedPreferences("widget_storage_$widgetId", Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    @JavascriptInterface
    fun storageSet(key: String, value: String) {
        val prefs = context.getSharedPreferences("widget_storage_$widgetId", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    @JavascriptInterface
    fun storageRemove(key: String) {
        val prefs = context.getSharedPreferences("widget_storage_$widgetId", Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    @JavascriptInterface
    fun getDeviceInfo(): String {
        return JSONObject().apply {
            put("model", android.os.Build.MODEL)
            put("sdk", android.os.Build.VERSION.SDK_INT)
            put("brand", android.os.Build.BRAND)
        }.toString()
    }

    // Inject this script into every WebView to set up the bridge
    companion object {
        fun getInjectionScript(widgetId: String): String = """
            (function() {
                window.WidgetBridge = {
                    getTime: function() { return AndroidBridge.getTime(); },
                    getWidgetId: function() { return AndroidBridge.getWidgetId(); },
                    postMessage: function(msg) { AndroidBridge.postMessage(JSON.stringify(msg)); },
                    storage: {
                        get: function(key) { return AndroidBridge.storageGet(key); },
                        set: function(key, val) { AndroidBridge.storageSet(key, String(val)); },
                        remove: function(key) { AndroidBridge.storageRemove(key); }
                    },
                    device: function() { return JSON.parse(AndroidBridge.getDeviceInfo()); }
                };
                // Alias for compatibility
                window.AndroidBridge = window.WidgetBridge;
                console.log('[WidgetBridge] Initialized for widget: ' + WidgetBridge.getWidgetId());
            })();
        """.trimIndent()
    }
}
