package com.widgetlauncher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.widgetlauncher.data.WidgetRepository
import fi.iki.elonen.NanoHTTPD

/**
 * Optional local HTTP server that serves widget HTML files over localhost.
 * This avoids file:// protocol restrictions and enables proper cross-origin behavior.
 *
 * Usage: Start this service from an Activity, then use
 *   http://localhost:8080/widget/<widgetId>
 * as the WebView URL instead of loadDataWithBaseURL.
 */
class LocalHttpServerService : Service() {

    private var server: WidgetServer? = null

    override fun onCreate() {
        super.onCreate()
        server = WidgetServer(8080, WidgetRepository.getInstance(applicationContext))
        server?.start()
    }

    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    inner class WidgetServer(port: Int, private val repo: WidgetRepository) : NanoHTTPD(port) {

        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri ?: return notFound()

            return when {
                uri.startsWith("/widget/") -> {
                    val id = uri.removePrefix("/widget/")
                    val widget = repo.getWidget(id) ?: return notFound()
                    newFixedLengthResponse(Response.Status.OK, "text/html", widget.code)
                }
                uri == "/health" -> {
                    newFixedLengthResponse(Response.Status.OK, "application/json", """{"status":"ok"}""")
                }
                else -> notFound()
            }
        }

        private fun notFound(): Response =
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found")
    }
}
