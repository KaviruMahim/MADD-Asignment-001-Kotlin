package com.example.mysleepzyapp

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import android.util.TypedValue
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SleeptimeDashboardActivity : AppCompatActivity() {

    // Current selections (match your mocks; you can change defaults)
    private var bedHour = 22
    private var bedMinute = 0
    private var wakeHour = 6
    private var wakeMinute = 0

    private lateinit var bedtimeCard: MaterialCardView
    private lateinit var alarmCard: MaterialCardView
    private lateinit var tvBed: TextView
    private lateinit var tvWake: TextView
    private lateinit var dialContainer: FrameLayout
    private lateinit var dialView: SleepDialView

    private lateinit var rowGoals: MaterialCardView
    private lateinit var rowRepeat: MaterialCardView
    private lateinit var rowSound: MaterialCardView
    private lateinit var goalRight: TextView
    private lateinit var repeatRight: TextView
    private lateinit var soundRight: TextView

    private lateinit var pickBedtimeLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickWakeupLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeptime_dashboard)

        // Back arrow
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Restore rotation state
        if (savedInstanceState != null) {
            bedHour = savedInstanceState.getInt(KEY_BED_HOUR, bedHour)
            bedMinute = savedInstanceState.getInt(KEY_BED_MINUTE, bedMinute)
            wakeHour = savedInstanceState.getInt(KEY_WAKE_HOUR, wakeHour)
            wakeMinute = savedInstanceState.getInt(KEY_WAKE_MINUTE, wakeMinute)
        }

        // ----- find views
        dialContainer = findViewById(R.id.sleepDialContainer)
        bedtimeCard = findViewById(R.id.bedtimeCard)
        alarmCard   = findViewById(R.id.alarmCard)
        tvBed  = findViewById(R.id.btnSetBedtime)
        tvWake = findViewById(R.id.btnSetWakeup)
        rowGoals = findViewById(R.id.rowGoals)
        rowRepeat = findViewById(R.id.rowRepeat)
        rowSound = findViewById(R.id.rowSound)
        goalRight = findViewById(R.id.goalRight)
        repeatRight = findViewById(R.id.repeatRight)
        soundRight = findViewById(R.id.soundRight)
        findViewById<MaterialButton>(R.id.stopButton)?.setOnClickListener {
            saveSleepSession()
        }

        // Bottom nav -> navigate to Profile when tapped
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.nav_sleep
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sleep -> true
                R.id.nav_analytics -> {
                    startActivity(Intent(this, MainHostActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, MindfulTipsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Add the pretty dial once
        dialView = SleepDialView(this)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialContainer.addView(dialView, lp)

        // Launchers for results
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
                    // Auto-save the session once both times are known
                    saveSleepSession()
                }
            }

        // Clickable cards open pickers
        bedtimeCard.setOnClickListener {
            val i = Intent(this, SetBedtimeActivity::class.java).apply {
                putExtra(SetBedtimeActivity.EXTRA_HOUR, bedHour)
                putExtra(SetBedtimeActivity.EXTRA_MINUTE, bedMinute)
            }
            pickBedtimeLauncher.launch(i)
        }
        alarmCard.setOnClickListener {
            val i = Intent(this, SetWakeupActivity::class.java).apply {
                putExtra(SetWakeupActivity.EXTRA_HOUR, wakeHour)
                putExtra(SetWakeupActivity.EXTRA_MINUTE, wakeMinute)
            }
            pickWakeupLauncher.launch(i)
        }

        updateUi()

        // Load saved preferences for goal/repeat/sound
        loadPrefs()

        // Click to choose Sleep Goal from presets
        rowGoals.setOnClickListener {
            val items = resources.getStringArray(R.array.sleep_goal_options)
            val current = goalRight.text?.toString()
            val idx = items.indexOfFirst { it.equals(current, ignoreCase = true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(this)
                .setTitle("Sleep goal")
                .setSingleChoiceItems(items, idx) { dialog, which ->
                    goalRight.text = items[which]
                    savePrefs()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Click to choose Repeat
        rowRepeat.setOnClickListener {
            val items = resources.getStringArray(R.array.repeat_options)
            val current = repeatRight.text?.toString()
            val idx = items.indexOfFirst { it.equals(current, ignoreCase = true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(this)
                .setTitle("Repeat")
                .setSingleChoiceItems(items, idx) { dialog, which ->
                    repeatRight.text = items[which]
                    savePrefs()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Click to choose Alarm Sound
        rowSound.setOnClickListener {
            val items = resources.getStringArray(R.array.alarm_sound_options)
            val current = soundRight.text?.toString()
            val idx = items.indexOfFirst { it.equals(current, ignoreCase = true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(this)
                .setTitle("Alarm sound")
                .setSingleChoiceItems(items, idx) { dialog, which ->
                    soundRight.text = items[which]
                    savePrefs()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_BED_HOUR, bedHour)
        outState.putInt(KEY_BED_MINUTE, bedMinute)
        outState.putInt(KEY_WAKE_HOUR, wakeHour)
        outState.putInt(KEY_WAKE_MINUTE, wakeMinute)
    }

    private fun updateUi() {
        tvBed.text  = formatTime(bedHour, bedMinute)
        tvWake.text = formatTime(wakeHour, wakeMinute)

        // Update dial
        dialView.setTimes(bedHour, bedMinute, wakeHour, wakeMinute)
    }

    private fun formatTime(h: Int, m: Int): String = String.format("%02d : %02d", h, m)

    private fun loadPrefs() {
        val sp = getSharedPreferences("sleep_prefs", MODE_PRIVATE)
        goalRight.text = sp.getString("goal", goalRight.text?.toString() ?: "Get up early")
        repeatRight.text = sp.getString("repeat", repeatRight.text?.toString() ?: "Every day")
        soundRight.text = sp.getString("sound", soundRight.text?.toString() ?: "Good Morning")
    }

    private fun savePrefs() {
        val sp = getSharedPreferences("sleep_prefs", MODE_PRIVATE)
        sp.edit()
            .putString("goal", goalRight.text?.toString())
            .putString("repeat", repeatRight.text?.toString())
            .putString("sound", soundRight.text?.toString())
            .apply()
    }

    // Persist a daily sleep session both locally (SQLite) and remotely (Firestore)
    private fun saveSleepSession() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Sign in to save your sleep session", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance()
        // Determine start and end date/time based on selected bed/wake
        val startCal = cal.clone() as Calendar
        startCal.set(Calendar.HOUR_OF_DAY, bedHour)
        startCal.set(Calendar.MINUTE, bedMinute)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = startCal.clone() as Calendar
        endCal.set(Calendar.HOUR_OF_DAY, wakeHour)
        endCal.set(Calendar.MINUTE, wakeMinute)
        // If wake time is earlier than or equal to bed time, it is next day
        if (endCal.timeInMillis <= startCal.timeInMillis) {
            endCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val start = startCal.time
        val end = endCal.time
        val durationMinutes = ((end.time - start.time) / (60_000L)).coerceAtLeast(0L)

        val dateId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(start)
        val payload = hashMapOf(
            "startTime" to Timestamp(start),
            "endTime" to Timestamp(end),
            "durationMinutes" to durationMinutes,
            "bedHour" to bedHour,
            "bedMinute" to bedMinute,
            "wakeHour" to wakeHour,
            "wakeMinute" to wakeMinute
        )

        // Save locally to SQLite so Analytics can work offline too
        SleepLocalStore.save(
            context = this,
            dateId = dateId,
            start = start,
            end = end,
            durationMin = durationMinutes,
            bedHour = bedHour,
            bedMinute = bedMinute,
            wakeHour = wakeHour,
            wakeMinute = wakeMinute
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("sleepSessions")
            .document(dateId)
            .set(payload)
            .addOnSuccessListener {
                Toast.makeText(this, "Sleep session saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        private const val KEY_BED_HOUR = "bedHour"
        private const val KEY_BED_MINUTE = "bedMinute"
        private const val KEY_WAKE_HOUR = "wakeHour"
        private const val KEY_WAKE_MINUTE = "wakeMinute"
    }
}

/**
 * A lightweight, non-interactive ring that mimics your screenshot:
 * - outer grey ring
 * - colored arc for sleep period (handles indicated by small circles)
 * - center text "Xhr Ym" + "Sleep duration"
 */
class SleepDialView(context: Context) : View(context) {

    private var bedMin = 22 * 60
    private var wakeMin = 6 * 60
    private val ringPaintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // subtle background ring (soft white)
        color = ContextCompat.getColor(context, R.color.white_90)
        style = Paint.Style.STROKE
        strokeWidth = dp(22f)
        strokeCap = Paint.Cap.ROUND
        alpha = 80
    }
    private val ringPaintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // main sleep arc in theme accent
        color = ContextCompat.getColor(context, R.color.accentYellow)
        style = Paint.Style.STROKE
        strokeWidth = dp(22f)
        strokeCap = Paint.Cap.ROUND
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white_70)
        style = Paint.Style.STROKE
        strokeWidth = dp(1.6f)
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.accentYellow)
        style = Paint.Style.FILL
    }
    private val textBig = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textSize = sp(20f)
    }
    private val textSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = sp(12f)
    }

    private val rect = RectF()

    fun setTimes(bedHour: Int, bedMinute: Int, wakeHour: Int, wakeMinute: Int) {
        bedMin = bedHour * 60 + bedMinute
        wakeMin = wakeHour * 60 + wakeMinute
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val r  = (minOf(width, height) / 2f) - dp(16f)
        rect.set(cx - r, cy - r, cx + r, cy + r)

        // Background full ring
        canvas.drawArc(rect, -90f, 360f, false, ringPaintBg)

        // Compute arc start/end (clockwise, -90 is 12 o'clock)
        val startA = minutesToAngle(bedMin)
        val sweep  = durationSweep(bedMin, wakeMin)

        // Sleep arc
        canvas.drawArc(rect, startA, sweep, false, ringPaintArc)

        // 12/3/6/9 faint ticks (optional)
        drawTicks(canvas, cx, cy, r)

        // Little circular handles at both ends
        val startPt = pointOnCircle(cx, cy, r, startA)
        val endPt   = pointOnCircle(cx, cy, r, (startA + sweep))
        canvas.drawCircle(startPt.x, startPt.y, dp(10f), handlePaint)
        canvas.drawCircle(endPt.x,   endPt.y,   dp(10f), handlePaint)

        // Center text
        val total = diffMinutes(bedMin, wakeMin)
        val h = total / 60
        val m = total % 60
        val big = String.format("%dhr %02dm", h, m)
        canvas.drawText(big, cx, cy - dp(6f), textBig)
        canvas.drawText("Sleep duration", cx, cy + dp(14f), textSmall)
    }

    private fun drawTicks(c: Canvas, cx: Float, cy: Float, r: Float) {
        val angles = floatArrayOf(-90f, 0f, 90f, 180f)
        angles.forEach { a ->
            val p1 = pointOnCircle(cx, cy, r - dp(10f), a)
            val p2 = pointOnCircle(cx, cy, r - dp(2f), a)
            c.drawLine(p1.x, p1.y, p2.x, p2.y, tickPaint)
        }
    }

    private fun minutesToAngle(min: Int): Float {
        val frac = (min % (24 * 60)) / (24f * 60f) // 0..1 over 24h
        return -90f + (frac * 360f)
    }

    private fun durationSweep(startMin: Int, endMin: Int): Float {
        val mins = diffMinutes(startMin, endMin)
        return (mins / (24f * 60f)) * 360f
    }

    private fun diffMinutes(start: Int, end: Int): Int {
        var d = end - start
        if (d < 0) d += 24 * 60
        return d
    }

    private fun pointOnCircle(cx: Float, cy: Float, r: Float, angleDeg: Float): PointF {
        val rad = Math.toRadians(angleDeg.toDouble())
        return PointF(
            (cx + r * Math.cos(rad)).toFloat(),
            (cy + r * Math.sin(rad)).toFloat()
        )
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)
}
