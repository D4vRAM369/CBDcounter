package com.d4vram.cbdcounter

object EmojiUtils {
    fun emojiForCount(count: Int): String {
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
}
