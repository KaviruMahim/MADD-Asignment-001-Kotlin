package com.example.mysleepzyapp

import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // Begin welcome flow sequence: Main -> Second -> Third -> Forth -> RegisterOrLogin
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }
}