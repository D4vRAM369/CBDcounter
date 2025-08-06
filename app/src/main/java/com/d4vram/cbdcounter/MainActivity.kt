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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var counterText: TextView
    private lateinit var dateText: TextView
    private lateinit var emojiText: TextView
    private lateinit var addButton: Button
    private lateinit var subtractButton: Button
    private lateinit var resetButton: Button
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    // Data
    private lateinit var sharedPrefs: SharedPreferences
    private var currentCount = 0
    private val historyList = ArrayList<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSharedPreferences()
        loadTodayData()
        loadHistoryData()
        setupClickListeners()
        updateDisplay()
    }

    private fun initViews() {
        counterText = findViewById(R.id.counterText)
        dateText = findViewById(R.id.dateText)
        emojiText = findViewById(R.id.emojiText)
        addButton = findViewById(R.id.addButton)
        subtractButton = findViewById(R.id.subtractButton)
        resetButton = findViewById(R.id.resetButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        historyAdapter = HistoryAdapter(historyList)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun initSharedPreferences() {
        sharedPrefs = getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)
    }

    private fun getCurrentDateKey(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getCurrentDateDisplay(): String {
        val formatter = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        return formatter.format(Date())
    }

    private fun loadTodayData() {
        val today = getCurrentDateKey()
        currentCount = sharedPrefs.getInt("count_$today", 0)
    }

    private fun loadHistoryData() {
        historyList.clear()
        val allEntries = sharedPrefs.all

        allEntries.forEach { (key, value) ->
            if (key.startsWith("count_") && value is Int) {
                val date = key.removePrefix("count_")
                historyList.add(HistoryItem(date, value))
            }
        }

        // Ordenar por fecha descendente y tomar solo los últimos 7
        historyList.sortByDescending {
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.date)
            } catch (e: Exception) {
                Date(0)
            }
        }

        if (historyList.size > 7) {
            historyList.subList(7, historyList.size).clear()
        }

        historyAdapter.notifyDataSetChanged()
    }

    private fun saveData() {
        val today = getCurrentDateKey()
        sharedPrefs.edit()
            .putInt("count_$today", currentCount)
            .apply()

        loadHistoryData() // Actualizar historial

        // SINCRONIZAR WIDDGET DESPUÉS DE CADA CAMBIO
        CBDWidgetProvider.updateAllWidgets(this)
    }

    private fun updateDisplay() {
        counterText.text = currentCount.toString()
        dateText.text = "Hoy: ${getCurrentDateDisplay()}"

        // Actualizar emoji según el conteo
        emojiText.text = when {
            currentCount == 0 -> "😌"
            currentCount <= 2 -> "🙂"
            currentCount <= 4 -> "😊"
            currentCount <= 6 -> "😁"
            currentCount <= 8 -> "🙄"
            currentCount <= 10 -> "😵‍"
            currentCount <= 12 -> "🥴"
            currentCount <= 15 -> "😵"
            else -> "🛸"
        }
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener {
            currentCount++
            updateDisplay()
            saveData()
            animateCounter(1.1f)
            Toast.makeText(this, "CBD agregado", Toast.LENGTH_SHORT).show()
        }

        subtractButton.setOnClickListener {
            if (currentCount > 0) {
                currentCount--
                updateDisplay()
                saveData()
                animateCounter(0.9f)
                Toast.makeText(this, "CBD restado", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Contador reiniciado", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
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

// Data class para el historial
data class HistoryItem(
    val date: String,
    val count: Int
)

// Adapter para el RecyclerView
class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.historyDate)
        val countText: TextView = itemView.findViewById(R.id.historyCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.dateText.text = item.date
        holder.countText.text = "${item.count} CBD"
    }

    override fun getItemCount(): Int = historyList.size
}