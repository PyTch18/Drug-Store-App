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

    fun addToCart(
        patientId: String,
        medication: Medication,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        val pharmacyId = medication.pharmacyId
        val medId = medication.id

        if (pharmacyId.isNullOrEmpty() || medId.isEmpty()) {
            onComplete(false, "Invalid medication data.")
            return
        }

        val medRef = database
            .getReference("pharmacies")
            .child(pharmacyId)
            .child("medications")
            .child(medId)

        // 1) Transaction only on the medication node to safely decrement stock
        medRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val med = currentData.getValue(Medication::class.java)
                    ?: return Transaction.abort()

                // If quantity <= 0, treat as out of stock
                if (med.quantity <= 0) {
                    return Transaction.abort()
                }

                // Decrement quantity by 1
                currentData.child("quantity").value = med.quantity - 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    onComplete(false, "Network or permission error.")
                    return
                }
                if (!committed) {
                    onComplete(false, "Item is out of stock.")
                    return
                }

                // 2) Stock successfully decremented, now update the cart
                val cartItemRef = database
                    .getReference("carts")
                    .child(patientId)
                    .child(medId)

                cartItemRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existing = snapshot.getValue(CartItem::class.java)
                        if (existing == null) {
                            val newItem = CartItem(
                                id = medId,
                                medicationId = medId,
                                name = medication.name,
                                price = medication.price,
                                quantity = 1,
                                imageUrl = medication.imageUrl,
                                pharmacyId = pharmacyId
                            )
                            cartItemRef.setValue(newItem)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onComplete(true, "Item added to cart.")
                                    } else {
                                        onComplete(false, "Failed to update cart.")
                                    }
                                }
                        } else {
                            val newQty = existing.quantity + 1
                            cartItemRef.child("quantity").setValue(newQty)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        onComplete(true, "Item added to cart.")
                                    } else {
                                        onComplete(false, "Failed to update cart.")
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onComplete(false, "Failed to update cart.")
                    }
                })
            }
        })
    }

    fun removeItem(patientId: String, cartItemId: String, onComplete: (Boolean) -> Unit) {
        val ref = database.getReference("carts").child(patientId).child(cartItemId)
        ref.removeValue().addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun clearCart(patientId: String, onComplete: (Boolean) -> Unit) {
        database.getReference("carts").child(patientId).removeValue()
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun checkout(patientId: String, onComplete: (Boolean) -> Unit) {
        val cartRef = database.getReference("carts").child(patientId)
        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                if (items.isEmpty()) {
                    onComplete(false)
                    return
                }

                val updates = mutableMapOf<String, Any?>()
                items.forEach { item ->
                    val quantityPath =
                        "/pharmacies/${item.pharmacyId}/medications/${item.medicationId}/quantity"
                    // Use ServerValue.increment to safely decrement total stock on checkout
                    updates[quantityPath] = ServerValue.increment(-item.quantity.toLong())
                }

                database.getReference().updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            clearCart(patientId, onComplete)
                        } else {
                            onComplete(false)
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(false)
            }
        })
    }
}
