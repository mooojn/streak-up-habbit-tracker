package com.example.streakup_habbit_tracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.streakup_habbit_tracker.data.HabitRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class HomeActivity : AppCompatActivity() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInputEditText: TextInputEditText
    
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("HomeActivity", "Google sign in failed", e)
                Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HabitRepository.initialize(applicationContext)  // safety net
        setContentView(R.layout.activity_home)

        nameInputLayout = findViewById(R.id.nameInputLayout)
        nameInputEditText = findViewById(R.id.nameInputEditText)
        val startButton: MaterialButton = findViewById(R.id.startButton)
        val googleSignInButton: MaterialButton = findViewById(R.id.googleSignInButton)
        
        auth = FirebaseAuth.getInstance()
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (auth.currentUser != null) {
            proceedToDashboard(auth.currentUser?.displayName ?: HabitRepository.userName)
            return
        }

        if (HabitRepository.userName.isNotBlank()) {
            nameInputEditText.setText(HabitRepository.userName)
        }

        startButton.setOnClickListener { startDashboard() }
        googleSignInButton.setOnClickListener {
            signIn()
        }
    }
    
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val name = user?.displayName ?: "User"
                    HabitRepository.userName = name
                    proceedToDashboard(name)
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startDashboard() {
        val name = nameInputEditText.text?.toString()?.trim().orEmpty()

        if (name.isBlank()) {
            nameInputLayout.error = getString(R.string.error_name_required)
            return
        }

        nameInputLayout.error = null
        HabitRepository.userName = name
        proceedToDashboard(name)
    }

    private fun proceedToDashboard(name: String) {
        val intent = Intent(this, DashboardActivity::class.java).putExtra(EXTRA_USER_NAME, name)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
    }
}
