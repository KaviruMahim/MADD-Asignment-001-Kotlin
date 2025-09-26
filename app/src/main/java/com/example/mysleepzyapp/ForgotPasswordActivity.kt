package com.example.mysleepzyapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { finish() }

        findViewById<Button>(R.id.resetButton).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailInput).text?.toString()?.trim().orEmpty()
            if (!isValidEmail(email)) {
                toast("Please enter a valid email")
                return@setOnClickListener
            }
            val btn = findViewById<Button>(R.id.resetButton)
            btn.isEnabled = false
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    btn.isEnabled = true
                    if (task.isSuccessful) {
                        toast("Reset link sent to $email")
                    } else {
                        toast(task.exception?.localizedMessage ?: "Failed to send reset email")
                    }
                }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
