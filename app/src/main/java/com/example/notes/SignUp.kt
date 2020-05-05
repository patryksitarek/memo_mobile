package com.example.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.note_creator.*
import kotlinx.android.synthetic.main.sign_in.signInPassword
import kotlinx.android.synthetic.main.sign_up.*

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
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

        System.out.println("-----------------------------------------------")
        System.out.println(signUpEmail.text)
        System.out.println(signUpPassword.text)


        auth.createUserWithEmailAndPassword(signUpEmail.text.toString(), signUpPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Account created", Toast.LENGTH_SHORT).show()
                    //System.out.println(auth.currentUser?.uid)

                    val data = hashMapOf(
                        "created" to FieldValue.serverTimestamp(),
                        "email" to signUpEmail.text.toString(),
                        "name" to signUpUsername.text.toString(),
                        "photoURL" to null
                    )

                    db.document("users/${auth.currentUser?.uid}").set(data)
                        .addOnSuccessListener {
                            Log.d("FragmentActivity", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("FragmentActivity", "Error writing document", e)
                        }

                    startActivity(Intent(this, SignIn::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Sign Up failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
