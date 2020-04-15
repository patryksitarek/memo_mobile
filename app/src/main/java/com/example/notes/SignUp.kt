package com.example.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.sign_in.signInPassword
import kotlinx.android.synthetic.main.sign_up.*

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        auth = FirebaseAuth.getInstance()
    }

    fun onClickSignUpButton(v: View) {

        if (signUpUsername.text.toString().isEmpty()) {
            signUpUsername.error = "Please enter username"
            signUpUsername.requestFocus()
            return
        }

        if (signUpEmail.text.toString().isEmpty()) {
            signUpEmail.error = "Please enter e-mail"
            signUpEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(signUpEmail.text.toString()).matches()) {
            signUpEmail.error = "Please enter valid email"
            signUpEmail.requestFocus()
            return
        }

        if (signUpEmail.text.toString() != signUpEmailRepeat.text.toString()) {
            signUpEmailRepeat.error = "E-mail wrongly repeated"
            signUpEmailRepeat.requestFocus()
            return
        }

        if (signUpPassword.text.toString().isEmpty()) {
            signUpPassword.error = "Please enter password"
            signUpPassword.requestFocus()
            return
        }

        if (signUpPassword.length() < 6) {
            signUpPassword.error = "Short password, at least 6 characters"
            signUpPassword.requestFocus()
            return
        }

        if (signUpPassword.text.toString() != signUpPasswordRepeat.text.toString()) {
            signUpPasswordRepeat.error = "Password wrongly repeated"
            signUpPasswordRepeat.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(signUpEmail.text.toString(), signUpPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Account created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SignIn::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Sign Up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
