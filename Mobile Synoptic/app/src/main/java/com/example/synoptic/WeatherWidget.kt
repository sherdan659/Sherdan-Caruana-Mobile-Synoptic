package com.example.synoptic

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import com.example.synoptic.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import java.util.TimerTask

class WeatherWidget : AppWidgetProvider() {

    companion object {

        //Battery

        private var timer: Timer? = null
        lateinit var receiver: BatteryBroadcastReceiver
        fun startBatteryTimer(context: Context) {
            Log.d("startBatteryTimer", "startBatteryTimer")

            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {

                    receiver = BatteryBroadcastReceiver()

                    // Register the receiver
                    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    context.registerReceiver(receiver, filter)

                    Log.d("BatteryBroadcastReceiverAftereverythingbeforeif", "State: ${BatteryBroadcastReceiver.batteryState}")


                    if (BatteryBroadcastReceiver.batteryState == "NORMAL") {
                        Log.d("BatteryBroadcastReceiver", "Battery state: ${BatteryBroadcastReceiver.batteryState}")
                        val batteryLevel = getBatteryLevel(context)
                        updateBatteryImage(context, batteryLevel)
                    }

                    else if (BatteryBroadcastReceiver.batteryState == "LOW") {
                        Log.d("BatteryBroadcastReceiver", "Battery state: ${BatteryBroadcastReceiver.batteryState}")

                        val views = RemoteViews(context.packageName, R.layout.weather_widget)
                        val resourceId = R.drawable.red_battery
                        views.setImageViewResource(R.id.batteryImageView, resourceId)

                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisWidget = ComponentName(context, WeatherWidget::class.java)
                        appWidgetManager.updateAppWidget(thisWidget, views)
                    }
                    else if (BatteryBroadcastReceiver.batteryState == "CHARGING") {
                        Log.d("BatteryBroadcastReceiver", "Battery state: ${BatteryBroadcastReceiver.batteryState}")

                        val views = RemoteViews(context.packageName, R.layout.weather_widget)
                        val resourceId = R.drawable.charging_battery
                        views.setImageViewResource(R.id.batteryImageView, resourceId)

                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val thisWidget = ComponentName(context, WeatherWidget::class.java)
                        appWidgetManager.updateAppWidget(thisWidget, views)
                    }
                }
            }, 0, 3600000) // 5000 just for testing  (5seconds) Needs to be 1Hour (3600000)
        }



        private fun getBatteryLevel(context: Context): Int {
            Log.d("getBatteryLevel", "getBatteryLevel")
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        private fun updateBatteryImage(context: Context, batteryLevel: Int) {
            val views = RemoteViews(context.packageName, R.layout.weather_widget)

            val resourceId = determineBatteryLevelImage(batteryLevel)

            views.setImageViewResource(R.id.batteryImageView, resourceId)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WeatherWidget::class.java)
            appWidgetManager.updateAppWidget(thisWidget, views)
        }

        fun determineBatteryLevelImage(batteryLevel: Int): Int {
            Log.d("determineBatteryLevelImage", "determineBatteryLevelImage")
            Log.d("BatteryLevel", "Battery level: $batteryLevel")

            return when {
                batteryLevel >= 75 -> R.drawable.green_battery
                batteryLevel in 45..74 -> R.drawable.yellow_battery
                batteryLevel in 10..44 -> R.drawable.orange_battery
                else -> R.drawable.no_image
                //broadcast will do the red battery image
            }
        }






        // Weather
        private const val API_URL = "https://api.jamesdecelis.com/api/v1/weather/"

        fun updateWeather(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, WeatherWidget::class.java)
            )

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val selectedCity = loadSelectedCity(context)
            val weatherData = fetchWeatherData(selectedCity)

            val views = RemoteViews(context.packageName, R.layout.weather_widget)
            weatherData?.let { data ->
                val parsedData = parseWeatherData(data)
                if (parsedData != null) {
                    val (temperature, status, location) = parsedData
                    views.setTextViewText(R.id.locationTextView, location)
                    views.setTextViewText(R.id.statusTextView, status)
                    views.setTextViewText(R.id.temperatureTextView, "$temperature°C")
                } else {
                    // Set default values if weather data is not available
                    views.setTextViewText(R.id.locationTextView, "N/A")
                    views.setTextViewText(R.id.statusTextView, "N/A")
                    views.setTextViewText(R.id.temperatureTextView, "N/A")
                }
            }

            // Hide the warning text view
            views.setViewVisibility(R.id.warningTextView, View.GONE)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun loadSelectedCity(context: Context): String {
            val sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(MainActivity.PREF_SELECTED_CITY, "Api not found") ?: "Api not found"
        }

        fun fetchWeatherData(city: String): String? {
            return try {
                val apiUrl = "$API_URL$city"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                try {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    response.toString()
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        fun parseWeatherData(weatherData: String): Triple<String, String, String>? {
            return try {
                val json = JSONObject(weatherData)
                val temperature = json.optInt("temp", -273).toString()
                val status = json.optString("condition", "Unknown")
                val location = json.optString("name", "N/A")
                Triple(temperature, status, location)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }
}