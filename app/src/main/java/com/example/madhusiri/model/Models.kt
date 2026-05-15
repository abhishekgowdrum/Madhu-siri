package com.example.madhusiri.model

enum class UserRole {
    BEEKEEPER,
    FARMER
}

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val village: String = "",
    val role: UserRole = UserRole.BEEKEEPER,
    val profileComplete: Boolean = false
)

data class Hive(
    val id: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val healthStatus: String = "Healthy",
    val honeyYield: Double = 0.0,
    val lastUpdated: Long = 0L
)

data class SprayAlert(
    val id: String = "",
    val farmerUid: String = "",
    val farmerName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L,
    val radiusInKm: Double = 2.0,
    val message: String = "Spraying in progress. Please close your hives."
)

data class HiveLog(
    val date: String = "",
    val healthNote: String = "",
    val honeyProduced: Double = 0.0
)
