package au.edu.utas.username.a1_android

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import au.edu.utas.username.a1_android.databinding.ActivityExerciseBinding
import au.edu.utas.username.a1_android.databinding.ActivityMainBinding
import kotlin.math.log
import android.util.Log
import android.widget.SeekBar
import androidx.core.view.isVisible

const val MODECHOOSE_KEY : String = "MODECHOOSE"
const val GOALTEXT_KEY : String = "GOALTEXT"
const val TIMEORCOUNT_KEY : String = "TIMEORCOUNT"
const val EXETEXT_KEY : String = "EXETEXT"
const val ISRANDOM_KEY : String = "ISRANDOM"
const val ISINDICATION_KEY : String = "ISINDICATION"
const val BUTTONSIZE_KEY : String = "BUTTONSIZE"
const val NUMBEROFBUTTON_KEY: String = "NUMBEROFBUTTON"
class ExerciseActivity : AppCompatActivity() {

    private lateinit var ui : ActivityExerciseBinding
    private var modeText = "free"
    private var goalText = "time"
    private var timeOrCount = ""
    private var exeText = "exe1"
    private var isRandom = ""
    private var isIndication = ""
    private var goalsVisible = false
    private var setTimeOrCount = 60
    private var buttonSizeText = "small"
    private var numberOfButtons = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.modeChoose.setOnCheckedChangeListener { _, checkedID ->
            if(checkedID == ui.freeMode.id){
                modeText = "free"
                ui.linearGoal.isVisible = false
                ui.goalSet.isVisible = false
                goalsVisible = false
            }else{
                modeText = "target"
                ui.linearGoal.isVisible = true
                ui.goalSet.isVisible = true
                goalsVisible = true
            }
        }

        ui.goals.setOnCheckedChangeListener { _, checkedID ->
            if(checkedID == ui.timeGoal.id){
                goalText = "time"
                timeOrCount = "time"
                ui.unit.text = "S"
                ui.add10s.text = "+ 10s"
                ui.add30s.text = "+ 30s"
                ui.add60s.text = "+ 60s"
            }else{
                goalText = "repeat"
                timeOrCount = "repeat"
                ui.unit.text = "T"
                ui.add10s.text = "+ 10t"
                ui.add30s.text = "+ 30t"
                ui.add60s.text = "+ 60t"
            }
        }

        ui.exeChoose.setOnCheckedChangeListener { _, checkedID ->
            if(checkedID == ui.exercise1.id){
                exeText = "exe1"
                //ui.customization.isVisible = false
            }else{
                exeText = "exe2"
                //ui.customization.isVisible = true
            }
        }

