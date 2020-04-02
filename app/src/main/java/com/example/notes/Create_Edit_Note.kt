package com.example.notes

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.note_creator.*
import java.text.SimpleDateFormat
import java.util.*


class Create_Edit_Note : AppCompatActivity() {

//    private lateinit var myRef: DatabaseReference
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_creator)

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
        if (item.itemId == R.id.saveButton) {

            //------------------------------ZAPISYWANIE---------------------------------------------
            // TODO: prevent the UI from being all over the place after some of the operations
            // TODO(IMPORTANT): make both the title and content required
            // TODO(IMPORTANT): make the edit screen properly display note content
            val title = noteTitle.text.toString()
            val content = noteContent.text.toString()

            if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {

                //EDYCJA ISTNIEJACEJ NOTATKI
                if (intent.hasExtra("id")) {
                    val noteId = intent.extras?.get("id") as String
                    db.collection("notes").document(noteId).get().addOnSuccessListener { n ->
                       // Add
                        noteContent.setText(n["text"] as String)
                    }

                    val data = hashMapOf(
                        "author" to db.document("users/lidOuRgtfJTsiq0vABRnMHmnl8H3"),
                        "title" to title,
                        "text" to content
                    )
                    db.collection("notes").document(noteId)
                        .update(data)
                        .addOnSuccessListener {
                            Log.d("FragmentActivity", "Successfully edited!")
                            Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
                            this.finish()
                        }
                        .addOnFailureListener { exception ->
                            Log.w("FragmentActivity", "Error writing document", exception)
                            Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT).show()
                        }
//

                    //TWORZENIE NOWEJ NOTATKI
                } else {
                    val data = hashMapOf(
                        "author" to db.document("users/lidOuRgtfJTsiq0vABRnMHmnl8H3"),
                        "title" to title,
                        "text" to content,
                        "created" to FieldValue.serverTimestamp()
                    )

                    db.collection("notes")
                        .add(data)
                        .addOnSuccessListener {
                            Log.d("FragmentActivity", "DocumentSnapshot successfully written!")
                            Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
                            this.finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("FragmentActivity", "Error writing document", e)
                            Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT).show()
                        }
                }

            }
            else Toast.makeText(applicationContext, "Note is empty!", Toast.LENGTH_SHORT).show()
            //--------------------------------------------------------------------------------------
        }
        return super.onOptionsItemSelected(item)
    }
    //----------------------------------------------------------------------------------------------
}
