package com.example.notes

import android.content.Intent
import android.hardware.SensorManager.getOrientation
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_view.*
import kotlinx.android.synthetic.main.note_view.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var myRef: DatabaseReference
    private lateinit var notes: ArrayList<DatabaseRow>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firebase = FirebaseDatabase.getInstance()
        myRef = firebase.getReference("ArrayData")
    }

    fun onClickCreateNote(v: View) {
        val intent = Intent(applicationContext, Create_Edit_Note::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        //----------------------------WCZYTAJ NOTATKI DO LISTY--------------------------------------
        myRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notes = ArrayList()
                for (i in dataSnapshot.children) {
                    val newRow = i.getValue(DatabaseRow::class.java)
                    notes.add(newRow!!)
                }
                setAdapter(notes)
            }


        })
        //------------------------------------------------------------------------------------------


        recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //lepsza siatka
        //recycler_view.layoutManager = LinearLayoutManager(applicationContext) //lista
    }

    private fun setAdapter(arrayData: ArrayList<DatabaseRow>) {
        recycler_view.adapter = CardViewAdapter(applicationContext, arrayData)
    }
}
