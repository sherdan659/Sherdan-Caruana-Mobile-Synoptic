package com.example.synoptic

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherWidget : AppWidgetProvider() {

    companion object {
        private const val API_URL = "https://api.jamesdecelis.com/api/v1/weather/"

        suspend fun updateWeather(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, WeatherWidget::class.java)
            )

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private suspend fun updateAppWidget(
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
                    views.setTextViewText(R.id.locationTextView, "N/A")
                    views.setTextViewText(R.id.statusTextView, "N/A")
                    views.setTextViewText(R.id.temperatureTextView, "N/A")
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private suspend fun loadSelectedCity(context: Context): String {
            val sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(MainActivity.PREF_SELECTED_CITY, "Api not found") ?: "Api not found"
        }

        private suspend fun fetchWeatherData(city: String): String? {
            return withContext(Dispatchers.IO) {
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
            }
        }

        private fun parseWeatherData(weatherData: String): Triple<String, String, String>? {
            val json = JSONObject(weatherData)
            val temperature = json.optInt("temp", -273).toString()
            val status = json.optString("condition", "Unknown")
            val location = json.optString("name", "N/A")
            return Triple(temperature, status, location)
        }
    }
}