package com.widgetlauncher.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.widgetlauncher.data.Widget
import com.widgetlauncher.data.colSpan

class WidgetAdapter(
    private val context: Context,
    private val cellSizePx: Int,
    private val gapPx: Int,
    private val onLongPress: (Widget) -> Unit
) : RecyclerView.Adapter<WidgetViewHolder>() {

    private var widgets: List<Widget> = emptyList()

    // Pool of WebViews for reuse (max visible at once)
    private val webViewPool = mutableListOf<WebView>()
    private val activeWebViews = mutableMapOf<String, WebView>()

    fun submitList(newWidgets: List<Widget>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = widgets.size
            override fun getNewListSize() = newWidgets.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                widgets[oldPos].id == newWidgets[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                widgets[oldPos] == newWidgets[newPos]
        })
        widgets = newWidgets
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getOrCreateWebView(widgetId: String): WebView {
        return activeWebViews.getOrPut(widgetId) {
            webViewPool.removeFirstOrNull() ?: WebView(context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return WidgetViewHolder(container, WebView(context))
    }

    override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
        val widget = widgets[position]
        val colSpan = widget.size.colSpan
        val width = cellSizePx * colSpan + gapPx * (colSpan - 1)
        val height = cellSizePx

        val container = holder.itemView as FrameLayout
        container.layoutParams = ViewGroup.LayoutParams(width, height)

        val wv = getOrCreateWebView(widget.id)
        wv.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        if (wv.parent != null) {
            (wv.parent as? ViewGroup)?.removeView(wv)
        }
        container.removeAllViews()
        container.addView(wv)

        holder.bind(widget, context)

        container.setOnLongClickListener {
            onLongPress(widget)
            true
        }
    }

    override fun onRecycledViewHolder(holder: WidgetViewHolder, isReleaseCallback: Boolean) {
        super.onDetachedFromRecyclerView(
            holder.itemView.parent as? RecyclerView ?: return
        )
    }

    override fun getItemCount() = widgets.size

    fun cleanup() {
        activeWebViews.values.forEach { it.destroy() }
        activeWebViews.clear()
        webViewPool.forEach { it.destroy() }
        webViewPool.clear()
    }
}
