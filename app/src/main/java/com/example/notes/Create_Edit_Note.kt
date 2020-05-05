package com.example.notes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.note_creator.*
import kotlinx.android.synthetic.main.popup_input.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Create_Edit_Note : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {

    private var shareNoteTo = ""

    private var day = 0
    private var month = 0
    private var year = 0
    private var minute = 0
    private var hour = 0
    private var lastCalendarButton = -1
    private lateinit var dateFromValue: Date
    private lateinit var dateUntilValue: Date

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var data: HashMap<String, Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.note_creator)

        //EDYTOWANIE
        if (intent.hasExtra("title"))  noteTitle.setText(intent.getStringExtra("title"))
        if (intent.hasExtra("content")) noteContent.setText(intent.getStringExtra("content"))

        //PRZYCISKI KALENDARZA
        dateFromButton.setOnClickListener {
            val dateDialog = DatePickerDialog()
            dateDialog.show(supportFragmentManager, "date_picker")

            lastCalendarButton = 0
        }

        dateUntilButton.setOnClickListener {
            val dateDialog = DatePickerDialog()
            dateDialog.show(supportFragmentManager, "date_picker")

            lastCalendarButton = 1
        }
    }

    //---------------------------------------MENU-BAR-----------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //ODPOWIEDNIK onClickListener
        if (item.itemId == R.id.saveButton) {

            //------------------------------ZAPISYWANIE---------------------------------------------
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
                                Toast.makeText(
                                    applicationContext,
                                    "Note saved!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                this.finish()
                            }
                            .addOnFailureListener { exception ->
                                Log.w("FragmentActivity", "Error writing document", exception)
                                Toast.makeText(
                                    applicationContext,
                                    "Failed to save",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                    //TWORZENIE NOWEJ NOTATKI
                } else {
                    data = hashMapOf(
                        "author" to arrayListOf(db.document("users/${auth.currentUser!!.uid}")),
                        "title" to title,
                        "text" to content,
                        "created" to FieldValue.serverTimestamp()
                        )

                    db.collection("notes")
                        .add(data)
                        .addOnSuccessListener {
                            Log.d("FragmentActivity", "DocumentSnapshot successfully written!")
                            Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT)
                                .show()
                            this.finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("FragmentActivity", "Error writing document", e)
                            Toast.makeText(applicationContext, "Failed to save", Toast.LENGTH_SHORT)
                                .show()
                        }
                }

            } else Toast.makeText(applicationContext, "Note is empty!", Toast.LENGTH_SHORT).show()
        } else if (item.itemId == R.id.shareButton) {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_input, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
            val mAlertDialog = mBuilder.show()

            mDialogView.popupCancel.setOnClickListener {
                mAlertDialog.dismiss()
            }

            mDialogView.popupShare.setOnClickListener {
                val title = noteTitle.text.toString()
                val content = noteContent.text.toString()
                mAlertDialog.dismiss()
                System.out.println("Share in popup clicked")
                var noteId = ""
                if (intent.hasExtra("id")) {
                    noteId = intent.getStringExtra("id")
                }

                val shareEmail: String = mDialogView.shareNoteTo.text.toString()
                db.collection("users").whereEqualTo("email", shareEmail).get()
                    .addOnSuccessListener { snapshot ->
                        val shareWith = if (snapshot.size() > 0) snapshot.documents[0].reference else null
                        if (shareWith == null) {
                            Toast.makeText(applicationContext, "User doesn't exist", Toast.LENGTH_SHORT)
                                .show()
                        } else if (noteId == "") {
                            val authors = arrayListOf<DocumentReference>(
                                db.document("users/${auth.currentUser!!.uid}"),
                                shareWith
                            )
                            db.collection("notes").add(hashMapOf(
                                "author" to authors,
                                "title" to title,
                                "text" to content,
                                "created" to FieldValue.serverTimestamp()
                            )).addOnSuccessListener { _ ->
                                Toast.makeText(
                                    applicationContext,
                                    "Note created and shared",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        } else {
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
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sharing failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //DATA PICKER
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        this.day = dayOfMonth
        this.month = month
        this.year = year


        //GO TO TIME PICKER
        val timeDialog = TimePickerDialog()
        timeDialog.show(supportFragmentManager, "time_picker")
    }

    //TIME PICKER
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        this.hour = hourOfDay
        this.minute = minute

        //UNIX DATE
        val date = SimpleDateFormat("dd-MM-yyyy-HH-mm").parse("$day-$month-$year-$hour-$minute")

        val userFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
        
        if (lastCalendarButton == 0) {
            dateFrom.setText(userFormat.format(date))
            dateFromValue = date
        } else {
            dateUntil.setText(userFormat.format(date))
            dateUntilValue = date
        }

    }
}