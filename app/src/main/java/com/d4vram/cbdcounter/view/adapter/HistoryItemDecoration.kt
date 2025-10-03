package com.d4vram.cbdcounter.view.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HistoryItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.bottom = spacing
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = spacing
        }
    }
}