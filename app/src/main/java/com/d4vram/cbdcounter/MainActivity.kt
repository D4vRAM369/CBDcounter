package com.d4vram.cbdcounter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    // Views principales
    private lateinit var counterText: TextView
    private lateinit var dateText: TextView
    private lateinit var emojiText: TextView
    private lateinit var addButton: Button
    private lateinit var subtractButton: Button
    private lateinit var resetButton: Button

    // Views del historial mejorado
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: ImprovedHistoryAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var statsContainer: View
    private lateinit var avgText: TextView
    private lateinit var totalText: TextView
    private lateinit var streakText: TextView

    // Data
    private lateinit var sharedPrefs: SharedPreferences
    private var currentCount = 0
    private val allHistoryData = ArrayList<HistoryItem>()
    private val displayedHistoryData = ArrayList<HistoryItem>()
    private var currentViewMode = ViewMode.WEEK

    enum class ViewMode {
        WEEK, MONTH, ALL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSharedPreferences()
        loadTodayData()
        loadAllHistoryData()
        setupTabLayout()
        setupClickListeners()
        updateDisplay()
        updateHistoryView()
        updateStats()
    }

    private fun initViews() {
        // Views principales
        counterText = findViewById(R.id.counterText)
        dateText = findViewById(R.id.dateText)
        emojiText = findViewById(R.id.emojiText)
        addButton = findViewById(R.id.addButton)
        subtractButton = findViewById(R.id.subtractButton)
        resetButton = findViewById(R.id.resetButton)

        // Views del historial
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        statsContainer = findViewById(R.id.statsContainer)
        avgText = findViewById(R.id.avgText)
        totalText = findViewById(R.id.totalText)
        streakText = findViewById(R.id.streakText)

        // Configurar RecyclerView con más espacio
        historyAdapter = ImprovedHistoryAdapter(displayedHistoryData)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Añadir decoración para espaciado
        historyRecyclerView.addItemDecoration(HistoryItemDecoration(16))
    }

    private fun initSharedPreferences() {
        sharedPrefs = getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)
    }

    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Semana"))
        tabLayout.addTab(tabLayout.newTab().setText("Mes"))
        tabLayout.addTab(tabLayout.newTab().setText("Todo"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentViewMode = when (tab?.position) {
                    0 -> ViewMode.WEEK
                    1 -> ViewMode.MONTH
                    2 -> ViewMode.ALL
                    else -> ViewMode.WEEK
                }
                updateHistoryView()
                updateStats()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun getCurrentDateKey(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getCurrentDateDisplay(): String {
        val formatter = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        return formatter.format(Date()).capitalize()
    }

    private fun loadTodayData() {
        val today = getCurrentDateKey()
        currentCount = sharedPrefs.getInt("count_$today", 0)
    }

    private fun loadAllHistoryData() {
        allHistoryData.clear()
        val allEntries = sharedPrefs.all
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        allEntries.forEach { (key, value) ->
            if (key.startsWith("count_") && value is Int) {
                val dateString = key.removePrefix("count_")
                try {
                    val date = dateFormat.parse(dateString)
                    if (date != null) {
                        allHistoryData.add(HistoryItem(dateString, value, date))
                    }
                } catch (e: Exception) {
                    // Ignorar fechas mal formateadas
                }
            }
        }

        // Ordenar por fecha descendente (más reciente primero)
        allHistoryData.sortByDescending { it.dateObject }
    }

    private fun updateHistoryView() {
        displayedHistoryData.clear()
        val calendar = Calendar.getInstance()
        val today = calendar.time

        when (currentViewMode) {
            ViewMode.WEEK -> {
                // Mostrar últimos 7 días
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.time
                displayedHistoryData.addAll(
                    allHistoryData.filter { it.dateObject >= weekAgo }
                )
            }
            ViewMode.MONTH -> {
                // Mostrar últimos 30 días
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val monthAgo = calendar.time
                displayedHistoryData.addAll(
                    allHistoryData.filter { it.dateObject >= monthAgo }
                )
            }
            ViewMode.ALL -> {
                // Mostrar todo el historial agrupado por meses
                displayedHistoryData.addAll(allHistoryData)
            }
        }

        historyAdapter.refresh()

        // Scroll suave al principio
        if (displayedHistoryData.isNotEmpty()) {
            historyRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun updateStats() {
        if (displayedHistoryData.isEmpty()) {
            avgText.text = "Promedio: 0"
            totalText.text = "Total: 0"
            streakText.text = "Racha: 0 días"
            return
        }

        // Calcular promedio
        val average = displayedHistoryData.map { it.count }.average()
        avgText.text = "Promedio: %.1f".format(average)

        // Calcular total
        val total = displayedHistoryData.sumOf { it.count }
        totalText.text = "Total: $total"

        // Calcular racha de días sin consumo (count = 0)
        val streak = calculateCleanStreak()
        streakText.text = "Racha limpia: $streak días"
    }

    private fun calculateCleanStreak(): Int {
        var streak = 0
        val sortedData = allHistoryData.sortedByDescending { it.dateObject }

        for (item in sortedData) {
            if (item.count == 0) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun saveData() {
        val today = getCurrentDateKey()
        sharedPrefs.edit()
            .putInt("count_$today", currentCount)
            .apply()

        loadAllHistoryData()
        updateHistoryView()
        updateStats()

        // Sincronizar widget
        CBDWidgetProvider.updateAllWidgets(this)
    }

    private fun updateDisplay() {
        counterText.text = currentCount.toString()
        dateText.text = getCurrentDateDisplay()

        // Actualizar emoji con animación
        val newEmoji = getEmoji(currentCount)
        if (emojiText.text != newEmoji) {
            emojiText.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    emojiText.text = newEmoji
                    emojiText.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } else {
            emojiText.text = newEmoji
        }

        // Cambiar color del contador según el nivel
        val color = when {
            currentCount == 0 -> R.color.green_safe
            currentCount <= 3 -> R.color.yellow_warning
            currentCount <= 6 -> R.color.orange_danger
            else -> R.color.red_critical
        }
        counterText.setTextColor(ContextCompat.getColor(this, color))
    }

    private fun getEmoji(count: Int): String {
        return when {
            count == 0 -> "😌"
            count <= 2 -> "🙂"
            count <= 3 -> "😄"
            count <= 5 -> "🤔"
            count <= 6 -> "🙄"
            count <= 7 -> "😶‍🌫️"
            count <= 8 -> "🫡"
            count <= 9 -> "🫥"
            count <= 11 -> "⛔️"
            else -> "💀"
        }
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener {
            currentCount++
            updateDisplay()
            saveData()
            animateCounter(1.1f)
            showFeedback("CBD agregado", false)
        }

        subtractButton.setOnClickListener {
            if (currentCount > 0) {
                currentCount--
                updateDisplay()
                saveData()
                animateCounter(0.9f)
                showFeedback("CBD restado", true)
            }
        }

        resetButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reiniciar contador")
                .setMessage("¿Estás seguro de que quieres reiniciar el contador de hoy?")
                .setPositiveButton("Sí") { _, _ ->
                    currentCount = 0
                    updateDisplay()
                    saveData()
                    showFeedback("¡Día reiniciado! 💪", true)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun showFeedback(message: String, isPositive: Boolean) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun animateCounter(scale: Float) {
        counterText.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(100)
            .withEndAction {
                counterText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}

// Data class mejorada con objeto Date
data class HistoryItem(
    val date: String,
    val count: Int,
    val dateObject: Date
)

// Adapter mejorado con agrupación visual
class ImprovedHistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            val monthYear = dateFormat.format(item.dateObject).capitalize()
            if (monthYear != lastMonth) {
                groupedData.add(monthYear)
                lastMonth = monthYear
            }
            groupedData.add(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (groupedData[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.historyDate)
        val countText: TextView = itemView.findViewById(R.id.historyCount)
        val emojiText: TextView = itemView.findViewById(R.id.historyEmoji)
        val progressBar: View = itemView.findViewById(R.id.progressBar)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_item, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.headerText.text = groupedData[position] as String
            }

            is ItemViewHolder -> {
                val item = groupedData[position] as HistoryItem
                val dayFormat = SimpleDateFormat("EEEE dd", Locale("es", "ES"))
                holder.dateText.text = dayFormat.format(item.dateObject).capitalize()
                holder.countText.text = "${item.count} CBD"

                // Emoji según el conteo
                holder.emojiText.text = when {
                    item.count == 0 -> "😌"
                    item.count <= 2 -> "🙂"
                    item.count <= 3 -> "😄"
                    item.count <= 5 -> "🤔"
                    item.count <= 6 -> "🙄"
                    item.count <= 7 -> "😶‍🌫️"
                    item.count <= 8 -> "🫡"
                    item.count <= 9 -> "🫥"
                    item.count <= 11 -> "⛔️"
                    else -> "💀"
                }

                // Barra de progreso visual
                val maxWidth = holder.itemView.width
                val progress = minOf(item.count / 10f, 1f)
                val layoutParams = holder.progressBar.layoutParams
                layoutParams.width = (maxWidth * progress).toInt()
                holder.progressBar.layoutParams = layoutParams

                // Color de la barra según el nivel
                val color = when {
                    item.count == 0 -> R.color.green_safe
                    item.count <= 3 -> R.color.yellow_warning
                    item.count <= 6 -> R.color.orange_danger
                    else -> R.color.red_critical
                }
                holder.progressBar.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, color)
                )
            }
        }
    }

    override fun getItemCount(): Int = groupedData.size

    fun refresh() {
        groupData()
        notifyDataSetChanged()
    }
}

    // Decoración para espaciado entre items
class HistoryItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = spacing
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing
            }
        }
    }