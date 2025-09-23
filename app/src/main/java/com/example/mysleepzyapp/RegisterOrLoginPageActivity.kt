package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterOrLoginPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_or_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val guestText = findViewById<TextView>(R.id.guestText)

        loginButton.setOnClickListener {
            Toast.makeText(this, "Login Clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {
            Toast.makeText(this, "Register Clicked", Toast.LENGTH_SHORT).show()

        }

        guestText.setOnClickListener {
            Toast.makeText(this, "Continue as Guest", Toast.LENGTH_SHORT).show()
            // TODO: Go to Home/Dashboard
        }
    }
}
