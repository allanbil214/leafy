package com.allanbil214.leafyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import android.graphics.BitmapFactory

class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plantTextView: TextView = itemView.findViewById(R.id.plantTextView)
        val diseaseTextView: TextView = itemView.findViewById(R.id.diseaseTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val outputTextView: TextView = itemView.findViewById(R.id.outputTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.plantTextView.text = historyItem.plant ?: "Unknown Plant"
        holder.diseaseTextView.text = historyItem.disease ?: "Unknown Disease"
        holder.outputTextView.text = historyItem.output ?: "No Output"

        // Display the image if available
        if (!historyItem.imageBase64.isNullOrEmpty()) {
            val imageBytes = Base64.decode(historyItem.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.imageView.setImageBitmap(bitmap)
            holder.imageView.visibility = View.VISIBLE
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = historyList.size
}
