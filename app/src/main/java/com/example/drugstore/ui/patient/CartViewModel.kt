package com.example.drugstore.ui.patient

import androidx.lifecycle.ViewModel
import com.example.drugstore.data.model.CartItem
import com.example.drugstore.data.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth


class CartViewModel(
    private val cartRepo: CartRepository = CartRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var items: List<CartItem> = emptyList()
        private set

    val totalPrice: Double
        get() = items.sumOf { it.price.toDouble() * it.quantity.toDouble() }


    fun observeCart(onStateChanged: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        cartRepo.observeCart(uid) { list ->
            items = list
            onStateChanged()
        }
    }

    fun removeItem(id: String) {
        val uid = auth.currentUser?.uid ?: return
        cartRepo.removeItem(uid, id) { /* no-op */ }
    }

    fun clearCart() {
        val uid = auth.currentUser?.uid ?: return
        cartRepo.clearCart(uid) { /* no-op */ }
    }
}
