package com.example.synoptic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatteryBroadcastReceiver : BroadcastReceiver() {


    companion object {
        var batteryState: String = "NORMAL"
        var previousChargingState: Boolean = false
        var counts = 1
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BatteryBroadcastReceiveronReceive", "BatteryBroadcastReceiveronReceive")

        val action = intent.action

        if (action == Intent.ACTION_BATTERY_CHANGED) {
            val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING

            batteryState = when {
                isCharging -> "CHARGING"
                batteryLevel <= 10 -> "LOW"
                else -> "NORMAL"
            }

            Log.d("BatteryBroadcastReceiver", "Battery level: $batteryLevel%, State: $batteryState")


            if (isCharging && previousChargingState != isCharging) {
                // Save charging state in SharedPreferences
                savesharedpreferences(context, batteryState)
            }

            previousChargingState = isCharging
        }
    }

    private fun savesharedpreferences(context: Context, state: String) {
        val currentTime = SimpleDateFormat("dd-MM-yyyy-ss-mm-HH", Locale.getDefault()).format(Date())
        val sharedPref = context.getSharedPreferences(
            "com.example.synoptic.PREFS",
            Context.MODE_PRIVATE
        )
        with(sharedPref.edit()) {
            putString("charging_state_$currentTime", "$state,$currentTime")
            apply()
        }
        Log.d("BatteryBroadcastReceiver", "Charging state saved: $state, Time: $currentTime")




    }
}
