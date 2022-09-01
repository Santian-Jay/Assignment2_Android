package au.edu.utas.username.a1_android

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import au.edu.utas.username.a1_android.databinding.ActivityPlay2Binding
import au.edu.utas.username.a1_android.databinding.GameOverBinding
import au.edu.utas.username.a1_android.ui.main.History
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

class Play2Activity : AppCompatActivity(), View.OnLongClickListener {
    private lateinit var ui : ActivityPlay2Binding
    private lateinit var gameOver : GameOverBinding
    private lateinit var gameOverBuilder : AlertDialog.Builder
    private var dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    lateinit var currentPhotoPath : String
    private lateinit var uri : Uri
    var size = 0
    var offset = 0
    var allPressed = arrayOf<Boolean>()
    var clickedList = arrayOf<Int>()
    var totalTimeOrCount: Long = 0
    var endGame = false
    var numberOfButtons = 3
    var maxY = 0
    var maxX = 0
    var minY = 0
    var thisID = ""
    var hasRepaeted = 0
    var playedDuration = 0
    var goalText = "time"
    var timeOrCount = 60
    var endTime = ""
    var completed = false

    var userButton = arrayOf<Button>()
    var targetButton = arrayOf<Button>()
    var mapping = mutableMapOf<String,Int>()
    var hasTouched = false
    var point = 0
    var exeText = "Exercise1"
    var modeText = ""
    var isRandom = "no"

    private lateinit var countDownTimer: CountDownTimer
    private lateinit var isIndication: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityPlay2Binding.inflate(layoutInflater)
        gameOver = GameOverBinding.inflate(layoutInflater)

        gameOverBuilder = AlertDialog.Builder(this)
        gameOverBuilder.setView(gameOver.root)

        setContentView(ui.root)

        var endGame = false
        var durationTime = 0
        modeText = intent.getStringExtra(MODECHOOSE_KEY).toString()
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

        Log.d(TEST_KEY, endGame.toString())
        if(numberOfButtons == 4){
            gameOver.l4.isEnabled = true; gameOver.l4.visibility = View.VISIBLE
        }else if(numberOfButtons == 5){
            gameOver.l4.isEnabled = true; gameOver.l4.visibility = View.VISIBLE
            gameOver.l5.isEnabled = true; gameOver.l5.visibility = View.VISIBLE
        }

        when(buttonSize){
            "small" -> {size = 140; maxX = 922; maxY = 856; minY = 150}
            "mid" -> {size = 180; maxX = 880; maxY = 816; minY = 200}
            else -> {size = 220; maxX = 840; maxY = 776; minY = 230}
        }

        gameOver.camera.setOnClickListener(){
            requestToTakeAPicture()
            gameOver.picture.visibility = View.VISIBLE
        }

        gameOver.selectImage.setOnClickListener() {
            chooseImage()
        }

        offset = size

        for (i in 0 until numberOfButtons){
            clickedList = clickedList.plus(0)
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
        createTargetButton()
        createUserButton()
    }
    private fun showButtons(){
        for (but in userButton.indices){
            userButton[but].visibility = View.VISIBLE
            targetButton[but].visibility = View.VISIBLE
        }
    }


    private fun prepareGame(){
        thisID = dateFormat.format(Date())
        ui.timeText.text = dateFormat.format(Date())
        ui.durationText.text = "0"
        ui.repeatText.text = "0"
        gameOver.timeStart.text = dateFormat.format(Date())
        if (isIndication == "yes"){
            ui.hint.visibility = View.VISIBLE
        }
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
                    Log.d(TEST_KEY, remain.toString())
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
        gameOver.first.text = "Rock: "
        gameOver.second.text = "Scissor: "
        gameOver.third.text = "Paper: "
        gameOver.repeat1.text = clickedList[0].toString()
        gameOver.repeat2.text = clickedList[1].toString()
        gameOver.repeat3.text = clickedList[2].toString()
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

    private fun setRandomPosition(){
        endGame = false
        ui.durationBar.progress = timeOrCount - 1
        ui.durationMapping.text = (timeOrCount -1).toString()
        var currentButton = arrayOf<Button>()

        for (j in 0 until userButton.size){
            targetButton[j].visibility = View.VISIBLE
            if (j == 0){
                userButton[j].x = (1..maxX).random().toFloat()
                userButton[j].y = (minY..maxY).random().toFloat()
                userButton[j].isVisible = true
                currentButton = currentButton.plus(userButton[j])

            }else{
                do {
                    var overlapping = false

                    userButton[j].x = (1..maxX).random().toFloat()
                    userButton[j].y = (minY..maxY).random().toFloat()

                    for (but in currentButton) {
                        overlapping = isOverlaping(but, userButton[j])
                        if (overlapping){
                            break
                        }
                    }
                } while (overlapping)
                userButton[j].isVisible = true
                currentButton = currentButton.plus(userButton[j])
            }
        }
    }

    private fun pauseDialog() {
        var dialog = AlertDialog.Builder(this@Play2Activity)
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
        var dialog = AlertDialog.Builder(this@Play2Activity)
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
        var history = History(
            id = thisID,
            startTime = thisID,
            endTime = currentTime,
            repeat = hasRepaeted,
            duration = playedDuration,
            completed = endGame,
            gameMode = exeText,
            //list = clickedList
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

    @SuppressLint("ResourceType")
    private fun createTargetButton(){
        var button: Button = Button(this)
        var button1: Button = Button(this)
        var button2: Button = Button(this)
        var button3: Button = Button(this)
        var button4: Button = Button(this)

        button.x = 860f; button.y = 0f
        button1.x = 645f; button1.y = 0f
        button2.x = 430f; button2.y = 0f
        button3.x = 214f; button3.y = 0f
        button4.x = 0f; button4.y = 0f

        button.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            isVisible = true
            tag = "T"
            id = 3
            setBackgroundResource(R.drawable.rock_s)
        }

        button1.apply {
            layoutParams= ViewGroup.LayoutParams(size, size)
            textSize = 30f
            isVisible = true
            tag = "T"
            id = 1
            setBackgroundResource(R.drawable.scissors_s)
        }

        button2.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            height = size
            isVisible = true
            tag = "T"
            id = 2
            setBackgroundResource(R.drawable.paper_s)
        }
        button3.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            height = size
            isVisible = true
            tag = "T"
            id = 2
            setBackgroundResource(R.drawable.paper_s)
        }
        button4.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            height = size
            isVisible = true
            tag = "T"
            id = 3
            setBackgroundResource(R.drawable.rock_s)
        }
        targetButton = targetButton.plus(button)
        targetButton = targetButton.plus(button1)
        targetButton = targetButton.plus(button2)
        targetButton = targetButton.plus(button3)
        targetButton = targetButton.plus(button4)

