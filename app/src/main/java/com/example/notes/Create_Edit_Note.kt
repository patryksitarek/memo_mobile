package com.example.notes

import android.content.ContentValues
import android.os.Bundle
import android.provider.BaseColumns
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.note_creator.*
import java.text.SimpleDateFormat
import java.util.*


class Create_Edit_Note : AppCompatActivity() {

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
        if (item?.itemId == R.id.saveButton) {
            val dbHelper = DatabaseHelper(applicationContext)
            val db = dbHelper.writableDatabase

            val title = noteTitle.text.toString()
            val content = noteContent.text.toString()
            val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val currentDateandTime: String = date.format(Date())

            val value = ContentValues()
            value.put(TableInfo.TABLE_COLUMN_TITLE, title)
            value.put(TableInfo.TABLE_COLUMN_CONTENT, content)
            value.put(TableInfo.TABLE_COLUMN_DATE, currentDateandTime)

            if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {
                //EDYCJA ISTNIEJACEJ
                if (intent.hasExtra("id")) {
                    db.update(TableInfo.TABLE_NAME, value, BaseColumns._ID + "=?", arrayOf(intent.getStringExtra("id")))
                }
                //TWORZENIE NOWEJ
                else {
                    db.insertOrThrow(TableInfo.TABLE_NAME, null, value)
                }

                Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
                db.close()
                this.finish()
            }
            else Toast.makeText(applicationContext, "Note is empty!", Toast.LENGTH_SHORT).show()

        }
        return super.onOptionsItemSelected(item)
    }
    //----------------------------------------------------------------------------------------------
}
