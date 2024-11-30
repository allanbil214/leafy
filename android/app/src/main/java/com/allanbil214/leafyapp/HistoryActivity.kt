package com.allanbil214.leafyapp

import android.os.Bundle
import android.widget.TextView
import android.view.View
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
            deleteHistoryItem(historyItem, position)
        }

        recyclerView.adapter = adapter
        togglePlaceholderVisibility(historyList)
    }

    private fun deleteHistoryItem(historyItem: HistoryItem, position: Int) {
        try {
            // Remove from adapter first
            adapter.removeItem(position)

            // Then update the stored data
            val updatedList = historyManager.loadHistory().toMutableList()
            updatedList.removeAll { it == historyItem }
            historyManager.saveHistory(updatedList)

            // Update placeholder visibility
            togglePlaceholderVisibility(updatedList)
        } catch (e: Exception) {
            // Handle any potential errors
            e.printStackTrace()
        }
    }

    private fun togglePlaceholderVisibility(historyList: List<HistoryItem>) {
        placeholderTextView.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
    }
}

