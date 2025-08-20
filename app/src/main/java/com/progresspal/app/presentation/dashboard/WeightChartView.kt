package com.progresspal.app.presentation.dashboard

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.progresspal.app.R
import com.progresspal.app.domain.models.Weight
import com.progresspal.app.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WeightChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LineChart(context, attrs, defStyleAttr) {
    
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    init {
        setupChart()
    }
    
    private fun setupChart() {
        // Chart appearance
        description.isEnabled = false
        setTouchEnabled(true)
        setDragEnabled(true)
        setScaleEnabled(true)
        setPinchZoom(true)
        setDoubleTapToZoomEnabled(true)
        setDrawGridBackground(false)
        legend.isEnabled = false
        
        // Enable highlighting for tap interactions
        isHighlightPerTapEnabled = true
        isHighlightPerDragEnabled = false
        
        // X-axis (dates)
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val date = Date(value.toLong() * 1000) // Convert back from days to timestamp
                    return dateFormat.format(date)
                }
            }
            textColor = context.getColor(R.color.pal_text_secondary)
        }
        
        // Y-axis (weight)
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = context.getColor(R.color.pal_divider)
            textColor = context.getColor(R.color.pal_text_secondary)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${String.format("%.1f", value)} kg"
                }
            }
        }
        
        // Disable right Y-axis
        axisRight.isEnabled = false
        
        // Set colors
        setBackgroundColor(context.getColor(R.color.pal_background))
        
        // Set custom marker view for tap interactions
        val markerView = WeightMarkerView(context)
        marker = markerView
    }
    
    fun updateChart(weights: List<Weight>) {
        if (weights.isEmpty()) {
            clear()
            return
        }
        
        // Sort weights by date (oldest first for chart display)
        val sortedWeights = weights.sortedBy { it.date.time }
        
        // Create entries for the chart
        val entries = ArrayList<Entry>()
        sortedWeights.forEach { weight ->
            val x = (weight.date.time / 1000).toFloat() // Convert timestamp to days
            val y = weight.weight
            entries.add(Entry(x, y))
        }
        
        // Create dataset
        val dataSet = LineDataSet(entries, "Weight").apply {
            color = context.getColor(R.color.pal_primary)
            setCircleColor(context.getColor(R.color.pal_primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = context.getColor(R.color.pal_primary)
            fillAlpha = 30
        }
        
        // Set data to chart
        val lineData = LineData(dataSet)
        data = lineData
        
        // Animate chart
        animateX(1000)
        
        // Refresh chart
        invalidate()
    }
    
    fun showEmptyState() {
        clear()
        // TODO: Could show a message or placeholder
        invalidate()
    }
}