        for (i in 0..numberOfButtons-1){
//            targetButton[i].setOnLongClickListener(this)
            targetButton[i].setOnDragListener(dragListener)
            ui.cl.addView(targetButton[i])
        }
    }

    @SuppressLint("ResourceType")
    private fun createUserButton(){
        var button: Button = Button(this)
        var button1: Button = Button(this)
        var button2: Button = Button(this)
        var button3: Button = Button(this)
        var button4: Button = Button(this)

        button.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            isVisible = true
            tag = "scissorsT"
            id = 1
            setBackgroundResource(R.drawable.rock_s)
        }

        button1.apply {
            layoutParams= ViewGroup.LayoutParams(size, size)
            textSize = 30f
            isVisible = true
            tag = "paperT"
            id = 2
            setBackgroundResource(R.drawable.scissors_s)
        }

        button2.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            height = size
            isVisible = true
            tag = "rockT"
            id = 3
            setBackgroundResource(R.drawable.paper_s)
        }
        button3.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            height = size
            isVisible = true
            tag = "rockT"
            id = 3
            setBackgroundResource(R.drawable.paper_s)
        }

        button4.apply {
            layoutParams = ConstraintLayout.LayoutParams(size, size)
            textSize = 30f
            height = size
            isVisible = true
            tag = "paperT"
            id = 2
            setBackgroundResource(R.drawable.scissors_s)
        }
        userButton = userButton.plus(button)
        userButton = userButton.plus(button1)
        userButton = userButton.plus(button2)
        userButton = userButton.plus(button3)
        userButton = userButton.plus(button4)


        for (i in 0..numberOfButtons-1){
            userButton[i].setOnLongClickListener(this)
            userButton[i].setOnDragListener(dragListener)
            ui.cl.addView(userButton[i])
        }
        setRandomPosition()
    }

    private var dragListener = View.OnDragListener{ view, dragEvent ->
        var vl = dragEvent.localState as View
        when(dragEvent.action){
            DragEvent.ACTION_DRAG_STARTED -> {
                dragEvent.clipDescription.hasMimeType((ClipDescription.MIMETYPE_TEXT_PLAIN))
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (view.id != vl.id){
                    vl.visibility = View.VISIBLE
                    hasTouched = false
                    Log.d(TEST_KEY, "haha")
                    Log.d(TEST_KEY, hasTouched.toString())
                }else if(view.tag == "T"){
                    Log.d(TEST_KEY, "hehe")
                    Log.d(TEST_KEY, hasTouched.toString())
                    hasTouched = true
                    view.visibility = View.GONE
                }
                when(vl.id){
                    1 -> clickedList[0]++
                    2 -> clickedList[1]++
                    else -> clickedList[2]++
                }
                //hasTouched = view.tag == vl.tag
                view.invalidate()
                true
            }

            DragEvent.ACTION_DRAG_LOCATION -> true

            DragEvent.ACTION_DRAG_EXITED -> {
                vl.visibility = View.GONE
                hasTouched = false
                view.invalidate()
                true
            }

            DragEvent.ACTION_DROP -> {
                var thisItem = dragEvent.clipData.getItemAt(0)
                var data = thisItem.text

                if (hasTouched){
                    point++
                }else{
                    vl.visibility = View.VISIBLE
                }

                if (point == numberOfButtons){
                    point = 0
                    hasRepaeted++
                    ui.repeatText.text = hasRepaeted.toString()

                    if (hasRepaeted == timeOrCount){
                        gameOver()
                    }

                    if (isRandom == "yes"){
                        setRandomPosition()
                    }else{
                        showButtons()
                    }
                }
                hasTouched = false
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                true
            }
            else -> false
        }
    }

    override fun onLongClick(view: View?): Boolean {
        var thisItem = ClipData.Item(view?.tag as? CharSequence)
        val dragData = ClipData(
            view?.tag as CharSequence,
            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
            thisItem
        )
        var myDragShadowBuilder = View.DragShadowBuilder(view)

        if (view != null){
            view.startDragAndDrop(dragData, myDragShadowBuilder, view, 0)
        }

        if (view != null){
            view.visibility = View.GONE
        }

        ui.cl.setOnDragListener(dragListener)
        return true
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