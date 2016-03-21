package com.takyam.push.mr.mrpush

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.NotificationCompat
import android.util.Log
import java.util.logging.Level

class MrPushService : Service() {
    val DEFAULT_INTERVAL = 1000 * 60 //1min
    val STOP_SERVICE_BY_TAP = "com.takyam.push.mr.mrpush.stop_service_by_tap"
    val STOP_SERVICE_BY_CLEAR = "com.takyam.push.mr.mrpush.stop_service_by_clear"

    private var sender: PendingIntent? = null
    //Notificationタップ時の終了処理を登録
    private val onStop = broadcastReceiver()
    private val stopIntentFilter = IntentFilter()

    init {
        stopIntentFilter.addAction(STOP_SERVICE_BY_TAP)
        stopIntentFilter.addAction(STOP_SERVICE_BY_CLEAR)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.i(Level.INFO.toString(), "onCreated!!!")

        registerReceiver(onStop, stopIntentFilter)

        val interval = DEFAULT_INTERVAL.toLong()

        //アラームマネージャにAlarmReceiverを登録
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        sender = PendingIntent.getBroadcast(this, 0, intent, 0)
        val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 1000, interval, sender)

        //サービス終了用のNotificationを出す
        val notification: Notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle("なろう新着チェックを停止する")
                .setContentIntent(PendingIntent.getBroadcast(this, 1, Intent(STOP_SERVICE_BY_TAP), 0))
                .setDeleteIntent(PendingIntent.getBroadcast(this, 2, Intent(STOP_SERVICE_BY_CLEAR), 0))
                .build()
        val sysmng: NotificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        sysmng.notify(0, notification)
    }

    //Notificationタップ時の終了処理
    fun broadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                Log.i(Level.INFO.toString(), "Stop service event received.")
                if (sender != null) {
                    Log.i(Level.INFO.toString(), "Checking stopped")
                    val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(sender)
                }
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(onStop)
        Log.i(Level.INFO.toString(), "Service Stopped");
    }
}
