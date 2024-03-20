package com.example.synoptic

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synoptic.BatteryBroadcastReceiver.Companion.counts

class DataDisplay : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)

        val recyclerView = findViewById<RecyclerView>(R.id.rvList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load data from SharedPreferences
        val sharedPreferences = getSharedPreferences("com.example.synoptic.PREFS", Context.MODE_PRIVATE)
        val chargingStateItems = mutableListOf<String>()
        sharedPreferences.all.entries.forEach { entry ->
            val key = entry.key
            val value = entry.value
            if (value is String && key.startsWith("charging_state")) {
                Log.d("ItemKey", key)
                Log.d("ItemValue", value)
                chargingStateItems.add(value)
            }
        }
        Log.d("Itmes", chargingStateItems.toString())
        // Pass the data to the adapter

        val adapter = ChargingStateAdapter(chargingStateItems)
        recyclerView.adapter = adapter
    }
}