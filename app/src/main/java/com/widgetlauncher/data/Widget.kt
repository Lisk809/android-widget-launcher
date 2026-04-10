package com.widgetlauncher.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class WidgetSize { SMALL, MEDIUM, LARGE, WIDE }
enum class WidgetFramework { VANILLA, VUE, REACT }

data class Widget(
    val id: String,
    var name: String,
    var description: String,
    var code: String,
    var framework: WidgetFramework = WidgetFramework.VANILLA,
    var size: WidgetSize = WidgetSize.MEDIUM,
    var color: String = "#6366f1",
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)

// Grid span helpers
val WidgetSize.colSpan: Int get() = when (this) {
    WidgetSize.SMALL -> 1
    WidgetSize.MEDIUM -> 2
    WidgetSize.LARGE -> 2
    WidgetSize.WIDE -> 4
}

val WidgetSize.rowSpan: Int get() = when (this) {
    WidgetSize.SMALL -> 1
    WidgetSize.MEDIUM -> 1
    WidgetSize.LARGE -> 2
    WidgetSize.WIDE -> 1
}

val WidgetSize.label: String get() = when (this) {
    WidgetSize.SMALL -> "1×1"
    WidgetSize.MEDIUM -> "2×1"
    WidgetSize.LARGE -> "2×2"
    WidgetSize.WIDE -> "4×1"
}
