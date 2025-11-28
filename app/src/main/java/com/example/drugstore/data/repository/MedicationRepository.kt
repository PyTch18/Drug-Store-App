package com.example.drugstore.data.repository

import com.example.drugstore.data.model.Medication
import com.google.firebase.database.*

class MedicationRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    fun getAllMedications(onResult: (List<Medication>) -> Unit) {
        val ref = database.getReference("medications")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meds = mutableListOf<Medication>()
                snapshot.children.forEach { pharmacyNode ->
                    pharmacyNode.children.forEach { medNode ->
                        medNode.getValue(Medication::class.java)?.let { meds.add(it) }
                    }
                }
                onResult(meds)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun getMedicationsForPharmacist(
        pharmacyId: String,
        onResult: (List<Medication>) -> Unit
    ) {
        val ref = database.getReference("medications").child(pharmacyId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meds = snapshot.children.mapNotNull { it.getValue(Medication::class.java) }
                onResult(meds)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun addOrUpdateMedication(
        pharmacyId: String,
        medication: Medication,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = database.getReference("medications").child(pharmacyId)
        val key = if (medication.id.isBlank()) ref.push().key else medication.id
        if (key == null) {
            onComplete(false)
            return
        }
        val updated = medication.copy(id = key, pharmacyId = pharmacyId)
        ref.child(key).setValue(updated).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun deleteMedication(
        pharmacyId: String,
        medicationId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = database.getReference("medications").child(pharmacyId).child(medicationId)
        ref.removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
