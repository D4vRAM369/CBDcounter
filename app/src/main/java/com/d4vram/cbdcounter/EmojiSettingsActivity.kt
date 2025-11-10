package com.d4vram.cbdcounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

/**
 * Actividad para personalizar los emojis según rangos de consumo
 */
class EmojiSettingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmojiRangeAdapter
    private lateinit var resetButton: MaterialButton

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
        setContentView(R.layout.activity_emoji_settings)

        // Configurar toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.emojiRangesRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar emojis guardados o usar los por defecto
        val currentEmojis = loadCustomEmojis()
        adapter = EmojiRangeAdapter(emojiRanges, currentEmojis) { range, newEmoji ->
            // Callback cuando se cambia un emoji
            saveCustomEmoji(range.count, newEmoji)
        }
        recyclerView.adapter = adapter

        // Configurar botón de reset
        resetButton = findViewById(R.id.resetButton)
        resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    /**
     * Carga los emojis personalizados guardados en SharedPreferences
     */
    private fun loadCustomEmojis(): Map<Int, String> {
        val prefs = getSharedPreferences("emoji_prefs", MODE_PRIVATE)
        val customEmojis = mutableMapOf<Int, String>()

        for (range in emojiRanges) {
            val savedEmoji = prefs.getString("emoji_${range.count}", null)
            if (savedEmoji != null) {
                customEmojis[range.count] = savedEmoji
            }
        }

        return customEmojis
    }

    /**
     * Guarda un emoji personalizado en SharedPreferences
     */
    private fun saveCustomEmoji(count: Int, emoji: String) {
        val prefs = getSharedPreferences("emoji_prefs", MODE_PRIVATE)
        prefs.edit().putString("emoji_$count", emoji).apply()
    }

    /**
     * Borra todos los emojis personalizados (volver a por defecto)
     */
    private fun resetToDefaults() {
        val prefs = getSharedPreferences("emoji_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Recargar el adapter
        adapter.resetToDefaults()
    }

    /**
     * Muestra diálogo de confirmación antes de resetear
     */
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Restaurar valores por defecto")
            .setMessage("¿Estás seguro de que quieres restaurar todos los emojis a sus valores originales?")
            .setPositiveButton("Sí") { _, _ ->
                resetToDefaults()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra selector de emojis con categorías
     */
    private fun showEmojiPicker(currentEmoji: String, onEmojiSelected: (String) -> Unit) {
        // Lista AMPLIADA de emojis disponibles organizados por categoría
        val emojis = listOf(
            // Caras positivas y neutrales
            "😌", "🙂", "😊", "😀", "😃", "😄", "😁", "😆", "😅", "🤣",
            "😂", "🙃", "😉", "😇", "🤩", "☺️", "🥲", "😋", "😛", "😜", "🤪", "😝",

            // Caras pensativas y confundidas
            "🤔", "🤨", "😐", "😑", "😶", "🙄", "😣", "😥", "😮", "😯", "😪", "😫", "🥱", "😴", "😌", "🤤",

            // Caras alteradas y mareadas
            "🫠", "😵", "😵‍💫", "🤯", "🥴", "😲",

            // Caras serias y militares
            "🫡", "😬", "🫨", "🫥",

            // Caras negativas y enfadadas
            "😞", "😔", "😟", "😕", "🙁", "☹️", "😰", "😨", "😧", "😦", "😈",
            "👿", "💀", "☠️", "👻", "👽", "👾",

            // Gestos y manos
            "👍", "👎", "🤞", "✌️", "👌", "🤌", "🤏", "✋", "🤚",

            // Objetos y símbolos relacionados con CBD/THC
            "🌿", "🍀", "🌱", "🌾", "🪴", "🍃",

            // Símbolos de advertencia y estado
            "⚠️", "🚫", "⛔️", "🔞", "📵", "🔕", "❌", "⭕️", "❗️", "❓",

            // Colores y formas
            "🟢", "🟡", "🟠", "🔴", "🟣", "🔵", "🟤", "⚫️", "⚪️", "🟥",
            "🟧", "🟨", "🟩", "🟦", "🟪", "🟫", "⬛️", "⬜️", "◼️", "◻️",
            "◾️", "◽️", "▪️", "▫️", "🔶", "🔷", "🔸", "🔹", "🔺", "🔻",

            // Símbolos adicionales
            "💚", "💛", "🧡", "❤️", "💜", "💙", "🖤", "🤍", "🤎", "💯",
            "💥", "💫", "⭐️", "🌟", "✨", "⚡️", "🔥", "💧", "💦", "☁️",

            // Números
            "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟",
        )

        val emojiArray = emojis.toTypedArray()
        var selectedIndex = emojis.indexOf(currentEmoji).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("Selecciona un emoji (${emojis.size} disponibles)")
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

    /**
     * Representa un rango de consumo con su emoji
     */
    data class EmojiRange(
        val count: Int,           // Valor representativo (0, 1, 3, 5, etc.)
        val defaultEmoji: String, // Emoji por defecto
        val colorRes: Int,        // Color del indicador
        val rangeText: String     // Texto a mostrar ("0", "1-2", "12+")
    )

    /**
     * Adapter del RecyclerView
     */
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

            // Configurar vistas
            holder.rangeText.text = range.rangeText
            holder.emojiText.text = currentEmoji
            holder.colorIndicator.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, range.colorRes)
            )

            // Click en el emoji para cambiarlo
            holder.emojiText.setOnClickListener {
                showEmojiPicker(currentEmoji) { newEmoji ->
                    // Actualizar el mapa de emojis personalizados
                    val mutableCustom = customEmojis.toMutableMap()
                    mutableCustom[range.count] = newEmoji
                    customEmojis = mutableCustom

                    // Notificar cambio
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
