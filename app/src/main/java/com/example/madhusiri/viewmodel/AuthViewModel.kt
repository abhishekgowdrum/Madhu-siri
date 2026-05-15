package com.example.madhusiri.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madhusiri.model.User
import com.example.madhusiri.model.UserRole
import com.example.madhusiri.repository.FirebaseRepository
import com.example.madhusiri.service.SprayAlertService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                val user = repository.getUser(uid)
                _currentUser.value = user
            }
        }
    }

    fun isUserLoggedIn() = auth.currentUser != null

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: ""
                    viewModelScope.launch {
                        val user = repository.getUser(uid)
                        _currentUser.value = user
                        _authState.value = AuthState.Success
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun register(email: String, pass: String, role: UserRole) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userAuth = task.result?.user
                    if (userAuth != null) {
                        viewModelScope.launch {
                            val user = User(
                                uid = userAuth.uid,
                                email = email,
                                role = role,
                                profileComplete = false
                            )
                            repository.saveUser(user)
                            _currentUser.value = user
                            _authState.value = AuthState.Success
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private val _detectedVillage = MutableStateFlow<String?>(null)
    val detectedVillage: StateFlow<String?> = _detectedVillage

    fun detectVillage(context: Context) {
        viewModelScope.launch {
            try {
                val helper = com.example.madhusiri.utils.LocationHelper(context)
                _detectedVillage.value = helper.getCurrentVillage()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Location error: ${e.message}")
            }
        }
    }

    private val _isDetailsSaved = MutableStateFlow(false)
    val isDetailsSaved: StateFlow<Boolean> = _isDetailsSaved

    suspend fun isProfileComplete(): Boolean {
        val user = _currentUser.value ?: return false
        return user.profileComplete
    }

    fun saveUserDetails(name: String, village: String) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val updatedUser = user.copy(
                    name = name,
                    village = village,
                    profileComplete = true
                )
                repository.saveUser(updatedUser)
                _currentUser.value = updatedUser
                _isDetailsSaved.value = true
                repository.subscribeToSprayAlerts()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error saving details: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Failed to save details")
            }
        }
    }

    fun startAlertService(context: Context) {
        val user = _currentUser.value
        if (user?.role == UserRole.BEEKEEPER) {
            val intent = Intent(context, SprayAlertService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun logout(context: Context) {
        val intent = Intent(context, SprayAlertService::class.java)
        context.stopService(intent)

        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
}
