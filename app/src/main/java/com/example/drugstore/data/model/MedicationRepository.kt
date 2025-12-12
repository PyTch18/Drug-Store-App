package com.example.drugstore.data.repository

import com.example.drugstore.data.model.Medication
import com.google.firebase.database.*

class MedicationRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    // For pharmacist: CRUD under /pharmacies/{pharmacistId}/medications/{medId}
    fun getMedicationsForPharmacist(
        pharmacistId: String,
        onResult: (List<Medication>) -> Unit
    ) {
        val ref = database
            .getReference("pharmacies")
            .child(pharmacistId)
            .child("medications")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Medication::class.java) }
                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun addOrUpdateMedication(
        pharmacistId: String,
        draft: Medication,
        onFinished: () -> Unit
    ) {
        val base = database
            .getReference("pharmacies")
            .child(pharmacistId)
            .child("medications")

        val id = if (draft.id.isBlank()) base.push().key else draft.id
        if (id == null) {
            onFinished()
            return
        }

        val toSave = draft.copy(
            id = id,
            pharmacistId = pharmacistId,
            pharmacyId = pharmacistId // simple 1:1 pharmacy = pharmacist
        )

        base.child(id).setValue(toSave).addOnCompleteListener { onFinished() }
    }

    fun deleteMedication(
        pharmacistId: String,
        medId: String,
        onFinished: () -> Unit
    ) {
        val ref = database
            .getReference("pharmacies")
            .child(pharmacistId)
            .child("medications")
            .child(medId)

        ref.removeValue().addOnCompleteListener { onFinished() }
    }

    // For patients: read all medications across pharmacies (simple flat list)
    fun getAllMedicationsForPatients(onResult: (List<Medication>) -> Unit) {
        val ref = database.getReference("pharmacies")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children
                    .flatMap { pharmacySnap ->
                        pharmacySnap
                            .child("medications")
                            .children
                            .mapNotNull { it.getValue(Medication::class.java) }
                    }
                onResult(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
}
