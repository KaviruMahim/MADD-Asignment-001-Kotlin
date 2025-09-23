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

        // Back arrow returns to previous screen
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }

        // Continue button (later go to dashboard or next feature)
        findViewById<Button>(R.id.continueButton).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }
}
