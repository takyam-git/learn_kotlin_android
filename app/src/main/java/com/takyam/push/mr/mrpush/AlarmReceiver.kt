package com.takyam.push.mr.mrpush

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        val narou = NarouChecker()
    }
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("INFO", "Broadcast Received")
        if (narou.isActive) {
            return
        }

        narou.check { newArrivals ->
            if (newArrivals.size == 0) {
                return@check
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NarouChecker.url))

            val builder: Notification.Builder = Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle("新着の小説を見つけました")
                    .setContentIntent(PendingIntent.getActivity(context, 3, intent, 0))

            val inboxStyle = Notification.InboxStyle(builder)
            inboxStyle.setBigContentTitle("新着の小説を見つけました")
            inboxStyle.setSummaryText("合計 " + newArrivals.size.toString() + "個の新着の小説を見つけました")
            newArrivals.forEach { newArrival -> inboxStyle.addLine(newArrival.toString()) }

            val sysmng: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            sysmng.notify(1, inboxStyle.build())
        }
    }
}
