package com.example.drugstore.ui.patient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    val medications = mutableStateListOf<Medication>()
    var isLoading by mutableStateOf(true)
        private set

    fun loadMedications() {
        isLoading = true
        medsRepo.getAllMedicationsForPatients { list ->
            medications.clear()
            medications.addAll(list)
            isLoading = false
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
