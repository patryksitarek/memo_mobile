package com.example.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SignIn : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)
    }

    fun onClickSignUp(v: View) {
        val intent = Intent(applicationContext, SignUp::class.java)
        startActivity(intent)
    }
}
