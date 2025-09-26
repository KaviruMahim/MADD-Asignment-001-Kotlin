package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ForthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forth)

        // Back arrow returns to previous screen (guard in case it's not present in this layout)
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            finish()
        }

        // Continue button -> go to Register/Login choice screen
        findViewById<Button>(R.id.continueButton).setOnClickListener {
            val intent = Intent(this, RegisterOrLoginPageActivity::class.java)
            startActivity(intent)
        }
    }
}
