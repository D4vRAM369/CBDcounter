package com.d4vram.cbdcounter

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "CBDCounter"

    // función privada para acceder a las SharedPreferences
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- Helpers para las NOTAS ----
    fun getNote(ctx: Context, date: String): String? =
        prefs(ctx).getString("NOTE_$date", null)

    fun setNote(ctx: Context, date: String, note: String?) {
        prefs(ctx).edit().apply {
            if (note.isNullOrBlank()) {
                remove("NOTE_$date")  // borra la nota si está vacía
            } else {
                putString("NOTE_$date", note)  // guarda la nota
            }
        }.apply()
    }

    fun hasNote(ctx: Context, date: String): Boolean =
        prefs(ctx).contains("NOTE_$date")
}
