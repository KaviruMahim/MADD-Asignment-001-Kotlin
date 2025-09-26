package com.example.mysleepzyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AccountInfoActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)

        val back = findViewById<ImageView>(R.id.backArrow)
        val nameEt = findViewById<EditText>(R.id.etName)
        val ageEt = findViewById<EditText>(R.id.etAge)
        val saveBtn = findViewById<Button>(R.id.btnSave)

        back?.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Prefill values from Auth/Firestore
        val user = auth.currentUser
        if (user != null) {
            if (!user.displayName.isNullOrBlank()) {
                nameEt.setText(user.displayName)
            }
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val age = doc.getLong("age")?.toInt()
                    if (age != null && age > 0) {
                        ageEt.setText(age.toString())
                    }
                    val name = doc.getString("name")
                    if (!name.isNullOrBlank() && nameEt.text.isBlank()) {
                        nameEt.setText(name)
                    }
                }
        }

        saveBtn?.setOnClickListener {
            val name = nameEt.text?.toString()?.trim().orEmpty()
            val ageText = ageEt.text?.toString()?.trim().orEmpty()
            val age = ageText.toIntOrNull()

            if (name.isBlank()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (age == null || age <= 0) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val u = auth.currentUser
            if (u == null) {
                Toast.makeText(this, "You need to be signed in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update FirebaseAuth displayName
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            u.updateProfile(updates)
                .addOnSuccessListener {
                    // Save to Firestore
                    val data = mapOf(
                        "name" to name,
                        "age" to age
                    )
                    firestore.collection("users").document(u.uid)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account info updated", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
