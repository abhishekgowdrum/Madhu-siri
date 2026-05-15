package com.example.madhusiri.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.madhusiri.MainActivity
import com.example.madhusiri.R
import com.example.madhusiri.model.SprayAlert
import com.google.firebase.database.*

class SprayAlertService : Service() {

    private var databaseRef: DatabaseReference? = null
    private var alertListener: ChildEventListener? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        observeSprayAlerts()
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "spray_monitor_channel"
        // minSdk is 26, so NotificationChannel is always available
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Madhu-Siri Background Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Monitoring Spray Alerts")
            .setContentText("We will notify you if a farmer reports spraying nearby.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(2, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(2, notification)
        }
    }

    private fun observeSprayAlerts() {
        databaseRef = FirebaseDatabase.getInstance().reference.child("spray_alerts")
        
        alertListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val alert = snapshot.getValue(SprayAlert::class.java)
                alert?.let {
                    // Only notify for alerts created in the last 2 minutes to avoid spamming old alerts on start
                    if (System.currentTimeMillis() - it.timestamp < 2 * 60 * 1000) {
                        sendAlertNotification(it)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        
        databaseRef?.addChildEventListener(alertListener!!)
    }

    private fun sendAlertNotification(alert: SprayAlert) {
        val channelId = "spray_notifications_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Spray Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NEW SPRAY ALERT!")
            .setContentText("Farmer ${alert.farmerName} is spraying. Check the map!")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(alert.id.hashCode(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        alertListener?.let { databaseRef?.removeEventListener(it) }
        super.onDestroy()
    }
}
