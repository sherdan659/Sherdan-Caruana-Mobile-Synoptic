package com.example.synoptic

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessageHandler : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("onMessageReceived", "This is a message")
        super.onMessageReceived(remoteMessage)
        // Extract the weather information from the FCM message
        val extremeWeatherWarning = remoteMessage.data["weather_warning"]

        // Display a notification for the extreme weather warning
        extremeWeatherWarning?.let {
            updateWidgetWithWarning(this, it)
        }
    }

    private fun updateWidgetWithWarning(context: Context, weatherWarning: String) {
        Log.d("updateWidgetWithWarning", "This is a message")

        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        // Show the warning text view
        views.setViewVisibility(R.id.warningTextView, View.VISIBLE)
        views.setTextViewText(R.id.warningTextView, weatherWarning)

        // Update the widget
        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, WeatherWidget::class.java)
        widgetManager.updateAppWidget(widgetComponent, views)
    }
}