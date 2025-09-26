package com.example.mysleepzyapp

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set bottom nav selected to Profile and handle navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = R.id.nav_profile
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> true
                R.id.nav_analytics -> {
                    startActivity(Intent(this, MainHostActivity::class.java))
                    true
                }
                R.id.nav_sleep -> {
                    startActivity(Intent(this, SleeptimeDashboardActivity::class.java))
                    true
                }
                R.id.nav_tips -> {
                    startActivity(Intent(this, MindfulTipsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Load display name from Firebase (if available)
        val displayName = FirebaseAuth.getInstance().currentUser?.displayName
        if (!displayName.isNullOrBlank()) {
            findViewById<TextView>(R.id.name)?.text = displayName
        }
    }
}
