package com.example.drugstore.ui.pharmacist

import androidx.lifecycle.ViewModel
import com.example.drugstore.data.model.Medication
import com.example.drugstore.data.repository.MedicationRepository
import com.google.firebase.auth.FirebaseAuth

class PharmacistMedicationsViewModel(
    private val medsRepo: MedicationRepository = MedicationRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var medications: List<Medication> = emptyList()
        private set

    fun loadMedications(onStateChanged: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        medsRepo.getMedicationsForPharmacist(uid) { list ->
            medications = list
            onStateChanged()
        }
    }

    fun saveMedication(
        draft: Medication,
        onFinished: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        medsRepo.addOrUpdateMedication(uid, draft) {
            loadMedications(onFinished)
        }
    }

    fun deleteMedication(id: String, onFinished: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        medsRepo.deleteMedication(uid, id) {
            loadMedications(onFinished)
        }
    }
}
