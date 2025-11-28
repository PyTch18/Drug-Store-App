package com.example.drugstore.data.repository

import com.example.drugstore.data.model.CartItem
import com.example.drugstore.data.model.Medication
import com.google.firebase.database.*

class CartRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    fun observeCart(patientId: String, onResult: (List<CartItem>) -> Unit) {
        val ref = database.getReference("carts").child(patientId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                onResult(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    fun addToCart(patientId: String, medication: Medication, onComplete: (Boolean) -> Unit) {
        val ref = database.getReference("carts").child(patientId)
        val key = ref.push().key ?: return onComplete(false)
        val item = CartItem(
            id = key,
            medicationId = medication.id,
            name = medication.name,
            price = medication.price.toDouble(),
            quantity = 1,
            imageUrl = medication.imageUrl,
            pharmacyId = medication.pharmacyId
        )
        ref.child(key).setValue(item).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun removeItem(patientId: String, cartItemId: String, onComplete: (Boolean) -> Unit) {
        val ref = database.getReference("carts").child(patientId).child(cartItemId)
        ref.removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun clearCart(patientId: String, onComplete: (Boolean) -> Unit) {
        val ref = database.getReference("carts").child(patientId)
        ref.removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
