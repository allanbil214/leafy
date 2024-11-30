package com.allanbil214.leafyapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyManager = HistoryManager(this)
        val historyList = historyManager.loadHistory()

        if (historyList.isEmpty()) {
            Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show()
        } else {
            val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = HistoryAdapter(historyList)
        }

    }
}
