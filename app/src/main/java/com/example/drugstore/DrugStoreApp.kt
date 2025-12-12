package com.example.drugstore

import android.app.Application
import com.example.drugstore.voip.VoipManager

class DrugStoreApp : Application() {

    lateinit var voipManager: VoipManager
        private set

    override fun onCreate() {
        super.onCreate()
        voipManager = VoipManager(this)
    }
}
