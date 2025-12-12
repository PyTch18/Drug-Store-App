package com.example.drugstore.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drugstore.data.voip.VoipManager

// Minimal user model – adapt to your real one.
data class AppUser(
    val id: String,
    val name: String,
    val userType: String,          // "PATIENT" or "PHARMACIST"
    val voipExtension: String,     // "1001" for patient, "2001" for pharmacist, etc.
    val voipPassword: String       // SIP secret from FreePBX
)

class SessionViewModel(
    private val voipManager: VoipManager
) : ViewModel() {

    // Call this once when login / role selection succeeds
    fun onUserLoggedIn(user: AppUser, pbxDomain: String) {
        voipManager.configureAccount(
            ext = user.voipExtension,
            password = user.voipPassword,
            domain = pbxDomain,
            displayName = user.name
        )
    }

    companion object {
        fun provideFactory(
            voipManager: VoipManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SessionViewModel(voipManager) as T
            }
        }
    }
}
