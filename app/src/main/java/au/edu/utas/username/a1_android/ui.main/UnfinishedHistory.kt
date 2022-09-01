package au.edu.utas.username.a1_android.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.username.a1_android.databinding.FragmentHistoryBinding
import au.edu.utas.username.a1_android.databinding.HistoryDetailsBinding
import au.edu.utas.username.a1_android.databinding.MyListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
val unfinishedItems = mutableListOf<History>()
private lateinit var inflaterView : FragmentHistoryBinding
class UnfinishedHistoryFragment : Fragment() {
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
        inflaterView.historyList.adapter = HistoryAdapter(history = unfinishedItems)

        details = HistoryDetailsBinding.inflate(layoutInflater)

        detailsBuilder = AlertDialog.Builder(this.context)
        detailsBuilder.setView(details.root)
        detail = detailsBuilder.create()

        inflaterView.historyList.layoutManager = LinearLayoutManager(this.context)

        details.cancel.setOnClickListener(){
            detail.cancel()
        }

        val db = Firebase.firestore

        var historyCollection = db.collection("histories")

        historyCollection
            .get()
            .addOnSuccessListener { result ->
                unfinishedItems.clear() //this line clears the list, and prevents a bug where items would be duplicated upon rotation of screen
                for (document in result)
                {
                    val history = document.toObject<History>()
                    history.id = document.id
                    Log.d(FIREBASE_TAG, history.toString())
                    if (history.completed.toString() == "false"){
                        unfinishedItems.add(history)
                    }
                }
                (inflaterView.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
                inflaterView.loading.visibility = View.GONE
            }

        details.delete.setOnClickListener(){
            var dialog = AlertDialog.Builder(this.activity)
            //dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.setTitle("Hi Dear")
                .setMessage("Confirm delete?")
                .setPositiveButton("yes"){ dialog, _ ->
                    val db = Firebase.firestore
                    val histories = db.collection("histories")
                    histories.document(itemID)
                        .delete()
                        .addOnSuccessListener {
                            unfinishedItems.removeAt(currentIndex)
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
                    var thisItem = unfinishedItems[currentIndex].format()
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

    inner class HistoryHolder(var ui: MyListItemBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class HistoryAdapter(private val history: MutableList<History>) : RecyclerView.Adapter<HistoryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
            val ui = MyListItemBinding.inflate(layoutInflater, parent, false)
            return HistoryHolder(ui)
        }

        override fun onBindViewHolder(holder: UnfinishedHistoryFragment.HistoryHolder, @SuppressLint(
            "RecyclerView"
        ) position: Int) {
            val history = history[position]
            holder.ui.startTime.text = history.startTime
            holder.ui.endTime.text = history.endTime
            holder.ui.completed.text = history.completed.toString()
            holder.ui.duration.text = String.format("%s Seconds",history.duration.toString())
            holder.ui.repeatCount.text = String.format("%s Times", history.repeat.toString())

            holder.ui.root.setOnClickListener(){
                currentIndex = position
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