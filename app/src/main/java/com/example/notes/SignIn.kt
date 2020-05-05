package com.example.notes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.sign_in.*
import java.io.Console


class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    fun onClickNeedAccount(v: View) {
        startActivity(Intent(this, SignUp::class.java))
    }

    fun onClickSignWithGoogle(v: View) {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account!!.getIdToken(), null)
                auth.signInWithCredential(credential)
                val user = auth.currentUser
                updateUI(user)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                System.out.println("Create note clicked")
            }
        }
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
                    updateUI(null)
                }
            }
    }

    fun updateUI(currentUser: FirebaseUser?) {
        if(currentUser != null) {
            Toast.makeText(baseContext, "Hello!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }
}
