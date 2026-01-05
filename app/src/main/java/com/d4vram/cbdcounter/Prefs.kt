package com.d4vram.cbdcounter

import android.content.Context
import android.content.SharedPreferences
import java.io.File

object Prefs {
    private const val PREFS_NAME = "CBDCounter"
    
    // Constantes de claves para evitar errores tipográficos
    const val KEY_COUNT_PREFIX = "count_"
    const val KEY_NOTE_PREFIX = "NOTE_"

    // función privada para acceder a las SharedPreferences
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- Helpers para las NOTAS ----
    fun getNote(ctx: Context, date: String): String? =
        prefs(ctx).getString("${KEY_NOTE_PREFIX}$date", null)

    fun setNote(ctx: Context, date: String, note: String?) {
        prefs(ctx).edit().apply {
            if (note.isNullOrBlank()) {
                remove("${KEY_NOTE_PREFIX}$date")  // borra la nota si está vacía
            } else {
                putString("${KEY_NOTE_PREFIX}$date", note)  // guarda la nota
            }
        }.apply()
    }

    fun hasNote(ctx: Context, date: String): Boolean =
        prefs(ctx).contains("${KEY_NOTE_PREFIX}$date")

    fun hasAudio(ctx: Context, date: String): Boolean {
        val audioFile = File(ctx.filesDir, "audios/audio_$date.mp3")
        return audioFile.exists()
    }
}
