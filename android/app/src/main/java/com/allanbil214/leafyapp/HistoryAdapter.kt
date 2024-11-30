package com.allanbil214.leafyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import android.graphics.BitmapFactory
import io.noties.markwon.Markwon

class HistoryAdapter(
    private val historyList: MutableList<HistoryItem>,
    private val onDeleteClicked: (HistoryItem, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plantTextView: TextView = itemView.findViewById(R.id.plantTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val diseaseTextView: TextView = itemView.findViewById(R.id.diseaseTextView)
        val diseaseInfoTextView: TextView = itemView.findViewById(R.id.diseaseInfoTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        var isExpanded = false
    }

    private var markwon: Markwon? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)

        if (markwon == null) {
            markwon = Markwon.create(parent.context)
        }

        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.plantTextView.text = historyItem.plant ?: "Unknown Plant"
        holder.diseaseTextView.text = historyItem.disease ?: "Unknown Disease"
        holder.dateTextView.text = historyItem.date ?: "Unknown Date"

        val markdownText = historyItem.output ?: "No Information"

        // Set initial text based on expansion state
        if (holder.isExpanded) {
            markwon?.setMarkdown(holder.diseaseInfoTextView, markdownText)
        } else {
            holder.diseaseInfoTextView.text = "Tap to read disease information..."
        }

        // Set initial expansion state
        holder.diseaseInfoTextView.maxLines = if (holder.isExpanded) Int.MAX_VALUE else 1

        // Display the image
        if (!historyItem.imageBase64.isNullOrEmpty()) {
            val imageBytes = Base64.decode(historyItem.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.imageView.setImageBitmap(bitmap)
        }

        // Set up click listeners for expansion
        val clickableViews = listOf(
            holder.plantTextView,
            holder.diseaseTextView,
            holder.diseaseInfoTextView,
            holder.imageView
        )

        clickableViews.forEach { view ->
            view.setOnClickListener {
                toggleExpansion(holder, markdownText)
            }
        }

        // Handle delete button click with position
        holder.deleteButton.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClicked(historyItem, adapterPosition)
            }
        }
    }

    private fun toggleExpansion(holder: HistoryViewHolder, markdownText: String) {
        holder.isExpanded = !holder.isExpanded
        if (holder.isExpanded) {
            holder.diseaseInfoTextView.maxLines = Int.MAX_VALUE
            markwon?.setMarkdown(holder.diseaseInfoTextView, markdownText)
        } else {
            holder.diseaseInfoTextView.maxLines = 1
            holder.diseaseInfoTextView.text = "Tap to read disease information..."
        }
    }

    override fun getItemCount(): Int = historyList.size

    fun removeItem(position: Int) {
        if (position in 0 until historyList.size) {
            historyList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, historyList.size)
        }
    }
}