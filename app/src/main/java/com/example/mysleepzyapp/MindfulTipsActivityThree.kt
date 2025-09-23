package com.example.mysleepzyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MindfulTipsActivityThree : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mindful_tips_three)
        findViewById<android.widget.ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
