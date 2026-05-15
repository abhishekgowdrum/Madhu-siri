package com.example.madhusiri

import android.app.Application
import com.google.firebase.FirebaseApp

class MadhuSiriApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
