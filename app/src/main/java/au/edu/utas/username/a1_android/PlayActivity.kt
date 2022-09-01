package au.edu.utas.username.a1_android

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import au.edu.utas.username.a1_android.databinding.ActivityPlayBinding
import au.edu.utas.username.a1_android.databinding.GameOverBinding
import au.edu.utas.username.a1_android.ui.main.History
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.LongToDoubleFunction
import kotlin.jvm.Throws

const val TEST_KEY : String = "TEST"
const val FIREBASE_TAG = "FirebaseLogging"
const val REQUEST_IMAGE_CAPTURE = 1

class PlayActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var ui : ActivityPlayBinding
    private lateinit var gameOver : GameOverBinding
    private lateinit var gameOverBuilder : AlertDialog.Builder
    private var dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    lateinit var currentPhotoPath : String
    private lateinit var uri : Uri
    var size = 0
    var offset = 0
    var placedButtons = arrayOf<Button>()
    var allPressed = arrayOf<Boolean>()
    var clickedList = arrayOf<Int>()
    var buttonList = arrayOf<String>()
    var totalTimeOrCount: Long = 0
    var endGame = false
    var numberOfButtons = 3
    var maxY = 0
    var maxX = 0
    var thisID = ""
    var id = ""
    var hasRepaeted = 0
    var playedDuration = 0
    var goalText = "time"
    var timeOrCount = 60
    var endTime = ""
    var completed = false
    var canUse = false
    var exeText = "Exercise1"
    var isRandom = "false"
    var temp = ""

    private lateinit var countDownTimer: CountDownTimer

    private lateinit var isIndication: String
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlayBinding.inflate(layoutInflater)
        gameOver = GameOverBinding.inflate(layoutInflater)
        gameOverBuilder = AlertDialog.Builder(this)
        gameOverBuilder.setView(gameOver.root)

        setContentView(ui.root)

        var endGame = false
        var modeText = intent.getStringExtra(MODECHOOSE_KEY)
        goalText = intent.getStringExtra(GOALTEXT_KEY).toString()
        exeText = intent.getStringExtra(EXETEXT_KEY).toString()
        isRandom = intent.getStringExtra(ISRANDOM_KEY).toString()
        isIndication = intent.getStringExtra(ISINDICATION_KEY).toString()
        var buttonSize = intent.getStringExtra(BUTTONSIZE_KEY)
        numberOfButtons = intent.getIntExtra(NUMBEROFBUTTON_KEY, 3)
        timeOrCount = intent.getIntExtra(TIMEORCOUNT_KEY, 60)

        totalTimeOrCount = (1000 * timeOrCount).toLong()
        ui.durationBar.max = timeOrCount

        prepareGame()

        gameOver.camera.setOnClickListener(){
            requestToTakeAPicture()
            gameOver.picture.visibility = View.VISIBLE
        }

        Log.d(TEST_KEY, endGame.toString())
        if(numberOfButtons == 4){
            gameOver.l4.isEnabled = true; gameOver.l4.visibility = View.VISIBLE
        }else if(numberOfButtons == 5){
            gameOver.l4.isEnabled = true; gameOver.l4.visibility = View.VISIBLE
            gameOver.l5.isEnabled = true; gameOver.l5.visibility = View.VISIBLE
        }

        when(buttonSize){
            "small" -> {size = 140; maxX = 944; maxY = 880}
            "mid" -> {size = 180; maxX = 904; maxY = 840}
            else -> {size = 220; maxX = 864; maxY = 800}
        }

        offset = size

        gameOver.selectImage.setOnClickListener() {
            chooseImage()
        }
        ui.pause.setOnClickListener(){
            pauseTimer()
        }

        ui.b2exe.setOnClickListener(){
            countDownTimer.cancel()
            b2exeDialog()
        }

        gameOver.cancel.setOnClickListener(){
            uploadHistory()
            var back2exe = Intent(this, ExerciseActivity::class.java)
            startActivity(back2exe)
        }

        gameOver.confirm.setOnClickListener(){
            uploadHistory()
            var go2History = Intent(this, HistoryActivity::class.java)
            startActivity(go2History)
        }

        for (j in 0 until numberOfButtons){
            var j = false
            allPressed = allPressed.plus(j)
        }
        initButton()
    }


    private fun prepareGame(){
        thisID = dateFormat.format(Date())
        ui.timeText.text = dateFormat.format(Date())
        ui.durationText.text = "0"
        ui.repeatText.text = "0"
        gameOver.timeStart.text = dateFormat.format(Date())
        when(goalText){
            "time" -> ui.durationMapping.text = "$timeOrCount S remaining"
            else -> {ui.durationMapping.text = "$timeOrCount T remaining";
                    ui.durationBar.progress = timeOrCount;
                    totalTimeOrCount = 24 * 60 * 60 * 1000}
        }
        startTimer()
    }

    private fun startTimer(){
        countDownTimer = object: CountDownTimer(totalTimeOrCount, 1000){
            override fun onTick(millisUntilFinished: Long) {
                var remain = (millisUntilFinished / 1000).toInt()
                totalTimeOrCount = millisUntilFinished
                if (goalText == "time"){
                    ui.durationBar.progress = remain
                    ui.durationMapping.text = String.format("%s S remaining",remain)
                    //Log.d(TEST_KEY, remain.toString())
                    endGame = false
                }else{

                }
                playedDuration++
                ui.durationText.text = playedDuration.toString()
            }
            override fun onFinish() {
                if (goalText == "time"){
                    endGame = true
                    gameOver()
                }
            }
        }.start()
    }

    private fun pauseTimer(){
        countDownTimer.cancel()
        pauseDialog()
    }

    private fun gameOver(){
        gameOver.durTime.text = String.format(" $playedDuration S")
        gameOver.timeEnd.text = dateFormat.format(Date())
        gameOver.repeat.text = hasRepaeted.toString()
        gameOver.repeat1.text = clickedList[0].toString()
        gameOver.repeat2.text = clickedList[1].toString()
        gameOver.repeat3.text = clickedList[2].toString()
        if (numberOfButtons == 4){
            gameOver.repeat4.text = clickedList[3].toString()
        }else if (numberOfButtons == 5){
            gameOver.repeat4.text = clickedList[3].toString()
            gameOver.repeat5.text = clickedList[4].toString()
        }
        gameOverBuilder.show()
    }


    private fun isOverlaping(button: Button, button_1: Button): Boolean{
        var isOverlap = false

        var isOverlap1 = button.x + offset >= button_1.x && button.x <= button_1.x + offset &&
                button.y + offset >= button_1.y && button.y <= button_1.y + offset

        var isOverlap2 = button_1.x + offset >= button.x && button_1.x <= button.x + offset &&
                button_1.y + offset >= button.y && button_1.y <= button.y + offset

        if (isOverlap1 && isOverlap2) isOverlap = true
        return isOverlap
    }

    private fun initButton(){
        for (but in 1..numberOfButtons){
            var button: Button = Button(this)
            button.id = but
            button.text = but.toString()
            button.textSize = 30f
            button.isVisible = true
            button.layoutParams=(ConstraintLayout.LayoutParams(size, size))

            clickedList = clickedList.plus(0)

            if (but - 1 == 0 && isIndication == "yes") {
                button.setBackgroundColor(Color.parseColor("#32CD32"))
            }

            if (but == numberOfButtons){
                button.setOnClickListener(this)
            }else {
                button.setOnClickListener() {
                    Log.d(TEST_KEY, canUse.toString())
                    if (!endGame){
                        if (but - 1 != 0 && but < numberOfButtons) {
                            temp+=but
                            Log.d(TEST_KEY, temp)
                            if (allPressed[but - 2]) {

                                placedButtons[but - 1].visibility = View.INVISIBLE
                                allPressed[but - 1] = true
                                clickedList[but - 1]++

                                if (but <= placedButtons.size - 1 && isIndication == "yes") {
                                    placedButtons[but].setBackgroundColor(Color.parseColor("#32CD32"))
                                }
                            }
                        } else if (but - 1 == 0) {
                            temp += but
                            Log.d(TEST_KEY, temp)
                            //Log.d(TEST_KEY, canUse.toString())
                            placedButtons[but - 1].visibility = View.INVISIBLE
                            allPressed[but - 1] = true
                            clickedList[but - 1]++
                            Log.d(TEST_KEY, temp)
                            if (but <= placedButtons.size - 2 && isIndication == "yes") {
                                placedButtons[but].setBackgroundColor(Color.parseColor("#32CD32"))
                            }
                        }
                }
                }
            }
            ui.cl.addView(button)

            if (but-1 == 0){
                button.x = (1..maxX).random().toFloat()
                button.y = (1..maxY).random().toFloat()
                placedButtons = placedButtons.plus(button)
            }else{
                do {
                    var overlapping = false
                    button.x = (1..maxX).random().toFloat()
                    button.y = (1..maxY).random().toFloat()
                    for (btn in placedButtons) {
                        overlapping = isOverlaping(btn, button)
                        if (overlapping){
                            break
                        }
                    }
                } while (overlapping)
                placedButtons = placedButtons.plus(button)
            }
        }
    }


    private fun setRandomPosition(){

        var new = temp.toString()
        buttonList = buttonList.plus(new)
        temp = ""
        endGame = false
        var currentButton = arrayOf<Button>()

        for (j in 0 until placedButtons.size){
            allPressed[j] = false
            if (j == 0){
                placedButtons[j].x = (1..maxX).random().toFloat()
                placedButtons[j].y = (1..maxY).random().toFloat()
                placedButtons[j].isVisible = true
                placedButtons[j].setBackgroundColor(Color.parseColor("#D3D3D3"))
                if (isIndication == "yes"){
                    placedButtons[j].setBackgroundColor(Color.parseColor("#32CD32"))
                }
                currentButton = currentButton.plus(placedButtons[j])

            }else{
                do {
                    var overlapping = false

                    placedButtons[j].x = (1..maxX).random().toFloat()
                    placedButtons[j].y = (1..maxY).random().toFloat()

                    for (but in currentButton) {
                        overlapping = isOverlaping(but, placedButtons[j])
                        if (overlapping){
                            break
                        }
                    }
                } while (overlapping)
                placedButtons[j].setBackgroundColor(Color.parseColor("#D3D3D3"))
                placedButtons[j].isVisible = true
                currentButton = currentButton.plus(placedButtons[j])
            }
        }
    }

    private fun showButtons(){
        var new = temp.toString()
        buttonList = buttonList.plus(new)
        temp = ""
        for (but in placedButtons.indices){
            if (but != 0 && isIndication == "yes"){
                placedButtons[but].setBackgroundColor(Color.parseColor("#D3D3D3"))
            }
            placedButtons[but].visibility = View.VISIBLE
            allPressed[but] = false
        }
    }

    override fun onClick(view: View?) {
        Log.d(TEST_KEY, "in")
        if (view != ui.pause && view != null){
            Log.d(TEST_KEY, "in2")
            temp+=numberOfButtons
            if (view.id == numberOfButtons){
                Log.d(TEST_KEY, "in3")
                if (!endGame && allPressed[numberOfButtons - 2]) {
                    Log.d(TEST_KEY, "hahahh")
                    view.visibility = View.GONE
                    hasRepaeted++
                    clickedList[numberOfButtons - 1]++
                    Log.d(TEST_KEY, hasRepaeted.toString())
                    ui.repeatText.text = hasRepaeted.toString()

                    if (goalText == "repeat"){
                        ui.durationBar.progress = timeOrCount - hasRepaeted
                        ui.durationMapping.text = String.format("%s T remaining",timeOrCount - hasRepaeted)
                    }

                    if (hasRepaeted == timeOrCount){
                        completed = true
                        endGame = true
                        gameOver()
                    }
                    Log.d(TEST_KEY, temp)
                    if (isRandom == "yes"){
                        setRandomPosition()
                    }else{
                        showButtons()
                    }
                }
            }
        }
    }

    private fun pauseDialog() {
        var dialog = AlertDialog.Builder(this@PlayActivity)
        dialog.setTitle("Hi, Dear")
            .setMessage("Do you want to continue?")
            .setPositiveButton("Yse") { dialog, which ->
                startTimer()
            }
            .setNegativeButton("No") { dialog, which ->
                gameOver()
            }
            .setCancelable(false)
            .show()
    }

    private fun b2exeDialog() {
        var dialog = AlertDialog.Builder(this@PlayActivity)
        dialog.setTitle("Hi, Dear")
            .setMessage("Do you want to end this training?")
            .setPositiveButton("Yse") { dialog, which ->
                gameOver()
            }
            .setNegativeButton("No") { dialog, which ->
                startTimer()
            }
            .setCancelable(false)
            .show()
    }

    private fun uploadHistory(){
        var db = Firebase.firestore
        var currentTime = dateFormat.format(Date())
        var historyCollection = db.collection("histories")
        Log.d(TEST_KEY,"daozhelma")
        var history = History(
            id = thisID,
            startTime = thisID,
            endTime = currentTime,
            repeat = hasRepaeted,
            duration = playedDuration,
            completed = endGame,
            gameMode = exeText,
            //list = buttonList
        )

        historyCollection
            .add(history)
            .addOnSuccessListener {
                Log.d(FIREBASE_TAG, "history upload successfully")
            }
            .addOnFailureListener{
                Log.d(FIREBASE_TAG, "history upload error")
            }

    }

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
            setPic(gameOver.picture)
            var file = Uri.fromFile(File(currentPhotoPath))

            var storage = FirebaseStorage.getInstance().reference.child("images/${thisID}.jpg")
            //var storage = FirebaseStorage.getInstance().getReference("images/${thisID}.jpg")
            storage.putFile(file)
                .addOnSuccessListener { Log.d(FIREBASE_TAG, "upload successful") }
                .addOnFailureListener{Log.d(FIREBASE_TAG, "upload failure") }
        }else if (requestCode == 100 && resultCode == RESULT_OK){
            uri = data?.data!!
            gameOver.picture.setImageURI(uri)

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

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, 100)
    }
}




