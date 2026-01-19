package com.d4vram.cbdcounter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
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
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    // Views principales
    private lateinit var counterText: TextView  // Oculto, para compatibilidad
    private lateinit var cbdCountText: TextView
    private lateinit var thcCountText: TextView
    private lateinit var cbdContainer: View
    private lateinit var thcContainer: View
    private lateinit var dateText: TextView
    private lateinit var emojiText: TextView
    private lateinit var addButton: Button
    private lateinit var addInfusedButton: MaterialButton
    private lateinit var statsButton: Chip
    private lateinit var subtractButton: Button
    private lateinit var resetButton: Button
    private lateinit var exportButton: ImageButton
    private lateinit var importButton: ImageButton
    private lateinit var settingsButton: ImageButton

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
    private lateinit var searchButton: ImageButton

    // Data
    private lateinit var sharedPrefs: SharedPreferences
    private var cbdCount = 0
    private var thcCount = 0
    private val currentCount: Int get() = cbdCount + thcCount  // Total para emoji y compatibilidad
    private val allHistoryData = ArrayList<HistoryItem>()
    private val displayedHistoryData = ArrayList<HistoryItem>()
    private var currentViewMode = ViewMode.WEEK

    private val importMimeTypes = arrayOf(
        "text/csv",
        "text/comma-separated-values",
        "application/csv",
        "application/vnd.ms-excel",
        "text/plain"
    )

    // Receptor para detectar cambio de día/hora mientras la app está abierta
    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    // Recargar datos del día actual
                    loadTodayData()
                    updateDisplay()
                    loadAllHistoryData()
                    updateHistoryView()
                    updateStats()
                }
            }
        }
    }

    private val importCsvLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            runCatching {
                uri?.let { handleImportCsv(it) }
            }.onFailure { e ->
                showFeedback("Error inesperado al importar: ${e.message}", true)
            }
        }

    enum class ViewMode { WEEK, MONTH, ALL }
    private enum class InfusionType(
        @StringRes val labelRes: Int,
        @StringRes val feedbackRes: Int,
        val icon: String
    ) {
        WEED(R.string.weed_option, R.string.cbd_infused_added_weed, "🌿"),
        POLEM(R.string.polem_option, R.string.cbd_infused_added_polem, "🍫")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Aplicar tema ANTES de super.onCreate para evitar flickering
        initSharedPreferences()
        Prefs.migrateToV14IfNeeded(this)  // Migrar datos al nuevo formato si es necesario
        applyStoredTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hacer que el contenido se dibuje detrás de la barra de estado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        runCatching {
            // initSharedPreferences() -> Ya llamado arriba
            initViews()
            loadTodayData()
            loadAllHistoryData()
            setupTabLayout()
            setupClickListeners()
            updateDisplay(animate = false)
            updateHistoryView()
            updateStats()
        }.onFailure { e ->
            showFeedback("Error al iniciar la app: ${e.message}", true)
            e.printStackTrace()
        }

        // Mostrar disclaimer médico en el primer uso
        showDisclaimerIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar emoji cuando se vuelve de EmojiSettingsActivity
        loadTodayData()
        updateDisplay(animate = false)
        
        // Registrar receptor para cambios de fecha/hora
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(dateChangeReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar receptor para evitar memory leaks
        try {
            unregisterReceiver(dateChangeReceiver)
        } catch (e: IllegalArgumentException) {
            // Receptor ya desregistrado, ignorar
        }
    }

    private fun initViews() {
        // Views principales
        counterText = findViewById(R.id.counterText)  // Oculto, para compatibilidad
        cbdCountText = findViewById(R.id.cbdCountText)
        thcCountText = findViewById(R.id.thcCountText)
        cbdContainer = findViewById(R.id.cbdContainer)
        thcContainer = findViewById(R.id.thcContainer)
        dateText = findViewById(R.id.dateText)
        emojiText = findViewById(R.id.emojiText)
        addButton = findViewById(R.id.addButton)
        addInfusedButton = findViewById(R.id.addInfusedButton)
        statsButton = findViewById(R.id.statsButton)
        subtractButton = findViewById(R.id.subtractButton)
        resetButton = findViewById(R.id.resetButton)
        exportButton = findViewById(R.id.exportButton)
        importButton = findViewById(R.id.importButton)
        settingsButton = findViewById(R.id.settingsButton)
        themeSwitch = findViewById(R.id.themeSwitch)    

        // Estado inicial del switch según preferencias o sistema
        val isNight = isDarkModeEnabled()
        themeSwitch.setOnCheckedChangeListener(null)
        themeSwitch.isChecked = isNight
        
        themeSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked != isDarkModeEnabled()) {
                setDarkMode(checked)
            }
        }

        // Historial
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        statsContainer = findViewById(R.id.statsContainer)
        avgText = findViewById(R.id.avgText)
        totalText = findViewById(R.id.totalText)
        streakText = findViewById(R.id.streakText)
        searchButton = findViewById(R.id.searchButton)

        // Adapter con callback para abrir el modal de notas
        historyAdapter = ImprovedHistoryAdapter(displayedHistoryData) { date ->
            DayModalFragment.newInstance(date).show(supportFragmentManager, "day_modal")
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Espaciado entre ítems
        historyRecyclerView.addItemDecoration(HistoryItemDecoration(16))



        // Abrir diálogo de búsqueda de notas
        searchButton.setOnClickListener {
            SearchNotesDialog().show(supportFragmentManager, "search_notes")
        }
    } // <-- CIERRE de initViews() AQUÍ




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
        cbdCount = Prefs.getCbdCount(this, today)
        thcCount = Prefs.getThcCount(this, today)
    }

    private fun loadAllHistoryData() {
        allHistoryData.clear()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val allDates = Prefs.getAllDatesWithData(this)
        allDates.forEach { dateString ->
            try {
                val date = dateFormat.parse(dateString)
                if (date != null) {
                    val cbd = Prefs.getCbdCount(this, dateString)
                    val thc = Prefs.getThcCount(this, dateString)
                    allHistoryData.add(HistoryItem(dateString, cbd, thc, date))
                }
            } catch (_: Exception) {}
        }
        allHistoryData.sortByDescending { it.dateObject }
    }

    private fun updateHistoryView() {
        displayedHistoryData.clear()
        val calendar = Calendar.getInstance()

        val filteredByTab = when (currentViewMode) {
            ViewMode.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.time
                allHistoryData.filter { it.dateObject >= weekAgo }
            }
            ViewMode.MONTH -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val monthAgo = calendar.time
                allHistoryData.filter { it.dateObject >= monthAgo }
            }
            ViewMode.ALL -> allHistoryData
        }

        displayedHistoryData.addAll(filteredByTab)

        historyAdapter.refresh()
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
        val average = displayedHistoryData.map { it.totalCount }.average()
        avgText.text = "Promedio: %.1f".format(average)
        val total = displayedHistoryData.sumOf { it.totalCount }
        totalText.text = "Total: $total"
        val streak = calculateCleanStreak()
        streakText.text = "Racha limpia: $streak días"
    }

    private fun calculateCleanStreak(): Int {
        var streak = 0
        val sortedData = allHistoryData.sortedByDescending { it.dateObject }
        for (item in sortedData) {
            if (item.totalCount == 0) streak++ else break
        }
        return streak
    }

    private fun saveData() {
        val today = getCurrentDateKey()
        Prefs.setCbdCount(this, today, cbdCount)
        Prefs.setThcCount(this, today, thcCount)

        loadAllHistoryData()
        updateHistoryView()
        updateStats()
        CBDWidgetProvider.updateAllWidgets(this)
    }

    private fun isDarkModeEnabled(): Boolean {
        // Si no hay preferencia guardada, usamos el estado actual del delegado o el sistema
        return sharedPrefs.getBoolean(Prefs.KEY_DARK_MODE, 
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ||
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        )
    }

    private fun applyStoredTheme() {
        val enabled = isDarkModeEnabled()
        val targetMode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        
        // Solo aplicar si el modo actual es diferente al objetivo para evitar bucles
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }

    private fun setDarkMode(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(Prefs.KEY_DARK_MODE, enabled).apply()
        applyStoredTheme()
    }

    private fun updateDisplay(animate: Boolean = true) {
        // Actualizar contadores duales
        cbdCountText.text = cbdCount.toString()
        thcCountText.text = thcCount.toString()
        counterText.text = currentCount.toString()  // Total oculto para compatibilidad

        dateText.text = getCurrentDateDisplay()
        val newEmoji = getEmoji(currentCount)

        if (animate && emojiText.text != newEmoji && emojiText.text.isNotEmpty()) {
            emojiText.animate().alpha(0f).setDuration(150).withEndAction {
                emojiText.text = newEmoji
                emojiText.animate().alpha(1f).setDuration(150).start()
            }.start()
        } else {
            emojiText.alpha = 1f
            emojiText.text = newEmoji
        }

        // Destacar el modo activo
        val isThc = Prefs.getSubstanceType(this) == "THC"
        highlightActiveCounter(isThc)

        // Actualizar colores según cantidad
        updateCounterColors()
    }

    private fun highlightActiveCounter(isThcActive: Boolean) {
        // El contador activo se ve más grande/destacado
        val activeScale = 1.1f
        val inactiveScale = 0.9f
        val activeAlpha = 1.0f
        val inactiveAlpha = 0.6f

        if (isThcActive) {
            thcContainer.scaleX = activeScale
            thcContainer.scaleY = activeScale
            thcContainer.alpha = activeAlpha
            cbdContainer.scaleX = inactiveScale
            cbdContainer.scaleY = inactiveScale
            cbdContainer.alpha = inactiveAlpha
        } else {
            cbdContainer.scaleX = activeScale
            cbdContainer.scaleY = activeScale
            cbdContainer.alpha = activeAlpha
            thcContainer.scaleX = inactiveScale
            thcContainer.scaleY = inactiveScale
            thcContainer.alpha = inactiveAlpha
        }
    }

    private fun updateCounterColors() {
        // Colores CBD según cantidad
        val cbdColor = when {
            cbdCount == 0 -> R.color.green_safe
            cbdCount <= 4 -> R.color.cbd_text
            cbdCount <= 6 -> R.color.orange_danger
            else -> R.color.red_critical
        }
        cbdCountText.setTextColor(ContextCompat.getColor(this, cbdColor))

        // Colores THC según cantidad
        val thcColor = when {
            thcCount == 0 -> R.color.green_safe
            thcCount <= 4 -> R.color.thc_text
            thcCount <= 6 -> R.color.orange_danger
            else -> R.color.red_critical
        }
        thcCountText.setTextColor(ContextCompat.getColor(this, thcColor))
    }

    private fun getEmoji(count: Int): String = EmojiUtils.emojiForCount(count, this)

    private fun setupClickListeners() {
        addButton.setOnClickListener { registerStandardIntake() }
        addInfusedButton.setOnClickListener { showInfusionDialog() }
        statsButton.setOnClickListener { openStatsCalendar() }
        settingsButton.setOnClickListener {
            // Abrir pantalla de ajustes generales
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        subtractButton.setOnClickListener {
        val isThc = Prefs.getSubstanceType(this) == "THC"
        val activeCount = if (isThc) thcCount else cbdCount

        if (activeCount > 0) {
            // Inflar layout personalizado
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null)

            // Crear el diálogo
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            // Configurar chips
            dialogView.findViewById<View>(R.id.chip_confirm).setOnClickListener {
                if (isThc) thcCount-- else cbdCount--
                updateDisplay()
                removeLastEntryFromTodayNote()  // Borrar último timestamp
                saveData()
                animateCounter(0.9f)
                val msg = if (isThc) getString(R.string.thc_subtracted) else getString(R.string.cbd_subtracted)
                showFeedback(msg, true)
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.chip_keep_note).setOnClickListener {
                if (isThc) thcCount-- else cbdCount--
                updateDisplay()
                // NO borramos la nota, solo restamos el contador
                saveData()
                animateCounter(0.9f)
                showFeedback("Restado -1 (nota mantenida)", true)
                dialog.dismiss()
            }

            dialogView.findViewById<View>(R.id.chip_cancel).setOnClickListener {
                dialog.dismiss()
            }

            // Mostrar con fondo transparente para que se vea bien el card
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }
        resetButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reiniciar contador")
                .setMessage("¿Estás seguro de que quieres reiniciar el contador de hoy? (CBD y THC)")
                .setPositiveButton("Sí") { _, _ ->
                    cbdCount = 0
                    thcCount = 0
                    updateDisplay()
                    saveData()
                    showFeedback("¡Día reiniciado! 💪", true)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        exportButton.setOnClickListener { exportCsv() }
        importButton.setOnClickListener {
            // Mostrar diálogo de confirmación antes de importar
            MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Importar datos")
                .setMessage("Esto BORRARÁ todos tus datos actuales (historial, notas y emojis personalizados) y los reemplazará con los del archivo CSV.\n\n¿Estás seguro de continuar?")
                .setPositiveButton("Sí, importar") { _, _ ->
                    importCsvLauncher.launch(importMimeTypes)
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
        val fileName = "cbd_counter_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date()) + ".csv"
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
            try {
                startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
                showFeedback("CSV exportado", false)
            } catch (e: Exception) {
                showFeedback("No se encontró una app para compartir el CSV", true)
            }
        }.onFailure { e ->
            showFeedback("Error al exportar CSV: ${e.message}", true)
        }
    }

    private fun buildCsvContent(): String {
        val allDates = Prefs.getAllDatesWithData(this)
        if (allDates.isEmpty()) return ""

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sortedDates = allDates.mapNotNull { dateString ->
            runCatching { dateFormat.parse(dateString) }.getOrNull()?.let { parsed ->
                dateString to parsed
            }
        }.sortedBy { it.second }

        val builder = StringBuilder("date,count_cbd,count_thc,note\n")
        sortedDates.forEach { (dateString, _) ->
            val cbdCount = Prefs.getCbdCount(this, dateString)
            val thcCount = Prefs.getThcCount(this, dateString)
            val note = Prefs.getNote(this, dateString) ?: ""

            builder.append(dateString)
                .append(',')
                .append(cbdCount)
                .append(',')
                .append(thcCount)
                .append(',')
                .append(escapeCsvField(note))
                .append('\n')
        }
        return builder.toString()
    }

    private fun handleImportCsv(uri: Uri) {
        val result = runCatching {
            contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                    val lines = reader.readLines()
                    if (lines.isEmpty()) throw IllegalArgumentException("Archivo vacío")

                    val editor = sharedPrefs.edit()
                    // Limpiar datos existentes
                    sharedPrefs.all.keys.filter {
                        it.startsWith("count_") || it.startsWith("NOTE_")
                    }.forEach { key -> editor.remove(key) }

                    // Detectar formato por cabecera
                    val header = lines.first().lowercase()
                    val isNewFormat = header.contains("count_cbd")

                    lines.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val columns = splitCsvLine(line)
                        if (columns.size < 2) return@forEach

                        val date = columns[0]

                        if (isNewFormat) {
                            // Nuevo formato: date,count_cbd,count_thc,note
                            val cbdCount = columns.getOrNull(1)?.toIntOrNull() ?: 0
                            val thcCount = columns.getOrNull(2)?.toIntOrNull() ?: 0
                            editor.putInt("${Prefs.KEY_COUNT_CBD_PREFIX}$date", cbdCount)
                            editor.putInt("${Prefs.KEY_COUNT_THC_PREFIX}$date", thcCount)

                            val rawNote = columns.getOrNull(3) ?: ""
                            val note = unescapeCsvField(rawNote)
                            if (note.isNotEmpty()) {
                                editor.putString("${Prefs.KEY_NOTE_PREFIX}$date", note)
                            }
                        } else {
                            // Formato legacy: date,count,note,substance
                            val count = columns[1].toIntOrNull() ?: return@forEach
                            val substance = if (columns.size >= 4) unescapeCsvField(columns[3]) else "CBD"

                            // Importar al contador correspondiente
                            if (substance == "THC") {
                                editor.putInt("${Prefs.KEY_COUNT_THC_PREFIX}$date", count)
                            } else {
                                editor.putInt("${Prefs.KEY_COUNT_CBD_PREFIX}$date", count)
                            }

                            val rawNote = if (columns.size >= 3) columns[2] else ""
                            val note = unescapeCsvField(rawNote)
                            if (note.isNotEmpty()) {
                                editor.putString("${Prefs.KEY_NOTE_PREFIX}$date", note)
                            }
                        }
                    }
                    editor.apply()
                }
            } ?: throw IllegalArgumentException("No se pudo abrir el archivo")
        }

        result.onSuccess {
            runCatching {
                loadTodayData()
                loadAllHistoryData()
                updateDisplay()
                updateHistoryView()
                updateStats()
            }
            showFeedback("Importación completada", false)
        }.onFailure { e ->
            showFeedback("Error al importar CSV: ${e.message}", true)
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

    private fun registerStandardIntake() {
        val isThc = Prefs.getSubstanceType(this) == "THC"
        val entry = if (isThc) "🟢 ${getCurrentTimestamp()}" else "🔹 ${getCurrentTimestamp()}"
        val feedback = if (isThc) getString(R.string.thc_added) else getString(R.string.cbd_added)
        registerIntake(entry, feedback)
    }

    private fun showInfusionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_infusion_choice, null)
        val weedButton = dialogView.findViewById<MaterialButton>(R.id.weedButton)
        val polemButton = dialogView.findViewById<MaterialButton>(R.id.polemButton)
        val title = dialogView.findViewById<TextView>(R.id.infusionTitle)
        val subtitle = dialogView.findViewById<TextView>(R.id.infusionSubtitle)

        // Adjust text based on substance
        val substanceType = Prefs.getSubstanceType(this)
        if (substanceType == "THC") {
            title.text = getString(R.string.infusion_question_thc)
            subtitle.text = getString(R.string.infusion_subtitle_thc)
            
            // Adjust colors for THC mode
            weedButton.setTextColor(ContextCompat.getColor(this, R.color.thc_weed_orange))
            weedButton.strokeColor = androidx.core.content.res.ResourcesCompat.getColorStateList(resources, R.color.thc_weed_outline, theme)
            polemButton.setTextColor(ContextCompat.getColor(this, R.color.thc_weed_orange))
             polemButton.strokeColor = androidx.core.content.res.ResourcesCompat.getColorStateList(resources, R.color.thc_weed_outline, theme)
        } 
        // Default CBD strings are already in layout (or we can set them explicitely)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        weedButton.text = "${InfusionType.WEED.icon} ${getString(InfusionType.WEED.labelRes)}"
        polemButton.text = "${InfusionType.POLEM.icon} ${getString(InfusionType.POLEM.labelRes)}"

        weedButton.setOnClickListener {
            handleInfusionSelection(InfusionType.WEED)
            dialog.dismiss()
        }
        polemButton.setOnClickListener {
            handleInfusionSelection(InfusionType.POLEM)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun handleInfusionSelection(type: InfusionType) {
        val label = getString(type.labelRes)
        val suffix = getString(R.string.infusion_note_suffix, label)
        val entry = "${type.icon} ${getCurrentTimestamp()}$suffix"
        // Infusión (weed/polen) SIEMPRE suma a THC
        registerThcIntake(entry, getString(type.feedbackRes))
    }

    /** Registra una toma que siempre va al contador THC (para infusiones) */
    private fun registerThcIntake(entry: String, feedbackMessage: String) {
        thcCount++
        updateDisplay()
        appendEntryToTodayNote(entry)
        saveData()
        animateCounter(1.1f)
        showFeedback("$feedbackMessage (THC)", false)
    }

    private fun registerIntake(entry: String, feedbackMessage: String) {
        val isThc = Prefs.getSubstanceType(this) == "THC"
        if (isThc) {
            thcCount++
        } else {
            cbdCount++
        }
        updateDisplay()
        appendEntryToTodayNote(entry)
        saveData()
        animateCounter(1.1f)
        showFeedback(feedbackMessage, false)
    }

    private fun getCurrentTimestamp(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    /**
     * Muestra el disclaimer médico la primera vez que se abre la app.
     * Requerido por las políticas de Google Play para apps relacionadas con sustancias.
     */
    private fun showDisclaimerIfNeeded() {
        val disclaimerAccepted = sharedPrefs.getBoolean("disclaimer_accepted", false)
        if (!disclaimerAccepted) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_message)
                .setPositiveButton(R.string.disclaimer_accept) { _, _ ->
                    sharedPrefs.edit().putBoolean("disclaimer_accepted", true).apply()
                }
                .setNegativeButton(R.string.disclaimer_decline) { _, _ ->
                    // Si el usuario no acepta, cerrar la app
                    Toast.makeText(
                        this,
                        "Debes aceptar el aviso para usar la aplicación",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .setCancelable(false) // No puede cancelar con el botón atrás
                .show()
        }
    }

    private fun appendEntryToTodayNote(entry: String) {
        val today = getCurrentDateKey()
        val currentNote = Prefs.getNote(this, today)

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

    private fun removeLastEntryFromTodayNote() {
        val today = getCurrentDateKey()
        val currentNote = Prefs.getNote(this, today)

        // Si no hay nota o está vacía, no hay nada que borrar
        if (currentNote.isNullOrBlank()) return

        // Dividir la nota en líneas
        val lines = currentNote.split("\n").toMutableList()

        // Eliminar la última línea
        if (lines.isNotEmpty()) {
            lines.removeAt(lines.lastIndex)
        }

        // Si quedan líneas, unirlas de nuevo; si no, guardar null
        val updatedNote = if (lines.isNotEmpty()) {
            lines.joinToString("\n")
        } else {
            null
        }

        Prefs.setNote(this, today, updatedNote)
    }

    private fun openStatsCalendar() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }
}

// Data class
data class HistoryItem(
    val date: String,
    val cbdCount: Int,
    val thcCount: Int,
    val dateObject: Date
) {
    val totalCount: Int get() = cbdCount + thcCount
}

// Adapter
class ImprovedHistoryAdapter(
    private val historyList: List<HistoryItem>,
    private val onDayClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }

    private val groupedData = mutableListOf<Any>()

    init { groupData() }

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
        val countText: TextView = itemView.findViewById(R.id.historyCount)  // Oculto
        val cbdChip: TextView = itemView.findViewById(R.id.cbdChip)
        val thcChip: TextView = itemView.findViewById(R.id.thcChip)
        val emojiText: TextView = itemView.findViewById(R.id.historyEmoji)
        val progressBar: View = itemView.findViewById(R.id.progressBar)
        val noteBadge: TextView? = itemView.findViewById(R.id.noteBadge)
        val audioBadge: TextView? = itemView.findViewById(R.id.audioBadge)
    }
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.history_header, parent, false))
        } else {
            ItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.history_item, parent, false))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.headerText.text = groupedData[position] as String
            is ItemViewHolder -> {
                val item = groupedData[position] as HistoryItem
                val dayFormat = SimpleDateFormat("EEEE dd", Locale("es", "ES"))
                holder.dateText.text = dayFormat.format(item.dateObject)
                    .replaceFirstChar { it.uppercase() }

                // Mostrar chips según los datos
                if (item.cbdCount > 0) {
                    holder.cbdChip.text = "${item.cbdCount} CBD"
                    holder.cbdChip.visibility = View.VISIBLE
                } else {
                    holder.cbdChip.visibility = View.GONE
                }

                if (item.thcCount > 0) {
                    holder.thcChip.text = "${item.thcCount} THC"
                    holder.thcChip.visibility = View.VISIBLE
                } else {
                    holder.thcChip.visibility = View.GONE
                }

                // Si ambos son 0, mostrar chip CBD con 0
                if (item.cbdCount == 0 && item.thcCount == 0) {
                    holder.cbdChip.text = "0 CBD"
                    holder.cbdChip.visibility = View.VISIBLE
                }

                val total = item.totalCount
                holder.emojiText.text = when {
                    total == 0 -> "😌"
                    total <= 2 -> "🙂"
                    total <= 4 -> "😄"
                    total <= 5 -> "🫠"
                    total <= 6 -> "🤔"
                    total <= 7 -> "🙄"
                    total <= 8 -> "😶‍🌫️"
                    total <= 9 -> "🫡"
                    total <= 10 -> "🫥"
                    total <= 11 -> "⛔️"
                    else -> "💀"
                }

                // Barra de progreso basada en el total
                val maxWidth = holder.itemView.width
                val progress = minOf(total / 10f, 1f)
                val layoutParams = holder.progressBar.layoutParams
                layoutParams.width = (maxWidth * progress).toInt()
                holder.progressBar.layoutParams = layoutParams

                // Color de la barra: del mayor, o verde si es 0
                val barColor = when {
                    total == 0 -> R.color.green_safe
                    item.thcCount > item.cbdCount -> {
                        // THC es mayor, usar escala verde
                        when {
                            total <= 4 -> R.color.thc_primary_light
                            total <= 6 -> R.color.orange_danger
                            else -> R.color.red_critical
                        }
                    }
                    else -> {
                        // CBD es mayor o igual, usar escala azul
                        when {
                            total <= 4 -> R.color.primary_light
                            total <= 6 -> R.color.orange_danger
                            else -> R.color.red_critical
                        }
                    }
                }
                holder.progressBar.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, barColor)
                )

                // Badge de nota visible si existe nota para ese día
                val ctx = holder.itemView.context
                holder.noteBadge?.visibility =
                    if (Prefs.hasNote(ctx, item.date)) View.VISIBLE else View.GONE

                // Badge de audio visible si existe audio para ese día
                holder.audioBadge?.visibility =
                    if (Prefs.hasAudio(ctx, item.date)) View.VISIBLE else View.GONE

                // Clicks para abrir el modal
                holder.itemView.setOnClickListener { onDayClick(item.date) }
                holder.noteBadge?.setOnClickListener { onDayClick(item.date) }
                holder.audioBadge?.setOnClickListener { onDayClick(item.date) }
            }
        }
    }

    override fun getItemCount() = groupedData.size

    fun refresh() {
        groupData()
        notifyDataSetChanged()
    }
}


// Decoración
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
