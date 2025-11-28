package com.example.drugstore.data.model

import java.math.BigDecimal

data class Cart(
    val id: String = "",
    val patientId: String = "",
    val items: List<CartItem> = emptyList() // use CartItem from CartItem.kt
) {
    fun calculateTotalPrice(): BigDecimal {
        return items.sumOf { cartItem ->
            BigDecimal(cartItem.price) * BigDecimal(cartItem.quantity)
        }
    }
}
