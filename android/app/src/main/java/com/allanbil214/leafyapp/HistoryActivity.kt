package com.allanbil214.leafyapp

import android.os.Bundle
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: HistoryAdapter
    private lateinit var historyManager: HistoryManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyManager = HistoryManager(this)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderTextView = findViewById(R.id.placeholderTextView)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val historyList = historyManager.loadHistory().toMutableList()
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(historyList) { historyItem, position ->
            showDeleteConfirmationDialog(historyItem, position)
        }

        recyclerView.adapter = adapter
        togglePlaceholderVisibility(historyList)
    }

    private fun showDeleteConfirmationDialog(historyItem: HistoryItem, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete History")
            .setMessage("Are you sure you want to delete this history item?")
            .setPositiveButton("Delete") { _, _ ->
                deleteHistoryItem(historyItem, position)
            }
            .setNegativeButton("Cancel") { _, _ ->
                adapter.resetDeletionState(position)
            }
            .setOnCancelListener {
                adapter.resetDeletionState(position)
            }
            .create()
            .show()
    }

    private fun deleteHistoryItem(historyItem: HistoryItem, position: Int) {
        try {
            adapter.removeItem(position)

            val updatedList = historyManager.loadHistory().toMutableList()
            updatedList.removeAll { it == historyItem }
            historyManager.saveHistory(updatedList)

            togglePlaceholderVisibility(updatedList)
        } catch (e: Exception) {
            e.printStackTrace()
            adapter.resetDeletionState(position)
        }
    }

    private fun togglePlaceholderVisibility(historyList: List<HistoryItem>) {
        placeholderTextView.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
    }
}