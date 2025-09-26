package com.example.mysleepzyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // Some layouts may not include a back arrow; guard to avoid NPE
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener { finish() }

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.nameInput).text?.toString()?.trim().orEmpty()
            val email = findViewById<TextInputEditText>(R.id.emailInput).text?.toString()?.trim().orEmpty()
            val password = findViewById<TextInputEditText>(R.id.passwordInput).text?.toString()?.trim().orEmpty()
            val confirm = findViewById<TextInputEditText>(R.id.confirmPasswordInput).text?.toString()?.trim().orEmpty()

            if (name.length < 2) {
                toast("Please enter your full name")
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                toast("Please enter a valid email")
                return@setOnClickListener
            }
            if (password.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }
            if (password != confirm) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            val btn = findViewById<Button>(R.id.registerButton)
            btn.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set display name
                        val profile = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        auth.currentUser?.updateProfile(profile)?.addOnCompleteListener {
                            // Navigate to dashboard regardless of profile update result
                            toast("Account created")
                            val i = Intent(this, MainHostActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(i)
                        }
                    } else {
                        btn.isEnabled = true
                        toast(task.exception?.localizedMessage ?: "Registration failed")
                    }
                }
        }

        findViewById<TextView>(R.id.loginNow).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
