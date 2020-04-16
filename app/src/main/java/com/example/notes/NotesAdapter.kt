package com.example.notes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.note_view.view.*
import java.util.*
import kotlin.collections.ArrayList


class CardViewAdapter(val context: Context,
                      var notes: ArrayList<Map<String, Any>>) : RecyclerView.Adapter<MyViewHolder>() {

//    var multiCheckMode = false
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

        System.out.println("View Holder Bound")
        //-------------------------------------WYŚWIETLANIE NOTATKI---------------------------------
        val cardView_note = holder.view.cardView_note
        val title = holder.view.viewTitle
        val content = holder.view.viewContent
        val date = holder.view.viewDate
        val context: Context = holder.view.context

        val note = notes[holder.adapterPosition]
//        val authorRef = note["author"] as DocumentReference
//        var author = "no data"
        content.text = ""
//        authorRef.get()
//            .addOnSuccessListener { doc ->
//                author = doc.data?.get("name") as String
//                content.text = String.format("author: $author\n%s", content.text)
//        }
        val created = note["created"] as Timestamp
        val contentText = String.format("text: %s\n", note["text"])

        content.append(note["text"] as String)
        title.text = note["title"] as String
        date.text = created.toDate().toString()
        //------------------------------------------------------------------------------------------



        //-----------------------------------EDYCJA NOTATKI-----------------------------------------
        cardView_note.setOnClickListener {
            System.out.println("LISTENER SET 1")
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


//        fun showHideCheckbox() {
//            if (multiCheckMode) {
//                cardView_note.noteChecked.setVisibility(View.VISIBLE)
//                holder.view.noteChecked.setChecked(true)
//            } else {
//                holder.view.noteChecked.setVisibility(View.GONE)
//            }
//            notifyDataSetChanged()
//        }
    }
}



class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)