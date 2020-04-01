package com.example.notes

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.provider.BaseColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.note_view.view.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter


class CardViewAdapter(val context: Context, /*val db: SQLiteDatabase, var notes: ArrayList<Note>*/ var notes: ArrayList<DatabaseRow>) : RecyclerView.Adapter<MyViewHolder>() {

    /*var multiCheckMode = false*/
    private lateinit var myRef: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cardView_note = layoutInflater.inflate(R.layout.note_view, parent, false)
        return MyViewHolder(cardView_note)
    }

    override fun getItemCount(): Int {
        val itemCount = notes.size
        return itemCount
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        //-------------------------------------WYŚWIETLANIE NOTATKI---------------------------------
        val cardView_note = holder.view.cardView_note
        val title = holder.view.viewTitle
        val content = holder.view.viewContent
        val date = holder.view.viewDate
        val context: Context = holder.view.context

        title.text = notes[holder.adapterPosition].title
        content.text = notes[holder.adapterPosition].content
        val dateString = notes[holder.adapterPosition].date //YYYYMMDDHHMM
        date.setText(dateString.substring(6, 8) +
                "." +
                dateString.substring(4, 6) +
                "." +
                dateString.substring(2, 4) +
                " " +
                dateString.substring(8, 10) +
                ":" +
                dateString.substring(10, 12)) //DD.MM.YY HH:MM
        //------------------------------------------------------------------------------------------



        //-----------------------------------EDYCJA NOTATKI-----------------------------------------
        cardView_note.setOnClickListener {
            val edit_intent = Intent(context, Create_Edit_Note::class.java)

            val edit_id = notes[holder.adapterPosition].id
            val edit_title = notes[holder.adapterPosition].title
            val edit_content = notes[holder.adapterPosition].content
            val edit_date = notes[holder.adapterPosition].date

            edit_intent.putExtra("id", edit_id)
            edit_intent.putExtra("title", edit_title)
            edit_intent.putExtra("content", edit_content)
            edit_intent.putExtra("date", edit_date)

            context.startActivity(edit_intent)
        }
        //------------------------------------------------------------------------------------------




        //----------------------------------USUWANIE NOTATKI----------------------------------------
        cardView_note.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                //multiCheckMode = true
                //showHideCheckbox()

                //db.delete(TableInfo.TABLE_NAME, BaseColumns._ID + "=?", arrayOf(notes[holder.adapterPosition].id.toString()))
                //notes.removeAt(holder.adapterPosition)
                //notifyItemRemoved(holder.adapterPosition)

                /*val delnotesid = ArrayList<Int>()
                for (id in notes[0]) {


                    delnotesid.add(id)
                }*/
                return true
            }
        })

        //------------------------------------------------------------------------------------------


        /*fun showHideCheckbox() {
            if (multiCheckMode) {
                cardView_note.noteChecked.setVisibility(View.VISIBLE)
                holder.view.noteChecked.setChecked(true)
            } else {
                holder.view.noteChecked.setVisibility(View.GONE)
            }
            notifyDataSetChanged()
        }
    */

    }
}



class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)