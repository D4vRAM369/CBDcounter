package com.d4vram.cbdcounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var lineChart: LineChartView
    private lateinit var tvToday: TextView
    private lateinit var tvWeek: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvStreak: TextView
    private lateinit var tvBusiestDay: TextView
    private lateinit var tvBestDay: TextView

    private val dateKeyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        window.statusBarColor = getColor(R.color.gradient_start)

        sharedPrefs = getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)

        val toolbar = findViewById<MaterialToolbar>(R.id.dashboardToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Initialize Views
        tvToday = findViewById(R.id.tvTodayCount)
        tvWeek = findViewById(R.id.tvWeekCount)
        tvAvg = findViewById(R.id.tvAvgCount)
        tvStreak = findViewById(R.id.tvStreakCount)
        tvBusiestDay = findViewById(R.id.tvBusiestDay)
        tvBestDay = findViewById(R.id.tvBestDay)
        lineChart = findViewById(R.id.lineChart)
        
        val btnViewCalendar = findViewById<MaterialButton>(R.id.btnViewCalendar)
        btnViewCalendar.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        val rangeChipGroup = findViewById<ChipGroup>(R.id.rangeChipGroup)
        rangeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val days = when (checkedId) {
                R.id.chip7Days -> 7
                R.id.chip14Days -> 14
                R.id.chip30Days -> 30
                else -> 7
            }
            loadChartData(days)
        }

        // Load Data
        calculateStats()
        loadChartData(7)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data on resume (in case settings changed or user returned from calendar)
        calculateStats()
        // Determine current selected chip range
        val rangeChipGroup = findViewById<ChipGroup>(R.id.rangeChipGroup)
        val days = when (rangeChipGroup.checkedChipId) {
            R.id.chip14Days -> 14
            R.id.chip30Days -> 30
            else -> 7
        }
        loadChartData(days)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, EmojiSettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun calculateStats() {
        // 1. Today
        val todayKey = dateKeyFormat.format(Date())
        val todayCount = sharedPrefs.getInt("count_$todayKey", 0)
        tvToday.text = todayCount.toString()

        // 2. Week Total & Average
        val calendar = Calendar.getInstance()
        var weekTotal = 0
        val counts = mutableListOf<Int>()
        
        // Go back 7 days (including today? usually "last 7 days" includes today)
        // Let's do last 7 days excluding today if we want completed days? 
        // No, user expects "this week" or "rolling 7 days". Lets do rolling 7 days including today.
        val tempCal = Calendar.getInstance()
        for (i in 0 until 7) {
            val key = dateKeyFormat.format(tempCal.time)
            val c = sharedPrefs.getInt("count_$key", 0)
            weekTotal += c
            counts.add(c)
            tempCal.add(Calendar.DAY_OF_YEAR, -1) // go back
        }
        tvWeek.text = weekTotal.toString()

        // Average (last 30 days for better accuracy?)
        val avgCal = Calendar.getInstance()
        var total30 = 0
        var daysWithData = 0
        for (i in 0 until 30) {
             val key = dateKeyFormat.format(avgCal.time)
             if (sharedPrefs.contains("count_$key")) {
                 total30 += sharedPrefs.getInt("count_$key", 0)
                 daysWithData++
             }
             avgCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val avg = if (daysWithData > 0) total30.toFloat() / daysWithData else 0f
        tvAvg.text = String.format("%.1f", avg)

        // 3. Streak
        tvStreak.text = "${calculateCleanStreak()} días"

        // 4. Patterns
        calculatePatterns()
    }

    private fun calculateCleanStreak(): Int {
        var streak = 0
        val calendar = Calendar.getInstance()
        
        // Check backwards from today
        // If today has 0, streak starts from yesterday? 
        // Logic: "Clean Streak" usually means days WITHOUT using (count == 0).
        // Let's assume standard logic: 0 means clean.
        
        // Check if today is clean so far?
        // If currentCount > 0, streak is currently 0.
        // If currentCount == 0, count today + previous days.
        
        // However, if the user hasn't finished the day, is it fair to count today?
        // Let's just count consecutive days with 0.
        
        // Start from today descending
        var checkingDate = Calendar.getInstance()
        
        // Safety Break after 365 days
        for(i in 0 until 365) {
            val key = dateKeyFormat.format(checkingDate.time)
            // If we don't have data for a day, do we assume 0 (clean)?
            // Usually yes if it's in the past.
            val count = sharedPrefs.getInt("count_$key", 0)
            if (count == 0) {
                streak++
            } else {
                break
            }
            checkingDate.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun calculatePatterns() {
        val allData = sharedPrefs.all
        val dayCounts = IntArray(7) { 0 } // Sun=0, Mon=1...
        val dayOccurrences = IntArray(7) { 0 }
        
        var maxCount = 0
        var bestDate = ""

        allData.forEach { (key, value) ->
            if (key.startsWith("count_") && value is Int) {
                val dateStr = key.removePrefix("count_")
                try {
                    val date = dateKeyFormat.parse(dateStr)
                    if (date != null && value > 0) {
                        val cal = Calendar.getInstance()
                        cal.time = date
                        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
                        dayCounts[dayOfWeek] += value
                        dayOccurrences[dayOfWeek]++
                        
                        if (value > maxCount) {
                            maxCount = value
                            bestDate = dateStr
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        // Busiest Day (Highest Average)
        var maxAvg = 0f
        var busiestDayIndex = -1
        
        for (i in 0 until 7) {
            if (dayOccurrences[i] > 0) {
                val avg = dayCounts[i].toFloat() / dayOccurrences[i]
                if (avg > maxAvg) {
                    maxAvg = avg
                    busiestDayIndex = i
                }
            }
        }

        val daysOfWeek = arrayOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        
        val busiestDayStr = if (busiestDayIndex != -1) "${daysOfWeek[busiestDayIndex]} (${String.format("%.1f", maxAvg)})" else "Sin datos"
        tvBusiestDay.text = getString(R.string.pattern_busiest_day, busiestDayStr)
        tvBestDay.text = getString(R.string.pattern_best_day, if (bestDate.isNotEmpty()) bestDate else "-", maxCount)
    }

    private fun loadChartData(days: Int) {
        val dataPoints = mutableListOf<Pair<String, Int>>()
        val calendar = Calendar.getInstance()
        
        // We want 'days' points ending today
        // Start date = Today - (days - 1)
        calendar.add(Calendar.DAY_OF_YEAR, -(days - 1))

        for (i in 0 until days) {
            val dateKey = dateKeyFormat.format(calendar.time)
            val label = labelFormat.format(calendar.time)
            val count = sharedPrefs.getInt("count_$dateKey", 0)
            dataPoints.add(Pair(label, count))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        lineChart.setData(dataPoints)
    }
}
