package au.edu.utas.username.a1_android.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.username.a1_android.databinding.FragmentHistoryBinding
import au.edu.utas.username.a1_android.databinding.MyListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class RecentlyDeletedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var inflaterView = FragmentHistoryBinding.inflate(layoutInflater, container, false)

        inflaterView.historyList.adapter = HistoryAdapter(history = items)

        inflaterView.historyList.layoutManager = LinearLayoutManager(this.context)

        val db = Firebase.firestore

        var historyCollection = db.collection("histories")

        historyCollection
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
                (inflaterView.historyList.adapter as RecentlyDeletedFragment.HistoryAdapter).notifyDataSetChanged()
                inflaterView.loading.visibility = View.GONE
            }

        return inflaterView.root

    }

    inner class HistoryHolder(var ui: MyListItemBinding) : RecyclerView.ViewHolder(ui.root) {}

    inner class HistoryAdapter(private val history: MutableList<History>) : RecyclerView.Adapter<RecentlyDeletedFragment.HistoryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentlyDeletedFragment.HistoryHolder {
            val ui = MyListItemBinding.inflate(layoutInflater, parent, false)
            return HistoryHolder(ui)
        }

        override fun onBindViewHolder(holder: RecentlyDeletedFragment.HistoryHolder, position: Int) {
            val history = history[position]
//            holder.ui.txtName.text = history.title
//            holder.ui.txtYear.text = history.year.toString()
        }

        override fun getItemCount(): Int {
            return history.size
        }
    }
}