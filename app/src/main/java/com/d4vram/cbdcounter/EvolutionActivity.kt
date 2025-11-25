package com.d4vram.cbdcounter

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EvolutionActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChartView
    private lateinit var rangeChipGroup: ChipGroup
    private lateinit var evolutionTitle: TextView
    private val dateKeyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val labelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evolution)

        val toolbar = findViewById<MaterialToolbar>(R.id.evolutionToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        lineChart = findViewById(R.id.lineChart)
        rangeChipGroup = findViewById(R.id.rangeChipGroup)
        evolutionTitle = findViewById(R.id.evolutionTitle)

        rangeChipGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip7Days -> loadData(7)
                R.id.chip30Days -> loadData(30)
            }
        }

        // Default load
        loadData(7)
    }

    private fun loadData(days: Int) {
        evolutionTitle.text = "Últimos $days días"
        val sharedPrefs = getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)
        val dataPoints = mutableListOf<Pair<String, Int>>()
        val calendar = Calendar.getInstance()

        // Go back 'days' days
        calendar.add(Calendar.DAY_OF_YEAR, -(days - 1))

        for (i in 0 until days) {
            val dateKey = dateKeyFormat.format(calendar.time)
            val label = labelFormat.format(calendar.time)
            val prefKey = "count_$dateKey"
            val count = sharedPrefs.getInt(prefKey, 0)
            
            dataPoints.add(Pair(label, count))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        lineChart.setData(dataPoints)
    }
}
