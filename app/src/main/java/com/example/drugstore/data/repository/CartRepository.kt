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
        val ref = database.getReference("carts").child(patientId).child(medication.id)

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val item = currentData.getValue(CartItem::class.java)
                if (item == null) {
                    // If the item doesn't exist, create it with quantity 1
                    val newItem = CartItem(
                        id = medication.id,
                        medicationId = medication.id,
                        name = medication.name,
                        price = medication.price,
                        quantity = 1,
                        imageUrl = medication.imageUrl,
                        pharmacyId = medication.pharmacyId
                    )
                    currentData.value = newItem
                } else {
                    // If it exists, just increment the quantity
                    item.quantity += 1
                    currentData.value = item
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                onComplete(error == null && committed)
            }
        })
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