        ui.random.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (ui.random.isChecked){
                isRandom = "yes"
                Log.d(MODECHOOSE_KEY, isRandom)
            }else{
                isRandom = "no"
                Log.d(MODECHOOSE_KEY, isRandom)
            }
        }

        ui.indication.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (ui.indication.isChecked){
                isIndication = "yes"
                Log.d(MODECHOOSE_KEY, isIndication)
            }else{
                isIndication = "no"
                Log.d(MODECHOOSE_KEY, isIndication)
            }
        }

        ui.add10s.setOnClickListener(){
            if (ui.seekBar.progress < 300 || setTimeOrCount < 300) {
                setTimeOrCount += 10
                ui.seekBar.progress += 10
                ui.setTime.text = setTimeOrCount.toString()
            }else{
                createDialog()
            }
        }

        ui.add30s.setOnClickListener(){
            if (ui.seekBar.progress < 300 || setTimeOrCount < 300){
                setTimeOrCount += 30
                ui.seekBar.progress += 30
                ui.setTime.text = setTimeOrCount.toString()
            }else{
                createDialog()
            }
        }

        ui.add60s.setOnClickListener(){
            if (ui.seekBar.progress < 300 || setTimeOrCount < 300) {
                setTimeOrCount += 60
                ui.seekBar.progress += 60
                ui.setTime.text = setTimeOrCount.toString()
            }else{
                createDialog()
            }
        }

        ui.small.setOnClickListener(){
            buttonSizeText = "small"
            ui.small.setBackgroundColor(Color.parseColor("#32CD32"))
            ui.mid.setBackgroundColor(Color.GRAY)
            ui.large.setBackgroundColor(Color.GRAY)
            Log.d(MODECHOOSE_KEY, buttonSizeText)
        }

        ui.mid.setOnClickListener(){
            buttonSizeText = "mid"
            ui.mid.setBackgroundColor(Color.parseColor("#32CD32"))
            ui.small.setBackgroundColor(Color.GRAY)
            ui.large.setBackgroundColor(Color.GRAY)
            Log.d(MODECHOOSE_KEY, buttonSizeText)
        }

        ui.large.setOnClickListener(){
            buttonSizeText = "large"
            ui.large.setBackgroundColor(Color.parseColor("#32CD32"))
            ui.mid.setBackgroundColor(Color.GRAY)
            ui.small.setBackgroundColor(Color.GRAY)
            Log.d(MODECHOOSE_KEY, buttonSizeText)
        }

        ui.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean){
                ui.setTime.text = "$p1"
                setTimeOrCount = p1
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //Log.d(MODECHOOSE_KEY, modeText)
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //Log.d(MODECHOOSE_KEY, modeText)
            }
        })

        ui.barOfButton.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                ui.numberOfButton.text = "$p1"
                numberOfButtons = p1
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        ui.minus.setOnClickListener(){
            if (numberOfButtons > 3 ){
                numberOfButtons -= 1
                ui.barOfButton.progress -= 1
            }else{
                numberOfButtons = 3
                showInfo(numberOfButtons)
            }
            ui.numberOfButton.text = numberOfButtons.toString()
        }

        ui.plus.setOnClickListener(){
            if (numberOfButtons < 5){
                numberOfButtons += 1
                ui.barOfButton.progress += 1
            }else{
                numberOfButtons = 5
                showInfo(numberOfButtons)
            }
            ui.numberOfButton.text = numberOfButtons.toString()
        }


        ui.startGame.setOnClickListener(){
            val startGame = Intent(this, PlayActivity::class.java)
            val start2Game = Intent(this, Play2Activity::class.java)
            startGame.putExtra(MODECHOOSE_KEY, modeText)
            startGame.putExtra(GOALTEXT_KEY, goalText)
            startGame.putExtra(EXETEXT_KEY, exeText)
            startGame.putExtra(ISRANDOM_KEY, isRandom)
            startGame.putExtra(ISINDICATION_KEY, isIndication)
            startGame.putExtra(TIMEORCOUNT_KEY, setTimeOrCount)
            startGame.putExtra(BUTTONSIZE_KEY, buttonSizeText)
            startGame.putExtra(NUMBEROFBUTTON_KEY, numberOfButtons)

            start2Game.putExtra(MODECHOOSE_KEY, modeText)
            start2Game.putExtra(GOALTEXT_KEY, goalText)
            start2Game.putExtra(EXETEXT_KEY, exeText)
            start2Game.putExtra(ISRANDOM_KEY, isRandom)
            start2Game.putExtra(ISINDICATION_KEY, isIndication)
            start2Game.putExtra(TIMEORCOUNT_KEY, setTimeOrCount)
            start2Game.putExtra(BUTTONSIZE_KEY, buttonSizeText)
            start2Game.putExtra(NUMBEROFBUTTON_KEY, numberOfButtons)
            when(exeText){
                "exe1" -> startActivity(startGame)
                else -> startActivity(start2Game)
            }
        }
    }

    private fun createDialog(){
        var dialog = AlertDialog.Builder(this@ExerciseActivity)
        dialog.setTitle("Hi, Dear")
            .setMessage("The maximum value is $setTimeOrCount")
            .setCancelable(true)
            .show()
    }

    private fun showInfo(n: Int){
        var dialog = AlertDialog.Builder(this@ExerciseActivity)
        dialog.setTitle("Hi, Dear")
                when(n){
                    3 -> dialog.setMessage("The minimum value is $n")
                    else -> dialog.setMessage("The maximum value is $n")
                }
            .setCancelable(true)
            .show()
    }
}