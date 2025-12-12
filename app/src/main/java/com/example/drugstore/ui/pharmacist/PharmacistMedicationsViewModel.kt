package com.example.drugstore.ui.pharmacist

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.drugstore.data.model.Medication
import com.example.drugstore.data.repository.MedicationRepository
import com.google.firebase.auth.FirebaseAuth

class PharmacistMedicationsViewModel(
    private val medsRepo: MedicationRepository = MedicationRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // Use a Compose-observable list
    val medications = mutableStateListOf<Medication>()

    fun loadMedications() {
        val uid = auth.currentUser?.uid ?: return
        medsRepo.getMedicationsForPharmacist(uid) { list ->
            medications.clear()
            medications.addAll(list)
        }
    }

    fun saveMedication(
        draft: Medication,
        onFinished: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        medsRepo.addOrUpdateMedication(uid, draft) {
            loadMedications() // Reload the list from Firebase
            onFinished()
        }
    }

    fun deleteMedication(id: String, onFinished: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        medsRepo.deleteMedication(uid, id) {
            loadMedications() // Reload the list from Firebase
            onFinished()
        }
    }
}
