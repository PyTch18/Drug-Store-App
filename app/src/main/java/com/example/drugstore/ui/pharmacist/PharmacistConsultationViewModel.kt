package com.example.drugstore.ui.pharmacist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.drugstore.data.repository.ConsultationRepository
import com.google.firebase.auth.FirebaseAuth

class PharmacistConsultationViewModel(
    private val repo: ConsultationRepository = ConsultationRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var isOnline by mutableStateOf(false)
        private set

    // Safely load the initial online status
    fun loadStatus() {
        val uid = auth.currentUser?.uid ?: return
        repo.getPharmacistOnlineStatus(uid) { initialStatus ->
            isOnline = initialStatus
        }
    }

    fun toggleOnline(newValue: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        isOnline = newValue
        repo.setPharmacistOnline(uid, newValue)
    }
}
