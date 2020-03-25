package com.example.notes

import android.content.Intent
import android.hardware.SensorManager.getOrientation
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.note_view.*
import kotlinx.android.synthetic.main.note_view.view.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickCreateNote(v: View) {
        val intent = Intent(applicationContext, Create_Edit_Note::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        //----------------------------WCZYTAJ NOTATKI DO LISTY--------------------------------------
        val dbHelper = DatabaseHelper(applicationContext)
        val db = dbHelper.writableDatabase

        val cursor = db.query(TableInfo.TABLE_NAME, null, null, null, null, null, TableInfo.TABLE_COLUMN_DATE + " DESC") //wedlug daty malejaco
        val notes = ArrayList<Note>()

        if(cursor.count > 0) {
            cursor.moveToFirst()
            while(!cursor.isAfterLast) {
                val note = Note()
                note.id = cursor.getInt(0)
                note.title = cursor.getString(1)
                note.content = cursor.getString(2)
                note.date = cursor.getString(3)

                notes.add(note)
                cursor.moveToNext()
            }
        }
        cursor.close()
        //------------------------------------------------------------------------------------------


        //recycler_view.layoutManager = GridLayoutManager(applicationContext, 2) //siatka
        recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) //lepsza siatka
        //recycler_view.layoutManager = LinearLayoutManager(applicationContext) //lista

        recycler_view.adapter = CardViewAdapter(applicationContext, db, notes)
    }
}
