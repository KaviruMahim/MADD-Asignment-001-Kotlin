package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MindfulTipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mindful_tips)

        // NEXT goes to page 2
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            startActivity(Intent(this, MindfulTipsActivityTwo::class.java))
        }

        // Optional back arrow behavior on every page:
        findViewById<android.widget.ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
