package com.example.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.sign_in.*
import kotlinx.android.synthetic.main.sign_up.*

class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        auth = FirebaseAuth.getInstance()
    }

    fun onClickNeedAccount(v: View) {
        startActivity(Intent(this, SignUp::class.java))
    }

    fun onClickSignInButton(v: View) {
        if (signInEmail.text.toString().isEmpty()) {
            signInEmail.error = "Please enter email"
            signInEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(signInEmail.text.toString()).matches()) {
            signInEmail.error = "Please enter valid email"
            signInEmail.requestFocus()
            return
        }

        if (signInPassword.text.toString().isEmpty()) {
            signInPassword.error = "Please enter password"
            signInPassword.requestFocus()
            return
        }

        if (signInPassword.length() < 6) {
            signInPassword.error = "Short password, at least 6 characters"
            signInPassword.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(signInEmail.text.toString(), signInPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
