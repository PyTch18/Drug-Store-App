package com.example.drugstore.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drugstore.data.model.Medication
import com.example.drugstore.data.repository.CartRepository
import com.example.drugstore.data.repository.MedicationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PatientMedicationsViewModel(
    private val medsRepo: MedicationRepository = MedicationRepository(),
    private val cartRepo: CartRepository = CartRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var medications: List<Medication> = emptyList()
        private set

    var isLoading: Boolean = true
        private set

    fun loadMedications(onStateChanged: () -> Unit) {
        isLoading = true
        onStateChanged()
        medsRepo.getAllMedications { list ->
            medications = list
            isLoading = false
            onStateChanged()
        }
    }

    fun addToCart(medication: Medication, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                cartRepo.addToCart(uid, medication, onResult)
            }
        }
    }
}
