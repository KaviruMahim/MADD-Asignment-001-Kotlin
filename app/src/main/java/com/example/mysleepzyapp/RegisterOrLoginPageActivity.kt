package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class RegisterOrLoginPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_or_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val guestText = findViewById<TextView>(R.id.guestText)
        val back = findViewById<ImageView>(R.id.backArrow)

        back?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        loginButton.setOnClickListener {
            Toast.makeText(this, "Login Clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {
            // Navigate to Register screen
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        guestText.setOnClickListener {
            // Continue without authentication
            val i = Intent(this, SleeptimeDashboardActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(i)
        }
    }
}
