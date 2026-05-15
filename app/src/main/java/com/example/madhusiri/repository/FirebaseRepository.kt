package com.example.madhusiri.repository

import com.example.madhusiri.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun saveUser(user: User) {
        db.child("users").child(user.uid).setValue(user).await()
    }

    suspend fun getUser(uid: String): User? {
        val snapshot = db.child("users").child(uid).get().await()
        return snapshot.getValue(User::class.java)
    }

    // Hive Operations
    suspend fun saveHive(hive: Hive) {
        val id = if (hive.id.isEmpty()) db.child("hives").push().key ?: "" else hive.id
        val hiveWithId = hive.copy(id = id)
        db.child("hives").child(id).setValue(hiveWithId).await()
    }

    fun getAllHives(): Flow<List<Hive>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hives = snapshot.children.mapNotNull { it.getValue(Hive::class.java) }
                trySend(hives)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.child("hives")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun deleteHive(hiveId: String) {
        db.child("hives").child(hiveId).removeValue().await()
    }

    // Spray Alert Operations
    suspend fun postSprayAlert(alert: SprayAlert) {
        val id = db.child("spray_alerts").push().key ?: ""
        val alertWithId = alert.copy(id = id, timestamp = System.currentTimeMillis())
        db.child("spray_alerts").child(id).setValue(alertWithId).await()
    }

    fun getSprayAlerts(): Flow<List<SprayAlert>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = snapshot.children.mapNotNull { it.getValue(SprayAlert::class.java) }
                    .filter { System.currentTimeMillis() - it.timestamp < 4 * 60 * 60 * 1000 } // Active for 4 hours
                trySend(alerts)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.child("spray_alerts")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Messaging
    fun subscribeToSprayAlerts() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("spray_alerts")
    }
}
