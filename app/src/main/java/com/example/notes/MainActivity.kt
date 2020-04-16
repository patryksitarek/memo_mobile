package com.example.notes

import android.content.Intent
import android.hardware.SensorManager.getOrientation
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_view.*
import kotlinx.android.synthetic.main.note_view.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var notesList: ArrayList<Map<String, Any>>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(applicationContext, SignIn::class.java)
            startActivity(intent)
            finish()
            return
        }
    }

    fun onClickCreateNote(v: View) {
        System.out.println("Create note clicked")
        val intent = Intent(applicationContext, Create_Edit_Note::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        //----------------------------WCZYTAJ NOTATKI DO LISTY--------------------------------------

        val docRef = db.collection("notes")
            .whereArrayContains("author", db.document("users/${auth.currentUser!!.uid}"))
//            .orderBy("created")
//            .whereEqualTo("author", db.document("users/lidOuRgtfJTsiq0vABRnMHmnl8H3"))

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("FragmentActivity", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.documents.isNotEmpty()) { // && snapshot.exists()) {
                notesList = ArrayList()
                for (doc in snapshot.documents) {
                    val data = doc.data
                    data?.set("id", doc.id)
                    notesList.add(data!!)
                }
                setAdapter(notesList)
            } else {
                Log.d("FragmentActivity", "Current data: null")
            }
        }
        //------------------------------------------------------------------------------------------


//        recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //siatka
        recycler_view.layoutManager = LinearLayoutManager(applicationContext) //lista
    }

    private fun setAdapter(arrayData: ArrayList<Map<String, Any>>) {
        recycler_view.adapter = CardViewAdapter(applicationContext, arrayData)
    }
}
