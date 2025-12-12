package com.example.drugstore.ui.pharmacist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.drugstore.data.model.Pharmacist // Corrected import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PharmacistProfileViewModel : ViewModel() {

    var pharmacist by mutableStateOf<Pharmacist?>(null)
        private set

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().getReference("pharmacists")

    init {
        loadPharmacist()
    }

    private fun loadPharmacist() {
        if (userId != null) {
            dbRef.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pharmacist = snapshot.getValue(Pharmacist::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun updatePharmacist(updatedPharmacist: Pharmacist) {
        if (userId != null) {
            dbRef.child(userId).setValue(updatedPharmacist)
        }
    }
}
