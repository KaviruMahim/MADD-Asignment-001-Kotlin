package com.example.mysleepzyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_history)

        val recycler = findViewById<RecyclerView>(R.id.sessionsList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val sessions = SleepLocalStore.weekSessions(this)
        recycler.adapter = SessionsAdapter(sessions)

        findViewById<View>(R.id.backArrow)?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private class SessionsAdapter(
        val data: List<SleepDbHelper.SleepSessionLocal>
    ) : RecyclerView.Adapter<SessionsAdapter.VH>() {
        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val date: TextView = view.findViewById(R.id.itemDate)
            val bed: TextView = view.findViewById(R.id.itemBed)
            val wake: TextView = view.findViewById(R.id.itemWake)
            val duration: TextView = view.findViewById(R.id.itemDuration)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep_session, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val s = data[position]
            holder.date.text = formatDate(Date(s.startMs))
            holder.bed.text = String.format(Locale.getDefault(), "%02d:%02d", s.bedHour, s.bedMinute)
            holder.wake.text = String.format(Locale.getDefault(), "%02d:%02d", s.wakeHour, s.wakeMinute)
            val h = (s.durationMin / 60).toInt()
            val m = (s.durationMin % 60).toInt()
            holder.duration.text = String.format(Locale.getDefault(), "%d hr %02d m", h, m)
        }

        override fun getItemCount(): Int = data.size

        private fun formatDate(d: Date): String {
            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            return sdf.format(d)
        }
    }
}
