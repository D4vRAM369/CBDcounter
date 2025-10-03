package com.d4vram.cbdcounter.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.d4vram.cbdcounter.Prefs
import com.d4vram.cbdcounter.R
import com.d4vram.cbdcounter.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class ImprovedHistoryAdapter(
    private val historyList: List<HistoryItem>,
    private val onDayClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }

    private val groupedData = mutableListOf<Any>()

    init {
        groupData()
    }

    private fun groupData() {
        groupedData.clear()
        if (historyList.isEmpty()) return
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        var lastMonth = ""
        historyList.forEach { item ->
            val monthYear = dateFormat.format(item.dateObject)
                .replaceFirstChar { it.uppercase() }
            if (monthYear != lastMonth) {
                groupedData.add(monthYear)
                lastMonth = monthYear
            }
            groupedData.add(item)
        }
    }

    override fun getItemViewType(position: Int) =
        if (groupedData[position] is String) TYPE_HEADER else TYPE_ITEM

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.historyDate)
        val countText: TextView = itemView.findViewById(R.id.historyCount)
        val emojiText: TextView = itemView.findViewById(R.id.historyEmoji)
        val progressBar: View = itemView.findViewById(R.id.progressBar)
        val noteBadge: TextView? =
            itemView.findViewById(R.id.noteBadge) // puede no existir si no lo añadiste
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_header, parent, false)
            )
        } else {
            ItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item, parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.headerText.text = groupedData[position] as String
            is ItemViewHolder -> {
                val item = groupedData[position] as HistoryItem
                val dayFormat = SimpleDateFormat("EEEE dd", Locale("es", "ES"))
                holder.dateText.text = dayFormat.format(item.dateObject)
                    .replaceFirstChar { it.uppercase() }
                holder.countText.text = "${item.count} CBD"

                holder.emojiText.text = when {
                    item.count == 0 -> "😌"
                    item.count <= 2 -> "🙂"
                    item.count <= 4 -> "😄"
                    item.count <= 5 -> "🫠"
                    item.count <= 6 -> "🤔"
                    item.count <= 7 -> "🙄"
                    item.count <= 8 -> "😶‍🌫️"
                    item.count <= 9 -> "🫡"
                    item.count <= 10 -> "🫥"
                    item.count <= 11 -> "⛔️"
                    else -> "💀"
                }

                // Barra de progreso (como ya tenías)
                val maxWidth = holder.itemView.width
                val progress = minOf(item.count / 10f, 1f)
                val layoutParams = holder.progressBar.layoutParams
                layoutParams.width = (maxWidth * progress).toInt()
                holder.progressBar.layoutParams = layoutParams
                val color = when {
                    item.count == 0 -> R.color.green_safe
                    item.count <= 3 -> R.color.yellow_warning
                    item.count <= 6 -> R.color.orange_danger
                    else -> R.color.red_critical
                }
                holder.progressBar.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, color)
                )

                // --- NUEVO: badge de nota visible si existe nota para ese día
                val ctx = holder.itemView.context
                holder.noteBadge?.visibility =
                    if (Prefs.hasNote(ctx, item.date)) View.VISIBLE else View.GONE

                // --- NUEVO: clicks para abrir el modal
                holder.itemView.setOnClickListener { onDayClick(item.date) }
                holder.noteBadge?.setOnClickListener { onDayClick(item.date) }
            }
        }
    }

    override fun getItemCount() = groupedData.size

    fun refresh() {
        groupData()
        notifyDataSetChanged()
    }
}