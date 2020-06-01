package com.example.notes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var notesList: ArrayList<Map<String, Any>>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        //recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //siatka
        recycler_view.layoutManager = LinearLayoutManager(applicationContext) //lista

        if (auth.currentUser == null) {
            val intent = Intent(applicationContext, SignIn::class.java)
            startActivity(intent)
            finish()
            return
        }

        radioFilter.setOnCheckedChangeListener { group, checkedId ->
            loadNotes()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_logout, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item?.itemId == R.id.signOutButton) {
            auth.signOut()
            val intent = intent
            finish()
            startActivity(intent)
        }
        else if (item?.itemId == R.id.calendarButton) {
            val intentCal = Intent(this, Calendar::class.java)
            startActivity(intentCal)
        }

        return super.onOptionsItemSelected(item)
    }


    fun onClickCreateNote(v: View) {
        System.out.println("Create note clicked")
        val intent = Intent(applicationContext, Create_Edit_Note::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {

        val docRef = db.collection("notes")
            .whereArrayContains("author", db.document("users/${auth.currentUser!!.uid}"))
            .orderBy("created", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FragmentActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.documents.isNotEmpty()) { // && snapshot.exists()) {
                    notesList = ArrayList()

                    for (doc in snapshot.documents) {
                        val data = doc.data

                        //TEXT FILTER, TRUE IF SEARCHBAR EMPTY
                        val titleContains = data!!["title"].toString().contains(searchText.text, ignoreCase = true)
                        val textContains = data!!["text"].toString().contains(searchText.text, ignoreCase = true)

                        if (titleContains || textContains) {
                            if (radioFilter.checkedRadioButtonId == radioAll.id) {
                                //RADIO BUTTON ALL
                                data?.set("id", doc.id)
                                notesList.add(data!!)
                            } else if (radioFilter.checkedRadioButtonId == radioPhoto.id) {
                                //RADIO BUTTON PHOTO
                                if (data!!["photoUUID"] != null) {
                                    data?.set("id", doc.id)
                                    notesList.add(data!!)
                                }
                            } else if (radioFilter.checkedRadioButtonId == radioText.id) {
                                //RADIO BUTTON TEXT
                                if (data!!["photoUUID"] == null) {
                                    data?.set("id", doc.id)
                                    notesList.add(data!!)
                                }
                            } else if (radioFilter.checkedRadioButtonId == radioEvent.id){
                                //RADIO BUTTON EVENT
                                if (data!!["isEvent"] == true) {
                                    data?.set("id", doc.id)
                                    notesList.add(data!!)
                                }
                            }
                        }
                    }
                    setAdapter(notesList)
                    } else {
                        Log.d("FragmentActivity", "Current data: null")
                    }
            }
    }

    fun searchButton(v: View) {
        loadNotes()
    }

    private fun setAdapter(arrayData: ArrayList<Map<String, Any>>) {
        recycler_view.adapter = CardViewAdapter(applicationContext, arrayData)
    }

}
