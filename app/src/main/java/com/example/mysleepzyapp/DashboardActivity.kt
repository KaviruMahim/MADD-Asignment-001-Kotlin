package com.example.mysleepzyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
