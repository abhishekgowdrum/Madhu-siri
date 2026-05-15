package com.example.madhusiri.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.madhusiri.MainActivity
import com.example.madhusiri.R
import com.google.firebase.database.*

class PowerMonitorService : Service() {

    private var databaseRef: DatabaseReference? = null
    private var statusListener: ValueEventListener? = null
    private var lastStatus: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val villageId = intent?.getStringExtra("village_id")
        
        if (villageId != null) {
            startForegroundService()
            monitorPowerStatus(villageId)
        }
        
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "power_monitor_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Madhu-Siri Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Madhu-Siri is active")
            .setContentText("Monitoring bee safety in your area...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

    }

    private fun monitorPowerStatus(villageId: String) {
        databaseRef = FirebaseDatabase.getInstance().reference.child("villages").child(villageId)
        
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                val updatedBy = snapshot.child("updatedBy").getValue(String::class.java)
                
                if (status != null && lastStatus != null && status != lastStatus) {
                    sendUpdateNotification(status, updatedBy ?: "Someone")
                }
                lastStatus = status
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        
        databaseRef?.addValueEventListener(statusListener!!)
    }

    private fun sendUpdateNotification(status: String, updatedBy: String) {
        val channelId = "power_updates_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Power Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Power is $status!")
            .setContentText("Updated by $updatedBy in your village.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        statusListener?.let { databaseRef?.removeEventListener(it) }
        super.onDestroy()
    }
}
