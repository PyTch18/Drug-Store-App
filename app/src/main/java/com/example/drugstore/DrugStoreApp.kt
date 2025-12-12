package com.example.drugstore

import android.app.Application
import com.example.drugstore.data.voip.VoipManager

class DrugStoreApp : Application() {
    // Create a single, app-wide instance of the VoipManager
    lateinit var voipManager: VoipManager

    override fun onCreate() {
        super.onCreate()
        // Initialize the manager when the app starts
        voipManager = VoipManager(this)
    }
}
