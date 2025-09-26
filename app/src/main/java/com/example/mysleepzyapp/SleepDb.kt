package com.example.mysleepzyapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

private const val DB_NAME = "sleepzy.db"
private const val DB_VERSION = 1

private const val TABLE_SESSIONS = "sleep_sessions"
private const val COL_DATE_ID = "date_id"                 // yyyy-MM-dd (local)
private const val COL_START_MS = "start_ms"
private const val COL_END_MS = "end_ms"
private const val COL_DURATION_MIN = "duration_min"
private const val COL_BED_HOUR = "bed_hour"
private const val COL_BED_MINUTE = "bed_minute"
private const val COL_WAKE_HOUR = "wake_hour"
private const val COL_WAKE_MINUTE = "wake_minute"

class SleepDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_SESSIONS (
                $COL_DATE_ID TEXT PRIMARY KEY,
                $COL_START_MS INTEGER NOT NULL,
                $COL_END_MS INTEGER NOT NULL,
                $COL_DURATION_MIN INTEGER NOT NULL,
                $COL_BED_HOUR INTEGER NOT NULL,
                $COL_BED_MINUTE INTEGER NOT NULL,
                $COL_WAKE_HOUR INTEGER NOT NULL,
                $COL_WAKE_MINUTE INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For now, simple drop + recreate if schema changes
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSIONS")
        onCreate(db)
    }

    fun saveSession(
        dateId: String,
        startMs: Long,
        endMs: Long,
        durationMin: Long,
        bedHour: Int,
        bedMinute: Int,
        wakeHour: Int,
        wakeMinute: Int
    ) {
        val cv = ContentValues().apply {
            put(COL_DATE_ID, dateId)
            put(COL_START_MS, startMs)
            put(COL_END_MS, endMs)
            put(COL_DURATION_MIN, durationMin)
            put(COL_BED_HOUR, bedHour)
            put(COL_BED_MINUTE, bedMinute)
            put(COL_WAKE_HOUR, wakeHour)
            put(COL_WAKE_MINUTE, wakeMinute)
        }
        // Replace if same dateId exists
        writableDatabase.insertWithOnConflict(
            TABLE_SESSIONS,
            null,
            cv,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /**
     * Returns hours slept per day for current week (Mon..Sun) as 7 floats.
     */
    fun getHoursThisWeek(): List<Float> {
        val hours = FloatArray(7) { 0f }

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
        val weekStartMs = cal.timeInMillis
        val nowMs = System.currentTimeMillis()

        val cursor = readableDatabase.query(
            TABLE_SESSIONS,
            arrayOf(COL_START_MS, COL_END_MS, COL_DURATION_MIN),
            "$COL_START_MS >= ? AND $COL_START_MS <= ?",
            arrayOf(weekStartMs.toString(), nowMs.toString()),
            null,
            null,
            "$COL_START_MS ASC"
        )

        cursor.use { c ->
            val calTmp = Calendar.getInstance()
            val idxStart = c.getColumnIndexOrThrow(COL_START_MS)
            val idxDuration = c.getColumnIndexOrThrow(COL_DURATION_MIN)
            while (c.moveToNext()) {
                val start = c.getLong(idxStart)
                val durationMin = c.getLong(idxDuration)
                calTmp.timeInMillis = start
                val weekdayIdx = ((calTmp.get(Calendar.DAY_OF_WEEK) + 5) % 7).coerceIn(0, 6)
                hours[weekdayIdx] += (durationMin.toFloat() / 60f)
            }
        }
        return hours.toList()
    }

    data class SleepSessionLocal(
        val startMs: Long,
        val endMs: Long,
        val durationMin: Long,
        val bedHour: Int,
        val bedMinute: Int,
        val wakeHour: Int,
        val wakeMinute: Int
    )

    fun getWeekSessions(): List<SleepSessionLocal> {
        val out = mutableListOf<SleepSessionLocal>()
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
        val weekStartMs = cal.timeInMillis
        val nowMs = System.currentTimeMillis()

        val cursor = readableDatabase.query(
            TABLE_SESSIONS,
            arrayOf(
                COL_START_MS,
                COL_END_MS,
                COL_DURATION_MIN,
                COL_BED_HOUR,
                COL_BED_MINUTE,
                COL_WAKE_HOUR,
                COL_WAKE_MINUTE
            ),
            "$COL_START_MS >= ? AND $COL_START_MS <= ?",
            arrayOf(weekStartMs.toString(), nowMs.toString()),
            null,
            null,
            "$COL_START_MS ASC"
        )
        cursor.use { c ->
            val idxStart = c.getColumnIndexOrThrow(COL_START_MS)
            val idxEnd = c.getColumnIndexOrThrow(COL_END_MS)
            val idxDur = c.getColumnIndexOrThrow(COL_DURATION_MIN)
            val idxBH = c.getColumnIndexOrThrow(COL_BED_HOUR)
            val idxBM = c.getColumnIndexOrThrow(COL_BED_MINUTE)
            val idxWH = c.getColumnIndexOrThrow(COL_WAKE_HOUR)
            val idxWM = c.getColumnIndexOrThrow(COL_WAKE_MINUTE)
            while (c.moveToNext()) {
                out.add(
                    SleepSessionLocal(
                        startMs = c.getLong(idxStart),
                        endMs = c.getLong(idxEnd),
                        durationMin = c.getLong(idxDur),
                        bedHour = c.getInt(idxBH),
                        bedMinute = c.getInt(idxBM),
                        wakeHour = c.getInt(idxWH),
                        wakeMinute = c.getInt(idxWM)
                    )
                )
            }
        }
        return out
    }
}

object SleepLocalStore {
    /**
     * Seed current week with sample data up to yesterday if there are no sessions yet.
     * Today is intentionally left empty so real data saved by the app will start from today.
     */
    fun seedWeekIfEmpty(context: Context) {
        val helper = SleepDbHelper(context.applicationContext)
        val existing = helper.getWeekSessions()
        if (existing.isNotEmpty()) return

        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Move to Monday of this week
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        val today = Calendar.getInstance()

        // Sample hours for Mon..Sun
        val sampleHours = floatArrayOf(6f, 5f, 7f, 6.5f, 8f, 4.5f, 7f)
        for (i in 0..6) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, i)
            // skip today and future
            if (dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                break
            }

            // Bedtime 22:00
            val bedHour = 22
            val bedMinute = 0
            val start = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, bedHour)
                set(Calendar.MINUTE, bedMinute)
            }

            // Compute wake time from hours slept, across midnight
            val durationMin = (sampleHours[i] * 60).toLong()
            val wake = (start.clone() as Calendar).apply { add(Calendar.MINUTE, durationMin.toInt()) }
            val wakeHour = wake.get(Calendar.HOUR_OF_DAY)
            val wakeMinute = wake.get(Calendar.MINUTE)

            val dateId = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(start.time)
            helper.saveSession(
                dateId = dateId,
                startMs = start.timeInMillis,
                endMs = wake.timeInMillis,
                durationMin = durationMin,
                bedHour = bedHour,
                bedMinute = bedMinute,
                wakeHour = wakeHour,
                wakeMinute = wakeMinute
            )
        }
    }
    fun save(
        context: Context,
        dateId: String,
        start: Date,
        end: Date,
        durationMin: Long,
        bedHour: Int,
        bedMinute: Int,
        wakeHour: Int,
        wakeMinute: Int
    ) {
        SleepDbHelper(context.applicationContext).saveSession(
            dateId,
            start.time,
            end.time,
            durationMin,
            bedHour,
            bedMinute,
            wakeHour,
            wakeMinute
        )
    }

    fun hoursThisWeek(context: Context): List<Float> =
        SleepDbHelper(context.applicationContext).getHoursThisWeek()

    fun weekSessions(context: Context): List<SleepDbHelper.SleepSessionLocal> =
        SleepDbHelper(context.applicationContext).getWeekSessions()
}
