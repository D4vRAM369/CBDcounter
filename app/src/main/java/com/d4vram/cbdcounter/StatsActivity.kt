package com.d4vram.cbdcounter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var monthLabel: TextView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var calendarRecycler: RecyclerView
    private lateinit var sharedPrefs: SharedPreferences

    private val displayCalendar: Calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
    private val dateKeyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val todayKey: String = dateKeyFormat.format(Date())
    private val calendarAdapter = CalendarAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        toolbar = findViewById(R.id.statsToolbar)
        monthLabel = findViewById(R.id.monthLabel)
        prevMonthButton = findViewById(R.id.prevMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        calendarRecycler = findViewById(R.id.calendarRecycler)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        sharedPrefs = getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)

        calendarRecycler.layoutManager = GridLayoutManager(this, 7)
        calendarRecycler.adapter = calendarAdapter

        prevMonthButton.setOnClickListener {
            displayCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        nextMonthButton.setOnClickListener {
            displayCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        updateCalendar()
    }

    private fun updateCalendar() {
        displayCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val formattedMonth = monthFormat.format(displayCalendar.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        monthLabel.text = formattedMonth
        calendarAdapter.submit(buildCalendarCells())
    }

    private fun buildCalendarCells(): List<CalendarCell> {
        val items = mutableListOf<CalendarCell>()
        val workingCalendar = displayCalendar.clone() as Calendar

        val firstDayOffset = mondayFirstDayOffset(workingCalendar.get(Calendar.DAY_OF_WEEK))
        repeat(firstDayOffset) { items.add(CalendarCell.Empty) }

        val daysInMonth = workingCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = workingCalendar.get(Calendar.MONTH)
        val currentYear = workingCalendar.get(Calendar.YEAR)
        val todayCalendar = Calendar.getInstance()

        for (day in 1..daysInMonth) {
            workingCalendar.set(Calendar.DAY_OF_MONTH, day)
            val dateKey = dateKeyFormat.format(workingCalendar.time)
            val prefKey = "count_$dateKey"
            val hasData = sharedPrefs.contains(prefKey)
            val count = sharedPrefs.getInt(prefKey, 0)
            val emoji = if (hasData) EmojiUtils.emojiForCount(count, this) else ""
            val isToday = dateKey == todayKey &&
                todayCalendar.get(Calendar.MONTH) == currentMonth &&
                todayCalendar.get(Calendar.YEAR) == currentYear

            items.add(CalendarCell.Day(day, emoji, hasData, isToday))
        }

        while (items.size % 7 != 0) {
            items.add(CalendarCell.Empty)
        }

        return items
    }

    private fun mondayFirstDayOffset(dayOfWeek: Int): Int {
        val adjusted = (dayOfWeek - Calendar.MONDAY)
        return if (adjusted < 0) adjusted + 7 else adjusted
    }

    private sealed class CalendarCell {
        object Empty : CalendarCell()
        data class Day(
            val number: Int,
            val emoji: String,
            val hasData: Boolean,
            val isToday: Boolean
        ) : CalendarCell()
    }

    private class CalendarAdapter : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
        private val items = mutableListOf<CalendarCell>()

        fun submit(data: List<CalendarCell>) {
            items.clear()
            items.addAll(data)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day, parent, false)
            return CalendarViewHolder(view)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayContainer: View = itemView.findViewById(R.id.dayContainer)
            private val dayNumber: TextView = itemView.findViewById(R.id.dayNumber)
            private val dayEmoji: TextView = itemView.findViewById(R.id.dayEmoji)

            fun bind(cell: CalendarCell) {
                when (cell) {
                    is CalendarCell.Empty -> {
                        dayNumber.text = ""
                        dayNumber.alpha = 1f
                        dayEmoji.text = ""
                        dayEmoji.visibility = View.INVISIBLE
                        dayContainer.background = null
                    }
                    is CalendarCell.Day -> {
                        dayNumber.text = cell.number.toString()
                        dayEmoji.text = cell.emoji
                        dayNumber.alpha = when {
                            cell.isToday -> 1f
                            cell.hasData -> 1f
                            else -> 0.45f
                        }
                        dayEmoji.visibility = if (cell.hasData && cell.emoji.isNotBlank()) View.VISIBLE else View.INVISIBLE
                        dayContainer.background = if (cell.isToday) {
                            androidx.appcompat.content.res.AppCompatResources.getDrawable(
                                itemView.context,
                                R.drawable.bg_calendar_today
                            )
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }
}
