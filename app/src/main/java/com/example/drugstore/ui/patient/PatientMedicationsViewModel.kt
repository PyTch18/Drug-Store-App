package com.example.drugstore.ui.patient

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.drugstore.data.model.Medication
import com.example.drugstore.data.repository.CartRepository
import com.example.drugstore.data.repository.MedicationRepository
import com.google.firebase.auth.FirebaseAuth

class PatientMedicationsViewModel(application: Application) : AndroidViewModel(application) {

    private val medsRepo: MedicationRepository = MedicationRepository()
    private val cartRepo: CartRepository = CartRepository()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    fun addToCart(medication: Medication) {
        val uid = auth.currentUser?.uid ?: return
        cartRepo.addToCart(uid, medication) { success, message ->
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
