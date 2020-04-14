package com.example.notes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.sign_in.signInPassword
import kotlinx.android.synthetic.main.sign_up.*

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)
    }

    fun onClickSignUpButton(v: View) {

        if (signUpUsername.text.isNullOrEmpty()) {
            usernameWarning.visibility = ImageView.VISIBLE
        }
        else usernameWarning.visibility = ImageView.GONE

        if ((signUpEmail.text.toString() != signUpEmailRepeat.text.toString()) || signUpEmail.text.isNullOrEmpty()) {
            mailWarning.visibility = ImageView.VISIBLE
        }
        else mailWarning.visibility = ImageView.GONE

        if ((signUpPassword.text.toString() != signUpPasswordRepeat.text.toString()) || signUpPassword.text.isNullOrEmpty()) {
            passwordWarning.visibility = ImageView.VISIBLE
        }
        else passwordWarning.visibility = ImageView.GONE
    }
}
