package com.example.mysleepzyapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException

class ProfileFragment : Fragment() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var avatarView: ImageView? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadAvatar(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        avatarView = view.findViewById(R.id.avatar)

        // Show signed-in user's display name if available
        val currentUser = auth.currentUser
        val nameText = view.findViewById<TextView>(R.id.name)
        val display = currentUser?.displayName?.takeIf { it.isNotBlank() }
        if (display != null) {
            nameText?.text = display
        }

        // Load existing avatar if available (Auth photoUrl > Firestore photoUrl)
        val authPhoto = currentUser?.photoUrl
        if (authPhoto != null) {
            Glide.with(requireContext())
                .load(authPhoto)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(avatarView!!)
        } else if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    val url = doc.getString("photoUrl")
                    if (!url.isNullOrBlank()) {
                        Glide.with(requireContext())
                            .load(url)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(avatarView!!)
                    }
                }
        }

        // Sign out flow -> go to initial welcome page
        view.findViewById<Button>(R.id.btnSignOut)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val ctx = requireContext()
            val intent = Intent(ctx, RegisterOrLoginPageActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
        }

        // Edit avatar -> image picker
        view.findViewById<View>(R.id.editAvatar)?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Open My Trophies screen
        view.findViewById<View>(R.id.trophiesRow)?.setOnClickListener {
            startActivity(Intent(requireContext(), TrophiesActivity::class.java))
        }

        // Open Account Information screen
        view.findViewById<View>(R.id.accountInfoRow)?.setOnClickListener {
            startActivity(Intent(requireContext(), AccountInfoActivity::class.java))
        }

        // Terms of Service dialog
        view.findViewById<View>(R.id.termsRow)?.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Terms of Service")
                .setMessage(
                    "By using Sleepzy, you agree to:\n\n" +
                    "• Use the app for personal, non-commercial purposes.\n" +
                    "• Accept that insights are informational and not medical advice.\n" +
                    "• Allow anonymous analytics that help improve app quality.\n\n" +
                    "We may update these terms to enhance safety and features."
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // Privacy Policy dialog
        view.findViewById<View>(R.id.privacyRow)?.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage(
                    "Your privacy matters. We:\n\n" +
                    "• Store your account securely with Firebase Authentication.\n" +
                    "• Keep sleep entries and preferences tied to your account.\n" +
                    "• Never sell your personal data.\n\n" +
                    "You can delete your account to remove your data from our systems."
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = auth.currentUser
        // Refresh name
        view?.findViewById<TextView>(R.id.name)?.let { tv ->
            val display = currentUser?.displayName?.takeIf { it.isNotBlank() }
            if (display != null) tv.text = display
        }
        // Refresh avatar
        val authPhoto = currentUser?.photoUrl
        if (authPhoto != null) {
            Glide.with(requireContext())
                .load(authPhoto)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(avatarView!!)
        } else if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    val url = doc.getString("photoUrl")
                    if (!url.isNullOrBlank()) {
                        Glide.with(requireContext())
                            .load(url)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(avatarView!!)
                    }
                }
        }
    }

    private fun uploadAvatar(uri: Uri) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "You need to be signed in", Toast.LENGTH_SHORT).show()
            return
        }

        // Optimistic local preview while uploading
        Glide.with(requireContext()).load(uri).into(avatarView!!)

        val ref = storage.reference.child("users/${user.uid}/profile.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Update FirebaseAuth profile
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()
                    user.updateProfile(profileUpdates)

                    // Save to Firestore for app data
                    val data = mapOf("photoUrl" to downloadUri.toString())
                    firestore.collection("users").document(user.uid).set(data, com.google.firebase.firestore.SetOptions.merge())

                    // Update UI
                    Glide.with(requireContext())
                        .load(downloadUri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(avatarView!!)
                    Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                val msg = if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    "Selected file not found. Please choose a different photo."
                } else {
                    "Upload failed: ${e.localizedMessage}"
                }
                // Revert to auth/placeholder image
                val fallback = auth.currentUser?.photoUrl
                if (fallback != null) {
                    Glide.with(requireContext()).load(fallback).error(R.drawable.ic_profile).into(avatarView!!)
                } else {
                    avatarView?.setImageResource(R.drawable.ic_profile)
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
    }
}
