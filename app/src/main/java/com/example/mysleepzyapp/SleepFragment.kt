package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SleepFragment : Fragment() {

    private var bedHour = 22
    private var bedMinute = 0
    private var wakeHour = 6
    private var wakeMinute = 0

    private lateinit var dialContainer: FrameLayout
    private lateinit var dialView: SleepDialView
    private lateinit var tvBed: TextView
    private lateinit var tvWake: TextView
    private lateinit var goalRight: TextView
    private lateinit var repeatRight: TextView
    private lateinit var soundRight: TextView

    private lateinit var pickBedtimeLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickWakeupLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            bedHour = savedInstanceState.getInt(KEY_BED_HOUR, bedHour)
            bedMinute = savedInstanceState.getInt(KEY_BED_MINUTE, bedMinute)
            wakeHour = savedInstanceState.getInt(KEY_WAKE_HOUR, wakeHour)
            wakeMinute = savedInstanceState.getInt(KEY_WAKE_MINUTE, wakeMinute)
        }

        pickBedtimeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                bedHour = result.data!!.getIntExtra(SetBedtimeActivity.EXTRA_HOUR, bedHour)
                bedMinute = result.data!!.getIntExtra(SetBedtimeActivity.EXTRA_MINUTE, bedMinute)
                updateUi()
            }
        }
        pickWakeupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                wakeHour = result.data!!.getIntExtra(SetWakeupActivity.EXTRA_HOUR, wakeHour)
                wakeMinute = result.data!!.getIntExtra(SetWakeupActivity.EXTRA_MINUTE, wakeMinute)
                updateUi()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_sleep, container, false)
        dialContainer = v.findViewById(R.id.sleepDialContainer)
        tvBed = v.findViewById(R.id.btnSetBedtime)
        tvWake = v.findViewById(R.id.btnSetWakeup)
        goalRight = v.findViewById(R.id.goalRight)
        repeatRight = v.findViewById(R.id.repeatRight)
        soundRight = v.findViewById(R.id.soundRight)

        // add custom view
        dialView = SleepDialView(requireContext())
        dialContainer.addView(
            dialView,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        v.findViewById<View>(R.id.bedtimeCard).setOnClickListener {
            val i = Intent(requireContext(), SetBedtimeActivity::class.java).apply {
                putExtra(SetBedtimeActivity.EXTRA_HOUR, bedHour)
                putExtra(SetBedtimeActivity.EXTRA_MINUTE, bedMinute)
            }
            pickBedtimeLauncher.launch(i)
        }
        v.findViewById<View>(R.id.alarmCard).setOnClickListener {
            val i = Intent(requireContext(), SetWakeupActivity::class.java).apply {
                putExtra(SetWakeupActivity.EXTRA_HOUR, wakeHour)
                putExtra(SetWakeupActivity.EXTRA_MINUTE, wakeMinute)
            }
            pickWakeupLauncher.launch(i)
        }

        // Preset pickers for goals, repeat, and sound
        v.findViewById<View>(R.id.rowGoals).setOnClickListener {
            val items = resources.getStringArray(R.array.sleep_goal_options)
            val current = goalRight.text?.toString()
            val idx = items.indexOfFirst { it.equals(current, true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sleep goal")
                .setSingleChoiceItems(items, idx) { dialog, which ->
                    goalRight.text = items[which]
                    savePrefs()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        v.findViewById<View>(R.id.rowRepeat).setOnClickListener {
            val items = resources.getStringArray(R.array.repeat_options)
            val current = repeatRight.text?.toString()
            val idx = items.indexOfFirst { it.equals(current, true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Repeat")
                .setSingleChoiceItems(items, idx) { dialog, which ->
                    repeatRight.text = items[which]
                    savePrefs()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        v.findViewById<View>(R.id.rowSound).setOnClickListener {
            val items = resources.getStringArray(R.array.alarm_sound_options)
            val current = soundRight.text?.toString()
            val idx = items.indexOfFirst { it.equals(current, true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alarm sound")
                .setSingleChoiceItems(items, idx) { dialog, which ->
                    soundRight.text = items[which]
                    savePrefs()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Load saved preferences for right-side labels
        loadPrefs(v)

        updateUi()
        return v
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_BED_HOUR, bedHour)
        outState.putInt(KEY_BED_MINUTE, bedMinute)
        outState.putInt(KEY_WAKE_HOUR, wakeHour)
        outState.putInt(KEY_WAKE_MINUTE, wakeMinute)
    }

    private fun updateUi() {
        tvBed.text = formatTime(bedHour, bedMinute)
        tvWake.text = formatTime(wakeHour, wakeMinute)
        dialView.setTimes(bedHour, bedMinute, wakeHour, wakeMinute)
    }

    private fun formatTime(h: Int, m: Int): String = String.format("%02d : %02d", h, m)

    private fun loadPrefs(root: View) {
        val sp = requireContext().getSharedPreferences("sleep_prefs", android.content.Context.MODE_PRIVATE)
        goalRight.text = sp.getString("goal", goalRight.text?.toString() ?: "Get up early")
        repeatRight.text = sp.getString("repeat", repeatRight.text?.toString() ?: "Every day")
        soundRight.text = sp.getString("sound", soundRight.text?.toString() ?: "Good Morning")
    }

    private fun savePrefs() {
        val sp = requireContext().getSharedPreferences("sleep_prefs", android.content.Context.MODE_PRIVATE)
        sp.edit()
            .putString("goal", goalRight.text?.toString())
            .putString("repeat", repeatRight.text?.toString())
            .putString("sound", soundRight.text?.toString())
            .apply()
    }

    companion object {
        private const val KEY_BED_HOUR = "bedHour"
        private const val KEY_BED_MINUTE = "bedMinute"
        private const val KEY_WAKE_HOUR = "wakeHour"
        private const val KEY_WAKE_MINUTE = "wakeMinute"
    }
}
