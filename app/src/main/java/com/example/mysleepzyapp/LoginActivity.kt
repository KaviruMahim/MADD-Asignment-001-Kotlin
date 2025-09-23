package com.example.mysleepzyapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<ImageView>(R.id.backArrow).setOnClickListener { finish() }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            // TODO: validate and go to home
            Toast.makeText(this, "Logging in…", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.registerNow).setOnClickListener {
            // TODO: navigate to Register screen
            Toast.makeText(this, "Go to Register", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.forgotPassword).setOnClickListener {
            // TODO: navigate to Forgot Password
            Toast.makeText(this, "Forgot Password", Toast.LENGTH_SHORT).show()
        }
    }
}
