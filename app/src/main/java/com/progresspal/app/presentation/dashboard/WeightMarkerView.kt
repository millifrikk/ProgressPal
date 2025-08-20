package com.progresspal.app.presentation.dashboard

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.progresspal.app.R
import java.text.SimpleDateFormat
import java.util.*

class WeightMarkerView(context: Context) : MarkerView(context, R.layout.marker_view_weight) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val tvDate: TextView = findViewById(R.id.tv_marker_date)
    private val tvWeight: TextView = findViewById(R.id.tv_marker_weight)
    
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let { entry ->
            val date = Date(entry.x.toLong() * 1000) // Convert back from timestamp
            val weight = entry.y
            
            tvDate.text = dateFormat.format(date)
            tvWeight.text = "${String.format("%.1f", weight)} kg"
        }
        super.refreshContent(e, highlight)
    }
    
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}