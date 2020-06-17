package com.example.notes

import android.Manifest
import android.app.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.note_creator.*
import kotlinx.android.synthetic.main.popup_input.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class Create_Edit_Note : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {

    private var shareNoteTo = ""

    //EVENT
    private var day = 0
    private var month = 0
    private var year = 0
    private var minute = 0
    private var hour = 0
    private var lastCalendarButton = -1
    private lateinit var dateFromValue: Date
    private lateinit var dateUntilValue: Date

    //FIREBASE
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    //FIRESTORE
    private val storageRef = FirebaseStorage.getInstance().reference.child("images")
    private  var photoUri: Uri? = null
    private lateinit var imageBitmap: Bitmap
    private lateinit var photoUUID: UUID

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_ATTACH_FILE = 2

    private val PERMISSION_CODE = 1000;
    private val IMAGE_CAPTURE_CODE = 1001
    var image_uri: Uri? = null


    private lateinit var data: HashMap<String, Any>

    lateinit var alarmManager: AlarmManager
    lateinit var alarmIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.note_creator)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = PendingIntent.getBroadcast(applicationContext, 0, Intent(applicationContext, AlarmReciver::class.java), 0)

        //EDYTOWANIE
        if (intent.hasExtra("title"))  noteTitle.setText(intent.getStringExtra("title"))
        if (intent.hasExtra("content")) noteContent.setText(intent.getStringExtra("content"))
        if (intent.hasExtra("tags")) noteTags.setText(intent.getStringExtra("tags").replace(",", ""))

        if (intent.hasExtra("dateFrom")) {
            val dateFromInSeconds = intent.getStringExtra("dateFrom").substring(18, 28) + "000"
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
            val unixDateFrom = java.util.Date(dateFromInSeconds.toLong())
            dateFrom.setText(sdf.format(unixDateFrom))
        }
        if (intent.hasExtra("photoUUID")) {
            photoUUID = UUID.fromString(intent.getStringExtra("photoUUID"))
            val tempFile = File.createTempFile("Images", "bmp")

            val imageRef = storageRef.child(photoUUID.toString())
            imageRef.getFile(tempFile).addOnSuccessListener{
                    val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                    imageAttach.setImageBitmap(bitmap)
                    noteContent.setVisibility(View.GONE)
                    imageAttach.setVisibility(View.VISIBLE)
                }.addOnFailureListener{
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

        //PRZYCISK KALENDARZA
        dateLayout.setOnClickListener {
            val dateDialog = DatePickerDialog()
            dateDialog.show(supportFragmentManager, "date_picker")

            lastCalendarButton = 0
        }

        imageAttach.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                val noteId = intent.extras?.get("id") as String
                val docRef = db.collection("notes").document(noteId)
                val updates = hashMapOf<String, Any>(
                    "photoUUID" to FieldValue.delete()
                )
                docRef.update(updates)

                photoUri = null

                storageRef.child(photoUUID.toString()).delete().addOnSuccessListener {
                    Toast.makeText(applicationContext, "Image deleted!", Toast.LENGTH_SHORT).show()
                    noteContent.setVisibility(View.VISIBLE)
                    imageAttach.setVisibility(View.GONE)
                }
                return true
            }
        })
    }

    //---------------------------------------MENU-BAR-----------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun floatingSaveButton(v: View) {

        //------------------------------ZAPISYWANIE---------------------------------------------
        val title = noteTitle.text.toString()
        val content = noteContent.text.toString()
        val tags = noteTags.text.split(" ")


        if (!title.isNullOrEmpty() || !content.isNullOrEmpty()) {

            //EDYCJA ISTNIEJACEJ NOTATKI
            if (intent.hasExtra("id")) {
                val noteId = intent.extras?.get("id") as String

                db.collection("notes").document(noteId).get().addOnSuccessListener { n ->
                    // Add
                    val noteRef = n.reference
                    noteContent.setText(n["text"] as String)
                    val authors = n.get("author") as ArrayList<DocumentReference>

                    data = hashMapOf(
                        "author" to authors,
                        "title" to title,
                        "text" to content,
                        "tags" to tags
                    )

                    if (lastCalendarButton != -1) {
                        data.put("isEvent", true)
                        data.put("start", Timestamp(dateFromValue))
                    }

                    if (photoUri != null || this::imageBitmap.isInitialized) {
                        /*storageRef.child(photoUUID.toString()).putFile(photoUri)
                        data.put("photoUUID", photoUUID.toString())*/

                        imageAttach.isDrawingCacheEnabled = true
                        imageAttach.buildDrawingCache()
                        val bitmap = (imageAttach.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val imageFile = baos.toByteArray()

                        storageRef.child(photoUUID.toString()).putBytes(imageFile)
                        data.put("photoUUID", photoUUID.toString())
                    }


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
                            Log.d("FragmentActivity", "Error writing document", exception)
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
                    "tags" to tags,
                    "created" to FieldValue.serverTimestamp()
                )

                if (!dateFrom.text.isEmpty()) {
                    data.put("isEvent", true)
                    data.put("start", Timestamp(dateFromValue))

                    alarmManager.set(AlarmManager.RTC_WAKEUP, dateFromValue.time, alarmIntent)
                }


                if (photoUri != null || this::imageBitmap.isInitialized) {

                    imageAttach.isDrawingCacheEnabled = true
                    imageAttach.buildDrawingCache()
                    val bitmap = (imageAttach.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val imageFile = baos.toByteArray()

                    storageRef.child(photoUUID.toString()).putBytes(imageFile).addOnFailureListener{
                        Log.d("storage", it.toString())
                    }
                    data.put("photoUUID", photoUUID.toString())
                }

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

        } else Toast.makeText(applicationContext, "Note is empty!", Toast.LENGTH_SHORT).show()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //ODPOWIEDNIK onClickListener

        if (item.itemId == R.id.shareButton) {
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
        } else if (item.itemId == R.id.attachButton) {
            openFileChooser()
            //dispatchTakePictureIntent()
        } else if (item.itemId == R.id.cameraButton) {
            dispatchTakePictureIntent()
        }
        return super.onOptionsItemSelected(item)
    }
    //----------------------------------------------------------------------------------------------


    //-------------------------------------LOAD-IMAGE/CAMERA----------------------------------------
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_ATTACH_FILE)
    }

    private fun dispatchTakePictureIntent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //permission was not enabled
                val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            } else{
                //permission already granted
                openCamera()
            }
        }
        else {
            //system os is < marshmallow
            openCamera()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    openCamera()
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ATTACH_FILE) {
            if(!(this::photoUUID.isInitialized)) photoUUID = UUID.randomUUID()
            photoUri = data?.data!!
            imageAttach.setImageURI(photoUri)

            noteContent.setVisibility(View.GONE)
            imageAttach.setVisibility(View.VISIBLE)
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(!(this::photoUUID.isInitialized)) photoUUID = UUID.randomUUID()
            imageAttach.setImageURI(photoUri)

            noteContent.setVisibility(View.GONE)
            imageAttach.setVisibility(View.VISIBLE)
        }
    }
    //----------------------------------------------------------------------------------------------



    //---------------------------------DATE-TIME-PICKERS--------------------------------------------
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        this.day = dayOfMonth
        this.month = month
        this.year = year

        //GO TO TIME PICKER
        val timeDialog = TimePickerDialog()
        timeDialog.show(supportFragmentManager, "time_picker")
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        this.hour = hourOfDay
        this.minute = minute

        //UNIX DATE
        val userFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
        val date = SimpleDateFormat("dd-MM-yyyy-HH-mm").parse("$day-${month+1}-$year-$hour-$minute")


        if (lastCalendarButton == 0) {
            dateFrom.setText(userFormat.format(date))
            dateFromValue = date
        }

    }
    //----------------------------------------------------------------------------------------------

}