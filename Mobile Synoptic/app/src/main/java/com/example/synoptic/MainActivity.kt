package com.example.synoptic

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "WeatherWidgetPrefs"
        const val PREF_SELECTED_CITY = "selectedCity"
    }

    private lateinit var cityMenu: Spinner
    private lateinit var updateButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityMenu = findViewById(R.id.cityMenu)
        updateButton = findViewById(R.id.updateButton)
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupCityMenu()
        setupUpdateButton()
    }

    private fun setupCityMenu() {
        val cities = arrayOf("Valletta", "Paris", "Rome")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cityMenu.adapter = adapter

        val savedCity = sharedPreferences.getString(PREF_SELECTED_CITY, null)
        val cityIndex = cities.indexOf(savedCity)
        if (cityIndex != -1) {
            cityMenu.setSelection(cityIndex)
        }
    }

    private fun setupUpdateButton() {
        updateButton.setOnClickListener {
            val selectedCity = cityMenu.selectedItem.toString()
            saveSelectedCity(selectedCity)
            GlobalScope.launch {
                WeatherWidget.updateWeather(this@MainActivity)
            }
        }
    }

    private fun saveSelectedCity(city: String) {
        sharedPreferences.edit().putString(PREF_SELECTED_CITY, city).apply()
    }
}