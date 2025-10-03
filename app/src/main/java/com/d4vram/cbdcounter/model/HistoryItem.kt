package com.d4vram.cbdcounter.model

import java.util.Date

data class HistoryItem(
    val date: String,
    val count: Int,
    val dateObject: Date
)
