// CBDWidgetProvider.kt - CORREGIDO
package com.d4vram.cbdcounter

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class CBDWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ADD_CBD = "com.d4vram.cbdcounter.ADD_CBD"
        const val ACTION_ADD_WEED = "com.d4vram.cbdcounter.ADD_WEED"
        const val ACTION_ADD_POLEM = "com.d4vram.cbdcounter.ADD_POLEM"
        const val ACTION_RESET_CBD = "com.d4vram.cbdcounter.RESET_CBD"
        private const val ACTION_SCHEDULED_REFRESH = "com.d4vram.cbdcounter.SCHEDULED_REFRESH"
        private const val MIDNIGHT_REQUEST_CODE = 420

        // Método estático para actualizar widgets desde MainActivity
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CBDWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CBDWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isEmpty()) {
                cancelMidnightUpdate(context)
                return
            }
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            context.sendBroadcast(intent)
            scheduleMidnightUpdate(context)
        }

        private fun scheduleMidnightUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                MIDNIGHT_REQUEST_CODE,
                Intent(context, CBDWidgetProvider::class.java).apply { action = ACTION_SCHEDULED_REFRESH },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

            if (canUseExact) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } catch (security: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                // Sin permiso para alarmas exactas en Android 12+, usamos una alarma inexacta.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }

        private fun cancelMidnightUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                MIDNIGHT_REQUEST_CODE,
                Intent(context, CBDWidgetProvider::class.java).apply { action = ACTION_SCHEDULED_REFRESH },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleMidnightUpdate(context)
    }

    override fun onEnabled(context: Context) {
        scheduleMidnightUpdate(context)
    }

    override fun onDisabled(context: Context) {
        cancelMidnightUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ADD_CBD -> {
                addCBD(context)
                updateAllWidgets(context)
            }
            ACTION_ADD_WEED -> {
                addWeed(context)
                updateAllWidgets(context)
            }
            ACTION_ADD_POLEM -> {
                addPolem(context)
                updateAllWidgets(context)
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
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            ACTION_SCHEDULED_REFRESH -> {
                updateAllWidgets(context)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.cbd_widget)

        // Obtener contador actual
        val count = getCurrentCount(context)
        val date = getCurrentDateDisplay()
        val emoji = EmojiUtils.emojiForCount(count, context)

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

        // Configurar botón Weed
        val weedIntent = Intent(context, CBDWidgetProvider::class.java).apply {
            action = ACTION_ADD_WEED
        }
        val weedPendingIntent = PendingIntent.getBroadcast(
            context, 2, weedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_weed, weedPendingIntent)

        // Configurar botón Polem
        val polemIntent = Intent(context, CBDWidgetProvider::class.java).apply {
            action = ACTION_ADD_POLEM
        }
        val polemPendingIntent = PendingIntent.getBroadcast(
            context, 3, polemIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_polem, polemPendingIntent)

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

    private fun addWeed(context: Context) {
        addCBD(context)
        val entry = "🌿 ${getCurrentTimestamp()}"
        appendNote(context, entry)
    }

    private fun addPolem(context: Context) {
        addCBD(context)
        val entry = "🍫 ${getCurrentTimestamp()}"
        appendNote(context, entry)
    }

    private fun appendNote(context: Context, entry: String) {
        val today = getCurrentDateKey()
        val currentNote = Prefs.getNote(context, today)
        val updatedNote = if (currentNote.isNullOrBlank()) {
            entry
        } else {
            "$currentNote\n$entry"
        }
        Prefs.setNote(context, today, updatedNote)
    }

    private fun getCurrentTimestamp(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
