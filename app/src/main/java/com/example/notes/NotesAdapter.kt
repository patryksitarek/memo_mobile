package com.example.notes

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.note_view.view.*


class CardViewAdapter(val context: Context,
                      var notes: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<MyViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cardView_note = layoutInflater.inflate(R.layout.note_view, parent, false)
        return MyViewHolder(cardView_note)
    }

    override fun getItemCount(): Int {
        return notes.size
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        //-------------------------------------WYŚWIETLANIE NOTATKI---------------------------------
        val cardView_note = holder.view.cardView_note
        val title = holder.view.viewTitle
        val content = holder.view.viewContent
        val date = holder.view.viewDate
        val context: Context = holder.view.context

        val note = notes[holder.adapterPosition]
        content.text = ""
        val tempfix = note["created"] == null
        val created = if (!tempfix) note["created"] as Timestamp else Timestamp.now()
        val contentText = String.format("text: %s\n", note["text"])

        content.append(note["text"] as String)
        title.text = note["title"] as String

        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
        val unixDate = java.util.Date(created.seconds * 1000)
        date.text = sdf.format(unixDate)
        //------------------------------------------------------------------------------------------



        //-----------------------------------EDYCJA NOTATKI-----------------------------------------
        cardView_note.setOnClickListener {
            val editIntent = Intent(context, Create_Edit_Note::class.java)

            val currentNote = notes[holder.adapterPosition]
            val editId = currentNote["id"] ?: ""
            val editTitle = currentNote["title"] ?: ""
            val editContent = currentNote["text"] ?: ""
            val editDate = currentNote["date"] ?: ""

            editIntent.putExtra("id", editId as String)
            editIntent.putExtra("title", editTitle as String)
            editIntent.putExtra("content", editContent as String)
            editIntent.putExtra("date", editDate as String)

            if (currentNote["isEvent"] == true) {
                editIntent.putExtra("dateFrom", currentNote["start"].toString())
                editIntent.putExtra("dateUntil", currentNote["end"].toString())
            }

            if (currentNote["photoUUID"] != null) {
                editIntent.putExtra("photoUUID", currentNote["photoUUID"].toString())
            }


            context.startActivity(editIntent)
        }
        //------------------------------------------------------------------------------------------




        //----------------------------------USUWANIE NOTATKI----------------------------------------
        cardView_note.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                val noteId = notes[holder.adapterPosition]["id"]
                db.collection("notes").document(noteId.toString()).delete()
                return true
            }
        })

        //------------------------------------------------------------------------------------------
    }
}



class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)