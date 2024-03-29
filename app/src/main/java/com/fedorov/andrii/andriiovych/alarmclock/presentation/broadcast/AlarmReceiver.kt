package com.fedorov.andrii.andriiovych.alarmclock.presentation.broadcast

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fedorov.andrii.andriiovych.alarmclock.presentation.MainActivity
import com.fedorov.andrii.andriiovych.alarmclock.R
import com.fedorov.andrii.andriiovych.alarmclock.presentation.AlarmCreator


class AlarmReceiver : BroadcastReceiver() {

    private var mediaPlayer: MediaPlayer? = null
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(AlarmCreator.ID, 0)
        val descriptionIntent = intent.getStringExtra(AlarmCreator.DESCRIPTION)

        mediaPlayer = MediaPlayer.create(context, R.raw.zvonok)
        mediaPlayer?.start()

        val pendingIntent = createIntent(context = context, id = id)

        createChanel(context = context)

        createNotification(
            context = context,
            descriptionIntent = descriptionIntent,
            pendingIntent = pendingIntent,
            id = id
        )
    }

    private fun createIntent(context: Context, id: Int): PendingIntent? {
        val fullScreenIntent = Intent(context.applicationContext, MainActivity::class.java)
        fullScreenIntent.action = "ACTION_CANCEL_NOTIFICATION"
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            context, id,
            fullScreenIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChanel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val existingChannel = notificationManager.getNotificationChannel(CHANEL_ID)
        if (existingChannel == null) {
            val channel = NotificationChannel(
                CHANEL_ID,
                context.getString(R.string.reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ""
                enableVibration(true)
                enableLights(true)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        context: Context,
        descriptionIntent: String?,
        pendingIntent: PendingIntent?,
        id: Int
    ) {
        val builder = NotificationCompat.Builder(context, CHANEL_ID)
            .setSmallIcon(R.drawable.icon_note)
            .setContentTitle(context.getString(R.string.my_tasks))
            .setContentText(descriptionIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setOnlyAlertOnce(true)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        with(NotificationManagerCompat.from(context.applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(id, builder.build())
        }
    }

    companion object {
        private const val CHANEL_ID = "1"
    }

    inner class NotificationCancelReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_CANCEL_NOTIFICATION") {
                mediaPlayer?.stop()
            }
        }
    }
}