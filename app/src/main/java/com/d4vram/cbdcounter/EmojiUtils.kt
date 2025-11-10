package com.d4vram.cbdcounter

import android.content.Context

object EmojiUtils {

    /**
     * Obtiene el emoji para un conteo, considerando personalizaciones del usuario
     */
    fun emojiForCount(count: Int, context: Context? = null): String {
        // Si hay contexto, intentar cargar emoji personalizado
        if (context != null) {
            val customEmoji = getCustomEmoji(context, count)
            if (customEmoji != null) {
                return customEmoji
            }
        }

        // Emojis por defecto
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

    /**
     * Obtiene el emoji personalizado guardado para un conteo específico
     */
    private fun getCustomEmoji(context: Context, count: Int): String? {
        val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)

        // Mapear el count al rango correcto
        val rangeKey = when {
            count == 0 -> 0
            count in 1..2 -> 1
            count in 3..4 -> 3
            count == 5 -> 5
            count == 6 -> 6
            count == 7 -> 7
            count == 8 -> 8
            count == 9 -> 9
            count == 10 -> 10
            count == 11 -> 11
            else -> 12 // 12+
        }

        return prefs.getString("emoji_$rangeKey", null)
    }
}
