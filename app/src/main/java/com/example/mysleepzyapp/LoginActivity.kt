package com.example.mysleepzyapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Some layouts may not include a back arrow; guard to avoid NPE
        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener { finish() }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailInput).text?.toString()?.trim().orEmpty()
            val password = findViewById<TextInputEditText>(R.id.passwordInput).text?.toString()?.trim().orEmpty()

            if (!isValidEmail(email)) {
                toast("Please enter a valid email")
                return@setOnClickListener
            }
            if (password.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            findViewById<Button>(R.id.loginButton).isEnabled = false
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    findViewById<Button>(R.id.loginButton).isEnabled = true
                    if (task.isSuccessful) {
                        toast("Welcome back!")
                        val i = Intent(this, MainHostActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra(MainHostActivity.EXTRA_SELECTED_TAB, R.id.nav_sleep)
                        startActivity(i)
                    } else {
                        toast(task.exception?.localizedMessage ?: "Login failed")
                    }
                }
        }

        findViewById<TextView>(R.id.registerNow).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView>(R.id.forgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
