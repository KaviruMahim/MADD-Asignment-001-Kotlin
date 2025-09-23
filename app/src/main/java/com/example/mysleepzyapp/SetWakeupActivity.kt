package com.example.mysleepzyapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

class SetWakeupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_wakeup)

        val picker: TimePicker = findViewById(R.id.wakeupPicker)
        val save: Button = findViewById(R.id.saveWakeup)
        val back: ImageView? = findViewById(R.id.backArrow)

        picker.setIs24HourView(true)

        val initHour = intent.getIntExtra(EXTRA_HOUR, 6)
        val initMinute = intent.getIntExtra(EXTRA_MINUTE, 0)
        setPickerTime(picker, initHour, initMinute)

        save.setOnClickListener {
            val (h, m) = getPickerTime(picker)
            setResult(RESULT_OK, Intent().apply {
                putExtra(EXTRA_HOUR, h)
                putExtra(EXTRA_MINUTE, m)
            })
            finish()
        }

        back?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setPickerTime(tp: TimePicker, hour: Int, minute: Int) {
        if (Build.VERSION.SDK_INT >= 23) {
            tp.hour = hour
            tp.minute = minute
        } else {
            @Suppress("DEPRECATION")
            run {
                tp.currentHour = hour
                tp.currentMinute = minute
            }
        }
    }

    private fun getPickerTime(tp: TimePicker): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= 23) {
            tp.hour to tp.minute
        } else {
            @Suppress("DEPRECATION")
            (tp.currentHour ?: 0) to (tp.currentMinute ?: 0)
        }
    }

    companion object {
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }
}
