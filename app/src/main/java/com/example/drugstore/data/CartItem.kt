package com.example.drugstore.data.model

data class CartItem(
    val id: String = "",
    val medicationId: String = "",
    val name: String = "",
    val price: Double = 0.0,   // <- must exist
    var quantity: Int = 1,
    val imageUrl: String? = null,
    val pharmacyId: String? = null
)

