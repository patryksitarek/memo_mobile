package com.example.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.sign_in.*
import kotlinx.android.synthetic.main.sign_in.email
import kotlinx.android.synthetic.main.sign_in.view.*
import kotlinx.android.synthetic.main.sign_up.*

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)
    }

    fun onClickSignUpButton(v: View) {

        if (username.text.isNullOrEmpty()) {
            usernameWarning.visibility = ImageView.VISIBLE
        }
        else usernameWarning.visibility = ImageView.GONE

        if ((email.text.toString() != email_repeat.text.toString()) || email.text.isNullOrEmpty()) {
            mailWarning.visibility = ImageView.VISIBLE
        }
        else mailWarning.visibility = ImageView.GONE

        if ((password.text.toString() != password_repeat.text.toString()) || password.text.isNullOrEmpty()) {
            passwordWarning.visibility = ImageView.VISIBLE
        }
        else passwordWarning.visibility = ImageView.GONE
    }
}
