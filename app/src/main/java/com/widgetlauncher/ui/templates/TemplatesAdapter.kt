package com.widgetlauncher.ui.templates

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.widgetlauncher.databinding.ItemTemplateBinding

class TemplatesAdapter(
    private val templates: List<WidgetTemplateItem>,
    private val onClick: (WidgetTemplateItem) -> Unit
) : RecyclerView.Adapter<TemplatesAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(template: WidgetTemplateItem) {
            binding.tvName.text = template.name
            binding.tvDesc.text = template.description
            binding.tvFramework.text = template.framework.uppercase()

            try {
                val color = Color.parseColor(template.previewColor)
                binding.viewColor.background.mutate().setTint(color)
                binding.btnUse.background.mutate().setTint(
                    Color.argb(40, Color.red(color), Color.green(color), Color.blue(color))
                )
                binding.tvFramework.setTextColor(color)
            } catch (e: Exception) {
                // use default color
            }

            binding.root.setOnClickListener { onClick(template) }
            binding.btnUse.setOnClickListener { onClick(template) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount() = templates.size
}
