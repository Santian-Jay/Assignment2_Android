package au.edu.utas.username.a1_android

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import au.edu.utas.username.a1_android.databinding.ActivityMainBinding

const val PREFERENCES_FILE = "perfs"
const val USERNAME_KEY = "Shy"
class MainActivity : AppCompatActivity() {

    private lateinit var ui : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        var settings = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        ui.userName.text = settings.getString(USERNAME_KEY, "Shy")?.toEditable()
        ui.userName.addTextChangedListener(object : TextWatcher{
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

        ui.userName.setOnEditorActionListener{ v, actionId, event ->
            with(settings.edit()){
                putString(USERNAME_KEY, ui.userName.text.toString())
                apply()
            }
            true
        }

        ui.exercise.setOnClickListener(){
            val exercise = Intent(this, ExerciseActivity::class.java)
            startActivity(exercise)
        }


        ui.history.setOnClickListener(){
            val history = Intent(this, HistoryActivity::class.java)
            startActivity(history)
        }

        ui.profile.setOnClickListener(){
            val profile = Intent(this, ProfileActivity::class.java)
            startActivity(profile)
        }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}