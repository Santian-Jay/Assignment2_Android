package au.edu.utas.username.a1_android.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.username.a1_android.HistoryActivity
import au.edu.utas.username.a1_android.HistoryDetailsActivity
import au.edu.utas.username.a1_android.R
import au.edu.utas.username.a1_android.databinding.FragmentHistoryBinding
import au.edu.utas.username.a1_android.databinding.GameOverBinding
import au.edu.utas.username.a1_android.databinding.HistoryDetailsBinding
import au.edu.utas.username.a1_android.databinding.MyListItemBinding
import com.google.android.gms.common.util.ProcessUtils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

const val FIREBASE_TAG = "FirebaseLogging"
const val HISTORY_INDEX = "History_Index"

val items = mutableListOf<History>()
private lateinit var inflaterView : FragmentHistoryBinding

class AllHistoryFragment : Fragment() {
    private lateinit var details : HistoryDetailsBinding
    private lateinit var detailsBuilder : AlertDialog.Builder
    private lateinit var detail : AlertDialog
    private var currentIndex = -1
    var itemID = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflaterView = FragmentHistoryBinding.inflate(layoutInflater, container, false)

        details = HistoryDetailsBinding.inflate(layoutInflater)

        detailsBuilder = AlertDialog.Builder(this.context)
        detailsBuilder.setView(details.root)
        detail = detailsBuilder.create()

        inflaterView.historyList.adapter = HistoryAdapter(history = items)

        inflaterView.historyList.layoutManager = LinearLayoutManager(this.context)

        details.cancel.setOnClickListener(){
            detail.cancel()
        }

        val db = Firebase.firestore

        var allHistoryCollection = db.collection("histories")

        allHistoryCollection
            .get()
            .addOnSuccessListener { result ->
                items.clear() //this line clears the list, and prevents a bug where items would be duplicated upon rotation of screen
                for (document in result)
                {
                    val history = document.toObject<History>()
                    history.id = document.id
                    Log.d(FIREBASE_TAG, history.toString())
                    items.add(history)
                }
                (inflaterView.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
                inflaterView.loading.visibility = View.GONE
            }

        details.delete.setOnClickListener(){
            var dialog = AlertDialog.Builder(this.activity)
            dialog.setTitle("Hi Dear")
                .setMessage("Confirm delete?")
                .setPositiveButton("yes"){ dialog, _ ->
                    val db = Firebase.firestore
                    val histories = db.collection("histories")
                    histories.document(itemID)
                        .delete()
                        .addOnSuccessListener {
                            items.removeAt(currentIndex)
                            Log.d(FIREBASE_TAG, currentIndex.toString())
                            (inflaterView.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
                            Log.d(FIREBASE_TAG, "successfully deleted")
                            detail.cancel()
                        }
                        .addOnFailureListener{( Log.d(FIREBASE_TAG, "failed to delete"))}
                }
                .setNegativeButton("No"){dialog, _ ->
                    detail.cancel()
                }.show()
        }

        details.share.setOnClickListener(){
            var shareDialog = AlertDialog.Builder(this.context)
            shareDialog
                .setTitle("Hi Dear")
                .setMessage("Share this record?")
                .setPositiveButton("Yes, right now!"){shareDialog, _ ->
                    var thisItem = items[currentIndex].format()
                    var sendItent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, thisItem)
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(sendItent, "Share via"))
                }
                .setNegativeButton("No, next time"){shareDialog, _ ->
                    shareDialog.cancel()
                }
                .show()
        }

        return inflaterView.root
    }

    //inner class PersonHolder(var ui: FragmentHistoryBinding) : RecyclerView.ViewHolder(ui.root) {}
    //inner class HistoryHolder(var ui: FragmentHistoryBinding) : RecyclerView.ViewHolder(ui.root) {}
    inner class HistoryHolder(var ui: MyListItemBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class HistoryAdapter(private val history: MutableList<History>) : RecyclerView.Adapter<HistoryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllHistoryFragment.HistoryHolder {
            val ui = MyListItemBinding.inflate(layoutInflater, parent, false)
            return HistoryHolder(ui)
        }

        override fun onBindViewHolder(holder: AllHistoryFragment.HistoryHolder, @SuppressLint("RecyclerView") position: Int) {
            //currentIndex = position
            val history = history[position]

            holder.ui.startTime.text = history.startTime
            holder.ui.endTime.text = history.endTime
            holder.ui.completed.text = history.completed.toString()
            holder.ui.duration.text = String.format("%s Seconds",history.duration.toString())
            holder.ui.repeatCount.text = String.format("%s Times", history.repeat.toString())

            holder.ui.root.setOnClickListener(){
                currentIndex = position
                Log.d(FIREBASE_TAG, currentIndex.toString())
                itemID = history.id.toString()
                details.startTime.text = history.startTime
                details.endTime.text = history.endTime
                details.completed.text = history.completed.toString()
                details.duration.text = String.format("%s Seconds",history.duration.toString())
                details.repeatCount.text = String.format("%s Times", history.repeat.toString())
                detail.show()
            }
        }

        override fun getItemCount(): Int {
            return history.size
        }
    }

    override fun onResume() {
        super.onResume()

        inflaterView.historyList.adapter?.notifyDataSetChanged()
    }
}

