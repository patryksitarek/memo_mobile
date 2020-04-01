package com.example.notes

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.note_creator.*
import java.text.SimpleDateFormat
import java.util.*


class Create_Edit_Note : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_creator)

        val firebase = FirebaseDatabase.getInstance()
        myRef = firebase.getReference("ArrayData")

        //EDYTOWANIE
        if (intent.hasExtra("title"))  noteTitle.setText(intent.getStringExtra("title"))
        if (intent.hasExtra("content")) noteContent.setText(intent.getStringExtra("content"))
    }

    //---------------------------------------MENU-BAR-----------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //ODPOWIEDNIK onClickListener
        if (item?.itemId == R.id.saveButton) {

            //------------------------------ZAPISYWANIE---------------------------------------------
            val title = noteTitle.text.toString()
            val content = noteContent.text.toString()
            val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val currentDateandTime: String = date.format(Date())

            if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {

                //EDYCJA ISTNIEJACEJ NOTATKI
                if (intent.hasExtra("id")) {
                    val firebaseInput = DatabaseRow(intent.getStringExtra("id"), title, content, currentDateandTime)
                    myRef.child(intent.getStringExtra("id")).setValue(firebaseInput)
                }

                //TWORZENIE NOWEJ NOTATKI
                else {
                    val firebaseInput = DatabaseRow("${Date().time}", title, content, currentDateandTime)
                    myRef.child("${Date().time}").setValue(firebaseInput)
                }

                Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
                this.finish()
            }
            else Toast.makeText(applicationContext, "Note is empty!", Toast.LENGTH_SHORT).show()
            //--------------------------------------------------------------------------------------
        }
        return super.onOptionsItemSelected(item)
    }
    //----------------------------------------------------------------------------------------------
}
