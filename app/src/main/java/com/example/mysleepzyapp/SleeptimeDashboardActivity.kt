package com.example.mysleepzyapp

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SleeptimeDashboardActivity : AppCompatActivity() {

    // Current selections (defaults from your mock)
    private var bedHour = 22
    private var bedMinute = 0
    private var wakeHour = 6
    private var wakeMinute = 10

    private lateinit var tvBedtime: TextView      // was Button → TextView to match XML
    private lateinit var tvWakeup: TextView       // was Button → TextView to match XML
    private lateinit var dialContainer: FrameLayout
    private lateinit var dialView: SleepDialView

    private lateinit var pickBedtimeLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickWakeupLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeptime_dashboard)

        // Restore across rotation
        if (savedInstanceState != null) {
            bedHour = savedInstanceState.getInt(KEY_BED_HOUR, bedHour)
            bedMinute = savedInstanceState.getInt(KEY_BED_MINUTE, bedMinute)
            wakeHour = savedInstanceState.getInt(KEY_WAKE_HOUR, wakeHour)
            wakeMinute = savedInstanceState.getInt(KEY_WAKE_MINUTE, wakeMinute)
        }

        // IDs must exist in XML with these exact names/types
        tvBedtime = findViewById(R.id.btnSetBedtime)
        tvWakeup  = findViewById(R.id.btnSetWakeup)
        dialContainer = findViewById(R.id.sleepDialContainer)  // see XML below

        // Add the pretty circular dial into the container
        dialView = SleepDialView(this)
        dialContainer.removeAllViews()
        dialContainer.addView(
            dialView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Result launchers
        pickBedtimeLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    bedHour = result.data!!.getIntExtra(SetBedtimeActivity.EXTRA_HOUR, bedHour)
                    bedMinute = result.data!!.getIntExtra(SetBedtimeActivity.EXTRA_MINUTE, bedMinute)
                    updateUi()
                }
            }

        pickWakeupLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    wakeHour = result.data!!.getIntExtra(SetWakeupActivity.EXTRA_HOUR, wakeHour)
                    wakeMinute = result.data!!.getIntExtra(SetWakeupActivity.EXTRA_MINUTE, wakeMinute)
                    updateUi()
                }
            }

        // Taps → open pickers
        tvBedtime.setOnClickListener {
            val i = Intent(this, SetBedtimeActivity::class.java).apply {
                putExtra(SetBedtimeActivity.EXTRA_HOUR, bedHour)
                putExtra(SetBedtimeActivity.EXTRA_MINUTE, bedMinute)
            }
            pickBedtimeLauncher.launch(i)
        }

        tvWakeup.setOnClickListener {
            val i = Intent(this, SetWakeupActivity::class.java).apply {
                putExtra(SetWakeupActivity.EXTRA_HOUR, wakeHour)
                putExtra(SetWakeupActivity.EXTRA_MINUTE, wakeMinute)
            }
            pickWakeupLauncher.launch(i)
        }

        updateUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_BED_HOUR, bedHour)
        outState.putInt(KEY_BED_MINUTE, bedMinute)
        outState.putInt(KEY_WAKE_HOUR, wakeHour)
        outState.putInt(KEY_WAKE_MINUTE, wakeMinute)
    }

    private fun updateUi() {
        tvBedtime.text = formatTime(bedHour, bedMinute) // keeps your “22 : 00” look
        tvWakeup.text  = formatTime(wakeHour, wakeMinute)
        dialView.setTimes(bedHour, bedMinute, wakeHour, wakeMinute)
    }

    private fun formatTime(h: Int, m: Int): String = String.format("%02d : %02d", h, m)

    companion object {
        private const val KEY_BED_HOUR = "bedHour"
        private const val KEY_BED_MINUTE = "bedMinute"
        private const val KEY_WAKE_HOUR = "wakeHour"
        private const val KEY_WAKE_MINUTE = "wakeMinute"
    }
}

/* ---------- Pretty circular dial view (same file) ---------- */

private class SleepDialView(context: Context) : android.view.View(context) {

    private var bedHour = 22
    private var bedMinute = 0
    private var wakeHour = 6
    private var wakeMinute = 10

    fun setTimes(bedH: Int, bedM: Int, wakeH: Int, wakeM: Int) {
        bedHour = bedH; bedMinute = bedM
        wakeHour = wakeH; wakeMinute = wakeM
        invalidate()
    }

    private val baseRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#E6FFFFFF")
        strokeWidth = dp(16f)
    }
    private val sleepArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#D6CFA3")
        strokeWidth = dp(16f)
    }
    private val innerDialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1A000000")
    }
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#9B8CF5")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textSize = dp(22f)
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCFFFFFF"); textAlign = Paint.Align.CENTER
        textSize = dp(12f)
    }

    private val arcRect = RectF()
    private var radius = 0f
    private var cx = 0f
    private var cy = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = dp(260f).toInt()
        val w = resolveSize(desired, widthMeasureSpec)
        val h = resolveSize(desired, heightMeasureSpec)
        val size = minOf(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cx = w / 2f
        cy = h / 2f
        radius = minOf(w, h) / 2f - dp(16f)
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // inner fill + base
        canvas.drawCircle(cx, cy, radius - dp(12f), innerDialPaint)
        canvas.drawArc(arcRect, 0f, 360f, false, baseRingPaint)

        // sleep arc
        val startDeg = timeToAngle(bedHour, bedMinute)
        val sweepDeg = sweepDegrees(bedHour, bedMinute, wakeHour, wakeMinute)
        canvas.drawArc(arcRect, startDeg, sweepDeg, false, sleepArcPaint)

        // markers
        drawMarker(canvas, startDeg)
        drawMarker(canvas, (startDeg + sweepDeg) % 360f)

        // labels
        val mins = durationMinutes(bedHour, bedMinute, wakeHour, wakeMinute)
        val hh = mins / 60; val mm = mins % 60
        val title = String.format("%dhr %02dm", hh, mm)
        val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(title, cx, baseline - dp(8f), textPaint)
        canvas.drawText("Sleep duration", cx, baseline + dp(18f), subTextPaint)
    }

    private fun drawMarker(c: Canvas, angleDeg: Float) {
        val rad = Math.toRadians(angleDeg.toDouble())
        val x = cx + kotlin.math.cos(rad) * radius
        val y = cy + kotlin.math.sin(rad) * radius
        c.drawCircle(x.toFloat(), y.toFloat(), dp(10f), markerPaint)
        c.drawCircle(x.toFloat(), y.toFloat(), dp(4.5f), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; color = Color.WHITE
        })
    }

    private fun timeToAngle(h: Int, m: Int): Float {
        val total = (h % 24) * 60 + m
        return ((total / (24f * 60f)) * 360f - 90f).mod(360f)
    }

    private fun durationMinutes(bH: Int, bM: Int, wH: Int, wM: Int): Int {
        val bed = bH * 60 + bM
        val wake = wH * 60 + wM
        var diff = wake - bed
        if (diff < 0) diff += 24 * 60
        return diff
    }

    private fun sweepDegrees(bH: Int, bM: Int, wH: Int, wM: Int): Float {
        val mins = durationMinutes(bH, bM, wH, wM)
        return mins / (24f * 60f) * 360f
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
}
