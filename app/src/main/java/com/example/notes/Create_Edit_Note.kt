package com.example.notes

//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase

import android.accessibilityservice.GestureDescription
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.note_creator.*
import kotlinx.android.synthetic.main.popup_input.view.*
import kotlinx.android.synthetic.main.sign_up.*


class Create_Edit_Note : AppCompatActivity() {

    private var shareNoteTo = ""

//    private lateinit var myRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
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
                         val noteRef = n.reference
                        noteContent.setText(n["text"] as String)
                        val authors = n.get("author") as ArrayList<DocumentReference>
                        val data = hashMapOf(
                            "author" to authors,
                            "title" to title,
                            "text" to content
                        )
                        db.collection("notes").document(noteId).update(data as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d("FragmentActivity", "Successfully edited!")
                                Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
                                this.finish()
                            }
                            .addOnFailureListener { exception ->
                                Log.w("FragmentActivity", "Error writing document", exception)
                                Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT).show()
                            }
                    }

                    //TWORZENIE NOWEJ NOTATKI
                } else {
                    val data = hashMapOf(
                        "author" to arrayListOf(db.document("users/${auth.currentUser!!.uid}")),
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

        else if (item.itemId == R.id.shareButton) {

            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_input, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            //show dialog
            val mAlertDialog = mBuilder.show()

            mDialogView.popupCancel.setOnClickListener {
                mAlertDialog.dismiss()
            }

            mDialogView.popupShare.setOnClickListener {
                mAlertDialog.dismiss()
                var noteId = ""
                if (intent.hasExtra("id")) {
                    noteId = intent.getStringExtra("id")
                }
                if (noteId == "") {
                    Toast.makeText(applicationContext, "Sharing failed", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // miejsce na obsługę udostępniania
                val shareEmail: String = mDialogView.shareNoteTo.text.toString()
                db.collection("users").whereEqualTo("email", shareEmail).get()
                    .addOnSuccessListener { snapshot ->
                        val shareWith = snapshot.documents[0].reference
                        val shareNote = db.collection("notes").document(noteId).get()
                            .addOnSuccessListener { docSnapshot ->
                                val authors =
                                    docSnapshot.get("author") as ArrayList<DocumentReference>
                                authors.add(shareWith)
                                docSnapshot.reference.update("author", authors)
                                    .addOnSuccessListener { _ ->
                                        Toast.makeText(
                                            applicationContext,
                                            "Note shared",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                    }.addOnFailureListener {
                        Toast.makeText(applicationContext, "Sharing failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            return super.onOptionsItemSelected(item)
        }

//                var noteId : String?
//                if (intent.hasExtra("id")) {
//                    noteId = intent.extras?.get("id") as String
//                } else {
//                    // docelowo test sprawdzajacy czy notatka jest juz utworzona powinien kontrolowac pojawianie sie przycisku
//                    // note doesn't exist
//                    Toast.makeText(applicationContext, "Sharing failed", Toast.LENGTH_SHORT).show()
//                    return false
//                }
//            val intent = Intent(applicationContext, ShareNote::class.java)
//            startActivity(intent)
                // docelowo start activity^, które pyta o użytkownika - podajemy email, a potem trzeba przeszukać
                // wszystkich użytkowników w bazie w poszukiwaniu tego adresu:
                // db.collection('users').whereEqualTo('email', <email podany przez usera>).get()
                // To jest promise, więc albo trzeba awaitować, albo w .then() albo coś, w każdym razie docelowo user
                // któremu udostępniamy notatke jest już znany, tutaj hardcoded




//        }
//
//    }
    //----------------------------------------------------------------------------------------------
}
