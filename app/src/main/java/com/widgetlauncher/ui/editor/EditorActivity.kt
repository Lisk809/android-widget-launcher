package com.widgetlauncher.ui.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.widgetlauncher.R
import com.widgetlauncher.bridge.AndroidBridge
import com.widgetlauncher.data.*
import com.widgetlauncher.databinding.ActivityEditorBinding
import java.util.UUID

class EditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WIDGET_ID = "widget_id"
        const val EXTRA_TEMPLATE_CODE = "template_code"
        const val EXTRA_TEMPLATE_NAME = "template_name"
        const val EXTRA_TEMPLATE_FRAMEWORK = "template_framework"
    }

    private lateinit var binding: ActivityEditorBinding
    private lateinit var repository: com.widgetlauncher.data.WidgetRepository
    private var existingWidget: Widget? = null
    private var currentFramework = WidgetFramework.VANILLA
    private var currentSize = WidgetSize.MEDIUM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        repository = com.widgetlauncher.data.WidgetRepository.getInstance(this)

        val widgetId = intent.getStringExtra(EXTRA_WIDGET_ID)
        if (widgetId != null) {
            existingWidget = repository.getWidget(widgetId)
        }

        setupUI()
        setupPreview()
    }

    private fun setupUI() {
        val widget = existingWidget

        binding.etName.setText(widget?.name ?: intent.getStringExtra(EXTRA_TEMPLATE_NAME) ?: "My Widget")
        binding.etCode.setText(widget?.code ?: intent.getStringExtra(EXTRA_TEMPLATE_CODE) ?: DEFAULT_HTML)
        currentFramework = widget?.framework ?: WidgetFramework.VANILLA
        currentSize = widget?.size ?: WidgetSize.MEDIUM

        // Framework selector
        binding.rgFramework.setOnCheckedChangeListener { _, checkedId ->
            currentFramework = when (checkedId) {
                R.id.rbVanilla -> WidgetFramework.VANILLA
                R.id.rbVue -> WidgetFramework.VUE
                R.id.rbReact -> WidgetFramework.REACT
                else -> WidgetFramework.VANILLA
            }
        }

        // Size selector
        binding.rgSize.setOnCheckedChangeListener { _, checkedId ->
            currentSize = when (checkedId) {
                R.id.rbSmall -> WidgetSize.SMALL
                R.id.rbMedium -> WidgetSize.MEDIUM
                R.id.rbLarge -> WidgetSize.LARGE
                R.id.rbWide -> WidgetSize.WIDE
                else -> WidgetSize.MEDIUM
            }
        }

        // Tab layout: Editor <-> Preview
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        binding.scrollEditor.visibility = android.view.View.VISIBLE
                        binding.webPreview.visibility = android.view.View.GONE
                    }
                    1 -> {
                        binding.scrollEditor.visibility = android.view.View.GONE
                        binding.webPreview.visibility = android.view.View.VISIBLE
                        refreshPreview()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Refresh preview button
        binding.btnRefresh.setOnClickListener { refreshPreview() }

        // Save button
        binding.btnSave.setOnClickListener { saveWidget() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupPreview() {
        binding.webPreview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        binding.webPreview.webChromeClient = WebChromeClient()
    }

    private fun refreshPreview() {
        val code = binding.etCode.text?.toString() ?: return
        val widgetId = existingWidget?.id ?: "preview"
        val bridge = AndroidBridge(this, widgetId)
        binding.webPreview.addJavascriptInterface(bridge, "AndroidBridge")

        val bridgeScript = "<script>${AndroidBridge.getInjectionScript(widgetId)}</script>"
        val fullHtml = if (code.contains("<head>", ignoreCase = true)) {
            code.replace("<head>", "<head>\n$bridgeScript", ignoreCase = true)
        } else {
            "$bridgeScript\n$code"
        }

        binding.webPreview.loadDataWithBaseURL(
            "http://localhost", fullHtml, "text/html", "UTF-8", null
        )
    }

    private fun saveWidget() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val code = binding.etCode.text?.toString() ?: ""

        if (name.isEmpty()) {
            binding.etName.error = "Name required"
            return
        }

        val widget = existingWidget?.copy(
            name = name,
            code = code,
            framework = currentFramework,
            size = currentSize,
            updatedAt = System.currentTimeMillis()
        ) ?: Widget(
            id = UUID.randomUUID().toString(),
            name = name,
            description = "",
            code = code,
            framework = currentFramework,
            size = currentSize,
            color = "#6366f1"
        )

        repository.saveWidget(widget)
        Toast.makeText(this, "Widget saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private val DEFAULT_HTML = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  background: linear-gradient(135deg, #1e1b4b, #312e81);
  display: flex; align-items: center; justify-content: center;
  height: 100vh; font-family: -apple-system, sans-serif; color: white;
}
h1 { font-size: 24px; font-weight: 300; }
</style>
</head>
<body>
<h1>Hello Widget!</h1>
</body>
</html>
        """.trimIndent()
    }
}
