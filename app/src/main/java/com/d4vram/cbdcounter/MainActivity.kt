package com.d4vram.cbdcounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d4vram.cbdcounter.model.HistoryItem
import com.d4vram.cbdcounter.util.ViewMode
import com.d4vram.cbdcounter.view.NoteBottomSheet
import com.d4vram.cbdcounter.view.adapter.ImprovedHistoryAdapter
import com.d4vram.cbdcounter.widget.CBDWidgetProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.content.edit
import com.d4vram.cbdcounter.view.adapter.HistoryItemDecoration

class MainActivity : AppCompatActivity(), NoteBottomSheet.Listener {

    // Views principales
    private lateinit var counterText: TextView
    private lateinit var dateText: TextView
    private lateinit var emojiText: TextView
    private lateinit var addButton: Button
    private lateinit var subtractButton: Button
    private lateinit var resetButton: Button
    private lateinit var exportButton: ImageButton
    private lateinit var importButton: ImageButton

    // Botón switch para cambiar el tema
    private lateinit var themeSwitch: SwitchMaterial

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

    private val importCsvLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { handleImportCsv(it) }
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
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)
        themeSwitch = findViewById(R.id.themeSwitch)

        // Estado inicial del switch según el tema actual
        val isNight = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        themeSwitch.isChecked = isNight == Configuration.UI_MODE_NIGHT_YES

        // Listener: alternar modo oscuro
        themeSwitch.setOnCheckedChangeListener { _, checked ->
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Historial
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        statsContainer = findViewById(R.id.statsContainer)
        avgText = findViewById(R.id.avgText)
        totalText = findViewById(R.id.totalText)
        streakText = findViewById(R.id.streakText)

        // Adapter con callback para abrir el modal de notas
        historyAdapter = ImprovedHistoryAdapter(displayedHistoryData) { date ->
            NoteBottomSheet.new(date).show(supportFragmentManager, "note_sheet")
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Espaciado entre ítems
        historyRecyclerView.addItemDecoration(HistoryItemDecoration(16))
    } // <-- CIERRE de initViews() AQUÍ

    // Listener del BottomSheet: refresca el listado cuando cambia una nota
    override fun onNoteChanged(date: String) {
        historyAdapter.refresh()
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
        return formatter.format(Date()).replaceFirstChar { it.uppercase() }
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
                } catch (_: Exception) {
                }
            }
        }
        allHistoryData.sortByDescending { it.dateObject }
    }

    private fun updateHistoryView() {
        displayedHistoryData.clear()
        val calendar = Calendar.getInstance()

        when (currentViewMode) {
            ViewMode.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.time
                displayedHistoryData.addAll(allHistoryData.filter { it.dateObject >= weekAgo })
            }

            ViewMode.MONTH -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val monthAgo = calendar.time
                displayedHistoryData.addAll(allHistoryData.filter { it.dateObject >= monthAgo })
            }

            ViewMode.ALL -> {
                displayedHistoryData.addAll(allHistoryData)
            }
        }
        historyAdapter.refresh()
        if (displayedHistoryData.isNotEmpty()) {
            historyRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun updateStats() {
        if (displayedHistoryData.isEmpty()) {
            avgText.text = getString(R.string.promedio_0)
            totalText.text = getString(R.string.total_0)
            streakText.text = getString(R.string.racha_0)
            return
        }
        val average = displayedHistoryData.map { it.count }.average()
        avgText.text = "Promedio: %.1f".format(average)
        val total = displayedHistoryData.sumOf { it.count }
        totalText.text = "Total: $total"
        val streak = calculateCleanStreak()
        streakText.text = "Racha limpia: $streak días"
    }

    private fun calculateCleanStreak(): Int {
        var streak = 0
        val sortedData = allHistoryData.sortedByDescending { it.dateObject }
        for (item in sortedData) {
            if (item.count == 0) streak++ else break
        }
        return streak
    }

    private fun saveData() {
        val today = getCurrentDateKey()
        sharedPrefs.edit { putInt("count_$today", currentCount) }
        loadAllHistoryData()
        updateHistoryView()
        updateStats()
        CBDWidgetProvider.updateAllWidgets(this)
    }

    private fun updateDisplay() {
        counterText.text = currentCount.toString()
        dateText.text = getCurrentDateDisplay()
        val newEmoji = getEmoji(currentCount)
        if (emojiText.text != newEmoji) {
            emojiText.animate().alpha(0f).setDuration(150).withEndAction {
                emojiText.text = newEmoji
                emojiText.animate().alpha(1f).setDuration(150).start()
            }.start()
        } else emojiText.text = newEmoji

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
            count <= 4 -> "😄"
            count <= 5 -> "🫠"
            count <= 6 -> "🤔"
            count <= 7 -> "🙄"
            count <= 8 -> "😶‍🌫️"
            count <= 9 -> "🫡"
            count <= 10 -> "🫥"
            count <= 11 -> "⛔️"
            else -> "💀"
        }
    }

    private fun setupClickListeners() {
        addButton.setOnClickListener {
            currentCount++
            updateDisplay()
            appendTimestampToTodayNote()
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
            AlertDialog.Builder(this).setTitle(getString(R.string.confirm_reset_title))
                .setMessage(getString(R.string.confirm_reset_message))
                .setPositiveButton("Sí") { _, _ ->
                    currentCount = 0
                    updateDisplay()
                    saveData()
                    showFeedback("¡Día reiniciado! 💪", true)
                }.setNegativeButton("Cancelar", null).show()
        }

        exportButton.setOnClickListener { exportCsv() }
        importButton.setOnClickListener {
            importCsvLauncher.launch(arrayOf("text/csv", "text/plain"))
        }

    }

    private fun showFeedback(message: String, isPositive: Boolean) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun animateCounter(scale: Float) {
        counterText.animate().scaleX(scale).scaleY(scale).setDuration(100).withEndAction {
            counterText.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    private fun exportCsv() {
        loadAllHistoryData()
        val csvContent = buildCsvContent()
        if (csvContent.isBlank()) {
            showFeedback("No hay datos para exportar", true)
            return
        }

        val exportDir = File(cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val fileName = "cbd_counter_" + SimpleDateFormat(
            "yyyyMMdd_HHmm", Locale.getDefault()
        ).format(Date()) + ".csv"
        val file = File(exportDir, fileName)

        val uriResult = runCatching {
            file.writeText(csvContent, Charsets.UTF_8)
            val authority = "$packageName.fileprovider"
            FileProvider.getUriForFile(this, authority, file)
        }

        uriResult.onSuccess { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
            showFeedback("CSV exportado", false)
        }.onFailure {
            showFeedback("Error al exportar CSV", true)
        }
    }

    private fun buildCsvContent(): String {
        val prefsMap = sharedPrefs.all
        if (prefsMap.isEmpty()) return ""

        val dates = mutableSetOf<String>()
        prefsMap.keys.forEach { key ->
            when {
                key.startsWith("count_") -> dates.add(key.removePrefix("count_"))
                key.startsWith("NOTE_") -> dates.add(key.removePrefix("NOTE_"))
            }
        }
        if (dates.isEmpty()) return ""

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sortedDates = dates.mapNotNull { dateString ->
            runCatching { dateFormat.parse(dateString) }.getOrNull()?.let { parsed ->
                dateString to parsed
            }
        }.sortedBy { it.second }

        val builder = StringBuilder("date,count,note\n")
        sortedDates.forEach { (dateString, _) ->
            val count = sharedPrefs.getInt("count_$dateString", 0)
            val note = Prefs.getNote(this, dateString) ?: ""
            builder.append(dateString).append(',').append(count).append(',')
                .append(escapeCsvField(note)).append('\n')
        }
        return builder.toString()
    }

    private fun handleImportCsv(uri: Uri) {
        val result = runCatching {
            contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    val lines = reader.readLines()
                    if (lines.isEmpty()) throw IllegalArgumentException("Archivo vacío")

                    sharedPrefs.edit {
                        sharedPrefs.all.keys.filter {
                            it.startsWith("count_") || it.startsWith("NOTE_")
                        }.forEach { key -> remove(key) }

                        lines.drop(1).forEach { line ->
                            if (line.isBlank()) return@forEach
                            val columns = splitCsvLine(line)
                            if (columns.size < 2) return@forEach

                            val date = columns[0]
                            val count = columns[1].toIntOrNull() ?: return@forEach
                            putInt("count_$date", count)

                            val rawNote = if (columns.size >= 3) columns[2] else ""
                            val note = unescapeCsvField(rawNote)
                            if (note.isNotEmpty()) {
                                putString("NOTE_$date", note)
                            }
                        }
                    }
                }
            } ?: throw IllegalArgumentException("No se pudo abrir el archivo")
        }

        result.onSuccess {
            loadTodayData()
            loadAllHistoryData()
            updateDisplay()
            updateHistoryView()
            updateStats()
            showFeedback("Importación completada", false)
        }.onFailure {
            showFeedback("Error al importar CSV", true)
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var escape = false
        line.forEach { char ->
            when {
                escape -> {
                    current.append(char)
                    escape = false
                }

                char == '\\' -> {
                    current.append(char)
                    escape = true
                }

                char == ',' -> {
                    fields.add(current.toString())
                    current.setLength(0)
                }

                else -> current.append(char)
            }
        }
        fields.add(current.toString())
        return fields
    }

    private fun escapeCsvField(value: String): String {
        if (value.isEmpty()) return ""
        val builder = StringBuilder()
        value.forEach { char ->
            when (char) {
                '\\' -> builder.append("\\\\")
                '\n' -> builder.append("\\n")
                ',' -> builder.append("\\,")
                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    private fun unescapeCsvField(value: String): String {
        if (value.isEmpty()) return ""
        val builder = StringBuilder()
        var escape = false
        value.forEach { char ->
            if (escape) {
                builder.append(
                    when (char) {
                        'n' -> '\n'
                        '\\' -> '\\'
                        ',' -> ','
                        else -> char
                    }
                )
                escape = false
            } else if (char == '\\') {
                escape = true
            } else {
                builder.append(char)
            }
        }
        if (escape) builder.append('\\')
        return builder.toString()
    }

    private fun appendTimestampToTodayNote() {
        val today = getCurrentDateKey()
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val currentNote = Prefs.getNote(this, today)
        val entry = "🔸 $timestamp"

        val updatedNote = if (currentNote.isNullOrBlank()) {
            entry
        } else {
            buildString {
                append(currentNote)
                if (!currentNote.endsWith("\n")) append("\n")
                append(entry)
            }
        }

        Prefs.setNote(this, today, updatedNote)
    }
}

