package com.example.mysleepzyapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AnalyticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.findViewById<android.widget.FrameLayout>(R.id.weeklyChartContainer)
        if (container != null) {
            try {
                // Ensure there is something to show on first run: seed this week if empty (up to yesterday)
                runCatching { SleepLocalStore.seedWeekIfEmpty(requireContext()) }

                val chart = com.github.mikephil.charting.charts.LineChart(requireContext())
                container.addView(
                    chart,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                // Set up base chart appearance
                chart.apply {
                    description.isEnabled = false
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    legend.textColor = Color.WHITE
                    legend.isEnabled = false

                    setTouchEnabled(true)
                    setPinchZoom(false)
                    setScaleEnabled(false)
                    setDrawGridBackground(false)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        textColor = Color.WHITE
                        setDrawGridLines(false)
                        granularity = 1f
                        valueFormatter = object : ValueFormatter() {
                            private val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            override fun getFormattedValue(value: Float): String {
                                val i = value.toInt().coerceIn(0, days.lastIndex)
                                return days[i]
                            }
                        }
                    }

                    axisLeft.apply {
                        textColor = Color.WHITE
                        setDrawGridLines(true)
                        gridColor = Color.parseColor("#22FFFFFF")
                        axisMinimum = 0f
                    }
                    axisRight.isEnabled = false
                    setExtraOffsets(8f, 8f, 8f, 8f)
                }

                // Load weekly data (local -> Firestore -> placeholders)
                loadWeeklyData { hoursPerDay ->
                    val entries = hoursPerDay.mapIndexed { index, hours -> Entry(index.toFloat(), hours) }
                    val dataSet = LineDataSet(entries, "Hours slept").apply {
                        color = Color.parseColor("#FFD54F")
                        lineWidth = 2.2f
                        setDrawCircles(true)
                        circleRadius = 3.8f
                        setCircleColor(Color.parseColor("#FFD54F"))
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(true)
                        fillColor = Color.parseColor("#33FFD54F")
                        highLightColor = Color.WHITE
                    }
                    chart.data = LineData(dataSet)
                    chart.invalidate()

                    // Also bind the summary tiles from local (preferred) or derived from 'hoursPerDay'
                    bindWeeklySummary(view, hoursPerDay)

                    // Update weekday chips to match the chart (Mon..Sun)
                    val dayIds = intArrayOf(
                        R.id.hoursMon, R.id.hoursTue, R.id.hoursWed,
                        R.id.hoursThu, R.id.hoursFri, R.id.hoursSat, R.id.hoursSun
                    )
                    for (i in 0 until minOf(dayIds.size, hoursPerDay.size)) {
                        val tv = view.findViewById<android.widget.TextView>(dayIds[i])
                        tv?.text = "${formatHours(hoursPerDay[i])}h"
                    }
                }
            } catch (t: Throwable) {
                // Fallback to a simple placeholder so the screen never crashes
                val tv = android.widget.TextView(requireContext()).apply {
                    text = "Weekly chart unavailable"
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    gravity = android.view.Gravity.CENTER
                }
                container.addView(
                    tv,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
    }

    private fun bindWeeklySummary(root: View, hoursPerDay: List<Float>) {
        // Try detailed local sessions for averages
        val sessions = runCatching { SleepLocalStore.weekSessions(requireContext()) }.getOrNull().orEmpty()

        val avgDurationMin: Float
        val avgBedMinutes: Float
        val avgWakeMinutes: Float

        if (sessions.isNotEmpty()) {
            avgDurationMin = (sessions.map { it.durationMin }.average()).toFloat()
            avgBedMinutes = (sessions.map { it.bedHour * 60 + it.bedMinute }.average()).toFloat()
            avgWakeMinutes = (sessions.map { it.wakeHour * 60 + it.wakeMinute }.average()).toFloat()
        } else {
            // Derive from hoursPerDay as a fallback (no bed/wake specifics)
            val nonZero = hoursPerDay.filter { it > 0f }
            avgDurationMin = if (nonZero.isNotEmpty()) nonZero.average().toFloat() * 60f else 0f
            avgBedMinutes = Float.NaN
            avgWakeMinutes = Float.NaN
        }

        fun mmToTimeStr(totalMin: Float): String {
            if (totalMin.isNaN()) return "--:--"
            val m = totalMin.toInt().coerceAtLeast(0) % (24 * 60)
            val h = m / 60
            val min = m % 60
            return String.format("%02d.%02d", h, min)
        }

        // Summary views
        val timeInBed = root.findViewById<android.widget.TextView>(R.id.timeInBed)
        val timeAsleep = root.findViewById<android.widget.TextView>(R.id.timeAsleep)
        val wakeTime = root.findViewById<android.widget.TextView>(R.id.wakeTime)
        val quality = root.findViewById<android.widget.TextView>(R.id.quality)

        // Time asleep (duration)
        if (timeAsleep != null) {
            val h = (avgDurationMin / 60f).toInt()
            val m = (avgDurationMin % 60f).toInt()
            timeAsleep.text = if (avgDurationMin > 0) String.format("%dhr %02dm", h, m) else "--"
        }

        // Time in bed (use average bed and wake)
        if (timeInBed != null) {
            if (!avgBedMinutes.isNaN() && !avgWakeMinutes.isNaN()) {
                timeInBed.text = mmToTimeStr(avgBedMinutes) + " PM - " + mmToTimeStr(avgWakeMinutes) + " AM"
            } else {
                timeInBed.text = "--"
            }
        }

        // Wake time
        if (wakeTime != null) {
            wakeTime.text = if (!avgWakeMinutes.isNaN()) mmToTimeStr(avgWakeMinutes) else "--:--"
        }

        // Simple quality metric: target 8h -> 100
        if (quality != null) {
            val qualityScore = if (avgDurationMin > 0) ((avgDurationMin / (8f * 60f)) * 100f).coerceIn(0f, 100f) else 0f
            quality.text = qualityScore.toInt().toString()
        }
    }

    /**
     * Reads last 7 days sleep sessions from Firestore and returns hours slept per day (Mon..Sun).
     * Expected collection per user: users/{uid}/sleepSessions with fields:
     * - startTime: Timestamp
     * - endTime: Timestamp
     * - durationMinutes: Long (optional, used if present)
     */
    private fun loadWeeklyData(callback: (List<Float>) -> Unit) {
        // 1) Try local SQLite first (works offline)
        runCatching {
            val local = SleepLocalStore.hoursThisWeek(requireContext())
            if (local.any { it > 0f }) {
                callback(local)
                return
            }
        }

        val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            // Not signed in -> return placeholders
            callback(defaultWeeklyPlaceholder())
            return
        }

        val db = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
        if (db == null) {
            callback(defaultWeeklyPlaceholder())
            return
        }

        // Compute start of current week (Mon) and now
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val weekStart = cal.time
        val now = Calendar.getInstance().time

        db.collection("users")
            .document(uid)
            .collection("sleepSessions")
            .whereGreaterThanOrEqualTo("startTime", Timestamp(weekStart))
            .whereLessThanOrEqualTo("startTime", Timestamp(now))
            .orderBy("startTime", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { qs ->
                val hours = FloatArray(7) { 0f }
                qs.documents.forEach { doc ->
                    val durationMin = (doc.getLong("durationMinutes") ?: run {
                        val start = (doc.getTimestamp("startTime") ?: Timestamp.now()).toDate().time
                        val end = (doc.getTimestamp("endTime") ?: Timestamp.now()).toDate().time
                        TimeUnit.MILLISECONDS.toMinutes((end - start).coerceAtLeast(0))
                    }).toFloat()

                    val startTs = doc.getTimestamp("startTime") ?: Timestamp.now()
                    val dayCal = Calendar.getInstance().apply { time = startTs.toDate() }
                    // Map Calendar.MONDAY..SUNDAY -> index 0..6
                    val idx = ((dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7).coerceIn(0, 6)
                    hours[idx] += (durationMin / 60f)
                }
                callback(hours.toList())
            }
            .addOnFailureListener {
                callback(defaultWeeklyPlaceholder())
            }
    }

    private fun defaultWeeklyPlaceholder(): List<Float> = listOf(6f, 5f, 3f, 5f, 6f, 2f, 6f)
}

private fun formatHours(h: Float): String {
    if (h <= 0f) return "0"
    val whole = h.toInt()
    return if (kotlin.math.abs(h - whole) < 0.05f) whole.toString()
    else String.format(Locale.getDefault(), "%.1f", h)
}
