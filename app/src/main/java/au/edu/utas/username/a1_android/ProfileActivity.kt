package au.edu.utas.username.a1_android

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import au.edu.utas.username.a1_android.databinding.ActivityProfileBinding
import au.edu.utas.username.a1_android.ui.main.AllHistoryFragment
import au.edu.utas.username.a1_android.ui.main.History
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

val profileItems = mutableListOf<History>()

class ProfileActivity : AppCompatActivity() {
    private var totalT = 0
    private var totalR = 0
    private var totalTT = 0
    private var totalF = 0
    private var totalUF = 0
    private lateinit var uri : Uri
    lateinit var currentPhotoPath : String
    var thisID = ""
    private var dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    private lateinit var ui : ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(ui.root)

        var settings = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        ui.name.text = settings.getString(USERNAME_KEY, "Shy")?.toEditable()

        ui.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                var content = p0.toString()
                with(settings.edit()){
                    putString(USERNAME_KEY, content)
                    apply()
                }
            }
        })

        ui.back.setOnClickListener(){
            var main = Intent(this, MainActivity::class.java)
            startActivity(main)
        }

        ui.avatar.setOnClickListener(){
            requestToTakeAPicture()
        }

        val db = Firebase.firestore

        var allHistoryCollection = db.collection("histories")

        allHistoryCollection
            .get()
            .addOnSuccessListener { result ->
                profileItems.clear() //this line clears the list, and prevents a bug where items would be duplicated upon rotation of screen
                for (document in result)
                {
                    val history = document.toObject<History>()
                    history.id = document.id
                    totalTT++
                    totalR += history.repeat!!
                    totalT += history.duration!!
                    when(history.completed.toString()){
                        "true" -> totalF++
                        else -> totalUF++
                    }
                    Log.d(au.edu.utas.username.a1_android.ui.main.FIREBASE_TAG, history.toString())
                    profileItems.add(history)
                }

                ui.trainingTime.text = String.format("$totalTT times")
                ui.totalRepeat.text = String.format("$totalR times")
                ui.totalTime.text = String.format("$totalT seconds")
                ui.totalFinished.text = String.format("completed $totalF exercises")
                ui.totalUnfinished.text = String.format("$totalUF unfinished exercises")
            }
    }
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestToTakeAPicture(){
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_IMAGE_CAPTURE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode){
            REQUEST_IMAGE_CAPTURE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    takeAPicture()
                }else{
                    Toast.makeText(this, "Cannot access camera, permission denied", Toast.LENGTH_LONG).show()
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                }
            }
        }
    }

    private fun takeAPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (takePictureIntent.resolveActivity(packageManager) != null){
//            try {
        val photoFile: File = createImageFile()!!
        val photoURI : Uri = FileProvider.getUriForFile(
            this,
            "au.edu.utas.username.a1_android",
            photoFile
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }catch (e: Exception){}
//        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return  File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jap",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            setPic(ui.avatar)
            var file = Uri.fromFile(File(currentPhotoPath))
            thisID = dateFormat.format(Date())
            var storage = FirebaseStorage.getInstance().reference.child("images/${thisID}.jpg")
            //var storage = FirebaseStorage.getInstance().getReference("images/${thisID}.jpg")
            storage.putFile(file)
                .addOnSuccessListener { Log.d(FIREBASE_TAG, "upload successful") }
                .addOnFailureListener{Log.d(FIREBASE_TAG, "upload failure") }
        }else if (requestCode == 100 && resultCode == RESULT_OK){
            uri = data?.data!!
            ui.avatar.setImageURI(uri)
            thisID = dateFormat.format(Date()).toString()
            var storage = FirebaseStorage.getInstance().reference.child("images/${thisID}.jpg")
            storage.putFile(uri)
                .addOnSuccessListener { Log.d(FIREBASE_TAG, "upload successful") }
                .addOnFailureListener{Log.d(FIREBASE_TAG, "upload failure") }
        }
    }

    private fun setPic(myImageView: ImageView) {
        val targetW : Int = myImageView.width
        val targetH : Int = myImageView.height

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)
            val photoW : Int = outWidth
            val photoH : Int = outHeight

            val scaleFactor : Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            inJustDecodeBounds = false
            inSampleSize = scaleFactor

        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            myImageView.setImageBitmap(bitmap)
        }
    }
}