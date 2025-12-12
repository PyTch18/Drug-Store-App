package com.example.drugstore

import androidx.lifecycle.ViewModel
import com.example.drugstore.data.model.Patient
import com.example.drugstore.data.model.Pharmacist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel : ViewModel() {

    // Updated to also fetch the user's name
    fun fetchUserCredentials(isPharmacist: Boolean, onCredentialsFetched: (String?, String?, String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbPath = if (isPharmacist) "pharmacists" else "patients"

        FirebaseDatabase.getInstance().getReference(dbPath).child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val (ext, pass, name) = if (isPharmacist) {
                        val pharmacist = snapshot.getValue(Pharmacist::class.java)
                        Triple(pharmacist?.voipExtension, pharmacist?.voipPassword, pharmacist?.name)
                    } else {
                        val patient = snapshot.getValue(Patient::class.java)
                        Triple(patient?.voipExtension, patient?.voipPassword, patient?.name)
                    }
                    onCredentialsFetched(ext, pass, name)
                }

                override fun onCancelled(error: DatabaseError) {
                    onCredentialsFetched(null, null, null)
                }
            })
    }
}
