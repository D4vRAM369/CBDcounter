// CBDWidgetProvider.kt - CORREGIDO
package com.d4vram.cbdcounter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class CBDWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_CBD = "com.d4vram.cbdcounter.ADD_CBD"
        const val ACTION_RESET_CBD = "com.d4vram.cbdcounter.RESET_CBD"

        // Método estático para actualizar widgets desde MainActivity
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CBDWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CBDWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ADD_CBD -> {
                addCBD(context)
                updateAllWidgets(context) // CORREGIDO: usar updateAllWidgets en lugar de updateAllWidgetsInternal
            }
            ACTION_RESET_CBD -> {
                resetCBD(context)
                updateAllWidgets(context) // CORREGIDO: usar updateAllWidgets en lugar de updateAllWidgetsInternal
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null) {
                    onUpdate(context, appWidgetManager, appWidgetIds)
                }
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.cbd_widget)

        // Obtener contador actual
        val count = getCurrentCount(context)
        val date = getCurrentDateDisplay()
        val emoji = getEmoji(count)

        // Actualizar vistas
        views.setTextViewText(R.id.widget_counter, count.toString())
        views.setTextViewText(R.id.widget_date, date)
        views.setTextViewText(R.id.widget_emoji, emoji)

        // Configurar botón +1
        val addIntent = Intent(context, CBDWidgetProvider::class.java).apply {
            action = ACTION_ADD_CBD
        }
        val addPendingIntent = PendingIntent.getBroadcast(
            context, 0, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_add_button, addPendingIntent)

        // Configurar botón reset
        val resetIntent = Intent(context, CBDWidgetProvider::class.java).apply {
            action = ACTION_RESET_CBD
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            context, 1, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_reset_button, resetPendingIntent)

        // Configurar tap para abrir la app
        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun addCBD(context: Context) {
        val sharedPrefs = context.getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)
        val today = getCurrentDateKey()
        val currentCount = sharedPrefs.getInt("count_$today", 0)

        sharedPrefs.edit()
            .putInt("count_$today", currentCount + 1)
            .apply()
    }

    private fun resetCBD(context: Context) {
        val sharedPrefs = context.getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)
        val today = getCurrentDateKey()

        sharedPrefs.edit()
            .putInt("count_$today", 0)
            .apply()
    }

    private fun getCurrentCount(context: Context): Int {
        val sharedPrefs = context.getSharedPreferences("CBDCounter", Context.MODE_PRIVATE)
        val today = getCurrentDateKey()
        return sharedPrefs.getInt("count_$today", 0)
    }

    private fun getCurrentDateKey(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getCurrentDateDisplay(): String {
        val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun getEmoji(count: Int): String {
        return when {
            count == 0 -> "😌"
            count <= 2 -> "🙂"
            count <= 4 -> "😊"
            count <= 6 -> "😁"
            count <= 8 -> "🙄"
            count <= 10 -> "😵‍💫"
            count <= 12 -> "🤤"
            count <= 15 -> "😵"
            else -> "🛸"
        }
    }
}