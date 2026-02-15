package com.d4vram.cbdcounter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmojiRangeAdapter

    // Lista de rangos con sus emojis por defecto
    private val emojiRanges = listOf(
        EmojiRange(0, "😌", R.color.green_safe, "0"),
        EmojiRange(1, "🙂", R.color.green_safe, "1-2"),
        EmojiRange(3, "😄", R.color.yellow_warning, "3-4"),
        EmojiRange(5, "🫠", R.color.yellow_warning, "5"),
        EmojiRange(6, "🤔", R.color.orange_danger, "6"),
        EmojiRange(7, "🙄", R.color.orange_danger, "7"),
        EmojiRange(8, "😶‍🌫️", R.color.orange_danger, "8"),
        EmojiRange(9, "🫡", R.color.red_critical, "9"),
        EmojiRange(10, "🫥", R.color.red_critical, "10"),
        EmojiRange(11, "⛔️", R.color.red_critical, "11"),
        EmojiRange(12, "💀", R.color.primary_purple, "12+")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Forzar status bar con color del toolbar
        window.statusBarColor = getColor(R.color.gradient_start)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupSubstanceToggle()
        setupBackupSection()
        setupEmojiSection()
    }

    // ========================================
    // SECCIÓN: Tipo de Sustancia (CBD/THC)
    // ========================================
    private fun setupSubstanceToggle() {
        val substanceToggle = findViewById<MaterialButtonToggleGroup>(R.id.substanceToggleGroup)
        val currentSubstance = Prefs.getSubstanceType(this)

        if (currentSubstance == "THC") {
            substanceToggle.check(R.id.btnThc)
        } else {
            substanceToggle.check(R.id.btnCbd)
        }

        substanceToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val type = if (checkedId == R.id.btnThc) "THC" else "CBD"
                Prefs.setSubstanceType(this, type)
                Toast.makeText(this, "Modo $type activado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ========================================
    // SECCIÓN: Backup
    // ========================================
    private fun setupBackupSection() {
        val autoBackupSwitch = findViewById<SwitchMaterial>(R.id.switchBackupAuto)
        val btnBackupCsv = findViewById<MaterialButton>(R.id.btnBackupCsv)
        val btnExportAudios = findViewById<MaterialButton>(R.id.btnExportAudios)

        // Cargar preferencia de auto backup
        autoBackupSwitch.isChecked = getSharedPreferences("CBDCounter", MODE_PRIVATE)
            .getBoolean("auto_backup", false)

        autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("CBDCounter", MODE_PRIVATE).edit()
                .putBoolean("auto_backup", isChecked).apply()
            Toast.makeText(
                this,
                "Backup automático ${if (isChecked) "activado" else "desactivado"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnBackupCsv.setOnClickListener { exportCsvBackup() }
        btnExportAudios.setOnClickListener { exportAudiosZip() }
    }

    private fun exportCsvBackup() {
        val csvContent = buildCsvContent()
        if (csvContent.isBlank()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        val backupDir = File(cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val fileName = "cbdcounter_backup_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
        val file = File(backupDir, fileName)

        try {
            file.writeText(csvContent, Charsets.UTF_8)
            shareFile(file, "text/csv", "Compartir Backup CSV")
            Toast.makeText(this, "Backup CSV exportado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportAudiosZip() {
        val audioDir = File(filesDir, "audios")
        if (!audioDir.exists() || audioDir.listFiles()?.isEmpty() == true) {
            Toast.makeText(this, "No hay audios para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        val zipFile = File(cacheDir, "audios_export.zip")
        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                audioDir.listFiles()?.forEach { file ->
                    FileInputStream(file).use { fis ->
                        zos.putNextEntry(ZipEntry(file.name))
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
            shareFile(zipFile, "application/zip", "Compartir ZIP de audios")
            Toast.makeText(this, "Audios exportados", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, title))
    }

    private fun buildCsvContent(): String {
        val prefs = getSharedPreferences("CBDCounter", MODE_PRIVATE)
        val allEntries = prefs.all
        if (allEntries.isEmpty()) return ""

        val dates = mutableSetOf<String>()
        allEntries.keys.forEach { key ->
            when {
                key.startsWith("count_") -> dates.add(key.removePrefix("count_"))
                key.startsWith("NOTE_") -> dates.add(key.removePrefix("NOTE_"))
            }
        }
        if (dates.isEmpty()) return ""

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sortedDates = dates.mapNotNull { dateString ->
            kotlin.runCatching { dateFormat.parse(dateString) }.getOrNull()?.let { parsed ->
                dateString to parsed
            }
        }.sortedBy { it.second }

        val builder = StringBuilder("date,count,note\n")
        sortedDates.forEach { (dateString, _) ->
            val count = prefs.getInt("count_$dateString", 0)
            val note = Prefs.getNote(this, dateString) ?: ""
            builder.append(dateString)
                .append(',')
                .append(count)
                .append(',')
                .append(escapeCsvField(note))
                .append('\n')
        }
        return builder.toString()
    }

    private fun escapeCsvField(value: String): String {
        if (value.isEmpty()) return ""
        return value.replace("\\", "\\\\").replace("\n", "\\n").replace(",", "\\,")
    }

    // ========================================
    // SECCIÓN: Personalizar Emojis
    // ========================================
    private fun setupEmojiSection() {
        recyclerView = findViewById(R.id.emojiRangesRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentEmojis = loadCustomEmojis()
        adapter = EmojiRangeAdapter(emojiRanges, currentEmojis) { range, newEmoji ->
            saveCustomEmoji(range.count, newEmoji)
        }
        recyclerView.adapter = adapter

        findViewById<MaterialButton>(R.id.resetEmojiButton).setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun loadCustomEmojis(): Map<Int, String> {
        val prefs = getSharedPreferences("emoji_prefs", MODE_PRIVATE)
        val customEmojis = mutableMapOf<Int, String>()
        for (range in emojiRanges) {
            prefs.getString("emoji_${range.count}", null)?.let {
                customEmojis[range.count] = it
            }
        }
        return customEmojis
    }

    private fun saveCustomEmoji(count: Int, emoji: String) {
        getSharedPreferences("emoji_prefs", MODE_PRIVATE)
            .edit().putString("emoji_$count", emoji).apply()
    }

    private fun resetToDefaults() {
        getSharedPreferences("emoji_prefs", MODE_PRIVATE).edit().clear().apply()
        adapter.resetToDefaults()
        Toast.makeText(this, "Emojis restaurados", Toast.LENGTH_SHORT).show()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Restaurar emojis")
            .setMessage("¿Restaurar todos los emojis a sus valores por defecto?")
            .setPositiveButton("Sí") { _, _ -> resetToDefaults() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEmojiPicker(currentEmoji: String, onEmojiSelected: (String) -> Unit) {
        val emojis = listOf(
            "😌", "🙂", "😊", "😀", "😃", "😄", "😁", "😆", "😅", "🤣",
            "😂", "🙃", "😉", "😇", "🤩", "☺️", "🥲", "😋", "😛", "😜", "🤪", "😝",
            "🤔", "🤨", "😐", "😑", "😶", "🙄", "😣", "😥", "😮", "😯", "😪", "😫", "🥱", "😴", "🤤",
            "🫠", "😵", "😵‍💫", "🤯", "🥴", "😲",
            "🫡", "😬", "🫨", "🫥",
            "😞", "😔", "😟", "😕", "🙁", "☹️", "😰", "😨", "😧", "😦", "😈",
            "👿", "💀", "☠️", "👻", "👽", "👾",
            "👍", "👎", "🤞", "✌️", "👌", "🤌", "🤏", "✋", "🤚",
            "🌿", "🍀", "🌱", "🌾", "🪴", "🍃",
            "⚠️", "🚫", "⛔️", "🔞", "📵", "🔕", "❌", "⭕️", "❗️", "❓",
            "🟢", "🟡", "🟠", "🔴", "🟣", "🔵", "🟤", "⚫️", "⚪️",
            "💚", "💛", "🧡", "❤️", "💜", "💙", "🖤", "🤍", "🤎", "💯",
            "💥", "💫", "⭐️", "🌟", "✨", "⚡️", "🔥"
        )

        val emojiArray = emojis.toTypedArray()
        var selectedIndex = emojis.indexOf(currentEmoji).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("Selecciona un emoji")
            .setSingleChoiceItems(emojiArray, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Aceptar") { _, _ ->
                onEmojiSelected(emojis[selectedIndex])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ========================================
    // Clases de datos y Adapter
    // ========================================
    data class EmojiRange(
        val count: Int,
        val defaultEmoji: String,
        val colorRes: Int,
        val rangeText: String
    )

    inner class EmojiRangeAdapter(
        private val ranges: List<EmojiRange>,
        private var customEmojis: Map<Int, String>,
        private val onEmojiChanged: (EmojiRange, String) -> Unit
    ) : RecyclerView.Adapter<EmojiRangeAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val colorIndicator: View = view.findViewById(R.id.colorIndicator)
            val rangeText: TextView = view.findViewById(R.id.rangeText)
            val emojiText: TextView = view.findViewById(R.id.emojiText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_emoji_range, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val range = ranges[position]
            val currentEmoji = customEmojis[range.count] ?: range.defaultEmoji

            holder.rangeText.text = range.rangeText
            holder.emojiText.text = currentEmoji
            holder.colorIndicator.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, range.colorRes)
            )

            holder.emojiText.setOnClickListener {
                showEmojiPicker(currentEmoji) { newEmoji ->
                    val mutableCustom = customEmojis.toMutableMap()
                    mutableCustom[range.count] = newEmoji
                    customEmojis = mutableCustom
                    notifyItemChanged(position)
                    onEmojiChanged(range, newEmoji)
                }
            }
        }

        override fun getItemCount() = ranges.size

        fun resetToDefaults() {
            customEmojis = emptyMap()
            notifyDataSetChanged()
        }
    }
}
