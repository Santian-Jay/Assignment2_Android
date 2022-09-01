package au.edu.utas.username.a1_android

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import au.edu.utas.username.a1_android.databinding.ActivityHistoryBinding
import au.edu.utas.username.a1_android.databinding.ActivityHistoryDetailsBinding
import au.edu.utas.username.a1_android.ui.main.HISTORY_INDEX
import au.edu.utas.username.a1_android.ui.main.items
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryDetailsActivity : AppCompatActivity() {
    private var itemID = ""
    private lateinit var ui: ActivityHistoryDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityHistoryDetailsBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val historyID = intent.getIntExtra(HISTORY_INDEX, -1)
        var historyObject = items[historyID]

        ui.startTime.text = historyObject.startTime
        ui.endTime.text = historyObject.endTime
        ui.completed.text = historyObject.completed.toString()
        ui.duration.text = String.format("%s Seconds",historyObject.duration.toString())
        ui.repeatCount.text = String.format("%s Times", historyObject.repeat.toString())
        itemID = historyObject.id.toString()

        ui.delete.setOnClickListener(){
            Log.d(au.edu.utas.username.a1_android.ui.main.FIREBASE_TAG, "yunxinglma")
            var dialog = AlertDialog.Builder(this)
            dialog.setTitle("Hi Dear")
                .setMessage("Confirm delete?")
                .setPositiveButton("yes"){ dialog, _ ->
                    val db = Firebase.firestore
                    val histories = db.collection("histories")
                    histories.document(itemID)
                        .delete()
                        .addOnSuccessListener { Log.d(au.edu.utas.username.a1_android.ui.main.FIREBASE_TAG, "successfully deleted") }
                        .addOnFailureListener{( Log.d(au.edu.utas.username.a1_android.ui.main.FIREBASE_TAG, "failed to delete"))}
                }
                .setNegativeButton("No"){dialog, _ ->
                }
        }
    }
}