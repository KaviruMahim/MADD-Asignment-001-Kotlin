package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // Back arrow returns to previous screen (guard in case it's not in this layout)
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            finish()
        }

        // Continue button (later go to dashboard or next feature)
        findViewById<Button>(R.id.continueButton).setOnClickListener {
            val intent = Intent(this, ForthActivity::class.java)
            startActivity(intent)
        }
    }
}
