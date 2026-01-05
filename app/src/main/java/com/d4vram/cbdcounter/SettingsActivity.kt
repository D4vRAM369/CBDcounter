package com.d4vram.cbdcounter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

class SettingsActivity : AppCompatActivity() {

    private lateinit var exportAudioZipButton: MaterialButton
    private lateinit var backupCsvButton: MaterialButton
    private lateinit var autoBackupSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        exportAudioZipButton = findViewById(R.id.exportAudioZipButton)
        backupCsvButton = findViewById(R.id.backupCsvButton)
        autoBackupSwitch = findViewById(R.id.autoBackupSwitch)

        // Load auto backup preference
        autoBackupSwitch.isChecked = getSharedPreferences("CBDCounter", MODE_PRIVATE)
            .getBoolean("auto_backup", false)

        exportAudioZipButton.setOnClickListener {
            exportAudiosZip()
        }

        backupCsvButton.setOnClickListener {
            backupCsvManual()
        }

        autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("CBDCounter", MODE_PRIVATE).edit()
                .putBoolean("auto_backup", isChecked).apply()
            Toast.makeText(this, "Backup automático ${if (isChecked) "activado" else "desactivado"}", Toast.LENGTH_SHORT).show()
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
                // For simplicity, no encryption yet; add dialog for password later
                audioDir.listFiles()?.forEach { file ->
                    FileInputStream(file).use { fis ->
                        zos.putNextEntry(ZipEntry(file.name))
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }

            // Share the ZIP
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", zipFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir ZIP de audios"))
            Toast.makeText(this, "ZIP exportado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backupCsvManual() {
        // Reuse logic from MainActivity
        val csvContent = buildCsvContent()
        if (csvContent.isBlank()) {
            Toast.makeText(this, "No hay datos para backup", Toast.LENGTH_SHORT).show()
            return
        }

        val backupDir = File(cacheDir, "backups").apply { if (!exists()) mkdirs() }
        val fileName = "backup_" + java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date()) + ".csv"
        val file = File(backupDir, fileName)

        try {
            file.writeText(csvContent, Charsets.UTF_8)
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir Backup CSV"))
            Toast.makeText(this, "Backup CSV exportado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear backup: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
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
}