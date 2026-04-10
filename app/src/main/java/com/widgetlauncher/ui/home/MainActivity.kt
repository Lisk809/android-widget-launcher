package com.widgetlauncher.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.FlexboxLayoutManager
import com.widgetlauncher.R
import com.widgetlauncher.data.Widget
import com.widgetlauncher.data.WidgetRepository
import com.widgetlauncher.databinding.ActivityMainBinding
import com.widgetlauncher.ui.editor.EditorActivity
import com.widgetlauncher.ui.templates.TemplatesActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: WidgetRepository
    private lateinit var adapter: WidgetAdapter

    private val cellSizeDp = 80 // Each grid cell is 80dp
    private val gapDp = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        repository = WidgetRepository.getInstance(this)

        val cellSizePx = dpToPx(cellSizeDp)
        val gapPx = dpToPx(gapDp)

        adapter = WidgetAdapter(
            context = this,
            cellSizePx = cellSizePx,
            gapPx = gapPx,
            onLongPress = { widget -> showWidgetOptions(widget) }
        )

        binding.recyclerView.apply {
            layoutManager = FlexboxLayoutManager(this@MainActivity)
            setHasFixedSize(false)
            this.adapter = this@MainActivity.adapter
        }

        binding.fabAdd.setOnClickListener {
            openTemplates()
        }

        loadWidgets()
    }

    override fun onResume() {
        super.onResume()
        loadWidgets()
    }

    private fun loadWidgets() {
        val widgets = repository.getWidgets()
        adapter.submitList(widgets)
        binding.tvEmpty.visibility = if (widgets.isEmpty())
            android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showWidgetOptions(widget: Widget) {
        AlertDialog.Builder(this)
            .setTitle(widget.name)
            .setItems(arrayOf("Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> openEditor(widget.id)
                    1 -> confirmDelete(widget)
                }
            }
            .show()
    }

    private fun confirmDelete(widget: Widget) {
        AlertDialog.Builder(this)
            .setTitle("Delete Widget")
            .setMessage("Remove \"${widget.name}\" from your home screen?")
            .setPositiveButton("Delete") { _, _ ->
                repository.deleteWidget(widget.id)
                loadWidgets()
                Toast.makeText(this, "Widget removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openEditor(widgetId: String? = null) {
        startActivity(Intent(this, EditorActivity::class.java).apply {
            widgetId?.let { putExtra(EditorActivity.EXTRA_WIDGET_ID, it) }
        })
    }

    private fun openTemplates() {
        startActivity(Intent(this, TemplatesActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_templates -> { openTemplates(); true }
            R.id.action_new -> { openEditor(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.cleanup()
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
}
