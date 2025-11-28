package com.example.drugstore.data.repository

import com.example.drugstore.data.model.Pharmacist
import com.google.firebase.database.*

class ConsultationRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    fun setPharmacistOnline(pharmacistId: String, online: Boolean) {
        database.getReference("pharmacists").child(pharmacistId)
            .child("isOnline").setValue(online)
    }

    fun getOnlinePharmacists(onResult: (List<Pharmacist>) -> Unit) {
        val ref = database.getReference("pharmacists")
        ref.orderByChild("isOnline").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list =
                        snapshot.children.mapNotNull { it.getValue(Pharmacist::class.java) }
                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }
}
