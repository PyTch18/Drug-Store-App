package com.example.drugstore.data.model

import java.math.BigDecimal

// Represents a single item within the cart
data class CartItem(
    val item: Item,
    val quantity: Int
)

// Represents the user's shopping cart
data class Cart(
    val id: String,
    val patientId: String, // To link the cart to a user
    val items: List<CartItem> = emptyList(), // List of products in the cart
) {
    // A helper function to calculate the total price of the cart
    fun calculateTotalPrice(): BigDecimal {
        return items.sumOf { cartItem ->
            cartItem.item.price * BigDecimal(cartItem.quantity)
        }
    }
}
