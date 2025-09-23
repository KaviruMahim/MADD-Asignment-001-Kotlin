package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MindfulTipsActivityTwo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mindful_tips_two)  // make sure the XML name matches

        // NEXT goes to page 3
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            startActivity(Intent(this, MindfulTipsActivityThree::class.java))
        }

        findViewById<android.widget.ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
